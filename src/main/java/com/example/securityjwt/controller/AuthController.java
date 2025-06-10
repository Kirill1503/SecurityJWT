package com.example.securityjwt.controller;

import com.example.securityjwt.DTO.UserDTO;
import com.example.securityjwt.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.signUp(userDTO));
    }

    @PostMapping("/signin")
    public ResponseEntity<UserDTO> signin(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.signIn(userDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserDTO> refreshToken(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.refreshToken(userDTO));
    }
}
