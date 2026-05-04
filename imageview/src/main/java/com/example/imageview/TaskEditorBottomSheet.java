package com.example.imageview;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskEditorBottomSheet extends BottomSheetDialogFragment {

    public interface TaskEditorListener {
        void onTaskSaved();

        void onTaskDeleted();
    }

    private static final String ARG_TODO_ID = "todo_id";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_PROJECT_NAME = "project_name";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_DESCRIPTION = "task_description";
    private static final String ARG_TASK_DATE = "task_date";
    private static final String ARG_TASK_TIME = "task_time";
    private static final String ARG_TASK_END_TIME = "task_end_time";
    private static final String ARG_TAG_VALUE = "tag_value";
    private static final String ARG_PRIORITY_VALUE = "priority_value";
    private static final String ARG_REMINDER_ENABLED = "reminder_enabled";
    private static final String ARG_REMINDER_TIME = "reminder_time";

    private EditText editTitle;
    private EditText editDescription;
    private TextView textMetaSummary;
    private TextView buttonSave;
    private TextView buttonDelete;
    private ImageButton buttonClose;

    private final List<Project> projects = new ArrayList<>();
    private final List<String> customTags = new ArrayList<>();

    private TodoRepository todoRepository;
    private ProjectRepository projectRepository;
    private TaskEditorUseCase taskEditorUseCase;
    private TodoItem editingItem;
    private TaskEditorState editorState;
    private TaskEditorListener listener;

    public static TaskEditorBottomSheet newCreateInstance() {
        return new TaskEditorBottomSheet();
    }

    public static TaskEditorBottomSheet newCreateFromDraft(AiTodoDraft draft) {
        TaskEditorBottomSheet sheet = new TaskEditorBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_TITLE, draft.getTitle());
        args.putString(ARG_TASK_DESCRIPTION, draft.getContent());
        args.putString(ARG_TASK_DATE, draft.getDate());
        args.putString(ARG_TASK_TIME, draft.getTime());
        args.putString(ARG_TAG_VALUE, draft.getEditorTag());
        args.putInt(ARG_PRIORITY_VALUE, draft.getPriority());
        sheet.setArguments(args);
        return sheet;
    }

    public static TaskEditorBottomSheet newEditInstance(long todoId) {
        TaskEditorBottomSheet sheet = new TaskEditorBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_TODO_ID, todoId);
        sheet.setArguments(args);
        return sheet;
    }

    public static TaskEditorBottomSheet newCreateForProject(long projectId, String projectName) {
        TaskEditorBottomSheet sheet = new TaskEditorBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_PROJECT_ID, projectId);
        args.putString(ARG_PROJECT_NAME, projectName);
        sheet.setArguments(args);
        return sheet;
    }

    public void setTaskEditorListener(TaskEditorListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_task_editor, container, false);
        todoRepository = new TodoRepository(requireContext());
        projectRepository = new ProjectRepository(requireContext());
        taskEditorUseCase = new TaskEditorUseCase(requireContext());
        editorState = TaskEditorState.fromArguments(ensureArgs());
        bindViews(view);
        setupStaticActions(view);
        loadProjects();
        loadCustomTags();
        loadTaskIfEditingOrApplyDraft();
        updateMetaSummary();
        return view;
    }

    private void bindViews(View view) {
        editTitle = view.findViewById(R.id.edit_task_title);
        editDescription = view.findViewById(R.id.edit_task_description);
        textMetaSummary = view.findViewById(R.id.text_meta_summary);
        buttonSave = view.findViewById(R.id.button_save_task);
        buttonDelete = view.findViewById(R.id.action_delete_task);
        buttonClose = view.findViewById(R.id.action_close);
    }

    private void setupStaticActions(View rootView) {
        buttonClose.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> saveTask());
        buttonDelete.setOnClickListener(v -> deleteTask());
        rootView.findViewById(R.id.action_date).setOnClickListener(v -> showDateAndTimePicker());
        rootView.findViewById(R.id.action_start_time).setOnClickListener(v -> showStartTimePicker());
        rootView.findViewById(R.id.action_end_time).setOnClickListener(v -> showEndTimePicker());
        rootView.findViewById(R.id.action_tag).setOnClickListener(v -> showTagPickerDialog());
        rootView.findViewById(R.id.action_project).setOnClickListener(v -> showProjectPickerDialog());
        rootView.findViewById(R.id.action_reminder).setOnClickListener(v -> toggleReminder());
    }

    private void loadProjects() {
        projectRepository.getAll(loadedProjects -> {
            projects.clear();
            if (loadedProjects != null) {
                projects.addAll(loadedProjects);
            }
        });
    }

    private void loadCustomTags() {
        todoRepository.getCustomTags(loadedTags -> {
            if (getActivity() == null) {
                return;
            }
            customTags.clear();
            if (loadedTags != null) {
                customTags.addAll(loadedTags);
            }
        });
    }

    private void loadTaskIfEditingOrApplyDraft() {
        if (editorState.getTodoId() <= 0L) {
            buttonDelete.setVisibility(View.GONE);
            applyDraftState();
            return;
        }

        buttonDelete.setVisibility(View.VISIBLE);
        todoRepository.getById(editorState.getTodoId(), item -> {
            if (item == null || getActivity() == null) {
                return;
            }
            editingItem = item;
            editorState.applyTodoItem(item);
            editorState.writeTo(ensureArgs());
            bindTask(item);
        });
    }

    private void applyDraftState() {
        editTitle.setText(editorState.getTitle());
        editDescription.setText(editorState.getDescription());
    }

    private void bindTask(TodoItem item) {
        editTitle.setText(item.getTitle());
        editDescription.setText(item.getDescription());
        updateMetaSummary();
    }

    private void showDateAndTimePicker() {
        Calendar calendar = Calendar.getInstance();
        android.app.DatePickerDialog dateDialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    editorState.setDate(date);
                    updateReminderTimeIfEnabled();
                    syncArgs();
                    updateMetaSummary();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dateDialog.show();
    }

    private void showStartTimePicker() {
        showTimePicker(true);
    }

    private void showEndTimePicker() {
        showTimePicker(false);
    }

    private void showTimePicker(boolean startTime) {
        Calendar calendar = Calendar.getInstance();
        String currentTime = startTime ? editorState.getStartTime() : editorState.getEndTime();
        if (!currentTime.isEmpty()) {
            String[] parts = currentTime.split(":");
            if (parts.length == 2) {
                try {
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                } catch (Exception ignored) {
                }
            }
        }

        android.app.TimePickerDialog timeDialog = new android.app.TimePickerDialog(
                requireContext(),
                (timeView, hourOfDay, minute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    if (startTime) {
                        editorState.setStartTime(time);
                    } else {
                        editorState.setEndTime(time);
                    }
                    updateReminderTimeIfEnabled();
                    syncArgs();
                    updateMetaSummary();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timeDialog.show();
    }

    private void toggleReminder() {
        if (editorState.getDate().isEmpty()) {
            Toast.makeText(requireContext(), "Set date first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean enabled = !editorState.isReminderEnabled();
        editorState.setReminderEnabled(enabled);
        if (enabled) {
            editorState.setReminderTimeMillis(taskEditorUseCase.buildReminderMillis(editorState));
            Toast.makeText(requireContext(), "Reminder enabled", Toast.LENGTH_SHORT).show();
        } else {
            editorState.setReminderTimeMillis(0L);
            Toast.makeText(requireContext(), "Reminder disabled", Toast.LENGTH_SHORT).show();
        }
        syncArgs();
        updateMetaSummary();
    }

    private void showTagPickerDialog() {
        List<String> tagOptions = new ArrayList<>();
        for (String tag : PriorityTagUtils.PRIORITY_TAGS) {
            tagOptions.add(tag);
        }
        tagOptions.addAll(customTags);
        tagOptions.add("Study");
        tagOptions.add("Work");
        tagOptions.add("Life");
        tagOptions.add("Urgent");
        tagOptions.add("Add tag...");

        String[] items = tagOptions.toArray(new String[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose tag")
                .setItems(items, (dialog, which) -> {
                    String selected = tagOptions.get(which);
                    if ("Add tag...".equals(selected)) {
                        showNewTagDialog();
                    } else {
                        editorState.setTag(selected);
                        editorState.setPriority(priorityFromTag(selected));
                        syncArgs();
                        updateMetaSummary();
                    }
                })
                .show();
    }

    private void showNewTagDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Enter tag");
        new AlertDialog.Builder(requireContext())
                .setTitle("Add tag")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    String tag = input.getText().toString().trim();
                    if (!tag.isEmpty()) {
                        editorState.setTag(tag);
                        editorState.setPriority(priorityFromTag(tag));
                        if (!PriorityTagUtils.isPriorityTag(tag) && !containsIgnoreCase(customTags, tag)) {
                            customTags.add(tag);
                        }
                        syncArgs();
                        updateMetaSummary();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        for (String value : values) {
            if (value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private void showProjectPickerDialog() {
        List<String> names = new ArrayList<>();
        names.add("No project");
        for (Project project : projects) {
            names.add(project.getTitle());
        }
        names.add("Add project...");

        String[] items = names.toArray(new String[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose project")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        editorState.setProjectId(0L);
                        editorState.setProjectName("");
                    } else if (which == names.size() - 1) {
                        showNewProjectDialog();
                    } else {
                        Project selected = projects.get(which - 1);
                        editorState.setProjectId(selected.getId());
                        editorState.setProjectName(selected.getTitle());
                    }
                    syncArgs();
                    updateMetaSummary();
                })
                .show();
    }

    private void showNewProjectDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Enter project name");
        new AlertDialog.Builder(requireContext())
                .setTitle("Add project")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    editorState.setProjectId(0L);
                    editorState.setProjectName(input.getText().toString().trim());
                    syncArgs();
                    updateMetaSummary();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateMetaSummary() {
        List<String> parts = new ArrayList<>();
        if (!editorState.getDate().isEmpty()) {
            String schedule = editorState.getDate();
            if (!editorState.getStartTime().isEmpty() && !editorState.getEndTime().isEmpty()) {
                schedule += " " + editorState.getStartTime() + "-" + editorState.getEndTime();
            } else if (!editorState.getStartTime().isEmpty()) {
                schedule += " " + editorState.getStartTime();
            }
            parts.add(schedule);
        }
        if (editorState.isReminderEnabled() && editorState.getReminderTimeMillis() > 0L) {
            parts.add("Reminder");
        }
        if (!editorState.getTag().isEmpty()) {
            parts.add("#" + editorState.getTag());
        }
        if (!editorState.getProjectName().isEmpty()) {
            parts.add(editorState.getProjectName());
        }
        textMetaSummary.setText(parts.isEmpty()
                ? "No extra information"
                : android.text.TextUtils.join("   ", parts));
    }

    private void updateReminderTimeIfEnabled() {
        if (editorState.isReminderEnabled()) {
            editorState.setReminderTimeMillis(taskEditorUseCase.buildReminderMillis(editorState));
        }
    }

    private void saveTask() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        editorState.setTitle(title);
        editorState.setDescription(editDescription.getText().toString().trim());
        if (editorState.getPriority() == 0) {
            editorState.setPriority(priorityFromTag(editorState.getTag()));
        }
        updateReminderTimeIfEnabled();
        syncArgs();

        taskEditorUseCase.save(editorState, editingItem, savedId -> {
            if (getActivity() == null) {
                return;
            }
            if (listener != null) {
                listener.onTaskSaved();
            }
            dismiss();
        });
    }

    private void deleteTask() {
        if (editingItem == null) {
            return;
        }
        taskEditorUseCase.delete(editingItem, ignored -> {
            if (getActivity() == null) {
                return;
            }
            if (listener != null) {
                listener.onTaskDeleted();
            }
            dismiss();
        });
    }

    private int priorityFromTag(String tag) {
        String normalized = PriorityTagUtils.normalize(tag);
        if (PriorityTagUtils.TAG_P1.equals(normalized)) return 1;
        if (PriorityTagUtils.TAG_P2.equals(normalized)) return 2;
        if (PriorityTagUtils.TAG_P3.equals(normalized)) return 3;
        if (PriorityTagUtils.TAG_P4.equals(normalized)) return 4;
        return 0;
    }

    private void syncArgs() {
        editorState.writeTo(ensureArgs());
    }

    private Bundle ensureArgs() {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
            setArguments(args);
        }
        return args;
    }
}
