package com.socialmedia.togetherly.service.impl;

import com.socialmedia.togetherly.dto.response.UserProfileDTO;
import com.socialmedia.togetherly.exception.BadRequestException;
import com.socialmedia.togetherly.exception.UserNotFoundException;
import com.socialmedia.togetherly.model.User;
import com.socialmedia.togetherly.repositories.UserRepository;
import com.socialmedia.togetherly.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserProfileDTO getCurrentUser(String username) {

        User userFromDB = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserProfileDTO.builder()
                .username(userFromDB.getUsername())
                .fullName(userFromDB.getFullName())
                .bio(userFromDB.getBio())
                .profilePicture(userFromDB.getProfilePictureUrl())
                .build();
    }

    @Override
    public void deactivateAccount(String username) {
        User userFromDB = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        userFromDB.setActive(false);
        userRepository.save(userFromDB);
    }

    @Override
    public void activateAccount(String username) {
        User userFromDB = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        userFromDB.setActive(true);

        userRepository.save(userFromDB);
    }

    @Override
    public void makeAccountPrivate(String username) {
        User userFromDB = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        userFromDB.setPrivate(true);

        userRepository.save(userFromDB);
    }

    @Override
    public void makeAccountPublic(String username) {
        User userFromDB = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        userFromDB.setPrivate(false);

        userRepository.save(userFromDB);
    }

    @Override
    public UserProfileDTO findUserByUsername(String username) {
        if (username == null) {
            throw new BadRequestException("Invalid request");
        }

        User userFromDb = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!userFromDb.isPrivate()) {
            return UserProfileDTO.builder()
                    .username(userFromDb.getUsername())
                    .fullName(userFromDb.getFullName())
                    .bio(userFromDb.getBio())
                    .profilePicture(userFromDb.getProfilePictureUrl())
                    .build();
        }

        return null;

    }

}
