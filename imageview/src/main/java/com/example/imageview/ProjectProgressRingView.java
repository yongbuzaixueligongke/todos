package com.example.imageview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ProjectProgressRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect textBounds = new Rect();
    private final RectF arcBounds = new RectF();
    private int progressPercent = 0;

    public ProjectProgressRingView(Context context) {
        super(context);
        init();
    }

    public ProjectProgressRingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProjectProgressRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        float strokeWidth = 9f * density;

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setColor(Color.parseColor("#E8EAED"));

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.parseColor("#7654B8"));

        textPaint.setColor(Color.parseColor("#2D221E"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(14f * density);
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = Math.max(0, Math.min(100, progressPercent));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float padding = Math.max(trackPaint.getStrokeWidth(), 8f);
        arcBounds.set(padding, padding, getWidth() - padding, getHeight() - padding);

        canvas.drawArc(arcBounds, -90f, 360f, false, trackPaint);
        canvas.drawArc(arcBounds, -90f, 360f * progressPercent / 100f, false, progressPaint);

        String text = progressPercent + "%";
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        float centerY = getHeight() / 2f - textBounds.exactCenterY();
        canvas.drawText(text, getWidth() / 2f, centerY, textPaint);
    }
}
