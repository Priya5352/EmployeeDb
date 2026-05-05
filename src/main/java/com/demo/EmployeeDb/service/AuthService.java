package com.demo.EmployeeDb.service;

import com.demo.EmployeeDb.dto.LoginRequest;
import com.demo.EmployeeDb.dto.LoginResponse;
import com.demo.EmployeeDb.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    //  Only DTOs in interface — no entity
    ResponseEntity<?> register(RegisterRequest request);
    ResponseEntity<?> login(LoginRequest request);
}