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
        System.out.println("=== Starting user validation ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        System.out.println("BindingResult: " + (bindingResult != null ? "not null" : "null"));

        if (user == null || bindingResult == null) {
            System.out.println("User or BindingResult is null, validation failed");
            return false;
        }

        try {
            userValidate.validate(user, bindingResult);
            boolean isValid = !bindingResult.hasErrors();
            System.out.println("Validation result: " + (isValid ? "PASSED" : "FAILED"));

            if (!isValid) {
                System.out.println("Validation errors:");
                bindingResult.getAllErrors().forEach(error -> System.err.println("  - " + error.getDefaultMessage()));
            }

            return isValid;
        } catch (Exception e) {
            System.err.println("Exception during validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}