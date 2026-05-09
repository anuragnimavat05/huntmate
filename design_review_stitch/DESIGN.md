---
name: Voyage Core
colors:
  surface: '#f9f9fc'
  surface-dim: '#d9dadc'
  surface-bright: '#f9f9fc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f6'
  surface-container: '#eeeef0'
  surface-container-high: '#e8e8ea'
  surface-container-highest: '#e2e2e5'
  on-surface: '#1a1c1e'
  on-surface-variant: '#41474d'
  inverse-surface: '#2f3133'
  inverse-on-surface: '#f0f0f3'
  outline: '#72787e'
  outline-variant: '#c1c7ce'
  surface-tint: '#386380'
  primary: '#00324b'
  on-primary: '#ffffff'
  primary-container: '#1b4965'
  on-primary-container: '#8eb8d8'
  inverse-primary: '#a1cced'
  secondary: '#00658d'
  on-secondary: '#ffffff'
  secondary-container: '#8ad2fe'
  on-secondary-container: '#005a7e'
  tertiary: '#123242'
  on-tertiary: '#ffffff'
  tertiary-container: '#2a485a'
  on-tertiary-container: '#98b6cb'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#c9e6ff'
  primary-fixed-dim: '#a1cced'
  on-primary-fixed: '#001e2f'
  on-primary-fixed-variant: '#1d4b67'
  secondary-fixed: '#c6e7ff'
  secondary-fixed-dim: '#87cffc'
  on-secondary-fixed: '#001e2d'
  on-secondary-fixed-variant: '#004c6b'
  tertiary-fixed: '#c8e7fd'
  tertiary-fixed-dim: '#accbe0'
  on-tertiary-fixed: '#001e2d'
  on-tertiary-fixed-variant: '#2d4a5c'
  background: '#f9f9fc'
  on-background: '#1a1c1e'
  surface-variant: '#e2e2e5'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 12px
    letterSpacing: 0.08em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base_unit: 8px
  margin_mobile: 16px
  margin_desktop: 24px
  gutter: 16px
  stack_sm: 4px
  stack_md: 12px
  stack_lg: 24px
---

## Brand & Style

The design system is built to bridge the gap between rugged adventure and digital sophistication. It targets modern explorers who value community and reliability. The aesthetic follows a **Modern Corporate** foundation—utilizing Material 3 logic—infused with **Minimalist** airy layouts to let high-resolution travel photography take center stage. 

The UI evokes a sense of calm confidence through its "Deep Sea" and "Mist" palette, while maintaining a social energy through vibrant functional accents. Interactions are smooth and purposeful, reinforcing a "trustworthy guide" persona. Every screen prioritizes legibility and ease of use, ensuring that the technology never gets in the way of the journey.

## Colors

This design system utilizes a tiered blue palette to establish depth and hierarchy. The **Deep Sea** primary color is reserved for high-emphasis actions and navigational anchors, providing the "trustworthy" foundation. **Sky Blue** and **Pale Azure** serve as tonal variations for interactive surfaces and secondary containers. 

The background uses **Mist**, a cool-white that reduces eye strain during long browsing sessions. For the social and categorization features, a set of vibrant "Destination Badges" and "Status" colors are employed to provide immediate visual cues for budget levels and travel styles without clashing with the core oceanic theme.

## Typography

The design system exclusively uses **Plus Jakarta Sans** to maintain a modern, friendly, and geometric appearance. Headlines use tighter letter spacing and bolder weights to create a strong editorial feel, reminiscent of premium travel magazines. 

Body text is optimized for readability with generous line heights, ensuring that user-generated stories and trip descriptions are easy to digest. Label styles are set in uppercase with increased letter spacing when used for metadata (like budget tiers or timestamps) to differentiate them from interactive text.

## Layout & Spacing

This design system employs a **Fluid Grid** model based on an 8px square rhythm. On mobile devices, a 4-column grid with 16px side margins is standard, while tablet and desktop layouts scale to 12 columns with 24px margins. 

Spacing is used to group related social content—for example, a user's profile image and their trip title are tightly coupled with `stack_sm`, while distinct sections of a trip itinerary are separated by `stack_lg`. Vertical rhythm is strictly maintained to ensure the "photo-first" cards feel organized rather than cluttered.

## Elevation & Depth

In line with Material 3 principles, hierarchy is established through **Tonal Layers** and **Ambient Shadows**. The design system avoids harsh, black shadows, opting instead for soft, diffused shadows tinted with the Primary color (#1B4965) at very low opacities (8-12%).

- **Level 0 (Surface):** The background Mist color.
- **Level 1 (Cards):** Resting state for feed items. Uses a subtle 1px border in Pale Azure or a low-altitude shadow.
- **Level 2 (Active/Floating):** Used for Bottom Sheets and FABs (Floating Action Buttons). These use a more pronounced shadow to indicate they are "closer" to the user and interactive.
- **Level 3 (Modals):** High-contrast depth used to pull the user's focus entirely.

## Shapes

The shape language is defined by **Rounded** corners, creating a welcoming and safe environment for social interaction. 

- **Small Components (Buttons, Input Fields):** Use a 0.5rem (8px) radius.
- **Medium Components (Cards, Modals):** Use a 1rem (16px) radius to soften the large blocks of imagery.
- **Large Components (Bottom Sheets, Hero Sections):** Use a 1.5rem (24px) radius on top corners to create a distinctive, "wrapped" look.
- **Full Round:** Icons and status chips utilize a pill-shape (circular ends) to contrast against the more structured card layouts.

## Components

### Buttons & Interaction
- **Primary Action:** Solid Deep Sea (#1B4965) with white text. High emphasis.
- **Secondary Action:** Pale Azure (#CAE9FF) container with Deep Sea text.
- **Tertiary/Ghost:** No container, Sky Blue text. Used for "See More" or "Cancel".

### Photo-First Cards
Cards are the primary vehicle for content. They feature a full-bleed image at the top, with a 2:3 or 16:9 aspect ratio. Content overlays (like price or location) should use a subtle dark gradient at the bottom or top of the image to ensure text legibility.

### Status Chips & Badges
- **Budget Chips:** Use a semantic color-coding (Green/Yellow/Red) but with a desaturated "Voyage" tone to keep them within the design system's aesthetic.
- **Travel Style:** Small pill-shaped badges (e.g., "Backpacking," "Luxury," "Solo") use Sky Blue backgrounds with Dark Navy text.
- **Destination Badges:** Vibrant, circular avatars with a 2px Pale Azure border to highlight specific cities or countries.

### Input Fields
Outlined style using a 1px Pale Azure border. On focus, the border thickens to 2px and changes to Sky Blue. Labels are always visible, floating above the input when active.

### Navigation
A bottom navigation bar with a glassmorphic blur (Backdrop Filter: 20px) allows the content to peek through while keeping navigation persistent and clear.