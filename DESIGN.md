---
name: Allyscan
description: A website accessibility scanner that shows its own report as the product.
colors:
  page-dark: "#0a0d14"
  surface-dark: "#131824"
  ink-dark: "#e9edf3"
  muted-dark: "#8891a1"
  accent-dark: "#5ab9fc"
  accent-ink-dark: "#0a0d14"
  page-light: "#dfdedb"
  surface-light: "#d3d2ce"
  ink-light: "#26251e"
  muted-light: "#55544d"
  accent-light: "#1f4fc9"
  accent-ink-light: "#ffffff"
  rail-dark: "#070a10"
  rail-light: "#26251e"
  critical-dark: "#ff8a80"
  serious-dark: "#ffb566"
  moderate-dark: "#e2d266"
  minor-dark: "#b8bec8"
  critical-light: "#b42318"
  serious-light: "#94420a"
  moderate-light: "#665a00"
  minor-light: "#45443e"
typography:
  display:
    fontFamily: "Host Grotesk, system-ui, sans-serif"
    fontSize: "52px"
    fontWeight: 500
    lineHeight: 1.04
    letterSpacing: "-0.035em"
  heading:
    fontFamily: "Host Grotesk, system-ui, sans-serif"
    fontSize: "48px"
    fontWeight: 500
    lineHeight: 1.1
    letterSpacing: "-0.035em"
  body:
    fontFamily: "Host Grotesk, system-ui, sans-serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: "0em"
  data:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "13px"
    fontWeight: 500
    lineHeight: 1.4
    letterSpacing: "0em"
rounded:
  sm: "4px"
  md: "6px"
  lg: "8px"
components:
  button-primary:
    backgroundColor: "{colors.accent-dark}"
    textColor: "{colors.accent-ink-dark}"
    rounded: "{rounded.sm}"
    height: "44px"
  input:
    backgroundColor: "{colors.page-dark}"
    textColor: "{colors.ink-dark}"
    rounded: "{rounded.sm}"
    height: "44px"
---

# Design System: Allyscan

## Overview

One system on every page, built on the Taste Skill kit (`static/assets/`, copied from
`.claude/skills/design-taste-frontend/assets`):

- **Shared:** `templates/fragments/site.html` (head assets, the nav, the footer, the kit's scripts) and
  `static/site.css` (grades, logo, impact and status tags, the report window, inner-page layout).
- **The report window** (`templates/fragments/report-window.html`, `static/report-window.js`) is the real
  report UI: the pages the crawl found on the left, the open page's violations on the right. The report page
  uses it with the scan's data; the home page uses the same fragment with the curated example scan's data, so
  the picture of the product is the product. With no example scan the home page says so instead of inventing one.
- **The home page** (`templates/scans.html`, `static/home.css`) is the kit's software landing page: one
  section per screen, one effect and two scroll moments. Its plan and deal are in `.taste-skill/plan.md`.
- **Inner pages** (report, how it works, my scans, settings, sign-in) use the same nav and controls in a
  working layout at their natural height.

Dark is the default for every visitor. Light (Settings, or System on a light OS) swaps to the deal's
second palette, stone and blueprint blue. Every text pair is checked to WCAG 2.0 AA, and axe-core reports
zero violations on every page in both themes at desktop and phone widths.

## Colors

- **Dark:** near-black ground `#0a0d14`, surface `#131824`, ink `#e9edf3`, sky-blue accent `#5ab9fc`
  (links, the one filled button, focus). Developers read code on dark editors; the accent is the colour of
  a link and a focus ring, the two things an accessibility tool talks about most.
- **Light:** stone `#dfdedb`, ink `#26251e`, blueprint blue `#1f4fc9`.
- **Impact:** critical, serious, moderate, minor in red, amber, olive and slate, always with a shape
  (triangle, square, circle, dash) so colour is never the only cue (WCAG 1.4.1).
- Never violet or lavender; never gradient text.

## Typography

Host Grotesk (headings at 500, text at 400) and JetBrains Mono for data only: WCAG criterion numbers,
URLs, selectors, counts. Loaded from Google Fonts.

## Layout

- Home: the kit's 12-column `.wrap` (1280 at 1440), sections exactly one or two screens, hero alone on the
  first screen (triptych: headline, a collage of report pieces, the scan form).
- Inner pages: the same fixed nav and `.wrap`, a two-column head (title left, the work right) and sections at natural height.

## Components

- **Report window:** see Overview. Every number on the home page comes from the example scan, never
  from made-up data.
- **Criterion index** (report page): every failed WCAG criterion in spec order with name, level and
  count, from axe-core's `wcagNNN` tags via `WcagCriterion.java`.
- **Diagrams:** the kit's lanes (crawl) and hub (stack, with the real Spring, Postgres, Playwright, Java
  and Fly.io marks), held still until hovered (`data-dg-still`).

## Do's and Don'ts

### Do
- Run `node .claude/skills/design-taste-frontend/scripts/gate.mjs <url>` after changing the home page,
  and `impeccable detect` plus axe-core after changing any page.
- Keep one filled button style per view and the main action worded "Scan a website" everywhere.

### Don't
- Don't edit files under `static/assets/` (the kit); write page CSS in `home.css` or `site.css`.
- Don't add kicker labels, side-stripe borders, glows or a second accent.
- Don't invent customers, figures or quotes on the home page.
