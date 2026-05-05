package com.demo.EmployeeDb.service;

import java.util.List;
import com.demo.EmployeeDb.dto.EmployeeRequestDto;
import com.demo.EmployeeDb.dto.EmployeeResponseDto;

public interface EmployeeService {
	
	// Creating a new employee
    EmployeeResponseDto addEmployee(EmployeeRequestDto dto);
    
    // Read all employees
    List<EmployeeResponseDto> getAllEmployees();
    
    // Read employee by id
    EmployeeResponseDto getEmployeeById(Long id);
    
    // Update an employee
    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto);
    
    // Delete employee by id
    void deleteEmployeeById(Long id);
    
    EmployeeResponseDto getEmployeeByUsername(String username);
    
    List<EmployeeResponseDto> getEmployeesByNamePrefix(String prefix);
}