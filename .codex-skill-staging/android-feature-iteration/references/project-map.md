# Project Map

Use this reference when the target workspace resembles the current project this skill was designed around.

## Modules

- `app`: a small shell module with starter Activities and resources.
- `imageview`: the richer feature module with most app logic, layouts, Room data access, and navigation targets.

Start with `settings.gradle.kts` to confirm the modules still match this shape.

## Key Files In The Feature Module

Read these first for feature work inside `imageview`:

- `imageview/build.gradle.kts`: dependencies include RecyclerView, Room, and Navigation.
- `imageview/src/main/AndroidManifest.xml`: registered Activities include `CalendarActivity`, `ProjectDetailActivity`, `TodoDetailActivity`, `MessageActivity`, and others.
- `imageview/src/main/java/com/example/imageview/AppDatabase.java`
- `imageview/src/main/java/com/example/imageview/TodoDao.java`
- `imageview/src/main/java/com/example/imageview/TodoItem.java`
- `imageview/src/main/java/com/example/imageview/TodoAdapter.java`
- `imageview/src/main/java/com/example/imageview/NavigationHelper.java`
- `imageview/src/main/java/com/example/imageview/NavigationConstants.java`

## Planned Shared Infrastructure

The improvement plan suggests consolidating repeated logic into these files or equivalents:

- `TodoConstants` for status, priority, sort mode, and filter defaults
- `DateUtils` for `yyyy-MM-dd` parsing, formatting, and overdue checks
- `AppExecutors` or `DatabaseExecutor` for database threading
- `TodoRepository` for task CRUD, search, filtering, sorting, and tag aggregation
- `TodoListViewModel` for task list state and UI-facing filters

If these files do not exist yet, prefer introducing them before layering on more feature code in `MessageActivity`.

## Architectural Boundaries From The Plan

- Focus MVVM work on the task main module first.
- Keep project and calendar screens compatible, but do not over-refactor them unless required by the user story.
- Accept destructive Room rebuilds only if the user is okay with clearing demo data.
- Use Android Studio run success as the main validation signal when command-line Java tooling is incomplete.

## Files Relevant To The User's Common Requests

### Swipe months in the calendar

- `imageview/src/main/java/com/example/imageview/CalendarActivity.java`
- `imageview/src/main/java/com/example/imageview/CalendarGridAdapter.java`
- `imageview/src/main/java/com/example/imageview/CalendarDay.java`
- `imageview/src/main/res/layout/activity_calendar.xml`
- `imageview/src/main/res/layout/item_calendar_day.xml`

Check both gesture handling and month-grid rebuild logic. If a todo click still opens a detail screen, decide whether that flow should stay.

### Remove todo detail page and edit in dialog

- `imageview/src/main/java/com/example/imageview/TodoDetailActivity.java`
- `imageview/src/main/res/layout/activity_todo_detail.xml`
- `imageview/src/main/java/com/example/imageview/ProjectDetailActivity.java`
- `imageview/src/main/res/layout/dialog_add_todo.xml`
- `imageview/src/main/java/com/example/imageview/NavigationHelper.java`
- `imageview/src/main/AndroidManifest.xml`

Reuse existing add or edit dialog layouts when possible. If the detail Activity is retired, also remove its navigation entry points and manifest registration.

### Convert todo ListView to RecyclerView

Look for:

- the todo page Activity or Fragment
- its list layout XML
- any adapter still tied to `ListView`
- click listeners that need to move into adapter callbacks

The project already uses RecyclerView in other screens, so prefer copying the existing pattern instead of inventing a new list architecture.
