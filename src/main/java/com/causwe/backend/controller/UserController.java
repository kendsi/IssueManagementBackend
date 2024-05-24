package com.causwe.backend.controller;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.User;
import com.causwe.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;
  
    @PostMapping("/signup")
    public ResponseEntity<User> createUser(@RequestBody User user, @CookieValue(value = "memberId", required = false) Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            throw new UnauthorizedException("Only admins can create users.");
        }

        User newUser = userService.createUser(user);
        if (newUser == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user, HttpServletResponse response) {
        Map<String, Object> responseBody = userService.login(user);
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

    @GetMapping
    public ResponseEntity<List<User>> getAllDevs() {
        List<User> devUsers = userService.getAllDevs();
        return new ResponseEntity<>(devUsers, HttpStatus.OK);
    }
}
