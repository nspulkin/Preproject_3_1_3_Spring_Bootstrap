package ru.kata.spring.boot_security.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import java.util.Optional;

@Component
public class UserValidate implements Validator {
    private UserRepository userRepository;

    @Autowired
    public UserValidate(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        User user = (User) target;

        Optional<User> foundUser = userRepository.findByName(user.getUsername());
        if (foundUser.isEmpty())
            return;

        errors.rejectValue("name", "", "Это имя уже занято");
    }
}
