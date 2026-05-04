package com.example.imageview;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;

public class AiTodoParserClient {
    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";

    public AiTodoDraft parseClipboardText(String clipboardText) throws Exception {
        if (BuildConfig.OPENAI_API_KEY == null || BuildConfig.OPENAI_API_KEY.trim().isEmpty()) {
            throw new IllegalStateException("Missing openai.api.key in local.properties");
        }

        JSONObject request = buildRequest(clipboardText);
        HttpURLConnection connection = (HttpURLConnection) new URL(RESPONSES_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(30000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(request.toString());
        }

        int responseCode = connection.getResponseCode();
        String body = readBody(responseCode >= 200 && responseCode < 300
                ? connection.getInputStream()
                : connection.getErrorStream());
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("OpenAI request failed: " + responseCode + " " + body);
        }

        JSONObject response = new JSONObject(body);
        JSONObject parsed = new JSONObject(extractOutputText(response));
        return new AiTodoDraft(
                parsed.optString("title", ""),
                parsed.optString("content", ""),
                parsed.optString("date", ""),
                parsed.optString("time", ""),
                parsed.optString("tag", ""),
                parsed.optInt("priority", 0)
        );
    }

    private JSONObject buildRequest(String clipboardText) throws Exception {
        JSONObject request = new JSONObject();
        request.put("model", BuildConfig.OPENAI_MODEL);
        request.put("store", false);
        request.put("instructions",
                "Extract one todo draft from clipboard text. Preserve the user's language. "
                        + "Assume today's date is " + currentDateString() + " in the user's local timezone. "
                        + "Use empty strings for unknown fields. Use date format yyyy-MM-dd and time format HH:mm. "
                        + "When the text includes only a time period, use these defaults: morning 09:00, noon 12:00, afternoon 15:00, evening 19:00. "
                        + "Priority is 1 for urgent/high, 2 for medium, 3 for low, 4 for lowest, 0 when unknown. "
                        + "If the text is not a plausible todo, return an empty title.");
        request.put("input", clipboardText);
        request.put("text", buildTextFormat());
        return request;
    }

    private String currentDateString() {
        Calendar calendar = Calendar.getInstance();
        return String.format(
                Locale.getDefault(),
                "%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private JSONObject buildTextFormat() throws Exception {
        JSONObject properties = new JSONObject();
        properties.put("title", stringSchema());
        properties.put("content", stringSchema());
        properties.put("date", stringSchema());
        properties.put("time", stringSchema());
        properties.put("tag", stringSchema());

        JSONObject prioritySchema = new JSONObject();
        prioritySchema.put("type", "integer");
        prioritySchema.put("enum", new JSONArray().put(0).put(1).put(2).put(3).put(4));
        properties.put("priority", prioritySchema);

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("properties", properties);
        schema.put("required", new JSONArray()
                .put("title")
                .put("content")
                .put("date")
                .put("time")
                .put("tag")
                .put("priority"));

        JSONObject format = new JSONObject();
        format.put("type", "json_schema");
        format.put("name", "todo_draft");
        format.put("strict", true);
        format.put("schema", schema);

        JSONObject text = new JSONObject();
        text.put("format", format);
        return text;
    }

    private JSONObject stringSchema() throws Exception {
        JSONObject schema = new JSONObject();
        schema.put("type", "string");
        return schema;
    }

    private String extractOutputText(JSONObject response) throws Exception {
        String directText = response.optString("output_text", "");
        if (!directText.isEmpty()) {
            return directText;
        }

        JSONArray output = response.optJSONArray("output");
        if (output == null) {
            throw new IOException("OpenAI response did not include output text");
        }
        for (int i = 0; i < output.length(); i++) {
            JSONObject item = output.optJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONArray content = item.optJSONArray("content");
            if (content == null) {
                continue;
            }
            for (int j = 0; j < content.length(); j++) {
                JSONObject contentItem = content.optJSONObject(j);
                if (contentItem == null) {
                    continue;
                }
                if ("output_text".equals(contentItem.optString("type"))) {
                    String text = contentItem.optString("text", "");
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
        }
        throw new IOException("OpenAI response did not include output text");
    }

    private String readBody(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
