package com.example.imageview;

import android.util.Log;

import java.util.Locale;

public class AiTodoRecognitionController {

    public interface Callback {
        void onMessage(String message);

        void onDraftReady(AiTodoDraft draft, String message);

        void onFailure(String message);
    }

    private static final String TAG = "AiTodoRecognition";

    private final AiTodoParserClient aiTodoParserClient = new AiTodoParserClient();
    private boolean requestInFlight = false;
    private String lastSuccessfulFingerprint = "";

    public void recognize(String clipboardText, Callback callback) {
        String normalizedText = clipboardText == null ? "" : clipboardText.trim();
        if (normalizedText.isEmpty()) {
            if (callback != null) {
                callback.onFailure("Clipboard has no text");
            }
            return;
        }

        String fingerprint = buildFingerprint(normalizedText);
        if (fingerprint.equals(lastSuccessfulFingerprint)) {
            if (callback != null) {
                callback.onMessage("This clipboard content was already recognized");
            }
            return;
        }
        if (requestInFlight) {
            if (callback != null) {
                callback.onMessage("AI recognition is already running");
            }
            return;
        }

        requestInFlight = true;
        if (callback != null) {
            callback.onMessage("Recognizing clipboard content");
        }

        DatabaseExecutor.execute(() -> {
            try {
                AiTodoDraft draft = aiTodoParserClient.parseClipboardText(normalizedText);
                AiTodoDraft finalDraft = draft.hasUsableTitle()
                        ? normalizeAiDraft(draft, normalizedText)
                        : LocalTodoTextParser.parse(normalizedText);
                DatabaseExecutor.runOnMainThread(() -> {
                    requestInFlight = false;
                    if (!finalDraft.hasUsableTitle()) {
                        if (callback != null) {
                            callback.onFailure("No usable task was recognized");
                        }
                        return;
                    }
                    lastSuccessfulFingerprint = fingerprint;
                    if (callback != null) {
                        callback.onDraftReady(
                                finalDraft,
                                draft.hasUsableTitle() ? "" : "AI was unavailable, used local parsing instead"
                        );
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "AI todo recognition failed", e);
                AiTodoDraft fallbackDraft = LocalTodoTextParser.parse(normalizedText);
                DatabaseExecutor.runOnMainThread(() -> {
                    requestInFlight = false;
                    if (fallbackDraft.hasUsableTitle()) {
                        lastSuccessfulFingerprint = fingerprint;
                        if (callback != null) {
                            callback.onDraftReady(fallbackDraft, buildAiFallbackMessage(e));
                        }
                    } else if (callback != null) {
                        callback.onFailure(buildAiFailureMessage(e));
                    }
                });
            }
        });
    }

    private String buildFingerprint(String text) {
        String normalized = text.trim().replaceAll("\\s+", " ");
        return normalized.length() + ":" + normalized.hashCode();
    }

    private AiTodoDraft normalizeAiDraft(AiTodoDraft draft, String sourceText) {
        AiTodoDraft localDraft = LocalTodoTextParser.parse(sourceText);
        String date = isIsoDate(draft.getDate()) ? draft.getDate() : localDraft.getDate();
        String time = isClockTime(draft.getTime()) ? draft.getTime() : localDraft.getTime();
        String tag = draft.getTag().isEmpty() ? localDraft.getTag() : draft.getTag();
        int priority = draft.getPriority() == 0 ? localDraft.getPriority() : draft.getPriority();
        return new AiTodoDraft(
                draft.hasUsableTitle() ? draft.getTitle() : localDraft.getTitle(),
                draft.getContent().isEmpty() ? localDraft.getContent() : draft.getContent(),
                date,
                time,
                tag,
                priority
        );
    }

    private boolean isIsoDate(String value) {
        return value != null && value.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private boolean isClockTime(String value) {
        return value != null && value.matches("([01]\\d|2[0-3]):[0-5]\\d");
    }

    private String buildAiFallbackMessage(Exception e) {
        return buildAiFailurePrefix(e) + ", used local parsing";
    }

    private String buildAiFailureMessage(Exception e) {
        return buildAiFailurePrefix(e) + ", you can create the task manually";
    }

    private String buildAiFailurePrefix(Exception e) {
        String message = e == null ? "" : safe(e.getMessage()).toLowerCase(Locale.ROOT);
        if (message.contains("missing openai.api.key")) {
            return "AI key is not configured";
        }
        if (message.contains("401") || message.contains("unauthorized") || message.contains("invalid_api_key")) {
            return "AI key is invalid";
        }
        if (message.contains("429") || message.contains("rate_limit")) {
            return "AI requests are rate limited";
        }
        if (message.contains("timeout") || message.contains("timed out")) {
            return "AI request timed out";
        }
        if (message.contains("failed to connect") || message.contains("unable to resolve host")) {
            return "AI network is unavailable";
        }
        return "AI is temporarily unavailable";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
