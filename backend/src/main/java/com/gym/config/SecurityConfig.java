package com.gym.config;

import com.gym.security.JwtAuthFilter;
import com.gym.security.JwtUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilter(JwtUtils jwtUtils) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new JwtAuthFilter(jwtUtils));
        bean.addUrlPatterns("/*");
        bean.setOrder(1);
        return bean;
    }
}
