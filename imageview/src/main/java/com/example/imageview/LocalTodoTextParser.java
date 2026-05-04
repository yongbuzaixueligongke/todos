package com.example.imageview;

import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocalTodoTextParser {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\u4E0A\\u5348|\\u4E0B\\u5348|\\u4E2D\\u5348|\\u665A\\u4E0A)?\\s*(\\d{1,2})[\\u70B9:\\uFF1A\\u65F6](\\d{1,2}\\u5206?)?");
    private static final Pattern PERIOD_PATTERN = Pattern.compile("(\\u4E0A\\u5348|\\u4E0B\\u5348|\\u4E2D\\u5348|\\u665A\\u4E0A)");

    private LocalTodoTextParser() {
    }

    public static AiTodoDraft parse(String text) {
        String normalized = safe(text).replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return new AiTodoDraft("", "", "", "", "", 0);
        }

        String date = extractDate(normalized);
        String time = extractTime(normalized);
        String title = cleanupTitle(normalized);
        boolean hasActionSignal = containsActionSignal(normalized) || containsActionSignal(title);
        if (date.isEmpty() && time.isEmpty() && !hasActionSignal) {
            return new AiTodoDraft("", normalized, "", "", "", 0);
        }
        if (title.isEmpty()) {
            if (!hasActionSignal) {
                return new AiTodoDraft("", normalized, date, time, "", 0);
            }
            title = normalized;
        }
        String tag = extractTag(normalized);
        int priority = extractPriority(normalized);
        return new AiTodoDraft(title, normalized, date, time, tag, priority);
    }

    private static String extractDate(String text) {
        Calendar calendar = Calendar.getInstance();
        if (text.contains("\u4ECA\u5929")) {
            return formatDate(calendar);
        }
        if (text.contains("\u660E\u5929")) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            return formatDate(calendar);
        }
        if (text.contains("\u540E\u5929")) {
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            return formatDate(calendar);
        }

        int targetDay = weekdayFromText(text);
        if (targetDay != -1) {
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            int delta = targetDay - currentDay;
            if (delta < 0) {
                delta += 7;
            }
            calendar.add(Calendar.DAY_OF_YEAR, delta);
            return formatDate(calendar);
        }
        return "";
    }

    private static int weekdayFromText(String text) {
        if (text.contains("\u5468\u65E5") || text.contains("\u661F\u671F\u65E5") || text.contains("\u793C\u62DC\u65E5") || text.contains("\u5468\u5929")) {
            return Calendar.SUNDAY;
        }
        if (text.contains("\u5468\u4E00") || text.contains("\u661F\u671F\u4E00") || text.contains("\u793C\u62DC\u4E00")) return Calendar.MONDAY;
        if (text.contains("\u5468\u4E8C") || text.contains("\u661F\u671F\u4E8C") || text.contains("\u793C\u62DC\u4E8C")) return Calendar.TUESDAY;
        if (text.contains("\u5468\u4E09") || text.contains("\u661F\u671F\u4E09") || text.contains("\u793C\u62DC\u4E09")) return Calendar.WEDNESDAY;
        if (text.contains("\u5468\u56DB") || text.contains("\u661F\u671F\u56DB") || text.contains("\u793C\u62DC\u56DB")) return Calendar.THURSDAY;
        if (text.contains("\u5468\u4E94") || text.contains("\u661F\u671F\u4E94") || text.contains("\u793C\u62DC\u4E94")) return Calendar.FRIDAY;
        if (text.contains("\u5468\u516D") || text.contains("\u661F\u671F\u516D") || text.contains("\u793C\u62DC\u516D")) return Calendar.SATURDAY;
        return -1;
    }

    private static String extractTime(String text) {
        Matcher matcher = TIME_PATTERN.matcher(text);
        if (matcher.find()) {
            String period = safe(matcher.group(1));
            int hour = parseInt(matcher.group(2), -1);
            if (hour < 0) {
                return "";
            }
            if ((period.equals("\u4E0B\u5348") || period.equals("\u665A\u4E0A")) && hour < 12) {
                hour += 12;
            } else if (period.equals("\u4E2D\u5348") && hour < 11) {
                hour += 12;
            }

            String minuteGroup = safe(matcher.group(3)).replace("\u5206", "");
            int minute = minuteGroup.isEmpty() ? 0 : parseInt(minuteGroup, 0);
            return String.format(Locale.getDefault(), "%02d:%02d", hour, Math.max(0, Math.min(minute, 59)));
        }

        Matcher periodMatcher = PERIOD_PATTERN.matcher(text);
        if (!periodMatcher.find()) {
            return "";
        }
        String period = periodMatcher.group(1);
        if (period.equals("\u4E0A\u5348")) {
            return "09:00";
        }
        if (period.equals("\u4E2D\u5348")) {
            return "12:00";
        }
        if (period.equals("\u4E0B\u5348")) {
            return "15:00";
        }
        if (period.equals("\u665A\u4E0A")) {
            return "19:00";
        }
        return "";
    }

    private static String cleanupTitle(String text) {
        String cleaned = text
                .replaceAll("#\\S+", "")
                .replaceAll("(\\u4ECA\\u5929|\\u660E\\u5929|\\u540E\\u5929|\\u5468[\\u4E00\\u4E8C\\u4E09\\u56DB\\u4E94\\u516D\\u65E5\\u5929]|\\u661F\\u671F[\\u4E00\\u4E8C\\u4E09\\u56DB\\u4E94\\u516D\\u65E5]|\\u793C\\u62DC[\\u4E00\\u4E8C\\u4E09\\u56DB\\u4E94\\u516D\\u65E5])", "")
                .replaceAll("(\\u4E0A\\u5348|\\u4E0B\\u5348|\\u4E2D\\u5348|\\u665A\\u4E0A)", "")
                .replaceAll("\\d{1,2}[\\u70B9:\\uFF1A\\u65F6](\\d{1,2}\\u5206?)?", "")
                .replaceAll("(\\u63D0\\u9192\\u6211|\\u8BB0\\u5F97|\\u5F85\\u529E|\\u4EFB\\u52A1)", "")
                .trim();
        return cleaned.replaceAll("^[,\\uFF0C\\u3002\\s]+|[,\\uFF0C\\u3002\\s]+$", "");
    }

    private static String extractTag(String text) {
        Matcher matcher = Pattern.compile("#([\\p{L}\\p{N}_-]+)").matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (text.contains("\u7D27\u6025") || text.contains("\u91CD\u8981")) {
            return PriorityTagUtils.TAG_P1;
        }
        return "";
    }

    private static int extractPriority(String text) {
        if (text.contains("\u7D27\u6025") || text.contains("\u91CD\u8981") || text.contains("\u9AD8\u4F18\u5148\u7EA7")) return 1;
        if (text.contains("\u4E2D\u4F18\u5148\u7EA7")) return 2;
        if (text.contains("\u4F4E\u4F18\u5148\u7EA7")) return 3;
        return 0;
    }

    private static boolean containsActionSignal(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        return Pattern.compile(
                "(todo|task|\\u5F85\\u529E|\\u4EFB\\u52A1|\\u63D0\\u9192|\\u8BB0\\u5F97|"
                        + "\\u53D1\\u9001|\\u53D1|\\u63D0\\u4EA4|\\u5B8C\\u6210|\\u5904\\u7406|"
                        + "\\u56DE\\u590D|\\u8054\\u7CFB|\\u4E70|\\u8D2D\\u4E70|\\u5F00\\u4F1A|"
                        + "\\u4F1A\\u8BAE|\\u6574\\u7406|\\u66F4\\u65B0|\\u590D\\u76D8|"
                        + "\\u9884\\u7EA6|\\u7F34\\u8D39|\\u652F\\u4ED8|\\u53D6|\\u5BC4)",
                Pattern.CASE_INSENSITIVE
        ).matcher(text).find();
    }

    private static String formatDate(Calendar calendar) {
        return String.format(
                Locale.getDefault(),
                "%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
