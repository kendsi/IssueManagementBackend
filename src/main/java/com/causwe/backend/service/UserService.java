// UserService.java
package com.causwe.backend.service;

import com.causwe.backend.model.User;
import java.util.List;

public interface UserService {
    User getUserById(Long userId);
    User getUserByUsername(String username);
    User createUser(String username, String password, User.Role role);
    User login(String username, String password);
    List<User> getAllDevs();
    //List<User> getAllUsers();
    //void deleteUser(Long userId);
}