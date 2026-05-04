package com.example.imageview;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PomodoroBottomSheet extends BottomSheetDialogFragment {

    public interface PomodoroListener {
        void onFocusSessionCompleted();
    }

    private static final String ARG_TASK_TITLE = "task_title";
    private static final int DEFAULT_FOCUS_MINUTES = 25;
    private static final int DEFAULT_BREAK_MINUTES = 5;

    private TextView titleText;
    private TextView modeText;
    private TextView timeText;
    private EditText focusMinutesInput;
    private EditText breakMinutesInput;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private CountDownTimer timer;
    private long remainingMillis;
    private boolean running = false;
    private boolean focusMode = true;
    private PomodoroListener listener;

    public static PomodoroBottomSheet newInstance(String taskTitle) {
        PomodoroBottomSheet sheet = new PomodoroBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_TASK_TITLE, taskTitle);
        sheet.setArguments(args);
        return sheet;
    }

    public void setPomodoroListener(PomodoroListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_pomodoro, container, false);
        titleText = view.findViewById(R.id.text_pomodoro_title);
        modeText = view.findViewById(R.id.text_pomodoro_mode);
        timeText = view.findViewById(R.id.text_pomodoro_time);
        focusMinutesInput = view.findViewById(R.id.edit_focus_minutes);
        breakMinutesInput = view.findViewById(R.id.edit_break_minutes);
        startButton = view.findViewById(R.id.button_pomodoro_start);
        pauseButton = view.findViewById(R.id.button_pomodoro_pause);
        resetButton = view.findViewById(R.id.button_pomodoro_reset);
        ImageButton closeButton = view.findViewById(R.id.action_close_pomodoro);

        String taskTitle = getArguments() != null ? getArguments().getString(ARG_TASK_TITLE, "") : "";
        titleText.setText(taskTitle == null || taskTitle.trim().isEmpty() ? "\u756A\u8304\u949F" : taskTitle.trim());
        focusMinutesInput.setText(String.valueOf(DEFAULT_FOCUS_MINUTES));
        breakMinutesInput.setText(String.valueOf(DEFAULT_BREAK_MINUTES));
        remainingMillis = minutesToMillis(readFocusMinutes());
        updateModeText();
        updateTimeText();

        startButton.setOnClickListener(v -> startTimer());
        pauseButton.setOnClickListener(v -> pauseTimer());
        resetButton.setOnClickListener(v -> resetTimer());
        closeButton.setOnClickListener(v -> dismiss());
        return view;
    }

    private void startTimer() {
        if (running) {
            return;
        }
        if (remainingMillis <= 0L) {
            remainingMillis = minutesToMillis(focusMode ? readFocusMinutes() : readBreakMinutes());
        }
        running = true;
        setInputsEnabled(false);
        timer = new CountDownTimer(remainingMillis, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMillis = millisUntilFinished;
                updateTimeText();
            }

            @Override
            public void onFinish() {
                boolean completedFocusSession = focusMode;
                running = false;
                if (completedFocusSession && listener != null) {
                    listener.onFocusSessionCompleted();
                }
                switchMode();
                startTimer();
            }
        };
        timer.start();
    }

    private void pauseTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        running = false;
        setInputsEnabled(true);
    }

    private void resetTimer() {
        pauseTimer();
        focusMode = true;
        remainingMillis = minutesToMillis(readFocusMinutes());
        updateModeText();
        updateTimeText();
    }

    private void switchMode() {
        focusMode = !focusMode;
        remainingMillis = minutesToMillis(focusMode ? readFocusMinutes() : readBreakMinutes());
        updateModeText();
        updateTimeText();
    }

    private int readFocusMinutes() {
        return readMinutes(focusMinutesInput, DEFAULT_FOCUS_MINUTES);
    }

    private int readBreakMinutes() {
        return readMinutes(breakMinutesInput, DEFAULT_BREAK_MINUTES);
    }

    private int readMinutes(EditText input, int fallback) {
        if (input == null) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(input.getText().toString().trim());
            return Math.max(1, Math.min(180, value));
        } catch (Exception e) {
            return fallback;
        }
    }

    private long minutesToMillis(int minutes) {
        return TimeUnit.MINUTES.toMillis(minutes);
    }

    private void setInputsEnabled(boolean enabled) {
        focusMinutesInput.setEnabled(enabled);
        breakMinutesInput.setEnabled(enabled);
    }

    private void updateModeText() {
        modeText.setText(focusMode ? "\u4E13\u6CE8\u4E2D" : "\u4F11\u606F\u4E2D");
        modeText.setTextColor(android.graphics.Color.parseColor(focusMode ? "#7654B8" : "#2EAD63"));
    }

    private void updateTimeText() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60;
        timeText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pauseTimer();
    }
}
