package com.demo.EmployeeDb.service;

import com.demo.EmployeeDb.dto.DepartmentRequestDto;
import com.demo.EmployeeDb.dto.DepartmentResponseDto;
import com.demo.EmployeeDb.dto.EmployeeResponseDto;
import java.util.List;

// Service interface for department operations
// Defines all business operations related to departments
public interface DepartmentService {

    // Create a new department
    DepartmentResponseDto addDepartment(DepartmentRequestDto dto);

    // Get all departments
    List<DepartmentResponseDto> getAllDepartments();

    // Get department by ID
    DepartmentResponseDto getDepartmentById(Long id);

    // Get all employees belonging to a department by ID
    List<EmployeeResponseDto> getEmployeesByDepartmentId(Long departmentId);

    // ✅ UPDATED — returns full employee details instead of just IDs
    List<EmployeeResponseDto> getEmployeeIdsByDepartmentName(String name);

    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto);

    void deleteDepartment(Long id);
}