package com.gym.coze;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CozeClientImpl implements CozeClient {

    private final CozeConfig cozeConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void streamRun(String prompt, String sessionId, CozeSseEmitter emitter) {
        HttpURLConnection conn = null;
        try {
            // Build request body matching gym_coze.py format exactly
            Map<String, Object> promptItem = new LinkedHashMap<>();
            promptItem.put("type", "text");
            promptItem.put("content", Map.of("text", prompt));

            Map<String, Object> query = new LinkedHashMap<>();
            query.put("prompt", List.of(promptItem));

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("query", query);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("content", content);
            body.put("type", "query");
            body.put("session_id", sessionId != null ? sessionId : "");
            body.put("project_id", cozeConfig.getProjectId());

            byte[] bodyBytes = objectMapper.writeValueAsBytes(body);

            conn = (HttpURLConnection) URI.create(cozeConfig.getApiUrl()).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setRequestProperty("Authorization", "Bearer " + cozeConfig.getApiToken());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
                os.flush();
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                log.error("Coze API returned status: {}", status);
                emitter.error(new RuntimeException("Coze API returned HTTP " + status));
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null && !emitter.isCompleted()) {
                    if (line.startsWith("data:")) {
                        String json = line.substring(5).trim();
                        try {
                            JsonNode node = objectMapper.readTree(json);
                            JsonNode contentNode = node.get("content");
                            if (contentNode != null && contentNode.has("answer")) {
                                JsonNode answer = contentNode.get("answer");
                                if (answer != null && !answer.isNull()) {
                                    emitter.send(answer.asText());
                                }
                            }
                            if (node.has("finish") && node.get("finish").asBoolean()
                                    && "answer".equals(node.path("type").asText())) {
                                emitter.complete();
                                return;
                            }
                        } catch (Exception e) {
                            // skip malformed JSON
                        }
                    }
                }
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("调用 Coze API 失败", e);
            emitter.error(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
