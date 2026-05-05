package com.demo.EmployeeDb.dto;


public class LoginResponse {

    private String token;
    private String username;
    private String role;

    // Constructor injection
    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Getters
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
