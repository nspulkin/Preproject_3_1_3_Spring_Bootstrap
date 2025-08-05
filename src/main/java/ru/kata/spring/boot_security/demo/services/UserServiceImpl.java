package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;
import ru.kata.spring.boot_security.demo.repositories.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RegistrationService registrationService;
    private final RoleRepository roleRepository;

    @Autowired
    public UserServiceImpl(@Lazy UserRepository userRepository, @Lazy RegistrationService registrationService, @Lazy RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.registrationService = registrationService;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> index() {
        List<User> users = userRepository.findAll();
        // Инициализируем роли для всех пользователей
        for (User user : users) {
            user.getRoles().size(); // Это загрузит роли
        }
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public User show(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User userEntity = user.get();
            userEntity.getRoles().size(); // Это загрузит роли
            return userEntity;
        }
        return null;
    }

    @Override
    @Transactional
    public void save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void update(int id, User updatedUser) {
        if (updatedUser == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user or ID");
        }
        updatedUser.setId(id);
        userRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        userRepository.deleteById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Теперь ищем только по email, так как убрали поле name
        Optional<User> user = userRepository.findByEmail(username);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // Инициализируем роли для избежания проблем с ленивой загрузкой
        User userEntity = user.get();
        userEntity.getRoles().size(); // Это загрузит роли
        return userEntity;
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("Getting current user for username: " + currentUsername);

            Optional<User> user = findByEmail(currentUsername);
            if (user.isPresent()) {
                User userEntity = user.get();
                userEntity.getRoles().size(); // Это загрузит роли
                System.out.println("Found current user: " + userEntity.getEmail());
                return userEntity;
            }

            System.out.println("No current user found for: " + currentUsername);
            return null;
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public User createUser(User user) {
        System.out.println("=== UserServiceImpl.createUser called ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));

        if (user == null) {
            System.err.println("User is null");
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            Optional<User> existingUser = findByEmail(user.getEmail());
            if (existingUser.isPresent()) {
                System.err.println("User with email '" + user.getEmail() + "' already exists");
                throw new IllegalArgumentException("User already exists");
            }

            System.out.println("Calling registrationService.register");
            registrationService.register(user);
            System.out.println("User created successfully");
            return user;
        } catch (Exception e) {
            System.err.println("Exception in createUser: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public User updateUser(int id, String firstName, String lastName, String password, String email, int age, Set<Role> roles) {
        if (id <= 0 || email == null) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        User existingUser = show(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setAge(age);
        user.setPassword(password != null && !password.trim().isEmpty() ? registrationService.encodePassword(password) : existingUser.getPassword());

        // Устанавливаем роли
        user.setRoles(roles != null ? roles : new HashSet<>());

        update(id, user);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        User existingUser = show(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("User not found");
        }
        delete(id);
    }
}
