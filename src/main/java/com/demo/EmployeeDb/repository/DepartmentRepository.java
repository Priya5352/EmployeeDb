package com.demo.EmployeeDb.repository;

import com.demo.EmployeeDb.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Repository for performing CRUD operations on the Department table
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Find department by name — used to check for duplicates before saving
    Optional<Department> findByName(String name);
}