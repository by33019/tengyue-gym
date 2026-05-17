package com.gym.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class SensitiveWordService {

    private final Set<String> words = new HashSet<>();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("sensitive_words.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    words.add(line);
                    count++;
                }
                log.info("敏感词库加载完成，共 {} 个词", count);
            }
        } catch (Exception e) {
            log.error("敏感词库加载失败", e);
        }
    }

    /**
     * 检查文本是否包含敏感词
     * @return 第一个命中的敏感词，如果不包含则返回 null
     */
    public String check(String text) {
        if (text == null || text.isEmpty()) return null;

        // 转小写用于英文不区分大小写匹配
        String lower = text.toLowerCase();

        for (String word : words) {
            if (lower.contains(word.toLowerCase())) {
                return word;
            }
        }
        return null;
    }

    /**
     * 检查文本是否包含敏感词，返回所有命中的词
     */
    public List<String> checkAll(String text) {
        List<String> hits = new ArrayList<>();
        if (text == null || text.isEmpty()) return hits;

        String lower = text.toLowerCase();

        for (String word : words) {
            if (lower.contains(word.toLowerCase())) {
                hits.add(word);
            }
        }
        return hits;
    }

    /** 重新加载词库（管理端热更新时使用） */
    public void reload() {
        words.clear();
        init();
    }
}
