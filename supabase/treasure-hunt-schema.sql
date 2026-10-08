create extension if not exists pgcrypto;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = timezone('utc', now());
  return new;
end;
$$;

create table if not exists public.hunts (
  id uuid primary key default gen_random_uuid(),
  creator_id uuid not null references public.profiles(id) on delete cascade,
  title text not null,
  slug text unique,
  summary text,
  city text,
  state text,
  country text default 'India',
  difficulty text not null default 'easy'
    check (difficulty in ('easy', 'moderate', 'hard')),
  status text not null default 'draft'
    check (status in ('draft', 'published', 'archived')),
  cover_image_url text,
  reward_label text,
  xp_reward integer not null default 100 check (xp_reward >= 0),
  estimated_minutes integer check (estimated_minutes is null or estimated_minutes > 0),
  starts_at timestamptz,
  ends_at timestamptz,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.hunt_steps (
  id uuid primary key default gen_random_uuid(),
  hunt_id uuid not null references public.hunts(id) on delete cascade,
  step_number integer not null check (step_number > 0),
  title text not null,
  clue_text text not null,
  location_name text,
  answer_text text,
  hint_text text,
  xp_reward integer not null default 20 check (xp_reward >= 0),
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  unique (hunt_id, step_number)
);

create table if not exists public.hunt_participants (
  id uuid primary key default gen_random_uuid(),
  hunt_id uuid not null references public.hunts(id) on delete cascade,
  user_id uuid not null references public.profiles(id) on delete cascade,
  status text not null default 'joined'
    check (status in ('joined', 'in_progress', 'completed', 'abandoned')),
  current_step_number integer not null default 1 check (current_step_number > 0),
  xp_earned integer not null default 0 check (xp_earned >= 0),
  started_at timestamptz not null default timezone('utc', now()),
  completed_at timestamptz,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  unique (hunt_id, user_id)
);

create table if not exists public.hunt_step_progress (
  id uuid primary key default gen_random_uuid(),
  participant_id uuid not null references public.hunt_participants(id) on delete cascade,
  hunt_step_id uuid not null references public.hunt_steps(id) on delete cascade,
  status text not null default 'unlocked'
    check (status in ('unlocked', 'completed')),
  answer_submitted text,
  completed_at timestamptz,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now()),
  unique (participant_id, hunt_step_id)
);

create index if not exists hunts_creator_id_idx
  on public.hunts (creator_id);

create index if not exists hunts_status_idx
  on public.hunts (status);

create index if not exists hunt_steps_hunt_id_idx
  on public.hunt_steps (hunt_id, step_number);

create index if not exists hunt_participants_user_id_idx
  on public.hunt_participants (user_id);

create index if not exists hunt_participants_hunt_id_idx
  on public.hunt_participants (hunt_id);

create index if not exists hunt_step_progress_participant_id_idx
  on public.hunt_step_progress (participant_id);

create trigger hunts_set_updated_at
before update on public.hunts
for each row
execute function public.set_updated_at();

create trigger hunt_steps_set_updated_at
before update on public.hunt_steps
for each row
execute function public.set_updated_at();

create trigger hunt_participants_set_updated_at
before update on public.hunt_participants
for each row
execute function public.set_updated_at();

create trigger hunt_step_progress_set_updated_at
before update on public.hunt_step_progress
for each row
execute function public.set_updated_at();

create or replace function public.sync_profile_hunt_stats(target_user_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
begin
  update public.profiles
  set
    hunts = coalesce(stats.completed_hunts, 0),
    xp = coalesce(stats.total_xp, 0)
  from (
    select
      hp.user_id,
      count(*) filter (where hp.status = 'completed')::integer as completed_hunts,
      coalesce(sum(case when hp.status = 'completed' then hp.xp_earned else 0 end), 0)::integer as total_xp
    from public.hunt_participants hp
    where hp.user_id = target_user_id
    group by hp.user_id
  ) stats
  where profiles.id = target_user_id
    and profiles.id = stats.user_id;

  update public.profiles
  set hunts = 0,
      xp = 0
  where id = target_user_id
    and not exists (
      select 1
      from public.hunt_participants hp
      where hp.user_id = target_user_id
        and hp.status = 'completed'
    );
end;
$$;

create or replace function public.handle_hunt_participant_stats()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  if tg_op = 'DELETE' then
    perform public.sync_profile_hunt_stats(old.user_id);
    return old;
  end if;

  perform public.sync_profile_hunt_stats(new.user_id);

  if tg_op = 'UPDATE' and old.user_id is distinct from new.user_id then
    perform public.sync_profile_hunt_stats(old.user_id);
  end if;

  return new;
end;
$$;

create trigger hunt_participants_sync_profile_stats
after insert or update or delete on public.hunt_participants
for each row
execute function public.handle_hunt_participant_stats();

alter table public.hunts enable row level security;
alter table public.hunt_steps enable row level security;
alter table public.hunt_participants enable row level security;
alter table public.hunt_step_progress enable row level security;

create policy "hunts are viewable by authenticated users"
on public.hunts
for select
to authenticated
using (true);

create policy "users can create hunts"
on public.hunts
for insert
to authenticated
with check (creator_id = auth.uid());

create policy "creators can update their hunts"
on public.hunts
for update
to authenticated
using (creator_id = auth.uid())
with check (creator_id = auth.uid());

create policy "creators can delete their hunts"
on public.hunts
for delete
to authenticated
using (creator_id = auth.uid());

create policy "hunt steps are viewable by authenticated users"
on public.hunt_steps
for select
to authenticated
using (true);

create policy "hunt creators can manage steps"
on public.hunt_steps
for all
to authenticated
using (
  exists (
    select 1
    from public.hunts h
    where h.id = hunt_steps.hunt_id
      and h.creator_id = auth.uid()
  )
)
with check (
  exists (
    select 1
    from public.hunts h
    where h.id = hunt_steps.hunt_id
      and h.creator_id = auth.uid()
  )
);

create policy "participants can see their own rows"
on public.hunt_participants
for select
to authenticated
using (
  user_id = auth.uid()
  or exists (
    select 1
    from public.hunts h
    where h.id = hunt_participants.hunt_id
      and h.creator_id = auth.uid()
  )
);

create policy "users can join hunts for themselves"
on public.hunt_participants
for insert
to authenticated
with check (user_id = auth.uid());

create policy "participants can update their own progress row"
on public.hunt_participants
for update
to authenticated
using (user_id = auth.uid())
with check (user_id = auth.uid());

create policy "participants can leave their own hunt row"
on public.hunt_participants
for delete
to authenticated
using (user_id = auth.uid());

create policy "participants can view their step progress"
on public.hunt_step_progress
for select
to authenticated
using (
  exists (
    select 1
    from public.hunt_participants hp
    where hp.id = hunt_step_progress.participant_id
      and hp.user_id = auth.uid()
  )
);

create policy "participants can create their step progress"
on public.hunt_step_progress
for insert
to authenticated
with check (
  exists (
    select 1
    from public.hunt_participants hp
    where hp.id = hunt_step_progress.participant_id
      and hp.user_id = auth.uid()
  )
);

create policy "participants can update their step progress"
on public.hunt_step_progress
for update
to authenticated
using (
  exists (
    select 1
    from public.hunt_participants hp
    where hp.id = hunt_step_progress.participant_id
      and hp.user_id = auth.uid()
  )
)
with check (
  exists (
    select 1
    from public.hunt_participants hp
    where hp.id = hunt_step_progress.participant_id
      and hp.user_id = auth.uid()
  )
);

comment on table public.hunts is 'Treasure hunt definitions created by users or admins.';
comment on table public.hunt_steps is 'Ordered clue/checkpoint steps for each treasure hunt.';
comment on table public.hunt_participants is 'Tracks which users joined/completed a treasure hunt.';
comment on table public.hunt_step_progress is 'Tracks per-step progress for each participant.';

-- Optional starter data:
-- insert into public.hunts (creator_id, title, slug, summary, city, state, difficulty, status, reward_label, xp_reward)
-- values ('YOUR_PROFILE_ID', 'The Pink City Secret', 'the-pink-city-secret', 'Solve Jaipur clues across iconic landmarks.', 'Jaipur', 'Rajasthan', 'hard', 'published', 'Royal Explorer badge', 120);
