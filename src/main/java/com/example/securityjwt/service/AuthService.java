package com.example.securityjwt.service;

import com.example.securityjwt.DTO.UserDTO;
import com.example.securityjwt.model.User;
import com.example.securityjwt.repository.UserRepository;
import com.example.securityjwt.roles.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, JWTUtils jwtUtils, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public UserDTO signUp(UserDTO userDTO) {
        UserDTO newUserDTO = new UserDTO();
        try {
            User newUser = new User();
            newUser.setUsername(userDTO.getUsername());
            newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            newUser.setRole(Role.valueOf(userDTO.getRole()));
            User userResult = userRepository.save(newUser);
            if (userResult != null && userResult.getId() > 0) {
                newUserDTO.setUser(userResult);
                newUserDTO.setMessage("User registered successfully");
                newUserDTO.setStatusCode(200);
                newUserDTO.setToken(jwtUtils.generateToken(userResult));
            }
        } catch (Exception e) {
            newUserDTO.setMessage(e.getMessage());
            newUserDTO.setStatusCode(500);
        }
        return newUserDTO;
    }

    public UserDTO signIn(UserDTO userDTO) {
        UserDTO newUserDTO = new UserDTO();
        try {
            var user = userRepository.findByUsername(userDTO.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (!user.isAccountNonLocked()) {
                newUserDTO.setMessage("User account is locked");
                newUserDTO.setStatusCode(500);
                return newUserDTO;
            }
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getPassword()));
            user.setFailedAttempts(0);
            userRepository.save(user);
            var token = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            newUserDTO.setStatusCode(200);
            newUserDTO.setToken(token);
            newUserDTO.setRefreshToken(refreshToken);
            newUserDTO.setExpirationTime((long) (1000 * 60 * 60 * 24));
            newUserDTO.setMessage("Successfully signed in");
        } catch (BadCredentialsException e) {
            var user = userRepository.findByUsername(userDTO.getUsername()).orElse(null);
            if (user != null) {
                user.setFailedAttempts(user.getFailedAttempts() + 1);
                if (user.getFailedAttempts() >= 5) {
                    user.setAccountNonLocked(false);
                    newUserDTO.setMessage("Account is locked after 5 failed attempts");
                }
                userRepository.save(user);
            }
            newUserDTO.setStatusCode(500);
            newUserDTO.setMessage("Invalid credentials");
        }
        return newUserDTO;
    }

    public UserDTO refreshToken(UserDTO userDTO) {
        UserDTO newUserDTO = new UserDTO();
        String username = jwtUtils.extractUsername(userDTO.getToken());
        User user = userRepository.findByUsername(username).orElseThrow();
        if (jwtUtils.isTokenValid(userDTO.getToken(), user)) {
            var token = jwtUtils.generateToken(user);
            newUserDTO.setStatusCode(200);
            newUserDTO.setToken(token);
            newUserDTO.setRefreshToken(userDTO.getRefreshToken());
            newUserDTO.setExpirationTime((long) (1000 * 60 * 60 * 24));
            newUserDTO.setMessage("Successfully refresh token");
        } else {
            newUserDTO.setStatusCode(500);
        }
        return newUserDTO;
    }
}
