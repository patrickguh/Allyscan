---
name: Allyscan
description: An accessibility auditor that reads like the standard it enforces.
colors:
  signal-light: "#1747c8"
  signal-dark: "#8cb0ff"
  signal-soft-light: "#e9eefb"
  signal-soft-dark: "#1c2540"
  ink-light: "#16181d"
  ink-dark: "#ecedef"
  muted-light: "#50555f"
  muted-dark: "#a3a8b2"
  page-light: "#ffffff"
  page-dark: "#121317"
  panel-light: "#f4f5f7"
  panel-dark: "#1b1d22"
  rule-light: "#d9dce1"
  rule-dark: "#2c2f36"
  rule-strong-light: "#767c86"
  rule-strong-dark: "#6c717b"
  rail-light: "#16181d"
  rail-dark: "#0a0b0d"
  rail-ink: "#f2f3f5"
  rail-muted: "#a9afb9"
  critical-light: "#b42318"
  critical-dark: "#ff8a80"
  serious-light: "#9a4500"
  serious-dark: "#ffb566"
  moderate-light: "#665a00"
  moderate-dark: "#e2d266"
  minor-light: "#4b525c"
  minor-dark: "#b8bec8"
  ok-light: "#1c6b36"
  ok-dark: "#6fd08c"
typography:
  display:
    fontFamily: "Atkinson Hyperlegible Next, system-ui, sans-serif"
    fontSize: "clamp(2rem, 4.5vw, 3rem)"
    fontWeight: 800
    lineHeight: 1.2
    letterSpacing: "-0.03em"
  heading:
    fontFamily: "Atkinson Hyperlegible Next, system-ui, sans-serif"
    fontSize: "1.35rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "-0.015em"
  body:
    fontFamily: "Atkinson Hyperlegible Next, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.6
    letterSpacing: "0em"
  data:
    fontFamily: "Atkinson Hyperlegible Mono, ui-monospace, monospace"
    fontSize: "0.93rem"
    fontWeight: 600
    lineHeight: 1.5
    letterSpacing: "-0.04em"
rounded:
  base: "2px"
  logo: "25%"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "40px"
components:
  button-primary:
    backgroundColor: "{colors.signal-light}"
    textColor: "{colors.page-light}"
    rounded: "{rounded.base}"
    padding: "10px 20px"
    height: "44px"
  button-secondary:
    backgroundColor: "{colors.page-light}"
    textColor: "{colors.ink-light}"
    rounded: "{rounded.base}"
    padding: "9px 16px"
    height: "44px"
  input:
    backgroundColor: "{colors.page-light}"
    textColor: "{colors.ink-light}"
    rounded: "{rounded.base}"
    padding: "9px 12px"
    height: "44px"
  sidebar:
    backgroundColor: "{colors.rail-light}"
    textColor: "{colors.rail-ink}"
    width: "240px"
---

# Design System: Allyscan

## Overview

**North star: the spec sheet.** Allyscan checks websites against WCAG 2.0 AA, so it should look like the standard it enforces: indexed, exact and calm. Every finding is filed under its WCAG success-criterion number (1.4.3, 4.1.2) the way the spec itself is organized. The design refuses the category defaults of score dials, rounded stat cards and gradient SaaS colors.

Mode is **Operate**: people come here to run a scan and read a report. The world contributes type, palette, density and one signature move (the criterion index). Navigation and controls stay standard.

The product must pass its own scan. Every text pair meets WCAG AA 4.5:1 in both themes, and axe-core reports zero violations on every page.

## Colors

Strategy: **restrained, with a committed shell.** The ink sidebar is a solid field in both themes; the work area is paper (light) or graphite (dark). One signal blue does the real jobs: links, focus, primary actions, selection.

### Primary
- **Signal** (`#1747c8` light / `#8cb0ff` dark): links, primary buttons, focus ring, text selection, the RUNNING status. White on it is 7.6:1; ink `#0d1220` on the dark version is 8.7:1.

### Neutral
- **Ink / Page / Panel / Rule**: text, ground, quiet fills (quotes, table headers) and hairlines. Muted text is 7.5:1 on page.
- **Rail** (`#16181d` / `#0a0b0d`): the sidebar and mobile top bar. Its focus ring uses the light signal `#8cb0ff`.

### Impact and status
Critical red, serious amber, moderate olive, minor slate, OK green. Each is used only as text on its own soft tint, and each impact also has a **shape** (triangle, square, circle, dash), so color is never the only cue (WCAG 1.4.1).

### Named rules
- **No gradients, ever.** No gradient text, no radial glows, no gradient buttons.
- **Theme tokens only.** Never hard-code a color in a template; the one exception is the logo square, which stays `#1747c8` in both themes.

## Typography

**Atkinson Hyperlegible Next** for all text, **Atkinson Hyperlegible Mono** for data. Both come from the Braille Institute and were designed for low-vision readers, which fits an accessibility product. They are self-hosted as variable WOFF2 files in `static/fonts/`.

### Hierarchy
- Display (home h1): clamp(2rem, 4.5vw, 3rem), 800, -0.03em.
- Page heading: clamp(1.75rem, 3.5vw, 2.25rem), 800.
- Section h2: 1.35rem, 700. Doc-section h2 on home: up to 1.75rem, 800.
- Body: 1rem / 1.6. Paragraph measure capped at 36em (about 75 characters).
- Data (mono): criterion numbers, URLs, selectors, counts, dates, status labels. Never decoration.

## Layout

Fixed 240px ink sidebar on desktop; at 56rem and below it becomes an off-canvas drawer behind an ink top bar. The work area is left-aligned, max 64rem, with side padding clamp(1rem, 4vw, 3rem). Sections are separated by 1px rules and vertical space, not by cards.

## Elevation & Depth

Flat. No shadows. Depth comes from hairline rules (`rule`), heavier ink rules that open an index (the principles row and the steps list start with a 1px ink line), and panel tints.

## Shapes

2px corners on everything (buttons, inputs, tables, tags, panels). The one exception is the logo mark, a rounded square (25%) kept from the original identity.

## Components

### Buttons
Primary: signal fill, 44px tall, bold label. Hover adds a 1px inset ring; active nudges down 1px. Secondary (`.law-links a`): page fill, strong-rule border, ink text, panel fill on hover.

### Inputs / Fields
Page fill, 1px strong-rule border (4.2:1 against the page), 44px tall. Focus: 3px signal outline. The home URL field and its Scan button join into one control.

### Chips
Category filters are square-cornered toggle buttons with a mono count; pressed is ink-filled. Status and impact tags are tint-on-soft with a mono label (status) or a shape marker (impact).

### Navigation
Sidebar links: rail-ink text, muted icons, rail-hover background; the active link gets the rail-active fill and bold weight. No side stripes.

### Criterion index (signature component)
The report's "Failed success criteria" table lists every WCAG criterion that failed, in spec order (1.4.4 before 1.4.10), with name, level and violation count, plus a row for best-practice rules. Each violation row also carries its criterion numbers in a WCAG column. The data comes from axe-core's `wcagNNN` tags via `WcagCriterion.java`.

## Do's and Don'ts

### Do:
- File every finding under its WCAG number when one exists.
- Use mono only for real data: numbers, URLs, selectors.
- Pair every status color with a label or shape.
- Re-run axe-core and `impeccable detect` after UI changes; both should report zero.

### Don't:
- Don't add gradients, glows, glass, or colored side stripes on boxes.
- Don't put a small uppercase kicker label above a heading.
- Don't use cards as the default container; use rules and space.
- Don't use purple, or any second accent color competing with signal blue.
- Don't hard-code colors outside the token blocks in `style.css`.
