package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gym.dto.PostDTO;
import com.gym.entity.Post;
import com.gym.mapper.PostMapper;
import com.gym.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    public Map<String, Object> create(Long userId, PostDTO dto) {
        Post p = new Post();
        p.setUserId(userId);
        p.setContent(dto.getContent());
        p.setLikeCount(0);
        p.setCommentCount(0);
        p.setStatus(1);
        postMapper.insert(p);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", p.getId());
        result.put("content", p.getContent());
        result.put("userId", p.getUserId());
        result.put("likeCount", 0);
        result.put("commentCount", 0);
        result.put("createdAt", p.getCreatedAt());
        return result;
    }

    @Override
    public Page<Map<String, Object>> list(int pageNum, int size) {
        Page<Post> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, 1)
                .orderByDesc(Post::getCreatedAt);

        Page<Post> result = postMapper.selectPage(page, wrapper);
        Page<Map<String, Object>> voPage = new Page<>(pageNum, size, result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("userId", p.getUserId());
            m.put("content", p.getContent());
            m.put("likeCount", p.getLikeCount());
            m.put("commentCount", p.getCommentCount());
            m.put("createdAt", p.getCreatedAt());
            return m;
        }).collect(java.util.stream.Collectors.toList()));
        return voPage;
    }

    @Override
    public void delete(Long userId, Long postId) {
        Post p = postMapper.selectById(postId);
        if (p == null) throw new RuntimeException("动态不存在");
        if (!p.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        p.setStatus(0);
        postMapper.updateById(p);
    }

    @Override
    public void toggleLike(Long userId, Long postId) {
        Post p = postMapper.selectById(postId);
        if (p == null) throw new RuntimeException("动态不存在");
        p.setLikeCount(p.getLikeCount() + 1);
        postMapper.updateById(p);
    }
}
