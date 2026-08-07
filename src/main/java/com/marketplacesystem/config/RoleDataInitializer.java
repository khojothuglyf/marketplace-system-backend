package com.marketplacesystem.config;

import com.marketplacesystem.entity.Role;
import com.marketplacesystem.entity.RoleName;
import com.marketplacesystem.entity.User;
import com.marketplacesystem.repository.RoleRepository;
import com.marketplacesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RoleDataInitializer implements CommandLineRunner {

    private final String adminEmail;
    private final String adminPassword;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RoleDataInitializer(@Value("${app.bootstrap.admin-email}") String adminEmail,
                               @Value("${app.bootstrap.admin-password}") String adminPassword,
                               RoleRepository roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedRoleIfAbsent(RoleName.ADMIN);
        seedRoleIfAbsent(RoleName.SELLER);
        seedRoleIfAbsent(RoleName.CUSTOMER);
        seedAdminIfAbsent();
    }

    private void seedRoleIfAbsent(RoleName name) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(new Role(name));
        }
    }

    private void seedAdminIfAbsent() {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role must exist before seeding the admin user"));
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        if (admin == null) {
            admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail(adminEmail);
            admin.setRole(adminRole);
            admin.setPassword(passwordEncoder.encode(adminPassword));
        } else if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
        }
        userRepository.save(admin);
    }
}
