package com.example.imageview;

import android.os.Bundle;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends BaseActivity {

    private static final int VIEW_MODE_WEEK = 0;
    private static final int VIEW_MODE_MONTH = 1;

    private TextView monthLabel;
    private TextView buttonWeekMode;
    private TextView buttonMonthMode;
    private View weekHeader;
    private View weekModeContainer;
    private HorizontalScrollView weekDateHorizontalScroll;
    private HorizontalScrollView weekTaskHorizontalScroll;
    private ObservableScrollView weekVerticalScroll;
    private WeekDateBarView weekDateBarView;
    private WeekTimeAxisView weekTimeAxisView;
    private WeekTaskGridView weekTaskGridView;
    private RecyclerView calendarGrid;
    private CalendarGridAdapter gridAdapter;
    private final Calendar currentMonth = Calendar.getInstance();
    private final Calendar selectedDate = Calendar.getInstance();
    private final List<Calendar> weekDates = new ArrayList<>();
    private TodoRepository todoRepository;
    private List<TodoItem> allTodos;
    private GestureDetector gestureDetector;
    private boolean calendarGridReady = false;
    private boolean weekInitialScrollApplied = false;
    private int currentViewMode = VIEW_MODE_WEEK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccountGuard.requireLogin(this)) {
            return;
        }
        setContentView(R.layout.activity_calendar);

        monthLabel = findViewById(R.id.month_label);
        buttonWeekMode = findViewById(R.id.button_week_mode);
        buttonMonthMode = findViewById(R.id.button_month_mode);
        weekHeader = findViewById(R.id.week_header);
        weekModeContainer = findViewById(R.id.week_mode_container);
        weekDateHorizontalScroll = findViewById(R.id.week_date_horizontal_scroll);
        weekTaskHorizontalScroll = findViewById(R.id.week_task_horizontal_scroll);
        weekVerticalScroll = findViewById(R.id.week_vertical_scroll);
        weekDateBarView = findViewById(R.id.week_date_bar);
        weekTimeAxisView = findViewById(R.id.week_time_axis);
        weekTaskGridView = findViewById(R.id.week_task_grid);
        calendarGrid = findViewById(R.id.calendar_grid);
        todoRepository = new TodoRepository(this);

        gridAdapter = new CalendarGridAdapter(
                day -> Toast.makeText(
                        this,
                        "\u9009\u62E9\uFF1A" + formatDate(day.getDate().getTimeInMillis()),
                        Toast.LENGTH_SHORT
                ).show(),
                todo -> showTaskEditor(todo.getId())
        );

        calendarGrid.setLayoutManager(new GridLayoutManager(this, 7));
        calendarGrid.setAdapter(gridAdapter);
        calendarGrid.setHasFixedSize(true);
        weekTaskGridView.setOnTodoClickListener(todo -> showTaskEditor(todo.getId()));

        Calendar today = Calendar.getInstance();
        currentMonth.setTimeInMillis(today.getTimeInMillis());
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        selectedDate.setTimeInMillis(today.getTimeInMillis());
        normalizeStartOfDay(selectedDate);
        buildScrollableWeekDates();
        setupWeekScrolling();
        setupModeSwitch();
        applyViewMode(VIEW_MODE_WEEK);

        setupGestures();
        waitForCalendarGridSize();
        loadAllTodos();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation(bottomNavigationView, R.id.nav_calendar);
    }

    private void setupWeekScrolling() {
        weekDateBarView.setOnDateSelectedListener(date -> {
            selectedDate.setTimeInMillis(date.getTimeInMillis());
            normalizeStartOfDay(selectedDate);
            refreshCalendarUi();
            scrollTaskGridToSelectedDate(true);
        });
        weekVerticalScroll.setOnScrollChangedListener((scrollX, scrollY, oldScrollX, oldScrollY) ->
                weekTimeAxisView.setScrollOffset(scrollY));
    }

    private void buildScrollableWeekDates() {
        weekDates.clear();
        Calendar start = (Calendar) selectedDate.clone();
        moveToWeekStart(start);
        start.add(Calendar.DAY_OF_YEAR, -21);
        for (int i = 0; i < 84; i++) {
            Calendar date = (Calendar) start.clone();
            normalizeStartOfDay(date);
            weekDates.add(date);
            start.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                if (currentViewMode == VIEW_MODE_WEEK) {
                    return false;
                }

                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                if (Math.abs(deltaY) >= Math.abs(deltaX)) {
                    return false;
                }

                if (deltaX < -80) {
                    moveForward();
                    return true;
                }
                if (deltaX > 80 && e1.getX() > 80) {
                    moveBackward();
                    return true;
                }
                return false;
            }
        });
    }

    private void setupModeSwitch() {
        buttonWeekMode.setOnClickListener(v -> applyViewMode(VIEW_MODE_WEEK));
        buttonMonthMode.setOnClickListener(v -> applyViewMode(VIEW_MODE_MONTH));
    }

    private void applyViewMode(int viewMode) {
        currentViewMode = viewMode;
        boolean weekMode = viewMode == VIEW_MODE_WEEK;
        weekHeader.setVisibility(weekMode ? android.view.View.GONE : android.view.View.VISIBLE);
        weekModeContainer.setVisibility(weekMode ? android.view.View.VISIBLE : android.view.View.GONE);
        calendarGrid.setVisibility(weekMode ? android.view.View.GONE : android.view.View.VISIBLE);

        buttonWeekMode.setBackgroundResource(weekMode ? R.drawable.calendar_mode_selected_bg : 0);
        buttonMonthMode.setBackgroundResource(weekMode ? 0 : R.drawable.calendar_mode_selected_bg);
        buttonWeekMode.setTypeface(Typeface.DEFAULT, weekMode ? Typeface.BOLD : Typeface.NORMAL);
        buttonMonthMode.setTypeface(Typeface.DEFAULT, weekMode ? Typeface.NORMAL : Typeface.BOLD);
        buttonWeekMode.setTextColor(weekMode ? 0xFF202124 : 0xFF5F6368);
        buttonMonthMode.setTextColor(weekMode ? 0xFF5F6368 : 0xFF202124);
        refreshCalendarUi();
        if (weekMode) {
            scrollTaskGridToSelectedDate(true);
            if (!weekInitialScrollApplied) {
                weekInitialScrollApplied = true;
                weekVerticalScroll.post(() -> weekVerticalScroll.scrollTo(0, dp(7 * 68)));
            }
        }
    }

    private void moveForward() {
        if (currentViewMode == VIEW_MODE_WEEK) {
            return;
        } else {
            currentMonth.add(Calendar.MONTH, 1);
            currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        }
        refreshCalendarUi();
    }

    private void moveBackward() {
        if (currentViewMode == VIEW_MODE_WEEK) {
            return;
        } else {
            currentMonth.add(Calendar.MONTH, -1);
            currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        }
        refreshCalendarUi();
    }

    private void waitForCalendarGridSize() {
        calendarGrid.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (calendarGrid.getHeight() <= 0) {
                    return true;
                }
                calendarGrid.getViewTreeObserver().removeOnPreDrawListener(this);
                calendarGridReady = true;
                refreshCalendarUi();
                return true;
            }
        });
    }

    private void loadAllTodos() {
        todoRepository.getAll(todos -> {
            allTodos = todos;
            refreshCalendarUi();
        });
    }

    private void refreshCalendarUi() {
        if (currentViewMode == VIEW_MODE_WEEK) {
            updateWeekUi();
        } else {
            updateMonthUi();
        }
    }

    private void updateWeekUi() {
        monthLabel.setText(formatSelectedDayLabel(selectedDate));
        weekDateBarView.setDates(weekDates);
        weekDateBarView.setSelectedDate(selectedDate);
        weekTaskGridView.setDates(weekDates);
        weekTaskGridView.setSelectedDate(selectedDate);
        weekTaskGridView.setTodos(buildWeekTodos());
        weekTimeAxisView.setScrollOffset(weekVerticalScroll.getScrollY());
    }

    private void updateMonthUi() {
        monthLabel.setText(formatMonthLabel(currentMonth));
        if (!calendarGridReady) {
            return;
        }

        int cellHeight = Math.max(1, calendarGrid.getHeight() / 6);
        gridAdapter.setDaysAndCellHeight(buildMonthDays(currentMonth), cellHeight);
    }

    private void showTaskEditor(long todoId) {
        TaskEditorBottomSheet sheet = TaskEditorBottomSheet.newEditInstance(todoId);
        sheet.setTaskEditorListener(new TaskEditorBottomSheet.TaskEditorListener() {
            @Override
            public void onTaskSaved() {
                loadAllTodos();
            }

            @Override
            public void onTaskDeleted() {
                loadAllTodos();
            }
        });
        sheet.show(getSupportFragmentManager(), "calendar_task_editor");
    }

    private List<CalendarDay> buildMonthDays(Calendar monthFirstDay) {
        List<CalendarDay> result = new ArrayList<>(42);
        Calendar cal = (Calendar) monthFirstDay.clone();
        int targetMonth = cal.get(Calendar.MONTH);
        int targetYear = cal.get(Calendar.YEAR);

        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDow = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDow - Calendar.MONDAY + 7) % 7;
        cal.add(Calendar.DAY_OF_MONTH, -offset);

        Calendar today = Calendar.getInstance();
        for (int i = 0; i < 42; i++) {
            Calendar dayCal = (Calendar) cal.clone();
            boolean inCurrentMonth = dayCal.get(Calendar.YEAR) == targetYear
                    && dayCal.get(Calendar.MONTH) == targetMonth;
            CalendarDay calendarDay = new CalendarDay(dayCal, inCurrentMonth, sameDay(dayCal, today));

            if (allTodos != null) {
                List<TodoItem> dayTodos = new ArrayList<>();
                for (TodoItem todo : allTodos) {
                    if (isTodoOnDay(todo, dayCal)) {
                        dayTodos.add(todo);
                    }
                }
                calendarDay.setTodos(dayTodos);
            }

            result.add(calendarDay);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return result;
    }

    private boolean isTodoOnDay(TodoItem todo, Calendar dayCal) {
        return todo != null && DateTimeUtils.isSameDay(todo.getStartDateTimeMillis(), dayCal);
    }

    private List<TodoItem> buildWeekTodos() {
        List<TodoItem> result = new ArrayList<>();
        if (allTodos == null || weekDates.isEmpty()) {
            return result;
        }
        Calendar rangeStart = (Calendar) weekDates.get(0).clone();
        Calendar rangeEnd = (Calendar) weekDates.get(weekDates.size() - 1).clone();
        rangeEnd.add(Calendar.DAY_OF_YEAR, 1);
        for (TodoItem todo : allTodos) {
            long startMillis = todo.getStartDateTimeMillis();
            if (startMillis >= rangeStart.getTimeInMillis()
                    && startMillis < rangeEnd.getTimeInMillis()) {
                result.add(todo);
            }
        }
        return result;
    }

    private boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private String formatDate(long millis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy\u5E74M\u6708d\u65E5", Locale.getDefault());
        return formatter.format(millis);
    }

    private String formatMonthLabel(Calendar cal) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy\u5E74M\u6708", Locale.getDefault());
        return formatter.format(cal.getTime());
    }

    private String formatSelectedDayLabel(Calendar cal) {
        SimpleDateFormat formatter = new SimpleDateFormat("M\u6708d\u65E5 E", Locale.getDefault());
        return formatter.format(cal.getTime());
    }

    private void scrollTaskGridToSelectedDate(boolean scrollDateBarToo) {
        weekTaskGridView.post(() -> {
            int selectedIndex = indexOfDate(selectedDate);
            if (selectedIndex < 0) {
                return;
            }
            int dayWidth = weekTaskGridView.getDayColumnWidth();
            int viewportWidth = Math.max(1, weekTaskHorizontalScroll.getWidth());
            int targetX = Math.max(0, selectedIndex * dayWidth - viewportWidth / 2 + dayWidth / 2);
            weekTaskHorizontalScroll.scrollTo(targetX, 0);
            if (scrollDateBarToo) {
                weekDateHorizontalScroll.scrollTo(targetX, 0);
            }
        });
    }

    private int indexOfDate(Calendar targetDate) {
        for (int i = 0; i < weekDates.size(); i++) {
            if (sameDay(weekDates.get(i), targetDate)) {
                return i;
            }
        }
        return -1;
    }

    private void moveToWeekStart(Calendar calendar) {
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek - Calendar.MONDAY + 7) % 7;
        calendar.add(Calendar.DAY_OF_YEAR, -offset);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void normalizeStartOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}
