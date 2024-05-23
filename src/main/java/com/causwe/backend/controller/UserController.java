package com.causwe.backend.controller;

import com.causwe.backend.exceptions.UnauthorizedException;

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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only admins can create users.");
        }

        User newUser = userService.createUser(modelMapper.map(userDTO, User.class));
        if (newUser == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        UserDTO newUserDTO = modelMapper.map(newUser, UserDTO.class);

        return new ResponseEntity<>(newUserDTO, HttpStatus.CREATED);
    }

    //TODO login responseBody는 Map<memberID.toString(), User>의 형태인데 DTO로 변환이 필요한지 결정
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        Map<String, Object> responseBody = userService.login(modelMapper.map(userDTO, User.class));
        if (responseBody != null) {
            Cookie cookie = new Cookie("memberId", responseBody.get("memberId").toString());
            cookie.setPath("/");
            response.addCookie(cookie);
            return new ResponseEntity<>(responseBody, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllDevs() {
        List<User> devUsers = userService.getAllDevs();

        List<UserDTO> devUserDTOs = devUsers
        .stream()
        .map(user -> modelMapper.map(user, UserDTO.class))
        .collect(Collectors.toList());


        return new ResponseEntity<>(devUserDTOs, HttpStatus.OK);
    }
}
