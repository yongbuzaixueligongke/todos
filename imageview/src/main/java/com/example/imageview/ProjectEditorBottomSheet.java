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

public class ProjectEditorBottomSheet extends BottomSheetDialogFragment {

    public interface ProjectEditorListener {
        void onProjectSaved(Project project);
    }

    private static final String ARG_MODE_TITLE = "mode_title";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_PROJECT_TITLE = "project_title";
    private static final String ARG_START_DATE = "start_date";
    private static final String ARG_END_DATE = "end_date";
    private static final String ARG_TAG = "tag";
    private static final String ARG_REMARK = "remark";

    private EditText editTitle;
    private EditText editRemark;
    private TextView textMetaSummary;
    private TextView buttonSave;
    private ImageButton buttonClose;
    private ProjectEditorListener listener;

    public static ProjectEditorBottomSheet newCreateInstance() {
        ProjectEditorBottomSheet sheet = new ProjectEditorBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_MODE_TITLE, "Project");
        sheet.setArguments(args);
        return sheet;
    }

    public static ProjectEditorBottomSheet newEditInstance(Project project) {
        ProjectEditorBottomSheet sheet = new ProjectEditorBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_MODE_TITLE, "Project");
        if (project != null) {
            args.putLong(ARG_PROJECT_ID, project.getId());
            args.putString(ARG_PROJECT_TITLE, safe(project.getTitle()));
            args.putString(ARG_START_DATE, safe(project.getStartTime()));
            args.putString(ARG_END_DATE, safe(project.getEndTime()));
            args.putString(ARG_TAG, safe(project.getTag()));
            args.putString(ARG_REMARK, safe(project.getRemark()));
        }
        sheet.setArguments(args);
        return sheet;
    }

    public void setProjectEditorListener(ProjectEditorListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_project_editor, container, false);
        bindViews(view);
        bindInitialValues();
        setupActions(view);
        updateMetaSummary();
        return view;
    }

    private void bindViews(View view) {
        editTitle = view.findViewById(R.id.edit_project_title);
        editRemark = view.findViewById(R.id.edit_project_remark);
        textMetaSummary = view.findViewById(R.id.text_project_meta_summary);
        buttonSave = view.findViewById(R.id.button_save_project);
        buttonClose = view.findViewById(R.id.action_close_project_editor);
        TextView title = view.findViewById(R.id.text_project_editor_title);
        title.setText(ensureArgs().getString(ARG_MODE_TITLE, "Project"));
    }

    private void bindInitialValues() {
        Bundle args = ensureArgs();
        editTitle.setText(args.getString(ARG_PROJECT_TITLE, ""));
        editRemark.setText(args.getString(ARG_REMARK, ""));
    }

    private void setupActions(View rootView) {
        buttonClose.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> saveProject());
        rootView.findViewById(R.id.action_project_start_date).setOnClickListener(v -> showDatePicker(ARG_START_DATE));
        rootView.findViewById(R.id.action_project_end_date).setOnClickListener(v -> showDatePicker(ARG_END_DATE));
        rootView.findViewById(R.id.action_project_tag).setOnClickListener(v -> showTagDialog());
    }

    private void showDatePicker(String targetArg) {
        Calendar calendar = Calendar.getInstance();
        String currentDate = ensureArgs().getString(targetArg, "");
        if (currentDate != null && !currentDate.isEmpty()) {
            String[] parts = currentDate.split("-");
            if (parts.length == 3) {
                try {
                    calendar.set(Calendar.YEAR, Integer.parseInt(parts[0]));
                    calendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));
                } catch (Exception ignored) {
                }
            }
        }

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
                    ensureArgs().putString(targetArg, date);
                    updateMetaSummary();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTagDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Tag");
        input.setText(ensureArgs().getString(ARG_TAG, ""));
        new AlertDialog.Builder(requireContext())
                .setTitle("Project tag")
                .setView(input)
                .setPositiveButton("OK", (dialog, which) -> {
                    ensureArgs().putString(ARG_TAG, input.getText().toString().trim());
                    updateMetaSummary();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateMetaSummary() {
        Bundle args = ensureArgs();
        String startDate = args.getString(ARG_START_DATE, "");
        String endDate = args.getString(ARG_END_DATE, "");
        String tag = args.getString(ARG_TAG, "");
        List<String> parts = new ArrayList<>();
        if (!startDate.isEmpty() || !endDate.isEmpty()) {
            String range;
            if (startDate.isEmpty()) {
                range = endDate;
            } else if (endDate.isEmpty()) {
                range = startDate;
            } else {
                range = startDate + " - " + endDate;
            }
            parts.add(range);
        }
        if (!tag.isEmpty()) {
            parts.add("#" + tag);
        }
        textMetaSummary.setText(parts.isEmpty()
                ? "No extra information"
                : android.text.TextUtils.join("   ", parts));
    }

    private void saveProject() {
        String title = editTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = ensureArgs();
        Project project = new Project(
                title,
                args.getString(ARG_START_DATE, ""),
                args.getString(ARG_END_DATE, ""),
                args.getString(ARG_TAG, ""),
                editRemark.getText().toString().trim()
        );
        project.setId(args.getLong(ARG_PROJECT_ID, 0L));
        if (listener != null) {
            listener.onProjectSaved(project);
        }
        dismiss();
    }

    private Bundle ensureArgs() {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
            setArguments(args);
        }
        return args;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
