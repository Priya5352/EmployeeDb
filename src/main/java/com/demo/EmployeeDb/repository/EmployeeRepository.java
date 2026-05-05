package com.demo.EmployeeDb.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.demo.EmployeeDb.entity.Employee;

// Database for employee
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByUsername(String username);
    
    List<Employee> findByNameStartingWithIgnoreCase(String prefix);
}