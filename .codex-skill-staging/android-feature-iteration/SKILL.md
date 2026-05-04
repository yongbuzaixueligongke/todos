---
name: android-feature-iteration
description: Help Codex extend and refactor an existing Android app that is already scaffolded, especially Java plus XML projects that are adding features incrementally. Use when working in an existing Android Studio project to modify screens, Activities, adapters, RecyclerView or ListView usage, Room entities or DAO flows, dialogs, navigation, manifest entries, resources, or feature wiring across Java and XML files.
---

# Android Feature Iteration

Use this skill to continue building an existing Android app instead of starting from scratch. Favor small, consistent changes that match the current project structure, naming, and UI patterns.

## Workflow

1. Inspect the project shape before editing.
2. Find the active module, entry Activity, and the files already responsible for the feature.
3. Trace the change across Java, XML, Room, adapters, and navigation before writing code.
4. Implement the smallest coherent change set.
5. Build or validate the affected module when possible.

## Inspect First

Read these files early when the request is about an existing app:

- `settings.gradle.kts` to identify modules.
- The target module's `build.gradle.kts` to confirm dependencies such as RecyclerView, Room, Navigation, or Material.
- The target module's `src/main/AndroidManifest.xml` to confirm launcher Activity and registered screens.
- The related `Activity`, `Adapter`, entity, DAO, and layout XML files for the feature.

If the project matches the sample structure in `references/project-map.md`, use that reference to jump directly to the likely files.

## Editing Rules

- Preserve the existing stack. Do not migrate Java plus XML screens to Compose unless the user explicitly asks.
- Reuse existing dialogs, adapters, base classes, and helper classes before creating new abstractions.
- Keep resource names, IDs, and package structure consistent with nearby code.
- Update all affected layers together. A UI change often also needs manifest, navigation, adapter, DAO, or layout updates.
- Remove obsolete screens and references when replacing a flow. Do not leave dead Activities or unused layout files behind unless the user asks to keep them.
- Prefer focused refactors over broad rewrites.

## Project Guardrails

- Treat the task module as the main evolution area. Prefer deeper architectural cleanup there before refactoring the whole app.
- Keep project pages and calendar pages compatible with task changes, but avoid whole-project rewrites unless the user explicitly expands scope.
- Prefer adding small shared infrastructure first when it will simplify repeated work: `TodoConstants`, `DateUtils`, `AppExecutors` or `DatabaseExecutor`, `TodoRepository`, and shared navigation constants.
- Route new task-related data access through a repository instead of letting `MessageActivity` or other UI classes call DAOs directly.
- Reuse `BaseActivity` for screens that participate in bottom navigation. Do not force every screen onto it if the screen is off the main flow.
- Replace hardcoded intent extra keys with `NavigationConstants`.
- Treat Android Studio run success as the main acceptance signal if the external shell Java setup is incomplete.
- Prefer destructive Room rebuilds over complex migrations only when the user has already accepted clearing local demo data.

## Delivery Order

When the user asks for broad upgrades rather than a single bug fix, prefer this order:

1. Stabilize shared constants, date utilities, executors, navigation constants, and database threading.
2. Finalize Room entities and DAO query surface.
3. Add or refine `TodoRepository`.
4. Add or refine a task-list ViewModel.
5. Connect `MessageActivity` or the task main screen to MVVM.
6. Add user-visible features such as search, filtering, sorting, and optional reminders.

Do not start with optional features such as reminders if the core task flow is still unstable.

## Common Task Patterns

### Change screen behavior

When changing how an existing screen works, inspect:

- the screen `Activity`
- its layout XML
- any adapter used by lists or grids
- helper classes that open the screen

Examples:

- Change a calendar page to swipe left and right between months.
- Replace a detail screen with an edit dialog launched from the parent page.
- Move an interaction from a separate Activity back into an existing list screen.

### Replace ListView with RecyclerView

When converting a list screen:

1. Find the list layout XML and replace `ListView` with `RecyclerView`.
2. Confirm the module already depends on `androidx.recyclerview`; if not, add it.
3. Create or adapt an adapter and `ViewHolder`.
4. Set a `LayoutManager` in the Activity or Fragment.
5. Reconnect click handling, empty states, and dataset refresh.
6. Remove obsolete `ListView`-specific code and listeners.

Check for side effects such as item click behavior moving from `setOnItemClickListener` into adapter callbacks.

### Replace detail Activity with dialog editing

When removing a detail page in favor of inline editing:

1. Identify where navigation to the detail page starts.
2. Move the edit logic to the calling screen or a shared helper.
3. Reuse an existing dialog layout if one already matches the fields.
4. Keep database updates off the main thread.
5. Refresh the visible item or list after save or delete.
6. Delete stale manifest entries, navigation helpers, and unused layout files if the detail page is fully retired.

### Update Room-backed features

For entity or data-flow changes, inspect:

- entity class
- DAO interface
- database class
- screen code that reads or writes the data
- adapter binding code

If a field changes, update both persistence and UI display paths in the same pass.

When the request touches task metadata, prefer these shared concepts from the project plan:

- `status`
- `priority`
- `createdAt`
- `updatedAt`

Keep overdue logic and completion logic consistent. If a task is checked complete, align both boolean and status representations if the codebase still carries both.

### Add search, filter, and sort

When implementing task list improvements:

1. Keep search, filtering, and sorting centralized in the repository or ViewModel layer instead of scattering logic across adapters and Activities.
2. Support combinations of project, tag, status, and priority filters.
3. Preserve a way to restore the full list when the search keyword is empty.
4. Keep sort modes explicit and stable, such as created time descending, start time ascending, end time ascending, priority descending, and title ascending.

Favor minimal UI additions needed to expose these controls. Do not redesign the whole screen just to add them.

### Add reminders only after core stability

Treat reminders as a stretch goal unless the user explicitly prioritizes them.

If reminders are in scope:

1. Use `AlarmManager` plus `BroadcastReceiver`.
2. Wrap scheduling and cancelation in a `ReminderScheduler`.
3. Handle alarm delivery in a `ReminderReceiver`.
4. Reuse `NavigationConstants.EXTRA_TODO_ID` for notification deep links.
5. Keep the feature simple: same-day reminder is enough unless the user asks for more.
6. Account for Android 13+ notification permission requirements.

## Implementation Checklist

- Confirm the target module and package.
- Confirm IDs referenced in Java exist in XML.
- Confirm layout file names match `setContentView(...)` or inflation calls.
- Confirm manifest entries still match the intended navigation flow.
- Confirm adapter callbacks still reach the correct screen logic.
- Confirm Room calls stay off the main thread.
- Confirm removed files are no longer referenced.

## Validation

When possible, run a targeted Gradle task for the affected module after changes. Favor the smallest useful command, such as:

```powershell
.\gradlew :imageview:assembleDebug
```

If a full build is too expensive, at least inspect for broken imports, missing resources, mismatched IDs, and stale manifest references.

Also validate behavior against the project plan when relevant:

- Add, edit, and delete flows stay synchronized across list, detail, and project subtasks.
- Search hits title, content, and description, and clears back to the full dataset.
- Filters can work alone and in combination.
- Sort order matches the selected mode.
- Calendar still renders tasks by `startTime`.
- Project detail still supports adding subtasks and deleting a project with its subtasks.
- Overdue tasks display as overdue only when unfinished and past their end date.

## Example Requests

- Use $android-feature-iteration to make the calendar screen switch months with horizontal swipes.
- Use $android-feature-iteration to remove the todo detail page and edit todos in a dialog from the project screen.
- Use $android-feature-iteration to replace a ListView with RecyclerView in the todo page while preserving item actions.
