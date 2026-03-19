package com.krish.service.Impl;

import com.krish.domain.AuthProvider;
import com.krish.domain.UserRole;
import com.krish.modal.User;
import com.krish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class DataInitializationComponent implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${admin.email}")
    private String adminEmail;
    
    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        initializeAdminUser();
    }

    private void initializeAdminUser(){
        if(userRepository.findByEmail(adminEmail)==null){
            User user = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName("Admin User")
                    .role(UserRole.ROLE_ADMIN)
                    .authProvider(AuthProvider.LOCAL)
                    .build();

            userRepository.save(user);
        }
    }
}
