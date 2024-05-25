package com.causwe.backend.controller;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.exceptions.UserNotFoundException;
import com.causwe.backend.dto.UserDTO;
import com.causwe.backend.model.User;
import com.causwe.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userData, @CookieValue(value = "memberId", required = false) Long memberId) {
        if (Objects.equals(userData.getUsername(), "") || Objects.equals(userData.getPassword(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            User currentUser = userService.getUserById(memberId);
            if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
                throw new UnauthorizedException("Only admins can create users.");
            }

            User newUser = userService.createUser(modelMapper.map(userData, User.class));
            UserDTO newUserDTO = modelMapper.map(newUser, UserDTO.class);
            return new ResponseEntity<>(newUserDTO, HttpStatus.CREATED);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDTO userData, HttpServletResponse response) {
        Map<String, Object> responseBody = userService.login(modelMapper.map(userData, User.class));
        if (responseBody != null) {
            Cookie cookie = new Cookie("memberId", responseBody.get("memberId").toString());
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            String cookieHeader = String.format("%s=%s; Path=%s; SameSite=None; Secure", cookie.getName(), cookie.getValue(), cookie.getPath());
            response.addHeader("Set-Cookie", cookieHeader);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("")
    public ResponseEntity<UserDTO> getUserById(@CookieValue(value = "memberId", required = false) Long memberId) {
        try {
            User user = userService.getUserById(memberId);
            UserDTO UserDTO =  modelMapper.map(user, UserDTO.class);
            return new ResponseEntity<>(UserDTO, HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/devs")
    public ResponseEntity<List<UserDTO>> getAllDevs() {
        List<User> devUsers = userService.getAllDevs();

        List<UserDTO> devUserDTOs = devUsers
        .stream()
        .map(user -> modelMapper.map(user, UserDTO.class))
        .collect(Collectors.toList());


        return new ResponseEntity<>(devUserDTOs, HttpStatus.OK);
    }
}
