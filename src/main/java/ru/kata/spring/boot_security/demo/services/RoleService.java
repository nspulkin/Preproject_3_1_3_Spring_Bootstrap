package ru.kata.spring.boot_security.demo.services;

import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.models.User;

import java.util.List;

public interface RoleService {
    void save(Role role);
    Role getRoleById(int id);
    void deleteRoleById(int id);
    void deleteRole(Role role);
}