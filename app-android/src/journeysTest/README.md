# Respect Compatible App Sanity Tester

Automated sanity checks that verify a **Respect-compatible app** can be installed, opened, and used to complete a lesson. This exists so broken apps are caught early, rather than discovered later by users or during release.

## Goal

For each app that claims Respect compatibility, run a lightweight, repeatable check that:
1. Confirms Respect is installed on the device.
2. Launches the app.
3. Completes one lesson.
4. Confirms the completion state is reflected correctly.

## How it works

Tests are written as **Journeys** — natural-language UI test steps interpreted by Gemini, which adapts to each app's actual layout rather than relying on fixed selectors. This makes the same style of test easier to reuse across different apps with different UIs.

- `respect_compatible_app_sanity_test.journey.xml` — per-app sanity journeys living under `journeysTest/`.
- Each journey follows the same rough shape: verify Respect is installed → launch the target app → get past onboarding → complete a lesson.

## Known limitation: no variables between steps

Journeys currently can't capture a value in one step and reuse it later — every step is evaluated independently based on what's on screen at that moment (see [Supported and unsupported features](https://developer.android.com/studio/gemini/journeys#supported-features)).

**Practical impact for this task:** any app-specific or per-run values (app package ID, lesson name, expected score, etc.) must be hardcoded per journey file rather than parameterized.
