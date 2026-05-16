package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gym.dto.CommentDTO;
import com.gym.entity.Comment;
import com.gym.entity.Post;
import com.gym.entity.User;
import com.gym.mapper.CommentMapper;
import com.gym.mapper.PostMapper;
import com.gym.mapper.UserMapper;
import com.gym.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> create(Long userId, CommentDTO dto) {
        Comment c = new Comment();
        c.setPostId(dto.getPostId());
        c.setUserId(userId);
        c.setParentId(dto.getParentId());
        c.setContent(dto.getContent());
        commentMapper.insert(c);

        Post p = postMapper.selectById(dto.getPostId());
        if (p != null) {
            p.setCommentCount(p.getCommentCount() + 1);
            postMapper.updateById(p);
        }

        User u = userMapper.selectById(userId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("postId", c.getPostId());
        m.put("userId", c.getUserId());
        m.put("username", getDisplayName(u));
        m.put("parentId", c.getParentId());
        m.put("content", c.getContent());
        m.put("createdAt", c.getCreatedAt());
        return m;
    }

    @Override
    public List<Map<String, Object>> listByPost(Long postId) {
        List<Comment> all = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getPostId, postId)
                        .orderByAsc(Comment::getCreatedAt));

        Map<Long, User> userCache = new HashMap<>();
        List<User> users = userMapper.selectList(null);
        for (User u : users) userCache.put(u.getId(), u);

        Map<Long, List<Map<String, Object>>> childrenMap = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        for (Comment c : all) {
            User u = userCache.get(c.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("postId", c.getPostId());
            item.put("userId", c.getUserId());
            item.put("username", getDisplayName(u));
            item.put("parentId", c.getParentId());
            item.put("content", c.getContent());
            item.put("createdAt", c.getCreatedAt());
            item.put("children", new ArrayList<>());

            if (c.getParentId() == null) {
                roots.add(item);
            } else {
                childrenMap.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(item);
            }
        }

        for (Map<String, Object> item : roots) {
            fillChildren(item, childrenMap);
        }

        return roots;
    }

    private void fillChildren(Map<String, Object> item, Map<Long, List<Map<String, Object>>> map) {
        Long id = ((Number) item.get("id")).longValue();
        List<Map<String, Object>> children = map.get(id);
        if (children != null) {
            item.put("children", children);
            for (Map<String, Object> child : children) {
                fillChildren(child, map);
            }
        }
    }

    @Override
    public void delete(Long userId, Long commentId) {
        Comment c = commentMapper.selectById(commentId);
        if (c == null) throw new RuntimeException("评论不存在");
        if (!c.getUserId().equals(userId)) throw new RuntimeException("无权删除");
        commentMapper.deleteById(commentId);
    }

    private String getDisplayName(User u) {
        if (u == null) return null;
        return (u.getIsAnonymous() != null && u.getIsAnonymous() == 1) ? null : u.getUsername();
    }
}
