---
version: 1
slug: "src-main-resources-templates-scans-html"
primary_target: "src/main/resources/templates/scans.html"
related_targets: ["src/main/resources/templates/scan-detail.html","src/main/resources/static/style.css"]
---

## Scope

Whole app shell and every server-rendered page (home/scan prompt, report, history, how-it-works, settings, auth pages). Visitor mode: Operate. Primary audience: recruiters and hiring engineers evaluating a portfolio project; secondary: people auditing a real site.

## Constraints

Keep every feature, route, form, copy fact and JS hook class. Keep dark + light + system themes (dark stays the default). Keep the logo shape. WCAG 2.0 AA in both themes is non-negotiable.

## Direction contract

THESIS: Allyscan reads like the standard it enforces. Every finding is filed under its WCAG success-criterion number, the way the spec itself is indexed. It refuses the category default: score dials, rounded stat cards, purple SaaS gradients.

OWN-WORLD: Standards-document world. Ink panel sidebar in both themes; paper-white work area (light) or graphite (dark); hairline rules instead of shadows; 2px corners; one signal blue for links, focus and primary actions; impact colours (red, amber, olive, slate) only where they carry meaning. Atkinson Hyperlegible Next for text, Atkinson Hyperlegible Mono for criterion numbers, URLs, selectors and counts.

STORY: The visitor sees what Allyscan checks, pastes a URL, and gets a report whose index says exactly which WCAG criteria failed and where.

FIRST VIEWPORT: Ink sidebar left. Work area: left-aligned h1 "What website should we check?", one-line sub, full-width URL field with a labelled Scan button, then the four WCAG principles (1 Perceivable to 4 Robust) as a numbered spec index row. Primary action: the Scan button.

FORM: Spec sheet, position 1 of 7 on the ordered list (user's choice over the rolled route-map direction). seed:e88677215321b8b8

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
