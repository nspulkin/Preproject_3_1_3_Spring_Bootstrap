package ru.kata.spring.boot_security.demo.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.services.UserService;

import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public DataLoader(@Lazy UserService userService, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        try {
            if (userService == null) {
                throw new IllegalArgumentException("UserService cannot be null");
            }
            if (passwordEncoder == null) {
                throw new IllegalArgumentException("PasswordEncoder cannot be null");
            }
            if (roleRepository == null) {
                throw new IllegalArgumentException("RoleRepository cannot be null");
            }

            this.userService = userService;
            this.passwordEncoder = passwordEncoder;
            this.roleRepository = roleRepository;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации DataLoader", e);
        }
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            // Проверяем, существует ли роль "ROLE_USER"
            Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
                Role role = new Role("ROLE_USER");
                return roleRepository.save(role);
            });

            // Проверяем, существует ли роль "ROLE_ADMIN"
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseGet(() -> {
                Role role = new Role("ROLE_ADMIN");
                return roleRepository.save(role);
            });

            // Создаем администратора с ролью "ROLE_ADMIN" только если он не существует
            if (userService.findByName("admin").isEmpty()) {
                User admin = new User("admin", passwordEncoder.encode("admin"), 30, "admin@example.com", Set.of(adminRole, userRole));
                userService.save(admin);
            }

            // Создаем обычного пользователя только если он не существует
            if (userService.findByName("user").isEmpty()) {
                User user = new User("user", passwordEncoder.encode("user"), 25, "user@example.com", Set.of(userRole));
                userService.save(user);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации данных: " + e.getMessage());
        }
    }
}



