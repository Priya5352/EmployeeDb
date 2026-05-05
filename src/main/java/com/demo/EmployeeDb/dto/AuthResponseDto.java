package com.demo.EmployeeDb.dto;

public class AuthResponseDto {
    private Long id;
    private String username;
    private String role;

    // Constructor injection
    public AuthResponseDto(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    // Getters 
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}