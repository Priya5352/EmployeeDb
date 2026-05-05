package com.demo.EmployeeDb.service;

import com.demo.EmployeeDb.dto.DepartmentRequestDto;
import com.demo.EmployeeDb.dto.DepartmentResponseDto;
import com.demo.EmployeeDb.dto.EmployeeResponseDto;
import com.demo.EmployeeDb.entity.Department;
import com.demo.EmployeeDb.entity.Employee;
import com.demo.EmployeeDb.entity.Role;
import com.demo.EmployeeDb.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// Implementation of DepartmentService interface
// Contains all business logic for department operations
// Entity used only internally — DTOs used for all input and output
@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    // Repository for performing CRUD operations on the Department table
    private final DepartmentRepository departmentRepository;

    // Constructor injection — Spring injects DepartmentRepository automatically
    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // Private helper — converts Department entity to DepartmentResponseDto
    // Entity never leaves the service layer
    private DepartmentResponseDto toDto(Department dept) {
        DepartmentResponseDto dto = new DepartmentResponseDto();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        return dto;
    }

    // Private helper — converts Employee entity to EmployeeResponseDto
    // Reused here to avoid dependency on EmployeeServiceImpl
    private EmployeeResponseDto toEmployeeDto(Employee emp) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(emp.getId());
        dto.setName(emp.getName());
        dto.setUsername(emp.getUsername());
        dto.setSalary(emp.getSalary());

        // Single String instead of List
        if (emp.getRoles() != null && !emp.getRoles().isEmpty()) {
            dto.setRole(emp.getRoles().get(0).getRoleName().replace("ROLE_", ""));
        }

        // Single Long instead of List
        if (emp.getDepartments() != null && !emp.getDepartments().isEmpty()) {
            dto.setDepartmentId(emp.getDepartments().get(0).getId());
        }

        // Single String instead of List
        if (emp.getDepartments() != null && !emp.getDepartments().isEmpty()) {
            dto.setDepartmentName(emp.getDepartments().get(0).getName());
        }

        return dto;
    }

    // Creates and saves a new department
    // Checks for duplicate department name before saving
    @Override
    public DepartmentResponseDto addDepartment(DepartmentRequestDto dto) {
        logger.trace("Log level: TRACE — addDepartment() called");
        logger.info("Log level: INFO — Adding department: {}", dto.getName());

        try {
            // Check if department name already exists
            if (departmentRepository.findByName(dto.getName()).isPresent()) {
                logger.warn("Log level: WARN — Department already exists: {}", dto.getName());
                throw new RuntimeException("Department already exists: " + dto.getName());
            }

            // Build and save new department entity
            Department dept = new Department();
            dept.setName(dto.getName());

            Department saved = departmentRepository.save(dept);
            logger.info("Log level: INFO — Department added successfully with id: {}", saved.getId());
            return toDto(saved);

        } catch (Exception e) {
            logger.error("Log level: ERROR — Error adding department {}: {}", dto.getName(), e.getMessage(), e);
            throw e;
        }
    }

    // Retrieves all departments from the database
    @Override
    public List<DepartmentResponseDto> getAllDepartments() {
        logger.trace("Log level: TRACE — getAllDepartments() called");
        logger.info("Log level: INFO — Fetching all departments");

        try {
            List<DepartmentResponseDto> list = departmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            logger.info("Log level: INFO — Total departments fetched: {}", list.size());
            return list;

        } catch (Exception e) {
            logger.error("Log level: ERROR — Error fetching departments: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Retrieves a single department by ID
    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        logger.trace("Log level: TRACE — getDepartmentById() called");
        logger.info("Log level: INFO — Fetching department with id: {}", id);

        try {
            Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));
            return toDto(dept);

        } catch (Exception e) {
            logger.error("Log level: ERROR — Error fetching department {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Retrieves all employees belonging to a specific department by ID
    @Override
    public List<EmployeeResponseDto> getEmployeesByDepartmentId(Long departmentId) {
        logger.trace("Log level: TRACE — getEmployeesByDepartmentId() called");
        logger.info("Log level: INFO — Fetching employees for department id: {}", departmentId);

        try {
            Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));

            List<EmployeeResponseDto> employees = dept.getEmployees().stream()
                .map(this::toEmployeeDto)
                .collect(Collectors.toList());

            logger.info("Log level: INFO — Total employees in department {}: {}", departmentId, employees.size());
            return employees;

        } catch (Exception e) {
            logger.error("Log level: ERROR — Error fetching employees for department {}: {}", departmentId, e.getMessage(), e);
            throw e;
        }
    }

    // ✅ UPDATED — returns full employee details instead of just IDs
    @Override
    public List<EmployeeResponseDto> getEmployeeIdsByDepartmentName(String name) {
        logger.trace("Log level: TRACE — getEmployeeIdsByDepartmentName() called");
        logger.info("Log level: INFO — Fetching employees for department name: {}", name);

        try {
            // Fetch department by name — throws exception if not found
            Department dept = departmentRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Department not found: " + name));

            // Return full employee details instead of just IDs
            List<EmployeeResponseDto> employees = dept.getEmployees().stream()
                .map(this::toEmployeeDto)
                .collect(Collectors.toList());

            logger.info("Log level: INFO — Total employees found for department {}: {}", name, employees.size());
            return employees;

        } catch (Exception e) {
            logger.error("Log level: ERROR — Error fetching employees for department {}: {}", name, e.getMessage(), e);
            throw e;
        }
    }

    // Updates an existing department name by ID
    // Throws exception if department not found
    // Returns updated department as DTO — entity never leaves service layer
    @Override
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto) {
        logger.trace("Log level: TRACE — updateDepartment() called");
        logger.info("Log level: INFO — Updating department with id: {}", id);
        logger.debug("Log level: DEBUG — New department name: {}", dto.getName());

        try {
            Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));

            existing.setName(dto.getName());

            DepartmentResponseDto updated = toDto(departmentRepository.save(existing));
            logger.info("Log level: INFO — Department updated successfully with id: {}", id);
            return updated;

        } catch (Exception e) {
            logger.error("Log level: ERROR — Error updating department {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Deletes a department by ID
    // Clears employee associations before deleting to avoid FK constraint errors
    // Throws exception if department not found
    @Override
    public void deleteDepartment(Long id) {
        logger.trace("Log level: TRACE — deleteDepartment() called");
        logger.info("Log level: INFO — Deleting department with id: {}", id);
        logger.debug("Log level: DEBUG — Looking up department id: {} for deletion", id);

        try {
            Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));

            existing.getEmployees().clear();
            departmentRepository.save(existing);

            departmentRepository.delete(existing);
            logger.info("Log level: INFO — Department deleted successfully with id: {}", id);

        } catch (Exception e) {
            logger.error("Log level: ERROR — Error deleting department {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }
}