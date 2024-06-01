package com.causwe.backend.service;

import com.causwe.backend.model.Developer;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User dev;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        dev = new Developer();
        dev.setUsername("dev");
        dev.setPassword("dev");
        dev.setId(1L);
    }

    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(dev));

        User foundUser = userService.getUserById(1L);

        assertNotNull(foundUser);
        assertEquals("dev", foundUser.getUsername());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        User foundUser = userService.getUserById(2L);
        assertNull(foundUser);
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("dev")).thenReturn(dev);
        
        User foundUser = userService.getUserByUsername("dev");
        assertNotNull(foundUser);
        assertEquals("dev", foundUser.getUsername());
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("dev")).thenReturn(null);
        
        User foundUser = userService.getUserByUsername("dev");
        assertNull(foundUser);
    }

    @Test
    public void testCreateUser_Success() {
        when(userRepository.findByUsername("dev")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(dev);
        when(passwordEncoder.encode(dev.getPassword())).thenReturn("encodedPassword");

        User createdUser = userService.createUser(dev.getUsername(), dev.getPassword(), dev.getRole());
        assertNotNull(createdUser);
        assertEquals("dev", createdUser.getUsername());
    }

    @Test
    public void testCreateUser_UserAlreadyExists() {
        when(userRepository.findByUsername("dev")).thenReturn(dev);

        User createdUser = userService.createUser(dev.getUsername(), dev.getPassword(), dev.getRole());
        assertNull(createdUser);
    }

    @Test
    public void testLogin_Success() {
        when(userRepository.findByUsername("dev")).thenReturn(dev);
        when(passwordEncoder.matches("dev", dev.getPassword())).thenReturn(true);
        
        User loginResponse = userService.login(dev.getUsername(), dev.getPassword());
        assertNotNull(loginResponse);
        assertEquals(dev.getId(), loginResponse.getId());
    }

    @Test
    public void testLogin_Failure() {
        when(userRepository.findByUsername("dev")).thenReturn(null);

        User loginResponse = userService.login(dev.getUsername(), dev.getPassword());
        assertNull(loginResponse);
    }

    @Test
    public void testGetAllDevs() {
        User dev2 = new Developer();
        dev2.setUsername("dev2");
        dev2.setPassword("dev2");
        dev2.setId(2L);

        List<User> users = new ArrayList<>();
        users.add(dev);
        users.add(dev2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> devs = userService.getAllDevs();
        assertNotNull(devs);
        assertEquals(2, devs.size());
    }
}