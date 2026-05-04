package com.example.imageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeekDateBarView extends View {

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    private final List<Calendar> dates = new ArrayList<>();
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint todayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint weekdayPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint dayPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Calendar selectedDate = Calendar.getInstance();
    private OnDateSelectedListener listener;
    private int dayColumnWidth;
    private int leadingInset;

    public WeekDateBarView(Context context) {
        super(context);
        init();
    }

    public WeekDateBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        dayColumnWidth = dp(88);
        leadingInset = dp(56);
        selectedPaint.setColor(Color.parseColor("#ECE5FF"));
        todayPaint.setColor(Color.parseColor("#EAF1FF"));
        dividerPaint.setColor(Color.parseColor("#E8EAED"));
        dividerPaint.setStrokeWidth(dp(1));
        weekdayPaint.setTextSize(sp(12));
        weekdayPaint.setColor(Color.parseColor("#5F6368"));
        dayPaint.setTextSize(sp(17));
        dayPaint.setFakeBoldText(true);
        dayPaint.setColor(Color.parseColor("#202124"));
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

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (height <= 0) {
            height = dp(64);
        }
        setMeasuredDimension(Math.max(dayColumnWidth, leadingInset + dates.size() * dayColumnWidth), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Calendar today = Calendar.getInstance();
        normalizeStartOfDay(today);

        for (int i = 0; i < dates.size(); i++) {
            Calendar date = dates.get(i);
            float left = leadingInset + i * dayColumnWidth;
            float right = left + dayColumnWidth;
            boolean selected = sameDay(date, selectedDate);
            boolean isToday = sameDay(date, today);
            if (selected || isToday) {
                Paint paint = selected ? selectedPaint : todayPaint;
                RectF rect = new RectF(left + dp(9), dp(7), right - dp(9), getHeight() - dp(7));
                canvas.drawRoundRect(rect, dp(18), dp(18), paint);
            }

            weekdayPaint.setFakeBoldText(selected);
            weekdayPaint.setColor(selected ? Color.parseColor("#202124") : Color.parseColor("#5F6368"));
            drawCenteredText(canvas, weekdayLabel(date), weekdayPaint, left, right, dp(24));

            dayPaint.setColor(selected ? Color.parseColor("#202124") : (isToday ? Color.parseColor("#1A73E8") : Color.parseColor("#202124")));
            dayPaint.setFakeBoldText(selected || isToday);
            drawCenteredText(canvas, String.valueOf(date.get(Calendar.DAY_OF_MONTH)), dayPaint, left, right, dp(49));

            canvas.drawLine(right, dp(12), right, getHeight() - dp(12), dividerPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float localX = event.getX() - leadingInset;
            if (localX < 0) {
                return true;
            }
            int index = (int) (localX / dayColumnWidth);
            if (index >= 0 && index < dates.size() && listener != null) {
                listener.onDateSelected((Calendar) dates.get(index).clone());
                return true;
            }
        }
        return true;
    }

    private void drawCenteredText(Canvas canvas, String text, Paint paint, float left, float right, float baseline) {
        float x = (left + right) / 2f - paint.measureText(text) / 2f;
        canvas.drawText(text, x, baseline, paint);
    }

    private String weekdayLabel(Calendar day) {
        switch (day.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return "\u5468\u4E00";
            case Calendar.TUESDAY:
                return "\u5468\u4E8C";
            case Calendar.WEDNESDAY:
                return "\u5468\u4E09";
            case Calendar.THURSDAY:
                return "\u5468\u56DB";
            case Calendar.FRIDAY:
                return "\u5468\u4E94";
            case Calendar.SATURDAY:
                return "\u5468\u516D";
            case Calendar.SUNDAY:
            default:
                return "\u5468\u65E5";
        }
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
}
