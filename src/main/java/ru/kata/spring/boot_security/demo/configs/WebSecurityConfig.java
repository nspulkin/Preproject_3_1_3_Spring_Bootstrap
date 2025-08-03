package ru.kata.spring.boot_security.demo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.services.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final SuccessUserHandler successUserHandler;
    private final UserService userService;

    @Autowired
    public WebSecurityConfig(SuccessUserHandler successUserHandler, UserService userService) {
        try {
            if (successUserHandler == null) {
                throw new IllegalArgumentException("SuccessUserHandler cannot be null");
            }
            if (userService == null) {
                throw new IllegalArgumentException("UserService cannot be null");
            }

            this.successUserHandler = successUserHandler;
            this.userService = userService;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации WebSecurityConfig", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    protected void configure(HttpSecurity http) throws Exception {
        try {
            http.csrf().disable().authorizeRequests().antMatchers("/auth/registration", "/auth/login", "/error").permitAll().antMatchers("/user/**").hasAnyRole("ADMIN", "USER").antMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated().and().formLogin().loginProcessingUrl("/process_login").successHandler(successUserHandler).loginPage("/auth/login").permitAll().and().logout().logoutUrl("/user/logout").logoutSuccessUrl("/auth/login").permitAll();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при настройке HTTP безопасности", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        try {
            auth.userDetailsService(userService).passwordEncoder(getPasswordEncoder());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при настройке аутентификации", e);
        }
    }

    @Bean
    public PasswordEncoder getPasswordEncoder() {
        try {
            return new BCryptPasswordEncoder();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании PasswordEncoder", e);
        }
    }

    // аутентификация inMemory
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        try {
            return userService;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при создании UserDetailsService", e);
        }
    }
}