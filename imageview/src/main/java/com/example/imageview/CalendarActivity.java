package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarActivity extends AppCompatActivity {

    private TextView calendarTitle;
    private TextView monthLabel;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private RecyclerView calendarGrid;
    private CalendarGridAdapter gridAdapter;
    private final Calendar currentMonth = Calendar.getInstance();
    private AppDatabase database;
    private TodoDao todoDao;
    private List<TodoItem> allTodos;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarTitle = findViewById(R.id.calendar_title);
        monthLabel = findViewById(R.id.month_label);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        calendarGrid = findViewById(R.id.calendar_grid);

        // 初始化数据库
        database = AppDatabase.getInstance(this);
        todoDao = database.todoDao();

        // 加载所有待办事项
        loadAllTodos();

        gridAdapter = new CalendarGridAdapter(
                day -> {
                    calendarTitle.setText("日历 · 选择日期：" + formatDate(day.getDate().getTimeInMillis()));
                    // 预留：未来可在这里打开“当天待办列表”或在格子内显示待办
                    Toast.makeText(this, "选择：" + formatDate(day.getDate().getTimeInMillis()), Toast.LENGTH_SHORT).show();
                },
                todo -> {
                    // 打开待办详情页
                    Intent intent = new Intent(this, TodoDetailActivity.class);
                    intent.putExtra("todo_id", todo.getId());
                    startActivity(intent);
                }
        );

        calendarGrid.setLayoutManager(new GridLayoutManager(this, 7));
        calendarGrid.setAdapter(gridAdapter);

        // 默认显示当前月 & 今天
        Calendar today = Calendar.getInstance();
        currentMonth.setTimeInMillis(today.getTimeInMillis());
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        calendarTitle.setText("日历 · 今天：" + formatDate(today.getTimeInMillis()));

        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            currentMonth.set(Calendar.DAY_OF_MONTH, 1);
            updateMonthUi();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            currentMonth.set(Calendar.DAY_OF_MONTH, 1);
            updateMonthUi();
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_message) {
                startActivity(new Intent(CalendarActivity.this, MessageActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_calendar) {
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(CalendarActivity.this, ProfileActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_projects) {
                Intent intent = new Intent(CalendarActivity.this, MessageActivity.class);
                intent.putExtra("navigate_to_projects", true);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadAllTodos() {
        executor.execute(() -> {
            allTodos = todoDao.getAll();
            runOnUiThread(() -> {
                updateMonthUi();
            });
        });
    }

    private void updateMonthUi() {
        monthLabel.setText(formatMonthLabel(currentMonth));
        gridAdapter.setDays(buildMonthDays(currentMonth));

        // 关键：把网格区域高度平均分成6行，让“每一天的格子”放大并铺满屏幕剩余空间
        calendarGrid.post(() -> {
            int h = calendarGrid.getHeight();
            if (h > 0) {
                int cellHeight = Math.max(1, h / 6);
                gridAdapter.setCellHeightPx(cellHeight);
            }
        });
    }

    private List<CalendarDay> buildMonthDays(Calendar monthFirstDay) {
        List<CalendarDay> result = new ArrayList<>(42);

        Calendar cal = (Calendar) monthFirstDay.clone();
        int targetMonth = cal.get(Calendar.MONTH);
        int targetYear = cal.get(Calendar.YEAR);

        // 以“周一”为一周开始（和你图二一致）
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDow = cal.get(Calendar.DAY_OF_WEEK); // 1=周日...7=周六
        int offset = (firstDow - Calendar.MONDAY + 7) % 7;
        cal.add(Calendar.DAY_OF_MONTH, -offset);

        Calendar today = Calendar.getInstance();
        for (int i = 0; i < 42; i++) {
            Calendar dayCal = (Calendar) cal.clone();
            boolean inCurrentMonth = (dayCal.get(Calendar.YEAR) == targetYear) && (dayCal.get(Calendar.MONTH) == targetMonth);
            boolean isToday = sameDay(dayCal, today);
            CalendarDay calendarDay = new CalendarDay(dayCal, inCurrentMonth, isToday);
            
            // 为当天分配待办事项
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
        // 检查待办事项的开始时间是否与当天相同
        String startTime = todo.getStartTime();
        if (startTime != null && !startTime.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar todoCal = Calendar.getInstance();
                todoCal.setTime(sdf.parse(startTime));
                return sameDay(todoCal, dayCal);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean sameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        return sdf.format(millis);
    }

    private String formatMonthLabel(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}

