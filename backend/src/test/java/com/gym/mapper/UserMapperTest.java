package com.gym.mapper;

import com.gym.GymApplication;
import com.gym.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = GymApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldInsertAndFindUserById() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encoded_password");
        user.setRole(0);
        user.setStatus(1);

        userMapper.insert(user);
        assertNotNull(user.getId());

        User found = userMapper.selectById(user.getId());
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals(0, found.getRole());
    }

    @Test
    void shouldFindUserByUsername() {
        User user = new User();
        user.setUsername("findme");
        user.setPassword("encoded_password");
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);

        User found = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "findme"));

        assertNotNull(found);
        assertEquals("findme", found.getUsername());
    }

    @Test
    void shouldReturnNullForNonExistentUsername() {
        User found = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, "noone"));

        assertNull(found);
    }

    @Test
    void shouldSoftDeleteUser() {
        User user = new User();
        user.setUsername("todelete");
        user.setPassword("encoded");
        user.setRole(0);
        user.setStatus(1);
        userMapper.insert(user);

        userMapper.deleteById(user.getId());

        User found = userMapper.selectById(user.getId());
        assertNull(found);
    }
}
