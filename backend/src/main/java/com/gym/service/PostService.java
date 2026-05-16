package com.gym.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.PostDTO;
import java.util.Map;

public interface PostService {
    Map<String, Object> create(Long userId, PostDTO dto);
    Page<Map<String, Object>> list(int page, int size);
    void delete(Long userId, Long postId);
    void toggleLike(Long userId, Long postId);
}
