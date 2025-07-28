package com.socialmedia.togetherly;

import com.socialmedia.togetherly.model.ERole;
import com.socialmedia.togetherly.model.Role;
import com.socialmedia.togetherly.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.UUID;

@SpringBootApplication
public class TogetherlyApplication {

    public static void main(String[] args) {
        SpringApplication.run(TogetherlyApplication.class, args);
    }

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            Arrays.stream(ERole.values()).forEach(roleEnum -> {
                roleRepository.findByName(roleEnum.name()).orElseGet(() -> {
                    Role role = new Role();
                    role.setId(UUID.randomUUID().toString());
                    role.setName(roleEnum.name());
                    return roleRepository.save(role);
                });
            });
        };
    }

}
