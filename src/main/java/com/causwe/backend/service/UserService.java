// UserService.java
package com.causwe.backend.service;

import com.causwe.backend.model.User;
import java.util.List;
import java.util.Map;

public interface UserService {
    User getUserById(Long userId);
    User getUserByUsername(String username);
    User createUser(User user);
    User login(User user);
    List<User> getAllDevs();
    //List<User> getAllUsers();
    //void deleteUser(Long userId);
}