package com.gym.coze;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "coze")
public class CozeConfig {
    private String apiUrl;
    private String apiToken;
    private String projectId;
}
