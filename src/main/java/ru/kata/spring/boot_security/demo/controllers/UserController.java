package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        try {
            if (userService == null) {
                throw new IllegalArgumentException("UserService cannot be null");
            }
            this.userService = userService;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации UserController", e);
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String userPage(@ModelAttribute("user") User user) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/auth/login";
            }
            user.setName(currentUser.getName());
            user.setId(currentUser.getId());
            user.setAge(currentUser.getAge());
            user.setEmail(currentUser.getEmail());
            user.setRoles(currentUser.getRoles());
            return "user";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/logout")
    @Transactional(readOnly = true)
    public String logoutPage() {
        return "redirect:/auth/login";
    }
}