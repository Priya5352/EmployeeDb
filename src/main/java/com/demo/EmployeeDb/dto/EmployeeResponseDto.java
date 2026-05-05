package com.demo.EmployeeDb.dto;

import com.fasterxml.jackson.annotation.JsonView;

public class EmployeeResponseDto {

    @JsonView(Views.Summary.class)
    private Long id;

    @JsonView(Views.Summary.class)
    private String name;

    @JsonView(Views.Summary.class)
    private String username;

    @JsonView(Views.Summary.class)
    private double salary;

    @JsonView(Views.Full.class)
    private String role;

    @JsonView(Views.Full.class)
    private Long departmentId;

    @JsonView(Views.Full.class)
    private String departmentName;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
}