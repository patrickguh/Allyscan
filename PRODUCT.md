# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Primary audience: recruiters, interviewers and hiring engineers looking at Allyscan as a student portfolio project (co-op and internship applications). They arrive from a résumé or GitHub link, usually on a laptop, and give it a few minutes: they try a scan, open a report, and judge both the engineering and the care behind it.

Secondary: anyone who wants to know whether a website meets AODA / WCAG 2.0 AA, such as site owners and developers, who can use the live tool for real.

## Product Purpose

Allyscan is an automated web accessibility auditor. You submit a URL; it crawls same-host pages (BFS, capped at 20), runs an axe-core scan in headless Chromium on each one, and stores every violation by page, category, impact and WCAG success criterion.

Success: a visitor understands what it does within seconds, runs a scan, and leaves with a report that is easy to act on. For the portfolio audience, success also means the product itself reads as careful, credible engineering.

## Positioning

A real full-site scanner, not a single-page checker: it crawls, queues the work asynchronously (Postgres job queue with `SELECT … FOR UPDATE SKIP LOCKED`), and scans every page it finds in a real browser. It is framed around Ontario's AODA (O. Reg. 191/11, s. 14), which most generic WCAG checkers do not address.

## Operating Context

- Guest scans: one-time view, purged an hour later. Accounts: permanent, private scan history.
- Reports group violations by page, filter by category (ARIA, color contrast, forms, keyboard, and more) and show impact (critical, serious, moderate, minor). Each rule name links to its Deque University page.
- Password reset by email with time-limited one-time codes.
- Deployed on Fly.io (https://allyscan.fly.dev/).

## Capabilities and Constraints

- Server-rendered Spring Boot + Thymeleaf; one global stylesheet (`src/main/resources/static/style.css`) and small vanilla JS files. No frontend build step.
- Dark and light themes plus a "system" option, chosen in Settings and remembered per device. Must be kept.
- Scans can take minutes; queued/running states are real and visible.

## Brand Commitments

- Name: Allyscan.
- Logo mark: the existing line drawing (path `M4 17h4l3-8h6l3 8`) inside a rounded square. Keep the shape; color may change.

## Evidence on Hand

- Real legal source: O. Reg. 191/11, s. 14 quote and its 2014 / 2021 compliance dates (already on the home page).
- No user counts, testimonials, customers or benchmarks exist. Do not invent any.

## Product Principles

1. Practise what it preaches: an accessibility tool must itself pass WCAG 2.0 AA in both themes, with no exceptions.
2. Show the work: the crawl, queue and scan pipeline is the interesting part; make it visible and honest.
3. Plain language over jargon: reports explain what is wrong and how to fix it.
4. Respect privacy: guest scans are truly temporary; say so plainly.

## Accessibility & Inclusion

WCAG 2.0 AA minimum (the standard Allyscan itself checks against): 4.5:1 body text contrast, visible focus, keyboard access everywhere, reduced-motion support, and semantic HTML.
