package com.demo.EmployeeDb.service;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demo.EmployeeDb.dto.EmployeeRequestDto;
import com.demo.EmployeeDb.dto.EmployeeResponseDto;
import com.demo.EmployeeDb.entity.Department;
import com.demo.EmployeeDb.entity.Employee;
import com.demo.EmployeeDb.entity.Role;
import com.demo.EmployeeDb.repository.DepartmentRepository;
import com.demo.EmployeeDb.repository.EmployeeRepository;
import com.demo.EmployeeDb.repository.RoleRepository;

// Implementation of EmployeeService interface
@Service

// @Transactional ensures each method runs in a database transaction
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    // Logger instance
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository repo;
    private final RoleRepository roleRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepo;

    // Constructor injection
    public EmployeeServiceImpl(EmployeeRepository repo, RoleRepository roleRepo,
                               BCryptPasswordEncoder passwordEncoder, DepartmentRepository departmentRepo) {
        this.repo = repo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepo = departmentRepo;
    }

    // Creates a new empty response DTO
    private EmployeeResponseDto toDto(Employee emp) {
        EmployeeResponseDto dto = new EmployeeResponseDto();

        // Map each field from the entity to the corresponding DTO field
        dto.setId(emp.getId());
        dto.setName(emp.getName());
        dto.setUsername(emp.getUsername());
        dto.setSalary(emp.getSalary());

        // ✅ UPDATED — single String instead of List
        if (emp.getRoles() != null && !emp.getRoles().isEmpty()) {
            dto.setRole(emp.getRoles().get(0).getRoleName().replace("ROLE_", ""));
        }

        // ✅ UPDATED — single Long instead of List
        if (emp.getDepartments() != null && !emp.getDepartments().isEmpty()) {
            dto.setDepartmentId(emp.getDepartments().get(0).getId());
        }

        // ✅ UPDATED — single String instead of List
        if (emp.getDepartments() != null && !emp.getDepartments().isEmpty()) {
            dto.setDepartmentName(emp.getDepartments().get(0).getName());
        }

        return dto;
    }

    // Converts employee DTO to employee entity
    private Employee toEntity(EmployeeRequestDto dto) {
        Employee emp = new Employee();
        emp.setName(dto.getName());
        emp.setUsername(dto.getUsername());
        emp.setPassword(passwordEncoder.encode(dto.getPassword()));
        emp.setSalary(dto.getSalary());

        // Only resolve roles if provided — otherwise set empty list
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            emp.setRoles(resolveRoles(dto.getRoles()));
        } else {
            emp.setRoles(new ArrayList<>()); // never null
        }
        if (dto.getDepartmentIds() != null && !dto.getDepartmentIds().isEmpty()) {
            emp.setDepartments(resolveDepartments(dto.getDepartmentIds()));
        } else {
            // Throw exception — department is required
            throw new RuntimeException("At least one department ID is required.");
        }
        return emp;
    }

    // Fetches role entity from the database by role name
    private List<Role> resolveRoles(List<String> roleNames) {
        List<Role> roles = new ArrayList<>();
        for (String roleName : roleNames) {
            Role role = roleRepo.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    private List<Department> resolveDepartments(List<Long> departmentIds) {
        List<Department> departments = new ArrayList<>();
        for (Long deptId : departmentIds) {
            Department dept = departmentRepo.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + deptId));
            departments.add(dept);
        }
        return departments;
    }

    // Creates and saves a new employee record in the database
    @Override
    public EmployeeResponseDto addEmployee(EmployeeRequestDto dto) {

        // Log Methods
        logger.trace(" addEmployee() called");
        logger.info(" Adding employee with username: {}", dto.getUsername());
        logger.debug(" Employee details: name={}, salary={}", dto.getName(), dto.getSalary());

        try {
            if (repo.findByUsername(dto.getUsername()).isPresent()) {
                logger.warn(" Username already exists: {}", dto.getUsername());
                throw new RuntimeException("Username already exists: " + dto.getUsername());
            }

            // Converts dto to entity
            EmployeeResponseDto saved = toDto(repo.save(toEntity(dto)));

            // Log successful creation with the generated employee ID
            logger.info(" Employee added successfully with id: {}", saved.getId());
            return saved;

        } catch (Exception e) {
            logger.error(" Error adding employee {}: {}", dto.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    // Retrieves all the employee records from the database
    @Override
    public List<EmployeeResponseDto> getAllEmployees() {

        // Log Methods
        logger.trace(" getAllEmployees() called");
        logger.info(" Fetching all employees");

        try {
            List<EmployeeResponseDto> list = repo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

            // Log Methods
            logger.info(" Total employees fetched: {}", list.size());
            logger.debug(" Employee list size: {}", list.size());
            return list;

        } catch (Exception e) {

            // Log Method
            logger.error(" Error fetching all employees: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Retrieves employee by id
    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {

        // Log Methods
        logger.trace(" getEmployeeById() called");
        logger.info(" Fetching employee with id: {}", id);
        logger.debug(" Looking up employee id: {}", id);

        try {
            EmployeeResponseDto dto = toDto(repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id)));

            // Log method
            logger.info(" Employee found: {}", dto.getUsername());
            return dto;

        } catch (Exception e) {

            // Log Method
            logger.error(" Error fetching employee {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Updates an employee record
    @Override
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {

        // Log methods
        logger.trace(" updateEmployee() called");
        logger.info(" Updating employee with id: {}", id);
        logger.debug(" Update details: name={}, salary={}", dto.getName(), dto.getSalary());

        try {
            Employee existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
            existing.setName(dto.getName());
            existing.setSalary(dto.getSalary());
            if (dto.getRoles() != null) {
                existing.setRoles(resolveRoles(dto.getRoles()));
            }

            if (dto.getDepartmentIds() != null && !dto.getDepartmentIds().isEmpty()) {
                existing.setDepartments(resolveDepartments(dto.getDepartmentIds()));
            }

            EmployeeResponseDto updated = toDto(repo.save(existing));
            logger.info(" Employee updated successfully with id: {}", id);
            return updated;

        } catch (Exception e) {
            logger.error(" Error updating employee {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Deletes an employee by id
    @Override
    public void deleteEmployeeById(Long id) {
        logger.trace(" deleteEmployeeById() called");
        logger.info(" Deleting employee with id: {}", id);
        logger.debug(" Looking up employee id: {} for deletion", id);

        try {
            Employee employee = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));

            if (employee.getUsername().equals("admin")) {
                logger.warn(" Attempt to delete admin user blocked");
                throw new RuntimeException("Cannot delete admin user!");
            }

            employee.getRoles().clear();
            repo.delete(employee);
            logger.info(" Employee deleted successfully with id: {}", id);

        } catch (Exception e) {
            logger.error(" Error deleting employee {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    // Retrieves a single employee by their username
    @Override
    public EmployeeResponseDto getEmployeeByUsername(String username) {
        logger.trace(" getEmployeeByUsername() called");
        logger.info(" Fetching employee with username: {}", username);
        logger.debug(" Looking up username: {}", username);

        try {
            EmployeeResponseDto dto = toDto(repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + username)));
            logger.info(" Employee found with username: {}", username);
            return dto;

        } catch (Exception e) {
            logger.error(" Error fetching employee by username {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    // Retrieves all employees whose name starts with the given prefix
    @Override
    public List<EmployeeResponseDto> getEmployeesByNamePrefix(String prefix) {
        logger.trace(" getEmployeesByNamePrefix() called");
        logger.info(" Searching employees with name starting with: {}", prefix);
        logger.debug(" Name prefix received: {}", prefix);

        try {
            List<EmployeeResponseDto> list = repo.findByNameStartingWithIgnoreCase(prefix)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

            logger.info(" Total employees found with prefix '{}': {}", prefix, list.size());
            return list;

        } catch (Exception e) {
            logger.error(" Error searching employees by prefix {}: {}", prefix, e.getMessage(), e);
            throw e;
        }
    }
}