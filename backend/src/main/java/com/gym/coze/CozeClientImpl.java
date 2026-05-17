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
            Map<String, Object> msg = new LinkedHashMap<>();
            msg.put("role", "user");
            msg.put("content", prompt);
            msg.put("content_type", "text");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("stream", true);
            body.put("auto_save_history", true);
            body.put("additional_messages", List.of(msg));

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
                                    try {
                                        JsonNode node = objectMapper.readTree(json);
                                        // Coze response: content.answer contains the text
                                        JsonNode content = node.get("content");
                                        if (content != null && content.has("answer")) {
                                            JsonNode answer = content.get("answer");
                                            if (answer != null && !answer.isNull()) {
                                                emitter.send(answer.asText());
                                            }
                                        }
                                        // Check finish flag
                                        if (node.has("finish") && node.get("finish").asBoolean()
                                                && "answer".equals(node.path("type").asText())) {
                                            emitter.complete();
                                            return null;
                                        }
                                    } catch (Exception e) {
                                        // skip malformed JSON lines
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
