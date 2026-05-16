package com.gym.service;

import com.gym.dto.CommentDTO;
import java.util.List;
import java.util.Map;

public interface CommentService {
    Map<String, Object> create(Long userId, CommentDTO dto);
    List<Map<String, Object>> listByPost(Long postId);
    void delete(Long userId, Long commentId);
}
