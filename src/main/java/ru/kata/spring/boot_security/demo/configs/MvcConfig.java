package ru.kata.spring.boot_security.demo.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Transactional(readOnly = true)
    public void addViewControllers(ViewControllerRegistry registry) {
        try {
            registry.addViewController("/auth/user").setViewName("user");
        } catch (Exception e) {
            System.err.println("Ошибка при настройке контроллеров представлений: " + e.getMessage());
        }
    }
}
