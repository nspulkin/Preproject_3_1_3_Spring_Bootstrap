package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.services.UserService;
import ru.kata.spring.boot_security.demo.services.UserValidationService;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final UserValidationService userValidationService;

    @Autowired
    public AdminController(UserService userService, UserValidationService userValidationService) {
        try {
            if (userService == null) {
                throw new IllegalArgumentException("UserService cannot be null");
            }
            if (userValidationService == null) {
                throw new IllegalArgumentException("UserValidationService cannot be null");
            }

            this.userService = userService;
            this.userValidationService = userValidationService;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации AdminController", e);
        }
    }

    @GetMapping()
    @Transactional(readOnly = true)
    public String usersPage(Model model) {
        try {
            model.addAttribute("listUser", userService.index());
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("thisUser", currentUser);
            }
            return "admin/users";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/show")
    @Transactional(readOnly = true)
    public String show(@RequestParam(value = "id") int id, Model model) {
        try {
            User user = userService.show(id);
            if (user == null) {
                return "redirect:/admin";
            }
            model.addAttribute("user", user);
            return "admin/show";
        } catch (Exception e) {
            return "redirect:/admin";
        }
    }

    @GetMapping("/new")
    @Transactional(readOnly = true)
    public String newUser(Model model) {
        try {
            model.addAttribute("user", new User());
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("thisUser", currentUser);
            }
            return "admin/new";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @PostMapping()
    @Transactional
    public String create(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        if (user == null) {
            return "admin/new";
        }

        if (!userValidationService.validateUser(user, bindingResult)) {
            return "admin/new";
        }

        try {
            userService.createUser(user);
            return "redirect:admin";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("name", "", e.getMessage());
            return "admin/new";
        }
    }

    @PutMapping("/edit")
    @Transactional
    public String update(@RequestParam(value = "id") int id, String name, String password, String email, int age, String role) {
        if (name == null || name.trim().isEmpty() || email == null || email.trim().isEmpty() || role == null || role.trim().isEmpty()) {
            return "redirect:/admin";
        }
        try {
            userService.updateUser(id, name, password, email, age, role);
            return "redirect:/admin";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin";
        }
    }

    @DeleteMapping("/delete")
    @Transactional
    public String delete(@RequestParam(value = "id") int id) {
        if (id <= 0) {
            return "redirect:/admin";
        }
        try {
            userService.deleteUser(id);
            return "redirect:/admin";
        } catch (IllegalArgumentException e) {
            return "redirect:/admin";
        }
    }
}