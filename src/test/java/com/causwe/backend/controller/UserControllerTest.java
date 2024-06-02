package com.causwe.backend.controller;

import com.causwe.backend.dto.UserRequestDTO;
import com.causwe.backend.dto.UserResponseDTO;
import com.causwe.backend.exceptions.GlobalExceptionHandler;
import com.causwe.backend.exceptions.UserNotFoundException;
import com.causwe.backend.model.Admin;
import com.causwe.backend.model.Developer;
import com.causwe.backend.model.User;
import com.causwe.backend.security.JwtTokenProvider;
import com.causwe.backend.service.UserService;
import com.causwe.backend.util.RoleConverter;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserController userController;

    private User admin;
    private User dev;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setId(1L);

        dev = new Developer();
        dev.setUsername("dev");
        dev.setPassword("dev");
        dev.setId(2L);

        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("dev");
        userRequestDTO.setPassword("dev");
        userRequestDTO.setRole(UserRequestDTO.Role.DEV);

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUsername("dev");
        userResponseDTO.setRole(UserResponseDTO.Role.DEV);        
    }

    @Test
    public void testGetUserById_Success() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(2L);
        when(userService.getUserById(2L)).thenReturn(dev);
        when(modelMapper.map(dev, UserResponseDTO.class)).thenReturn(userResponseDTO);

        mockMvc.perform(get("/api/users")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(dev.getUsername()));
    }

    @Test
    public void testGetUserById_NotFound() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(userService.getUserById(3L)).thenThrow(new UserNotFoundException(3L));

        mockMvc.perform(get("/api/users")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateUser_Success() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(admin);
        when(userService.createUser(
            userRequestDTO.getUsername(), 
            userRequestDTO.getPassword(), 
            RoleConverter.convertToUserRole(userRequestDTO.getRole()))
        ).thenReturn(dev);
        when(modelMapper.map(dev, UserResponseDTO.class)).thenReturn(userResponseDTO);

        mockMvc.perform(
            post("/api/users/signup")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"username\":\"dev\"}"));
    }

    @Test
    public void testCreateUser_BadReqeust() throws Exception {
        userRequestDTO.setUsername("");

        mockMvc.perform(
            post("/api/users/signup")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateUser_Unauthorized() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(2L);
        when(userService.getUserById(2L)).thenReturn(dev);

        mockMvc.perform(
            post("/api/users/signup")
                .cookie(new Cookie("jwt", "token"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogin_Success() throws Exception {
        when(userService.login(userRequestDTO.getUsername(), userRequestDTO.getPassword())).thenReturn(dev);
        when(modelMapper.map(dev, UserResponseDTO.class)).thenReturn(userResponseDTO);

        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    public void testLogin_Failure() throws Exception {
        when(userService.login(userRequestDTO.getUsername(), userRequestDTO.getPassword())).thenReturn(null);

        mockMvc.perform(
            post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAllDevs() throws Exception {
        User dev2 = new Developer();
        dev2.setUsername("dev2");
        dev2.setPassword("dev2");
        dev2.setId(3L);

        UserResponseDTO dev2ResponseDTO = new UserResponseDTO();
        dev2ResponseDTO.setUsername("testUser2");
        dev2ResponseDTO.setRole(UserResponseDTO.Role.DEV);

        List<User> devUsers = Arrays.asList(dev, dev2);
        when(userService.getAllDevs()).thenReturn(devUsers);
        when(modelMapper.map(any(User.class), eq(UserResponseDTO.class))).thenReturn(userResponseDTO, dev2ResponseDTO);

        mockMvc.perform(
            get("/api/users/devs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(dev.getUsername()));
    }

    @Test
    public void testLogout() throws Exception {
        mockMvc.perform(
            post("/api/users/logout")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully."))
                .andExpect(cookie().maxAge("jwt", 0));
    }
}