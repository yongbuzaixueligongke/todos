package com.example.imageview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateHandlingTest {

    @Test
    public void parseTaskStartMillis_usesNineAmForDateOnlyTasks() throws Exception {
        long actual = DateTimeUtils.parseTaskStartMillis("2026-05-03", "");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        format.setLenient(false);
        long expected = format.parse("2026-05-03 09:00").getTime();

        assertEquals(expected, actual);
    }

    @Test
    public void parseDateTimeMillis_usesProvidedTimeForIntervalTasks() throws Exception {
        long actual = DateTimeUtils.parseDateTimeMillis("2026-05-03", "10:30");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        format.setLenient(false);
        long expected = format.parse("2026-05-03 10:30").getTime();

        assertEquals(expected, actual);
    }

    @Test
    public void overdue_usesIntervalEndTimeWhenPresent() {
        TodoItem item = new TodoItem();
        item.setCompleted(false);
        long now = System.currentTimeMillis();
        item.setStartDateTimeMillis(now - 120000L);
        item.setEndDateTimeMillis(now - 60000L);

        assertTrue(item.isOverdue());
    }

    @Test
    public void overdue_dateOnlyWaitsUntilEndOfDay() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 9);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        TodoItem item = new TodoItem();
        item.setCompleted(false);
        item.setStartDateTimeMillis(today.getTimeInMillis());
        item.setEndDateTimeMillis(0L);

        assertFalse(item.isOverdue());
    }
}
