package com.socialmedia.togetherly.repositories;

import com.socialmedia.togetherly.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {

    Optional<Role> findByName(String role);
}