package com.socialmedia.togetherly.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class AdminController {

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public String adminHome() {
        return "Welcome to ADMIN panel";
    }
}


