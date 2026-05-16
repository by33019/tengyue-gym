package com.gym.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.dto.LoginDTO;
import com.gym.dto.RegisterDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("ctrl_register");
        dto.setPassword("pass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldRejectDuplicateRegistration() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("ctrl_dup");
        dto.setPassword("pass123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("ctrl_login");
        dto.setPassword("pass123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        LoginDTO login = new LoginDTO();
        login.setUsername("ctrl_login");
        login.setPassword("pass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void shouldRejectWrongPassword() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("ctrl_pwd");
        dto.setPassword("right");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        LoginDTO login = new LoginDTO();
        login.setUsername("ctrl_pwd");
        login.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void shouldRefreshToken() throws Exception {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("ctrl_refresh");
        dto.setPassword("pass");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        LoginDTO login = new LoginDTO();
        login.setUsername("ctrl_refresh");
        login.setPassword("pass");
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(response)
                .get("data").get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists());
    }
}
