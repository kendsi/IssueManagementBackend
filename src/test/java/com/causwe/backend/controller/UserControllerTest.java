package com.causwe.backend.controller;

import com.causwe.backend.dto.UserDTO;
import com.causwe.backend.exceptions.GlobalExceptionHandler;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.User;
import com.causwe.backend.service.UserService;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserController userController;

    private User admin;
    private User user;
    private UserDTO userDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        admin = new User("admin", "admin", User.Role.ADMIN);
        admin.setId(1L);

        user = new User("testUser", "password", User.Role.DEV);
        user.setId(2L);

        userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setPassword("password");
        userDTO.setRole(UserDTO.Role.DEV);
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        when(userService.getUserById(1L)).thenReturn(admin);
        when(userService.createUser(user)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);
        when(modelMapper.map(any(UserDTO.class), eq(User.class))).thenReturn(user);

        mockMvc.perform(
            post("/api/users/signup")
                .cookie(new Cookie("memberId", "1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"username\":\"testUser\"}"));
    }

    @Test
    public void testCreateUser_Unauthorized() throws Exception {
        when(userService.getUserById(anyLong())).thenReturn(user);
        when(modelMapper.map(any(UserDTO.class), eq(User.class))).thenReturn(user);

        mockMvc.perform(
            post("/api/users/signup")
                .cookie(new Cookie("memberId", "5"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnauthorizedException))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateUser_InvalidInput() throws Exception {
        when(userService.getUserById(1L)).thenReturn(admin);
        when(modelMapper.map(any(UserDTO.class), eq(User.class))).thenReturn(user);
        userDTO.setUsername("");

        mockMvc.perform(
            post("/api/users/signup")
                .cookie(new Cookie("memberId", "1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_Success() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", user);
        responseBody.put("memberId", user.getId());

        when(modelMapper.map(any(UserDTO.class), eq(User.class))).thenReturn(user);
        when(userService.login(user)).thenReturn(responseBody);

        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogin_Failure() throws Exception {
        when(modelMapper.map(any(UserDTO.class), eq(User.class))).thenReturn(user);
        when(userService.login(user)).thenReturn(null);

        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllDevs() throws Exception {
        User user2 = new User("testUser2", "password", User.Role.DEV);
        user2.setId(3L);

        UserDTO user2DTO = new UserDTO();
        user2DTO.setUsername("testUser2");
        user2DTO.setPassword("password");
        user2DTO.setRole(UserDTO.Role.DEV);

        List<User> devUsers = new ArrayList<>();
        devUsers.add(user);
        devUsers.add(user2);

        when(userService.getAllDevs()).thenReturn(devUsers);
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(modelMapper.map(user2DTO, User.class)).thenReturn(user2);

        mockMvc.perform(
            get("/api/users/devs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}