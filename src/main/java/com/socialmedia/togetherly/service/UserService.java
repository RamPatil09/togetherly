package com.socialmedia.togetherly.service;

import com.socialmedia.togetherly.dto.response.UserProfileDTO;

public interface UserService {

    UserProfileDTO getCurrentUser(String username);

    void deactivateAccount(String username);

    void activateAccount(String username);

    void makeAccountPrivate(String username);

    void makeAccountPublic(String username);

    UserProfileDTO findUserByUsername(String username);
}
