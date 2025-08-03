package ru.kata.spring.boot_security.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.models.Role;
import ru.kata.spring.boot_security.demo.repositories.RoleRepository;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        try {
            if (roleRepository == null) {
                throw new IllegalArgumentException("RoleRepository cannot be null");
            }
            this.roleRepository = roleRepository;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при инициализации RoleServiceImpl", e);
        }
    }

    @Override
    @Transactional
    public void save(Role role) {
        try {
            if (role == null) {
                throw new IllegalArgumentException("Role cannot be null");
            }
            roleRepository.save(role);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении роли", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Role getRoleById(int id) {
        try {
            if (id <= 0) {
                return null;
            }
            return roleRepository.findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении роли", e);
        }
    }

    @Override
    @Transactional
    public void deleteRoleById(int id) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid role ID");
            }
            roleRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении роли", e);
        }
    }

    @Override
    @Transactional
    public void deleteRole(Role role) {
        try {
            if (role == null) {
                throw new IllegalArgumentException("Role cannot be null");
            }
            roleRepository.delete(role);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при удалении роли", e);
        }
    }
}
