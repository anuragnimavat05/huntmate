drop table if exists public.hunt_step_progress cascade;
drop table if exists public.hunt_participants cascade;
drop table if exists public.hunt_steps cascade;
drop table if exists public.hunts cascade;

drop function if exists public.handle_hunt_participant_stats() cascade;
drop function if exists public.sync_profile_hunt_stats(uuid) cascade;
drop function if exists public.set_updated_at() cascade;
