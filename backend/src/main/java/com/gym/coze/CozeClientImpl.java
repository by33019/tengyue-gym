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
import java.util.HashMap;
import java.util.Map;

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
            Map<String, Object> body = new HashMap<>();
            body.put("project_id", cozeConfig.getProjectId());
            body.put("session_id", sessionId != null ? sessionId : "");
            body.put("prompt", prompt);
            body.put("stream", true);

            restTemplate.execute(
                    cozeConfig.getApiUrl(),
                    HttpMethod.POST,
                    request -> {
                        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
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
                                    if ("[DONE]".equals(json)) {
                                        emitter.complete();
                                        return null;
                                    }
                                    try {
                                        JsonNode node = objectMapper.readTree(json);
                                        if (node.has("content")) {
                                            emitter.send(node.get("content").asText());
                                        } else if (node.has("answer")) {
                                            emitter.send(node.get("answer").asText());
                                        } else if (node.has("delta")) {
                                            emitter.send(node.get("delta").asText());
                                        } else if (node.has("message")) {
                                            emitter.send(node.get("message").asText());
                                        } else {
                                            emitter.send(json);
                                        }
                                    } catch (Exception e) {
                                        emitter.send(json);
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
