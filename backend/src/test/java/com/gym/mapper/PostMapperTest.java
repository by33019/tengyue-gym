package com.gym.mapper;

import com.gym.entity.Post;
import com.gym.entity.Comment;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PostMapperTest {

    @Autowired private PostMapper postMapper;
    @Autowired private CommentMapper commentMapper;

    @Test
    void shouldInsertAndQueryPost() {
        Post p = new Post(); p.setUserId(1L); p.setContent("测试动态");
        p.setLikeCount(0); p.setCommentCount(0); p.setStatus(1);
        postMapper.insert(p);
        assertNotNull(p.getId());

        Post found = postMapper.selectById(p.getId());
        assertEquals("测试动态", found.getContent());
    }

    @Test
    void shouldSoftDeletePost() {
        Post p = new Post(); p.setUserId(1L); p.setContent("待删除");
        p.setLikeCount(0); p.setCommentCount(0); p.setStatus(1);
        postMapper.insert(p);

        postMapper.deleteById(p.getId());
        assertNull(postMapper.selectById(p.getId()));
    }

    @Test
    void shouldInsertCommentAndReply() {
        Comment c1 = new Comment(); c1.setPostId(1L); c1.setUserId(1L);
        c1.setContent("一级评论");
        commentMapper.insert(c1);

        Comment c2 = new Comment(); c2.setPostId(1L); c2.setUserId(2L);
        c2.setParentId(c1.getId()); c2.setContent("回复");
        commentMapper.insert(c2);

        List<Comment> list = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, 1L));
        assertEquals(2, list.size());
        assertNotNull(list.get(1).getParentId());
    }
}
