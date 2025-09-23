package com.back.postpilot.service;

import com.back.postpilot.entity.Role;
import com.back.postpilot.entity.UserEntity;
import com.back.postpilot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only create users if database is empty
        if (userRepository.count() == 0) {
            createUser("admin", "password123", "admin@example.com", "Admin User", Role.ADMIN);
            createUser("superadmin", "superpass123", "superadmin@example.com", "Super Admin", Role.SUPER_ADMIN);
            createUser("user", "userpass123", "user@example.com", "Regular User", Role.USER);

            System.out.println("✅ Test users created successfully!");
        }
    }

    private void createUser(String username, String password, String email, String name, Role role) {
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // ✅ Encode password
        user.setEmail(email);
        user.setName(name);
        user.setRole(role);

        userRepository.save(user);
        System.out.println("Created user: " + username + " with role: " + role);
    }
}