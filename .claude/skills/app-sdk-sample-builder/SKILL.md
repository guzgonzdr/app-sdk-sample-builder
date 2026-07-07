---
name: app-sdk-sample-builder
description: >-
  Build a runnable sample of the Usercentrics App SDK (iOS, Android, or both) from a
  plain-language description — an existing feature to showcase, a customer bug to reproduce,
  or a workaround to prove out. Every sample shows the consent banner's first layer and lets
  you trigger the deeper layers (second layer, category toggles, vendor/TCF detail) granularly.
  Use this skill whenever someone asks to "build/create/spin up a sample", "reproduce a bug",
  "recreate a customer issue", "show how a feature works", or "test a workaround" against the
  App SDK / mobile CMP / consent banner — even if they don't say the word "skill". It serves
  two kinds of requester: technical requesters get a real GitHub branch plus an IDE handoff;
  non-technical requesters get an interactive banner simulation rendered as an artifact plus a
  plain-language summary delivered to their Slack. The mode is inferred from the request and
  confirmed when unclear.
---

# App SDK Sample Builder

This skill turns a description into a working sample of the Usercentrics App SDK and produces a
traceable record of what was built and why. It has two modes because the same request from a
solutions engineer and from a customer-success manager should produce very different artifacts.

## Prerequisites & setup

This skill is meant to be used company-wide, so it hardcodes nothing about one person's machine or account.
It ships **inside the sample-apps repository** — cloning that repo gives you the base iOS/Android apps and
this skill together. Set up once:

1. **Clone the sample-apps repo** anywhere on your machine. It contains `android/UCTestApp`,
   `ios/UCTestApp`, and this skill under `.claude/skills/app-sdk-sample-builder` — Claude Code loads the
   skill automatically when you work from the clone. Pull latest before a session so the base apps are current.
2. **Grant the connectors you'll use** (via your Claude connector settings, or `claude mcp` / `/mcp` in an
   interactive session):
   - **GitHub** — required for technical mode (create branches, commit).
   - **Slack** — required for non-technical mode (deliver the Canvas).
   - **Atlassian / Jira** and, if available, **Document360** — for bug/workaround research (Step 3).
     Optional for pure feature showcases.
3. **Everything else is resolved at runtime — never hardcode it:**
   - the **repo slug + local root** from the clone the skill is running in (`git remote get-url origin`, and
     the repo root above `.claude/`),
   - the **Jira cloudId** via `getAccessibleAtlassianResources`,
   - the **Settings ID** from the requester (or the shared demo default in the quickref if they don't give one).

Do not assume a fixed checkout path or a specific person's repo — read them from the environment. Base branch
per platform: use the repo's base branch (`main`, or `main-ios` / `main-android` if the repo defines split
bases). Changelog path on the new branch: `samples/<branch-name>/CHANGELOG.md`. Keep the base branch pristine.

## Step 0 — Determine the mode (do this first)

There is no user-set parameter; infer the mode from the request, and confirm only when it's
genuinely ambiguous. Requesters won't announce "I'm non-technical," so route on concrete signals:

- Mentions a repo, branch, IDE, Xcode/Android Studio, or code → **technical**.
- No such signal, or the ask is "show me / let me see / share with the customer" → lean
  **non-technical** and confirm once: "Do you want to work with the code yourself (technical) or
  just see and share how it behaves (non-technical)?"

- **technical** → real GitHub branch + sample code + IDE handoff + engineering changelog.
- **non-technical** → interactive banner simulation (artifact) + plain-language doc + Slack Canvas. No GitHub, no IDE.

## Step 1 — Ground yourself in the real SDK (both modes, always)

Generated samples are worthless if they call methods that don't exist. Never write SDK code from
memory. Before generating anything:

1. Read `references/sdk-quickref.md` for the initialization pattern, how to present the first
   layer, and how to programmatically trigger the second layer / granular consent on each platform.
2. If a Document360 connector is available, search it for the exact API for the feature in scope
   (e.g. "show second layer programmatically", "TCF vendor list", "consent state change listener").
   The live docs override anything in the quickref if they conflict.
3. If you cannot verify a method or class exists, mark that line `// VERIFY:` rather than
   inventing a plausible-looking name. A hallucinated API is worse than an honest gap.
4. For any sample that renders the banner, fetch the account's **real CMP configuration** for the
   Settings ID (or ruleSetId) — its actual categories, services, banner copy, and theme — and build
   from that, never from invented services. See "Fetch the real CMP config" in
   `references/sdk-quickref.md`.

## Step 2 — Classify the request

Every request is one of three intents. Detect it and adjust behavior:

**Example 1** — *Feature showcase*
Input: "Show how the SDK re-shows the banner after a settings reset on Android."
→ No ticket search needed. Build a clean sample demonstrating that feature end to end.

**Example 2** — *Bug reproduction*
Input: "Customer says the second layer doesn't list vendor X on iOS."
→ Search connectors for related tickets (see Step 3), reproduce the reported behavior in the
sample, and note whether it reproduces.

**Example 3** — *Workaround*
Input: "Customer needs consent collected before a specific screen loads — is there a way?"
→ Search connectors AND Document360 for known guidance, then build a sample that proves the
workaround.

## Step 3 — Connector research (bug + workaround intents only)

Do more than "find a ticket". The goal is to *resolve*, not just *reference*.

- Search the available ticketing connectors (Jira/Atlassian, Asana) for issues matching the
  customer's symptom — search by the observed behavior, the platform, and the SDK area, not just
  a verbatim quote. (Jira: resolve the cloudId at runtime via `getAccessibleAtlassianResources`;
  project keys are in the quickref's "Environment & access".)
- Prioritize **resolved/closed** tickets: extract the root cause and the resolution, and check
  whether that fix applies to the current request. If it does, build the sample *around that known
  fix* and say so.
- Search Document360 for official guidance and best practices on the same topic so any advice you
  give is grounded in real docs, not generated opinion.
- Record every ticket you used (ID + one-line relevance) — these go into the document and, for
  technical mode, get cross-linked in the branch. If you find nothing relevant, state that
  explicitly; don't pad with weak matches.

## What every sample must contain (both modes)

1. SDK initialization using the verified pattern.
2. The **banner first layer** shown on launch.
3. Controls to trigger the deeper layers **granularly** — at minimum: open second layer, toggle a
   consent category, and inspect/read the resulting consent state.
4. Whatever the specific request adds (the feature, the bug repro, or the workaround).

## Verification pass (both modes, after generating code/config)

Re-scan every SDK call in the generated output against the **Verification allowlist** at the end of
`references/sdk-quickref.md` (and anything confirmed via Document360). Any identifier not in that
list gets flagged `// VERIFY:` in the output with a one-line note. Do not present a sample as
"ready to run" if unverified calls remain — say which lines need checking. The quickref also marks
which APIs are fully confirmed vs. name-only; prefer the fully-confirmed ones in samples.

The quickref covers iOS (Swift), Android (Kotlin), Flutter (Dart), and React Native (TypeScript),
so build the sample for whichever platform the requester names. GitHub branches still use the
native base branches (`main-android` / `main-ios`); pick the base that matches the target platform,
or the closest one for cross-platform stacks.

## Confirm before any side effect (both modes)

Creating a branch and posting to Slack are actions with an audience — never do them silently.
Before the first write, show a one-line preview and wait for a go-ahead:

- Technical: "About to create branch `sample/ios/...` off `main-ios` in `<org>/<repo>` and commit
  the sample + changelog. Go ahead?"
- Non-technical: "About to post the summary to your Slack as a Canvas. Go ahead?"

---

## MODE: technical

Produce a real, reviewable branch and hand off cleanly. Do not run the app — the requester opens
their IDE and runs it themselves. State that explicitly at the end.

1. **Branch.** After confirmation, create a new GitHub branch in the sample-apps repo (resolved at
   runtime — see Prerequisites) off the base branch for the target platform (`main`, or
   `main-ios` / `main-android` if the repo defines split bases; for "both", create one branch per
   platform). Name it descriptively, e.g. `sample/ios/second-layer-vendor-repro`. Use the GitHub connector.
2. **Code.** Commit the sample built to the spec in "What every sample must contain", using only
   verified APIs. Keep it minimal and readable — this is a sample, not a product.
3. **Changelog document.** Commit a markdown doc at `samples/<branch-name>/CHANGELOG.md`
   containing, in this exact order:
   - **Description** — the request as given.
   - **Intent** — feature / bug repro / workaround.
   - **What changed** — the files touched and why, in plain engineering language.
   - **How it's solved** — for bugs/workarounds, the mechanism; for features, how the sample
     demonstrates it.
   - **Related tickets** — IDs, links, and one line each on relevance (omit the section if none).
   - **How to run** — which IDE, base branch, and entry point.
4. **Traceability.** Make sure the branch name, the changelog, and the ticket references all point
   at each other, so anyone landing on any one of them can find the rest.
5. **Handoff.** End with: repo, base branch, new branch name, changelog path, and "Open in
   Xcode/Android Studio and run — this skill does not launch an emulator or device."

---

## MODE: non-technical

The requester should never see code, GitHub, or an IDE. Deliver something they can *see* and a
summary they can *share*.

**Do not claim to run a native app in an artifact — you can't, and a technical judge will catch
it.** Instead build a faithful web simulation of the banner and its layers.

1. **Interactive banner simulation (artifact).** First fetch the account's **real CMP config** for
   the Settings ID (see "Fetch the real CMP config" in `references/sdk-quickref.md`) and build the
   simulation from those actual categories, services, labels, and theme — never invented ones. Then
   build a single-file React artifact that faithfully recreates the App SDK consent banner styled to
   match the real thing:
   - Renders **inside a phone mockup** — device frame, status bar, bottom sheet matching the config's
     `firstLayerMobileVariant` — and applies the account's real **theme** (colors, border radius, font
     from the config's `customization` block) so it looks like the real thing on a device. Keep the
     banner fixed to the configured appearance; only the surrounding page follows the viewer's theme.
   - Opens on the **first layer**.
   - Lets the user drill into the **second layer**, toggle consent **categories**, and view
     **vendor/TCF-style detail**.
   - Reflects the specific request (shows the feature behaving, or the bug's symptom, or the
     workaround working).
   - Uses in-memory React state only — **never** localStorage/sessionStorage (they fail in
     artifacts).
   - Alongside the simulation, surface the **real SDK configuration JSON** (the `settingsId`/`ruleSetId`
     plus the fetched categories, services, and theme) so there's a genuine artifact, not just a mock.
     Offer a way to view/copy it.
2. **Plain-language document.** Write a short, high-level, step-by-step doc — no code, no jargon:
   - **What was requested** (in the requester's own framing).
   - **How it was recreated** (the behavior, described plainly).
   - **If it was a bug: how it could be solved.**
   - **Best practices** the customer can apply, grounded in the Document360 search from Step 3.
3. **Deliver to Slack as a Canvas.** After confirmation, create a Slack Canvas (not a plain
   message) with the document so it renders as a proper formatted doc with headings, and post it to
   the requester's own Slack. Confirm delivery (channel/DM + Canvas link) in your final message.

The Canvas is the only lasting artifact in this mode; the simulation lives in the conversation.

---

## Demo & reliability notes

Live connectors fail unpredictably (auth, wifi, rate limits). Degrade gracefully rather than
crashing the flow:

- If GitHub write, Slack post, or ticket search fails, complete everything else, show the fully
  prepared output (branch diff / document / Canvas body), and tell the requester exactly which step
  needs a retry. Never fail silently.
- Keep the interactive simulation self-contained so it always renders even when every external
  call is down — it's the most reliable thing to show.
- Prefer one clean end-to-end path over half-finishing several.
