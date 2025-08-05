package ru.kata.spring.boot_security.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import java.util.Optional;

@Component
public class UserValidate implements Validator {
    private final UserRepository userRepository;

    @Autowired
    public UserValidate(UserRepository userRepository) {
        try {
            if (userRepository == null) {
                throw new IllegalArgumentException("UserRepository cannot be null");
            }
            this.userRepository = userRepository;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации UserValidate", e);
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {
        try {
            return User.class.equals(clazz);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validate(Object target, Errors errors) {
        System.out.println("=== UserValidate.validate called ===");
        System.out.println("Target: " + (target != null ? target.getClass().getSimpleName() : "null"));
        System.out.println("Errors: " + (errors != null ? "not null" : "null"));

        try {
            if (target == null || !(target instanceof User) || errors == null) {
                System.out.println("Invalid parameters for validation");
                return;
            }

            User user = (User) target;
            System.out.println("Validating user: " + user.getEmail());

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                System.out.println("Email is empty or null");
                errors.rejectValue("email", "", "Email не может быть пустым");
                return;
            }

            if (userRepository == null) {
                System.out.println("UserRepository is null");
                return;
            }

            System.out.println("Checking if user with email '" + user.getEmail() + "' already exists");
            Optional<User> foundUser = userRepository.findByEmail(user.getEmail());
            if (foundUser.isPresent()) {
                System.out.println("User with this email already exists");
                errors.rejectValue("email", "", "Этот email уже занят");
            } else {
                System.out.println("Email is available");
            }

            // Проверяем роли (только если они уже установлены)
            if (user.getRoles() != null && user.getRoles().isEmpty()) {
                System.out.println("User has no roles");
                errors.rejectValue("roles", "", "Пользователь должен иметь хотя бы одну роль");
            }
        } catch (Exception e) {
            System.err.println("Exception in UserValidate: " + e.getMessage());
            e.printStackTrace();
            if (errors != null) {
                errors.rejectValue("name", "", "Ошибка при проверке имени");
            }
        }
    }
}

