package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.util.UserValidate;

@Service
public class UserValidationService {
    private final UserValidate userValidate;

    @Autowired
    public UserValidationService(UserValidate userValidate) {
        this.userValidate = userValidate;
    }

    @Transactional(readOnly = true)
    public boolean validateUser(User user, BindingResult bindingResult) {
        if (user == null || bindingResult == null) {
            return false;
        }

        userValidate.validate(user, bindingResult);
        return !bindingResult.hasErrors();
    }
}