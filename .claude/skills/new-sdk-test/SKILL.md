---
name: new-sdk-test
description: Start a new Usercentrics App SDK reproduction test. Creates a git branch off the pristine base app, scaffolds a structured report (problem/fix/changes/ticket), and auto-looks up related Jira tickets via the Atlassian MCP. Use when reproducing a customer issue or topic on the iOS or Android App SDK.
---

# /new-sdk-test

Bootstraps one reproduction test in the SDK Test Lab. The base apps on `main` stay pristine;
every test lives on its own branch with its own report.

## Inputs
Parse from the user's invocation: `<platform> <short description>`
- `platform`: `ios`, `android`, or `both`.
- `description`: free text of the customer issue/topic (used for the title and Jira search).
If platform is missing, ask. If description is missing, ask.

## Steps

1. **Confirm clean base.** Run `git -C ~/sdk-test-lab status --porcelain`. If there are uncommitted
   changes on `main`, stop and tell the user to commit/stash first (the base must stay pristine).

2. **Make a slug + branch.** Slug = lowercased description, non-alphanumerics → `-`, trimmed,
   max ~40 chars. Branch name = `test/<YYYY-MM-DD>-<slug>` (today's date).
   Create it off `main`:
   `git -C ~/sdk-test-lab checkout main && git -C ~/sdk-test-lab checkout -b <branch>`

3. **Scaffold the report.** Copy `docs/REPORT_TEMPLATE.md` to `tests/<slug>/REPORT.md` and replace the
   placeholders: `{{TITLE}}` (description, title-cased), `{{DATE}}`, `{{PLATFORM}}`, `{{BRANCH}}`,
   `{{SETTINGS_ID}}` (`PLACEHOLDER_SETTINGS_ID` unless the user gave a real one), `{{CONTEXT}}`
   (anything the user already shared, else leave the comment).

4. **Look up Jira tickets (Atlassian MCP).**
   - `getAccessibleAtlassianResources` → get the `cloudId` (usercentrics = `2b67acf2-d0e2-4136-be8d-6b1d9838bfcf`).
   - Do **not** call `getVisibleJiraProjects` with expand — its full dump overflows context. A JQL text
     search already scopes to visible projects (auto-discovery). The **App SDK** project key is **MSDK**
     (project name "App SDK"); `CTS` (Product Portal) holds customer support tickets. For SDK-specific
     issues prefer `project in (MSDK, CTS) AND text ~ "..."`; widen to all projects if nothing matches.
   - `searchJiraIssuesUsingJql` with JQL from 2–4 strong keywords, e.g.
     `text ~ "banner" AND text ~ "consent" ORDER BY updated DESC`.
     **Always pass `fields: ["summary","status","key"]` and `maxResults: 8`** — otherwise the response
     overflows. If results are still large, save-to-file then `jq` out
     `.issues.nodes[] | {key, summary: .fields.summary, status: .fields.status.name}`.
   - Write matches into `{{TICKETS}}` as a list:
     `- [KEY](https://usercentrics.atlassian.net/browse/KEY) — summary _(status)_`.
     If none found, write `No existing ticket found.`
   - On MCP error/timeout, write `Jira lookup unavailable — check manually.` and continue.

5. **Hand off.** Tell the user the branch + report path, and that the base app for `<platform>` is ready
   to edit on this branch to reproduce the issue. Remind: build with
   - Android: `cd android/UCTestApp && JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew :app:assembleDebug`
   - iOS: `cd ios/UCTestApp && xcodegen generate && DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer xcodebuild -scheme UCTestApp -destination 'platform=iOS Simulator,name=iPhone 16 Pro' build`

6. **After the fix (when the user says it's done).** Fill the report's Problem / Root cause / Fix sections,
   and populate **Changes made** from `git -C ~/sdk-test-lab diff main...<branch> --stat`. Set Status to
   `Resolved` (or `Workaround` / `Needs SDK fix` as appropriate).

## Notes
- Never modify files on `main` — only on the test branch.
- The report is the deliverable; keep it accurate and concise.
- Don't commit unless the user asks.
