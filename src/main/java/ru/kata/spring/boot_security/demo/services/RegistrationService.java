package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import java.util.Collections;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegistrationService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(User user) {
        System.out.println("=== RegistrationService.register called ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));

        if (user == null) {
            System.err.println("User is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            System.err.println("Password is null or empty");
            throw new IllegalArgumentException("User password cannot be null or empty");
        }

        try {
            System.out.println("Encoding password");
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Если роли не установлены, устанавливаем роль USER по умолчанию
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                System.out.println("No roles set, setting default USER role");
                Role role = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
                user.setRoles(Collections.singleton(role));
            } else {
                System.out.println("User has " + user.getRoles().size() + " roles already set, not overwriting");
            }

            System.out.println("Saving user to repository");
            userRepository.save(user);
            System.out.println("User saved successfully");
        } catch (Exception e) {
            System.err.println("Exception in register: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public String encodePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return passwordEncoder.encode(password);
    }
}

