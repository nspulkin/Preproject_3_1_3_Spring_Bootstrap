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
        try {
            if (target == null || !(target instanceof User) || errors == null) {
                return;
            }

            User user = (User) target;

            if (user.getName() == null || user.getName().trim().isEmpty()) {
                errors.rejectValue("name", "", "Имя не может быть пустым");
                return;
            }

            if (userRepository == null) {
                return;
            }
            Optional<User> foundUser = userRepository.findByName(user.getName());
            if (foundUser.isPresent()) {
                errors.rejectValue("name", "", "Это имя уже занято");
            }
        } catch (Exception e) {
            if (errors != null) {
                errors.rejectValue("name", "", "Ошибка при проверке имени");
            }
        }
    }
}

