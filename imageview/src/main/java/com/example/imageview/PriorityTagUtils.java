package com.example.imageview;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public final class PriorityTagUtils {

    public static final String TAG_P1 = "P1";
    public static final String TAG_P2 = "P2";
    public static final String TAG_P3 = "P3";
    public static final String TAG_P4 = "P4";
    public static final String[] PRIORITY_TAGS = {TAG_P1, TAG_P2, TAG_P3, TAG_P4};

    private PriorityTagUtils() {
    }

    public static String normalize(String tag) {
        return tag == null ? "" : tag.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isPriorityTag(String tag) {
        String normalized = normalize(tag);
        for (String priorityTag : PRIORITY_TAGS) {
            if (priorityTag.equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    public static int colorForTag(String tag) {
        switch (normalize(tag)) {
            case TAG_P1:
                return Color.parseColor("#E94457");
            case TAG_P2:
                return Color.parseColor("#F57C00");
            case TAG_P3:
                return Color.parseColor("#FBC02D");
            case TAG_P4:
                return Color.parseColor("#2EAD63");
            default:
                return Color.parseColor("#8A8F98");
        }
    }

    public static int softColorForTag(String tag) {
        switch (normalize(tag)) {
            case TAG_P1:
                return Color.parseColor("#FFF0F2");
            case TAG_P2:
                return Color.parseColor("#FFF3E0");
            case TAG_P3:
                return Color.parseColor("#FFFDE7");
            case TAG_P4:
                return Color.parseColor("#EAF7EF");
            default:
                return Color.parseColor("#F5F5F5");
        }
    }

    public static GradientDrawable createDotDrawable(String tag, int sizePx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(colorForTag(tag));
        drawable.setSize(sizePx, sizePx);
        drawable.setBounds(0, 0, sizePx, sizePx);
        return drawable;
    }

    public static GradientDrawable createTagRowDrawable(String tag, boolean fixed, int radiusPx) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radiusPx);
        if (fixed) {
            drawable.setColor(softColorForTag(tag));
            drawable.setStroke(Math.max(1, radiusPx / 8), colorForTag(tag));
        } else {
            drawable.setColor(Color.WHITE);
            drawable.setStroke(Math.max(1, radiusPx / 8), Color.TRANSPARENT);
        }
        return drawable;
    }

    public static GradientDrawable createCalendarTodoDrawable(String tag, int radiusPx) {
        boolean priority = isPriorityTag(tag);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radiusPx);
        drawable.setColor(priority ? softColorForTag(tag) : Color.parseColor("#F5F7FA"));
        drawable.setStroke(
                Math.max(1, radiusPx / 6),
                priority ? colorForTag(tag) : Color.parseColor("#D7DEE8")
        );
        return drawable;
    }

    public static void applyTagIcon(TextView textView, String tag) {
        String normalized = normalize(tag);
        if (normalized.isEmpty()) {
            textView.setText("");
            textView.setCompoundDrawables(null, null, null, null);
            textView.setVisibility(View.GONE);
            return;
        }

        textView.setText(normalized);
        int iconSize = dp(textView, 10);
        textView.setCompoundDrawables(createDotDrawable(normalized, iconSize), null, null, null);
        textView.setCompoundDrawablePadding(dp(textView, 6));
        textView.setTextColor(Color.parseColor("#30343B"));
        textView.setVisibility(View.VISIBLE);
    }

    public static int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }
}
