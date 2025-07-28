package com.socialmedia.togetherly.controller;

import com.socialmedia.togetherly.dto.response.UserProfileDTO;
import com.socialmedia.togetherly.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDTO> getCurrentUser() {
        String currentUsername = getCurrentUserName();
        UserProfileDTO currentUser = userService.getCurrentUser(currentUsername);
        return new ResponseEntity<>(currentUser, HttpStatus.OK);
    }

    @PostMapping("/activate-account")
    public ResponseEntity<String> activateAccount() {
        String currentUsername = getCurrentUserName();
        userService.activateAccount(currentUsername);
        return new ResponseEntity<>("Your account is active now", HttpStatus.OK);
    }

    @PostMapping("/deactivate-account")
    public ResponseEntity<String> deactivateAccount() {
        String currentUsername = getCurrentUserName();
        userService.deactivateAccount(currentUsername);
        return new ResponseEntity<>("Your account is deactivated now", HttpStatus.OK);
    }

    @PostMapping("/private-account")
    public ResponseEntity<String> makeAccountPrivate() {
        String currentUsername = getCurrentUserName();
        userService.makeAccountPrivate(currentUsername);
        return new ResponseEntity<>("Your account is private now", HttpStatus.OK);
    }

    @PostMapping("/public-account")
    public ResponseEntity<String> makeAccountPublic() {
        String currentUsername = getCurrentUserName();
        userService.makeAccountPublic(currentUsername);
        return new ResponseEntity<>("Your account is public now", HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfileByUsername(@RequestParam String username) {
        UserProfileDTO userByUsername = userService.findUserByUsername(username);
        return new ResponseEntity<>(userByUsername, HttpStatus.OK);
    }


    private String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }

        return null;
    }
}
