# Allyscan

An automated web accessibility auditor. Submit a URL, and Allyscan crawls the
site, runs an [axe-core](https://github.com/dequelabs/axe-core) accessibility
scan (WCAG/AODA rules) against every page it finds, and produces a report
indexed by the WCAG success criteria that failed.

**Live:** https://allyscan.fly.dev/

## Features

- **Full-site scanning.** Give it one URL and it crawls same-host links (BFS,
  capped at 20 pages) and scans every page it finds, not just the one you
  submitted.
- **Reports indexed by WCAG criterion.** Every violation is filed under the
  WCAG success criteria it fails (1.4.3 Contrast, 4.1.2 Name, Role, Value…),
  with a per-report table of failed criteria in spec order, plus filters by
  category and links to Deque University's fix guidance for each rule.
- **Accounts, or not.** Sign up for a free account to keep a private, permanent
  scan history, or scan without one — anonymous scans are a genuine one-time
  view, purged from the database an hour later rather than kept indefinitely.
- **Password reset via email**, with time-limited one-time codes.
- **Dark/light theming**, remembered per device.

## Architecture

```
Browser / curl
      |
      v
Spring MVC (ScanController = JSON API, ScanViewController = HTML UI)
      |
      v
ScanService  ---enqueue--->  scan_jobs (Postgres, status=QUEUED)
                                    ^
                                    | SELECT ... FOR UPDATE SKIP LOCKED
                                    |
                              ScanWorker (@Scheduled, polls every 2s)
                                    |
                    +---------------+----------------+
                    |                                |
              SiteCrawler (jsoup)              Playwright + axe-core
           same-host BFS, capped at            headless Chromium scan
           20 pages                            per discovered page
                    |                                |
                    +---------------+----------------+
                                    v
                          pages / violations tables
```

Key decisions and why:

- **Async job queue over synchronous scanning.** A full-site scan can take
  minutes; a request/response cycle shouldn't block on that. `ScanController`
  just enqueues a row and returns immediately; `ScanWorker` does the work on a
  schedule.
- **`SELECT ... FOR UPDATE SKIP LOCKED` for job claiming.** If more than one
  worker instance is ever running, this guarantees each queued job is claimed
  by exactly one worker, without a separate distributed lock.
- **`ScanWorker` is a separate class from `ScanService`.** Spring's
  `@Transactional` is implemented via a proxy; calling an `@Transactional`
  method from *within the same class* bypasses the proxy and the transaction
  never starts. Keeping the worker's orchestration outside the service avoids
  that trap.
- **Retry + reaper, not just try/once.** `attempts` tracks retries (max 3
  before `FAILED`); `reapStaleRunningJobs` recovers jobs stuck in `RUNNING`
  for more than 5 minutes, which happens if a worker crashes mid-scan.
- **WCAG criteria come from axe-core's own tags.** axe tags each rule with
  compact criterion tags (`wcag143` → 1.4.3, `wcag1410` → 1.4.10);
  `WcagCriterion` parses them and sorts numerically, so the report's index
  uses the spec's own numbering instead of a hand-maintained rule map.
- **Anonymous scans are actually deleted, not just hidden.** A scan run
  without an account is a one-time view — `AnonymousScanCleanup` purges
  anonymous jobs (cascading to their pages/violations) after 1 hour. It's not
  meant to be a permanent record of a site you didn't ask to keep.
- **"Keep me signed in" uses persistent remember-me tokens, not a longer
  session cookie.** Backed by Spring Security's `PersistentTokenRepository`
  (`JdbcTokenRepositoryImpl`), so staying signed in survives a browser
  restart without just extending the session's own lifetime.

## Running locally

Requirements: Java 17 and Docker (for Postgres).

```bash
docker run -d --name allyscan-db -p 5432:5432 \
  -e POSTGRES_DB=allyscan -e POSTGRES_PASSWORD=devpassword postgres:16

./mvnw spring-boot:run
```

Then open http://localhost:8080/ — submit a URL, watch it move from
`QUEUED` → `RUNNING` → `DONE`, and click through to the report for the
violation breakdown. Sign up for a free account to keep a private scan
history; scanning without one is a one-time anonymous view.

## Pages

| Path                | Description                                          |
|----------------------|------------------------------------------------------|
| `/`                  | Scan list + submit form (signed-in users see their own history) |
| `/scans/{id}/report` | Failed WCAG criteria, plus violations by page         |
| `/how-it-works`      | Plain-language explanation of the scanning pipeline   |
| `/login`, `/register` | Auth forms                                           |
| `/forgot-password`, `/reset-password` | Emailed reset-code flow             |
| `/settings`          | Appearance (theme), account, log out                  |
| `/account/scans`     | Full scan history for a signed-in user                |

## API

| Method | Path                     | Auth        | Description                          |
|--------|--------------------------|-------------|---------------------------------------|
| POST   | `/scans?url=...`         | required    | Enqueue a scan (JSON)                 |
| GET    | `/scans`                 | —           | List scan jobs (JSON)                 |
| GET    | `/scans/{id}`            | —           | Get one scan job (JSON)               |
| GET    | `/scans/{id}/violations` | —           | Flat list of violations for a job     |
| POST   | `/guest-scan`            | —           | Enqueue an anonymous one-time-view scan (HTML form) |

## Deployment

Containerized with a multi-stage Dockerfile (Maven build stage, then a
Playwright-preinstalled runtime image so headless Chromium is available
without extra setup). Deployed to Fly.io as the `allyscan` app, backed by a
free-tier Neon Postgres instance — local dev uses a Docker Postgres instead,
switched via `SPRING_DATASOURCE_*` Fly secrets overriding
`application.properties` (Spring's env-var precedence). Scaled to a single
machine deliberately: two machines both trying to run Flyway migrations
against a brand-new database on first boot is a real race, not a theoretical
one.

`main` pushes trigger `.github/workflows/fly-deploy.yml` via `FLY_API_TOKEN`.
