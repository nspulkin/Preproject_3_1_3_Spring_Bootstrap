package ru.kata.spring.boot_security.demo.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.kata.spring.boot_security.demo.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    List<User> index();

    User show(int id);

    void save(User user);

    void update(int id, User updatedUser);

    void delete(int id);


    Optional<User> findByEmail(String email);

    // Новые методы для переноса логики из контроллеров
    User getCurrentUser();

    User createUser(User user);

    User updateUser(int id, String firstName, String lastName, String password, String email, int age, java.util.Set<ru.kata.spring.boot_security.demo.models.Role> roles);

    void deleteUser(int id);

    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
