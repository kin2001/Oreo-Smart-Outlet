# Oreo Smart Outlet Working Rules

## Scope

These instructions apply to the Android app workspace under this directory.

The related folders are:

- Android app: `D:\smart_outlet\Oreo Smart Outlet`
- ESP8266 firmware: `D:\smart_outlet\smart_outlet101`
- Stitch reference design: `D:\smart_outlet\stitch_smart_outlet_controller`
- Task tracker: `D:\smart_outlet\OREO_SMART_OUTLET_TASKS_V1_1.md`
- App plan: `D:\smart_outlet\OREO_SMART_OUTLET_APP_PLAN_V1_1.md`

## Sources of Truth

At the beginning of a phase:

1. Read the task tracker and identify the current phase.
2. Read the app plan once to understand the phase requirements.
3. Inspect Git status and the relevant existing implementation.

Do not reopen the full app plan for every batch. Remember the relevant
requirements after the first read. Reopen it only when:

- the user says the plan changed;
- an exact requirement is uncertain;
- the current code conflicts with the remembered requirement; or
- verification requires an exact section.

Read the task tracker at the start of each batch because its completion state
can change.

The tracker is not trusted blindly. If a checked task is incomplete in the
actual code, report the mismatch and include the missing work in an appropriate
batch. Do not change its checkbox until the implementation has passed the
user's tests.

## Work in Batches

Do not implement an entire phase in one pass, and do not implement every
tracker checkbox as a separate change.

Group tasks into the smallest coherent batch based on:

- shared files;
- shared functionality;
- dependency order; and
- the ability to test the results together.

A batch may cover several tracker tasks. Avoid reopening the same files across
multiple batches unless integration or a dependency genuinely requires it.
Every completed batch must leave the project in a buildable and testable state.

Before editing, tell the user:

- the batch name and goal;
- which tracker tasks it covers;
- which files will be edited;
- which files will be created; and
- what will deliberately remain unchanged.

Implement only that batch, explain it, provide testing instructions, and stop.
Do not begin the next batch until the user reports the test result.

## Ponytail Full Mode

For relevant coding, debugging, review, and design work, read and apply:

`C:\Users\user\.codex\plugins\cache\ponytail\ponytail\4.8.4\skills\ponytail\SKILL.md`

Apply all relevant Ponytail rules, including:

- understand the affected flow before editing;
- reuse suitable code already present;
- prefer Kotlin or Java standard-library features;
- prefer native Android and Jetpack Compose features;
- prefer already-installed dependencies;
- do not add speculative abstractions or scaffolding;
- use the fewest files and the smallest correct change;
- fix shared root causes instead of patching individual symptoms;
- preserve input validation, error handling, security, accessibility, and
  hardware safety; and
- leave one small runnable check for new non-trivial logic.

## File Boundaries

Codex may directly edit only the files required by the current approved batch.

- Do not edit or reformat unrelated files.
- Do not clean up unrelated code.
- Preserve existing user changes and dirty-worktree content.
- Inspect callers before changing shared behavior.
- Treat firmware and Stitch reference files as read-only unless the current
  phase explicitly requires changes there.
- Do not create a new file when an existing suitable file can hold the change
  clearly.
- Use `apply_patch` for source and documentation edits.

## Batch Testing Handoff

The user runs Gradle builds and tests. Do not run them unless the user
explicitly asks Codex to do so.

After completing a batch:

1. List every changed or created file.
2. Explain what changed and why it belongs there.
3. Explain the relevant data or UI flow in beginner-friendly language.
4. Give the exact test commands needed for that batch.
5. Give any required emulator, phone, or ESP8266 manual checks.
6. State the expected result and what error output the user should send back.
7. Stop and wait for the user's result.

Use the smallest relevant commands. Examples:

```powershell
.\gradlew.bat testDebugUnitTest --no-daemon --max-workers=1
```

```powershell
.\gradlew.bat assembleDebug --no-daemon --max-workers=1
```

```powershell
.\gradlew.bat connectedDebugAndroidTest --no-daemon --max-workers=1
```

Do not require connected-device tests when a local unit test is sufficient.

## Updating the Task Tracker

Follow this sequence:

1. Complete one batch.
2. Give the user test commands and manual checks.
3. Wait for the user to test.
4. Fix the same batch if the test fails.
5. After the user confirms success, check only the tracker tasks fully
   completed by that batch.
6. Propose the next batch and wait before starting it.

Do not check an item merely because code was written. The `Commit and push`
item remains unchecked until the user confirms that the end-of-phase commit
and push both succeeded.

## Git Ownership

The user owns branches, commits, pushes, merges, pull requests, and tags.
Codex must not execute those actions unless the user explicitly changes this
rule.

Do not commit or push after an individual batch. Commit and push only once,
after every implementation and test batch in the current phase has passed.

At the end of the phase, give the user explicit commands such as:

```powershell
git status --short
git diff --check
git add <explicit-phase-files>
git diff --cached --stat
git commit -m "<phase commit message>"
git push origin <phase branch>
```

Avoid `git add -A` when unrelated changes exist. After the user confirms the
commit and push succeeded, mark the phase's `Commit and push` tracker item
complete.

## Communication

The user is learning Android development. Keep explanations focused on the
current batch and include:

- what changed;
- where it changed;
- why that location is correct;
- how the new code works;
- how to test it; and
- what should happen when it works.

Do not dump instructions for every remaining batch at once.
