package com.demo.EmployeeDb.entity;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

// Marks this as a JPA managed entity mapped to the department table
@Entity
@Table(name = "department")
public class Department {

    // Primary key — auto incremented by the database
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the department — e.g. Engineering, HR, Finance
    private String name;

    // Many departments can have many employees
    // mappedBy refers to the field name in Employee entity
    // JsonIgnore prevents infinite recursion during serialization
    @ManyToMany(mappedBy = "departments")
    @JsonIgnore
    private List<Employee> employees;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Employee> getEmployees() { return employees; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
}