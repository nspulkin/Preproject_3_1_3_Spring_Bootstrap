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
            // Проверяем, существует ли роль "USER"
            Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
                Role role = new Role("USER");
                return roleRepository.save(role);
            });

            // Проверяем, существует ли роль "ADMIN"
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role role = new Role("ADMIN");
                return roleRepository.save(role);
            });

            // Создаем администратора с ролью "ADMIN" только если он не существует
            if (userService.findByEmail("admin@mail.ru").isEmpty()) {
                User admin = new User("admin", "admin", passwordEncoder.encode("admin"), 30, "admin@mail.ru", Set.of(adminRole, userRole));
                userService.save(admin);
                System.out.println("admin admin created with email: admin@mail.ru");
            }

            // Создаем обычного пользователя только если он не существует
            if (userService.findByEmail("user@mail.ru").isEmpty()) {
                User user = new User("user", "user", passwordEncoder.encode("user"), 25, "user@mail.ru", Set.of(userRole));
                userService.save(user);
                System.out.println("user user created with email: user@mail.ru");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации данных: " + e.getMessage());
        }
    }
}



