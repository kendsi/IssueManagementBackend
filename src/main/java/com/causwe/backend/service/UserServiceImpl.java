package com.causwe.backend.service;

import com.causwe.backend.model.*;
import com.causwe.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUserById(Long userId) {
        // if (userId == null) {
        //     return null;
        // }
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User createUser(String username, String password, User.Role role) {
        if (userRepository.findByUsername(username) != null) {
            return null;
        }
        User newUser = switch (role) {
            case ADMIN -> new Admin();
            case PL -> new ProjectLead();
            case DEV -> new Developer();
            case TESTER -> new Tester();
        };
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        return userRepository.save(newUser);
    }

    @Override
    public User login(String username, String password) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null && passwordEncoder.matches(password, existingUser.getPassword())) {
            return existingUser;
        }
        return null;
    }

    @Override
    public List<User> getAllDevs() {
        return userRepository.findAll().stream()
                .filter(User::isDeveloper)
                .collect(Collectors.toList());
    }
    /*
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }
    */
}
