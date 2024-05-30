package com.causwe.backend.service;

import com.causwe.backend.model.User;
import com.causwe.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("testUser", "password", User.Role.DEV);
        user.setId(1L);
    }

    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(1L);

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        User foundUser = userService.getUserById(2L);
        assertNull(foundUser);
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        
        User foundUser = userService.getUserByUsername("testUser");
        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("testUser")).thenReturn(null);
        
        User foundUser = userService.getUserByUsername("testUser");
        assertNull(foundUser);
    }

    @Test
    public void testCreateUser_Success() {
        when(userRepository.findByUsername("testUser")).thenReturn(null);
        when(userRepository.save(user)).thenReturn(user);

        User createdUser = userService.createUser(user);
        assertNotNull(createdUser);
        assertEquals("testUser", createdUser.getUsername());
    }

    @Test
    public void testCreateUser_UserAlreadyExists() {
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(null);

        User createdUser = userService.createUser(user);
        assertNull(createdUser);
    }

    @Test
    public void testLogin_Success() {
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        
        Map<String, Object> loginResponse = userService.login(user);
        assertNotNull(loginResponse);
        assertEquals(user.getId(), loginResponse.get("memberId"));
    }

    @Test
    public void testLogin_Failure() {
        when(userRepository.findByUsername("testUser")).thenReturn(null);

        Map<String, Object> loginResponse = userService.login(user);
        assertNull(loginResponse);
    }

    @Test
    public void testGetAllDevs() {
        User user2 = new User("devUser", "password", User.Role.DEV);
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> devs = userService.getAllDevs();
        assertNotNull(devs);
        assertEquals(2, devs.size());
    }
}