package com.demo.EmployeeDb.controller;

import com.demo.EmployeeDb.dto.LoginRequest;
import jakarta.validation.Valid;
import com.demo.EmployeeDb.dto.RegisterRequest;
import com.demo.EmployeeDb.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Marks this as a RestController
@RestController
@RequestMapping("/auth")// All endpoints in this controller are prefixed with /auth
public class AuthController {

    // Service is injected 
    private final AuthService authService;

    // Constructor injection
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    // Registering the new employee
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request); 
    }

    // Employee login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request); 
    }
}