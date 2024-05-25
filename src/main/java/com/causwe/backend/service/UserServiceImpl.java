package com.causwe.backend.service;

import com.causwe.backend.model.User;
import com.causwe.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return null;
        }
        return userRepository.save(user);
    }

    @Override
    public Map<String, Object> login(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", existingUser);
            responseBody.put("memberId", existingUser.getId());
            return responseBody;
        }
        return null;
    }

    @Override
    public List<User> getAllDevs() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.DEV)
                .collect(Collectors.toList());
    }
}
