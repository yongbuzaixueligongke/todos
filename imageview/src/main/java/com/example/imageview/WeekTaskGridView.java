package com.example.imageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WeekTaskGridView extends View {

    public interface OnTodoClickListener {
        void onTodoClick(TodoItem todo);
    }

    private static final int HOURS_PER_DAY = 24;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedDayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cardStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint smallTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final List<Calendar> dates = new ArrayList<>();
    private final List<TodoItem> todos = new ArrayList<>();
    private final List<HitRect> hitRects = new ArrayList<>();
    private final Calendar selectedDate = Calendar.getInstance();
    private OnTodoClickListener todoClickListener;
    private int dayColumnWidth;
    private int hourHeight;

    public WeekTaskGridView(Context context) {
        super(context);
        init();
    }

    public WeekTaskGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        dayColumnWidth = dp(88);
        hourHeight = dp(68);
        linePaint.setColor(Color.parseColor("#E8EAED"));
        linePaint.setStrokeWidth(dp(1));
        backgroundPaint.setColor(Color.WHITE);
        selectedDayPaint.setColor(Color.parseColor("#FAF7FF"));
        cardPaint.setStyle(Paint.Style.FILL);
        cardStrokePaint.setStyle(Paint.Style.STROKE);
        cardStrokePaint.setStrokeWidth(dp(1));
        textPaint.setColor(Color.parseColor("#202124"));
        textPaint.setTextSize(sp(12));
        textPaint.setFakeBoldText(true);
        smallTextPaint.setColor(Color.parseColor("#3C4043"));
        smallTextPaint.setTextSize(sp(10));
    }

    public int getDayColumnWidth() {
        return dayColumnWidth;
    }

    public void setDates(List<Calendar> newDates) {
        dates.clear();
        if (newDates != null) {
            for (Calendar date : newDates) {
                Calendar copy = (Calendar) date.clone();
                normalizeStartOfDay(copy);
                dates.add(copy);
            }
        }
        requestLayout();
        invalidate();
    }

    public void setSelectedDate(Calendar date) {
        if (date != null) {
            selectedDate.setTimeInMillis(date.getTimeInMillis());
            normalizeStartOfDay(selectedDate);
        }
        invalidate();
    }

    public void setTodos(List<TodoItem> items) {
        todos.clear();
        if (items != null) {
            todos.addAll(items);
        }
        Collections.sort(todos, Comparator.comparingLong(TodoItem::getStartDateTimeMillis));
        invalidate();
    }

    public void setOnTodoClickListener(OnTodoClickListener listener) {
        this.todoClickListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int requestedWidth = Math.max(dayColumnWidth, dates.size() * dayColumnWidth);
        int requestedHeight = hourHeight * HOURS_PER_DAY + dp(16);
        setMeasuredDimension(requestedWidth, requestedHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        hitRects.clear();
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        drawGrid(canvas);
        drawTodos(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            for (HitRect hitRect : hitRects) {
                if (hitRect.rect.contains(event.getX(), event.getY())) {
                    if (todoClickListener != null) {
                        todoClickListener.onTodoClick(hitRect.todo);
                    }
                    return true;
                }
            }
        }
        return true;
    }

    private void drawGrid(Canvas canvas) {
        for (int i = 0; i < dates.size(); i++) {
            if (sameDay(dates.get(i), selectedDate)) {
                canvas.drawRect(i * dayColumnWidth, 0, (i + 1) * dayColumnWidth, getHeight(), selectedDayPaint);
            }
        }
        for (int i = 0; i <= dates.size(); i++) {
            float x = i * dayColumnWidth;
            canvas.drawLine(x, 0, x, getHeight(), linePaint);
        }
        for (int hour = 0; hour <= HOURS_PER_DAY; hour++) {
            float y = hour * hourHeight;
            canvas.drawLine(0, y, getWidth(), y, linePaint);
        }
    }

    private void drawTodos(Canvas canvas) {
        List<PositionedTodo> positionedTodos = buildPositionedTodos();
        for (PositionedTodo positioned : positionedTodos) {
            drawTodo(canvas, positioned);
        }
    }

    private List<PositionedTodo> buildPositionedTodos() {
        List<PositionedTodo> result = new ArrayList<>();
        for (TodoItem todo : todos) {
            int dayIndex = dayIndexFor(todo);
            if (dayIndex < 0 || dayIndex >= dates.size()) {
                continue;
            }
            int overlapIndex = 0;
            int overlapCount = 1;
            List<TodoItem> sameDay = todosForDay(dayIndex);
            for (TodoItem other : sameDay) {
                if (other == todo) {
                    continue;
                }
                if (overlaps(todo, other)) {
                    overlapCount++;
                    if (other.getStartDateTimeMillis() < todo.getStartDateTimeMillis()
                            || (other.getStartDateTimeMillis() == todo.getStartDateTimeMillis()
                            && other.getId() < todo.getId())) {
                        overlapIndex++;
                    }
                }
            }
            result.add(new PositionedTodo(todo, dayIndex, overlapIndex, Math.min(overlapCount, 3)));
        }
        return result;
    }

    private void drawTodo(Canvas canvas, PositionedTodo positioned) {
        TodoItem todo = positioned.todo;
        long startMillis = todo.getStartDateTimeMillis();
        long endMillis = todo.getEndDateTimeMillis();
        if (startMillis <= 0L) {
            return;
        }
        if (endMillis <= startMillis) {
            endMillis = startMillis + 60L * 60L * 1000L;
        }

        float columnLeft = positioned.dayIndex * dayColumnWidth;
        float slotWidth = (dayColumnWidth - dp(10)) / positioned.overlapCount;
        float left = columnLeft + dp(5) + positioned.overlapIndex * slotWidth;
        float right = Math.min(columnLeft + dayColumnWidth - dp(5), left + slotWidth - dp(4));
        float top = yForMillis(startMillis) + dp(3);
        float bottom = Math.max(top + dp(42), yForMillis(endMillis) - dp(3));

        int strokeColor = PriorityTagUtils.colorForTag(todo.getTag());
        cardPaint.setColor(PriorityTagUtils.softColorForTag(todo.getTag()));
        cardStrokePaint.setColor(strokeColor);
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, dp(8), dp(8), cardPaint);
        canvas.drawRoundRect(rect, dp(8), dp(8), cardStrokePaint);
        hitRects.add(new HitRect(rect, todo));

        float textLeft = left + dp(7);
        float textRight = right - dp(7);
        float availableTextWidth = Math.max(1f, textRight - textLeft);
        textPaint.setColor(Color.parseColor("#202124"));
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(sp(12));
        canvas.drawText(ellipsize(todo.getTitle(), textPaint, availableTextWidth), textLeft, top + dp(18), textPaint);

        smallTextPaint.setColor(Color.parseColor("#3C4043"));
        smallTextPaint.setFakeBoldText(false);
        smallTextPaint.setTextSize(sp(10));
        canvas.drawText(ellipsize(timeRange(todo), smallTextPaint, availableTextWidth), textLeft, top + dp(34), smallTextPaint);
    }

    private float yForMillis(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        return (hour + minute / 60f) * hourHeight;
    }

    private String timeRange(TodoItem todo) {
        String start = DateTimeUtils.formatTime(todo.getStartDateTimeMillis());
        String end = DateTimeUtils.formatTime(todo.getEndDateTimeMillis());
        if (end.isEmpty()) {
            return start;
        }
        return start + " - " + end;
    }

    private int dayIndexFor(TodoItem todo) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(todo.getStartDateTimeMillis());
        normalizeStartOfDay(date);
        for (int i = 0; i < dates.size(); i++) {
            if (sameDay(date, dates.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private List<TodoItem> todosForDay(int dayIndex) {
        List<TodoItem> result = new ArrayList<>();
        for (TodoItem todo : todos) {
            if (dayIndexFor(todo) == dayIndex) {
                result.add(todo);
            }
        }
        return result;
    }

    private boolean overlaps(TodoItem first, TodoItem second) {
        long firstStart = first.getStartDateTimeMillis();
        long firstEnd = first.getEndDateTimeMillis() > firstStart
                ? first.getEndDateTimeMillis()
                : firstStart + 60L * 60L * 1000L;
        long secondStart = second.getStartDateTimeMillis();
        long secondEnd = second.getEndDateTimeMillis() > secondStart
                ? second.getEndDateTimeMillis()
                : secondStart + 60L * 60L * 1000L;
        return firstStart < secondEnd && secondStart < firstEnd;
    }

    private String ellipsize(String text, TextPaint paint, float width) {
        CharSequence value = TextUtils.ellipsize(text == null ? "" : text, paint, width, TextUtils.TruncateAt.END);
        return value.toString();
    }

    private boolean sameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
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

    private float sp(int value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    private static class PositionedTodo {
        final TodoItem todo;
        final int dayIndex;
        final int overlapIndex;
        final int overlapCount;

        PositionedTodo(TodoItem todo, int dayIndex, int overlapIndex, int overlapCount) {
            this.todo = todo;
            this.dayIndex = dayIndex;
            this.overlapIndex = overlapIndex;
            this.overlapCount = overlapCount;
        }
    }

    private static class HitRect {
        final RectF rect;
        final TodoItem todo;

        HitRect(RectF rect, TodoItem todo) {
            this.rect = rect;
            this.todo = todo;
        }
    }
}
