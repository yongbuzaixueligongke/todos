package com.example.imageview;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtils {

    public static final String DEFAULT_REMINDER_TIME = "09:00";

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private DateTimeUtils() {
    }

    public static long parseDateStartMillis(String date) {
        return parseDateMillis(date, true);
    }

    public static long parseDateEndMillis(String date) {
        return parseDateMillis(date, false);
    }

    public static long parseTaskStartMillis(String date, String time) {
        if (isBlank(date)) {
            return 0L;
        }
        String effectiveTime = isBlank(time) ? DEFAULT_REMINDER_TIME : time.trim();
        return parseDateTimeMillis(date, effectiveTime);
    }

    public static long parseDateTimeMillis(String date, String time) {
        if (isBlank(date) || isBlank(time)) {
            return 0L;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault());
            format.setLenient(false);
            Date parsed = format.parse(date.trim() + " " + time.trim());
            return parsed != null ? parsed.getTime() : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public static String formatDate(long millis) {
        if (millis <= 0L) {
            return "";
        }
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(millis));
    }

    public static String formatTime(long millis) {
        if (millis <= 0L) {
            return "";
        }
        return new SimpleDateFormat(TIME_PATTERN, Locale.getDefault()).format(new Date(millis));
    }

    public static String formatTaskSchedule(long startMillis, long endMillis) {
        if (startMillis <= 0L) {
            return "No schedule";
        }

        String date = formatDate(startMillis);
        if (endMillis > 0L) {
            return date + " " + formatTime(startMillis) + "-" + formatTime(endMillis);
        }

        return date;
    }

    public static String formatTaskSchedule(long startMillis, long endMillis, boolean hasExplicitStartTime) {
        if (startMillis <= 0L) {
            return "No schedule";
        }

        String date = formatDate(startMillis);
        if (endMillis > 0L) {
            return date + " " + formatTime(startMillis) + "-" + formatTime(endMillis);
        }
        if (hasExplicitStartTime) {
            return date + " " + formatTime(startMillis);
        }
        return date;
    }

    public static boolean isSameDay(long millis, Calendar day) {
        if (millis <= 0L || day == null) {
            return false;
        }
        Calendar value = Calendar.getInstance();
        value.setTimeInMillis(millis);
        return value.get(Calendar.YEAR) == day.get(Calendar.YEAR)
                && value.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR);
    }

    public static long endOfDayMillis(long millis) {
        if (millis <= 0L) {
            return 0L;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    private static long parseDateMillis(String date, boolean startOfDay) {
        if (isBlank(date)) {
            return 0L;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
            format.setLenient(false);
            Date parsed = format.parse(date.trim());
            if (parsed == null) {
                return 0L;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsed);
            if (startOfDay) {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
            }
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            return 0L;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
