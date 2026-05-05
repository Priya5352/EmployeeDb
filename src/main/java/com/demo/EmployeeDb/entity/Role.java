package com.demo.EmployeeDb.entity;

import jakarta.persistence.*;
import java.util.List;

//Entity Role class
@Entity
public class Role {
    
	//Primary key to the Role Table
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    //Employee role name
    private String roleName;

    //ManyToMany Relationship between Employee Table and Role Table
    @ManyToMany(mappedBy = "roles")
    private List<Employee> employees;

    //Default Constructor required by JPA
    public Role() {}

    //Getters and Setters
    public Long getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

	
}