package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserService;
import ru.kata.spring.boot_security.demo.services.UserValidationService;

import javax.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserValidationService userValidationService;
    private final UserService userService;

    @Autowired
    public AuthController(UserValidationService userValidationService, UserService userService) {
        try {
            if (userValidationService == null) {
                throw new IllegalArgumentException("UserValidationService cannot be null");
            }
            if (userService == null) {
                throw new IllegalArgumentException("UserService cannot be null");
            }

            this.userValidationService = userValidationService;
            this.userService = userService;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации AuthController", e);
        }
    }

    @GetMapping("/login")
    @Transactional(readOnly = true)
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/registration")
    @Transactional(readOnly = true)
    public String registrationPage(@ModelAttribute("user") User user) {
        return "auth/registration";
    }

    @PostMapping("/registration")
    @Transactional
    public String performRegistration(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        if (user == null) {
            return "/auth/registration";
        }

        if (!userValidationService.validateUser(user, bindingResult)) {
            return "/auth/registration";
        }

        try {
            userService.createUser(user);
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "", e.getMessage());
            return "/auth/registration";
        }
    }
}
