package com.gym.coze;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CozeClientImpl implements CozeClient {

    private final CozeConfig cozeConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }

    @Override
    public void streamRun(String prompt, String sessionId, CozeSseEmitter emitter) {
        RestTemplate restTemplate = createRestTemplate();

        try {
            // Build request body in Coze Workflow format
            Map<String, Object> textContent = new LinkedHashMap<>();
            textContent.put("text", prompt);

            Map<String, Object> promptItem = new LinkedHashMap<>();
            promptItem.put("type", "text");
            promptItem.put("content", textContent);

            Map<String, Object> query = new LinkedHashMap<>();
            query.put("prompt", List.of(promptItem));

            Map<String, Object> content = new LinkedHashMap<>();
            content.put("query", query);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("content", content);
            body.put("type", "query");
            body.put("session_id", sessionId != null ? sessionId : "");
            body.put("project_id", cozeConfig.getProjectId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "text/event-stream");
            headers.setBearerAuth(cozeConfig.getApiToken());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.execute(
                    cozeConfig.getApiUrl(),
                    HttpMethod.POST,
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                        request.getHeaders().set("Accept", "text/event-stream");
                        request.getHeaders().setBearerAuth(cozeConfig.getApiToken());
                        request.getBody().write(objectMapper.writeValueAsBytes(body));
                    },
                    response -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
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
                                        // Finish when answer is complete
                                        if (node.has("finish") && node.get("finish").asBoolean()
                                                && "answer".equals(node.path("type").asText())) {
                                            emitter.complete();
                                            return null;
                                        }
                                    } catch (Exception e) {
                                        // skip malformed JSON
                                    }
                                }
                            }
                            emitter.complete();
                        } catch (Exception e) {
                            log.error("读取 Coze SSE 响应失败", e);
                            emitter.error(e);
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.error("调用 Coze API 失败", e);
            emitter.error(e);
        }
    }
}
