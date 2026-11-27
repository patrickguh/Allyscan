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

Two layers, one palette:

- **The home page** (`templates/scans.html`, `static/home.css`) is built with the Taste Skill kit
  (`static/assets/`, copied from `.claude/skills/design-taste-frontend/assets`). It is a software page:
  one section per screen, the product's own report shown as live HTML, one effect and two scroll moments.
  The plan and the deal that produced it are in `.taste-skill/plan.md`.
- **The app pages** (report, scan history, how it works, settings, sign-in) use `static/style.css`: the
  same tokens and fonts in a denser working layout with a sidebar, checked with Impeccable.

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
- App pages: a 240px sidebar (dark rail in both themes) and a left-aligned work area up to 64rem.

## Components

- **Report window** (home, `#report`): the kit's `window` fragment as the page list and its `doc`
  fragment as the open page with marked issues. Demonstration data only (no real site is named).
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
- Don't edit files under `static/assets/` (the kit); write page CSS in `home.css`.
- Don't add kicker labels, side-stripe borders, glows or a second accent.
- Don't invent customers, figures or quotes on the home page.
