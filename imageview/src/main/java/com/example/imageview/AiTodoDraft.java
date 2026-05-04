package com.example.imageview;

public class AiTodoDraft {
    private final String title;
    private final String content;
    private final String date;
    private final String time;
    private final String tag;
    private final int priority;

    public AiTodoDraft(String title, String content, String date, String time, String tag, int priority) {
        this.title = safe(title);
        this.content = safe(content);
        this.date = safe(date);
        this.time = safe(time);
        this.tag = safe(tag);
        this.priority = Math.max(0, Math.min(priority, 4));
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTag() {
        return tag;
    }

    public int getPriority() {
        return priority;
    }

    public String getEditorTag() {
        if (!tag.isEmpty()) {
            return tag;
        }
        if (priority >= 1 && priority <= 4) {
            return "P" + priority;
        }
        return "";
    }

    public boolean hasUsableTitle() {
        return !title.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
