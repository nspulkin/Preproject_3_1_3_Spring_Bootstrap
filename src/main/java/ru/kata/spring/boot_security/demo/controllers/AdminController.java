package ru.kata.spring.boot_security.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.services.UserService;
import ru.kata.spring.boot_security.demo.services.UserValidationService;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final UserValidationService userValidationService;
    private final RoleRepository roleRepository;

    @Autowired
    public AdminController(UserService userService, UserValidationService userValidationService, RoleRepository roleRepository) {
        try {
            if (userService == null) {
                throw new IllegalArgumentException("UserService cannot be null");
            }
            if (userValidationService == null) {
                throw new IllegalArgumentException("UserValidationService cannot be null");
            }
            if (roleRepository == null) {
                throw new IllegalArgumentException("RoleRepository cannot be null");
            }

            this.userService = userService;
            this.userValidationService = userValidationService;
            this.roleRepository = roleRepository;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации AdminController", e);
        }
    }

    @GetMapping()
    @Transactional(readOnly = true)
    public String usersPage(Model model) {
        try {
            System.out.println("Loading users page...");
            model.addAttribute("listUser", userService.index());

            User currentUser = userService.getCurrentUser();
            System.out.println("Current user in users page: " + (currentUser != null ? currentUser.getEmail() : "null"));

            if (currentUser != null && currentUser.getRoles() != null) {
                model.addAttribute("thisUser", currentUser);
            }
            return "admin/users";
        } catch (Exception e) {
            System.err.println("Error in usersPage method: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Loading new user page...");
            model.addAttribute("user", new User());

            User currentUser = userService.getCurrentUser();
            System.out.println("Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));

            if (currentUser != null) {
                model.addAttribute("thisUser", currentUser);
                if (currentUser.getRoles() != null) {
                    System.out.println("Current user roles: " + currentUser.getRoles().size());
                } else {
                    System.out.println("Current user has no roles");
                }
            } else {
                System.out.println("No current user found, but continuing...");
            }

            return "admin/new";
        } catch (Exception e) {
            System.err.println("Error in newUser method: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/auth/login";
        }
    }

    @PostMapping()
    @Transactional
    public String create(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, @RequestParam(value = "role", required = false) String[] roles, Model model) {
        System.out.println("=== Starting user creation ===");
        System.out.println("User object: " + (user != null ? user.getEmail() : "null"));
        System.out.println("Roles array: " + (roles != null ? String.join(", ", roles) : "null"));

        if (user == null) {
            System.out.println("User is null, returning to form");
            return "admin/new";
        }

        System.out.println("User details - FirstName: " + user.getFirstName() + ", LastName: " + user.getLastName() + ", Email: " + user.getEmail() + ", Age: " + user.getAge());

        // Сначала обрабатываем роли
        Set<Role> userRoles = new HashSet<>();
        if (roles != null && roles.length > 0) {
            System.out.println("Creating user with roles: " + String.join(", ", roles));
            for (String roleName : roles) {
                if (roleName != null && !roleName.trim().isEmpty()) {
                    Role role = roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
                    userRoles.add(role);
                }
            }
        }

        // Если роли не выбраны или пустые, устанавливаем роль USER по умолчанию
        if (userRoles.isEmpty()) {
            System.out.println("Creating user with default role: USER");
            Role defaultRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
            userRoles.add(defaultRole);
        }

        // Дополнительная проверка - если все еще нет ролей, добавляем USER
        if (userRoles.isEmpty()) {
            System.out.println("Still no roles, adding USER role");
            Role userRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
            userRoles.add(userRole);
        }

        // Устанавливаем роли пользователю
        user.setRoles(userRoles);

        // Теперь валидируем пользователя с установленными ролями
        if (!userValidationService.validateUser(user, bindingResult)) {
            System.out.println("User validation failed");
            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(error -> System.err.println("Validation error: " + error.getDefaultMessage()));
            }
            // Добавляем текущего пользователя в модель при ошибке валидации
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("thisUser", currentUser);
            }
            return "admin/new";
        }

        System.out.println("User validation passed");

        try {
            System.out.println("Creating user: " + user.getFirstName() + " " + user.getLastName() + " with email: " + user.getEmail());
            userService.createUser(user);
            System.out.println("User created successfully");
            return "redirect:/admin";
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            bindingResult.rejectValue("name", "", "Ошибка при создании пользователя: " + e.getMessage());
            // Добавляем текущего пользователя в модель при ошибке
            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("thisUser", currentUser);
            }
            return "admin/new";
        }
    }

    @PutMapping("/edit")
    @Transactional
    public String update(@RequestParam(value = "id") int id, String firstName, String lastName, String password, String email, int age, @RequestParam(value = "role", required = false) String[] roles) {
        if (email == null || email.trim().isEmpty()) {
            return "redirect:/admin";
        }
        try {
            // Обрабатываем роли
            Set<Role> userRoles = new HashSet<>();
            if (roles != null && roles.length > 0) {
                for (String roleName : roles) {
                    if (roleName != null && !roleName.trim().isEmpty()) {
                        Role role = roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
                        userRoles.add(role);
                    }
                }
            }

            // Если роли не выбраны или пустые, устанавливаем роль USER по умолчанию
            if (userRoles.isEmpty()) {
                Role defaultRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
                userRoles.add(defaultRole);
            }

            userService.updateUser(id, firstName, lastName, password, email, age, userRoles);
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