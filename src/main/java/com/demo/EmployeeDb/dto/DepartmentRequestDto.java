package com.demo.EmployeeDb.dto;

import jakarta.validation.constraints.*;

public class DepartmentRequestDto {

    @NotBlank(message = "Department name is required.")
    @Size(min = 2, max = 50, message = "Department name must be between 2 and 50 characters.")
    private String name;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}