package com.example.imageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Locale;

public class WeekTimeAxisView extends View {

    private static final int HOURS_PER_DAY = 24;

    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int hourHeight;
    private int scrollOffset;

    public WeekTimeAxisView(Context context) {
        super(context);
        init();
    }

    public WeekTimeAxisView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        hourHeight = dp(68);
        textPaint.setTextSize(sp(10));
        textPaint.setColor(Color.parseColor("#6F7680"));
        linePaint.setColor(Color.parseColor("#E8EAED"));
        linePaint.setStrokeWidth(dp(1));
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = Math.max(0, scrollOffset);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        for (int hour = 0; hour <= HOURS_PER_DAY; hour++) {
            float y = hour * hourHeight - scrollOffset;
            if (y < -hourHeight || y > getHeight() + hourHeight) {
                continue;
            }
            canvas.drawLine(getWidth() - dp(10), y, getWidth(), y, linePaint);
            if (hour < HOURS_PER_DAY) {
                canvas.drawText(String.format(Locale.getDefault(), "%02d:00", hour), dp(6), y + dp(15), textPaint);
            }
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private float sp(int value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
