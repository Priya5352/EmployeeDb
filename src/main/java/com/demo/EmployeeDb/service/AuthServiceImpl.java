package com.demo.EmployeeDb.service;

// Importing all DTO classes used for request and response
import com.demo.EmployeeDb.dto.*;
// Importing Employee entity for database operations
import com.demo.EmployeeDb.entity.Employee;
// Importing Role entity to assign roles to employees
import com.demo.EmployeeDb.entity.Role;
// Importing repository to perform database operations on Employee table
import com.demo.EmployeeDb.repository.EmployeeRepository;
// Importing repository to fetch roles from the Role table
import com.demo.EmployeeDb.repository.RoleRepository;
// Importing JwtUtil to generate JWT tokens after successful login
import com.demo.EmployeeDb.security.JwtUtil;
// Importing Logger for structured logging
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Importing ResponseEntity to build HTTP responses with status codes and body
import org.springframework.http.ResponseEntity;
// Importing BCryptPasswordEncoder to hash and verify passwords
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// Marks this class as a Spring Service — auto-detected and managed by Spring container
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

// Implementation of AuthService interface
// Handles all authentication logic — register and login
// Uses repositories directly to interact with the database
@Service
public class AuthServiceImpl implements AuthService {

    // Logger instance for this class
    // Logs messages at TRACE, DEBUG, INFO, WARN, ERROR levels
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    // Repository to perform CRUD operations on the Employee table
    private final EmployeeRepository employeeRepository;

    // Repository to fetch Role records from the Role table
    private final RoleRepository roleRepository;

    // BCryptPasswordEncoder used to hash passwords before saving
    // and to verify plain text passwords against stored hashes during login
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor injection
    // Spring automatically injects all three dependencies at startup
    public AuthServiceImpl(EmployeeRepository employeeRepository,
                           RoleRepository roleRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Private helper method — converts Employee entity to AuthResponseDto
    // Extracts only the fields needed for auth response — id, username, role
    // Entity never leaves this class — only the DTO is returned to the controller
    private AuthResponseDto toDto(Employee emp) {
        // If the employee has no roles assigned, default to ROLE_USER
        // Otherwise take the first role from the list
        String role = emp.getRoles().isEmpty()
            ? "ROLE_USER"
            : emp.getRoles().get(0).getRoleName();
        // Build and return the AuthResponseDto with id, username and role
        return new AuthResponseDto(emp.getId(), emp.getUsername(), role);
    }

    // Handles employee registration
    // Validates username uniqueness, fetches role from DB, hashes password and saves employee
    // Returns 201 on success, 409 if username taken, 400 if role not found, 500 on error
    @Override
    public ResponseEntity<?> register(RegisterRequest request) {

        // Log method entry at TRACE level
        logger.trace(" register() called");
        // Log the registration attempt with the requested username
        logger.info("Register attempt for username: {}", request.getUsername());
        // Log full request details at DEBUG level for troubleshooting
        logger.debug(" Register request received: name={}, role={}", request.getName(), request.getRoleName());

        // Check if username already exists in the database
        // findByUsername returns Optional — isPresent() checks if a record was found
        if (employeeRepository.findByUsername(request.getUsername()).isPresent()) {
            // Log warning — duplicate username detected
            logger.warn(" Username already taken: {}", request.getUsername());
            // Return 409 Conflict — username must be unique
            return ResponseEntity.status(409).body("Username already taken.");
        }

        // Fetch the Role from the database using the role name sent in the request
        // orElse(null) returns null if the role name does not exist in the role table
        Role role = roleRepository.findByRoleName(request.getRoleName()).orElse(null);
        if (role == null) {
            // Log warning — requested role does not exist in the database
            logger.warn(" Role not found: {}", request.getRoleName());
            // Return 400 Bad Request — role must exist in the role table before registering
            return ResponseEntity.status(400)
                .body("Role '" + request.getRoleName() + "' not found.");
        }

        try {
            // Build a new Employee entity from the registration request fields
            Employee emp = new Employee();
            emp.setName(request.getName());
            emp.setSalary(request.getSalary());
            emp.setUsername(request.getUsername());
            // Hash the plain text password using BCrypt before saving to database
            // BCrypt adds a random salt — same password produces different hashes each time
            emp.setPassword(passwordEncoder.encode(request.getPassword()));
            // Assign the fetched role to the employee as a list
            emp.setRoles(List.of(role));

            // Save the new employee record to the database
            Employee saved = employeeRepository.save(emp);

            // Convert the saved entity to DTO immediately — entity never leaves service layer
            AuthResponseDto dto = toDto(saved);

            // Log successful registration
            logger.info(" Employee registered successfully: {}", dto.getUsername());
            // Log saved employee details at DEBUG level
            logger.debug(" Saved employee id={}, role={}", dto.getId(), dto.getRole());

            // Return 201 Created with a success message and the saved employee details
            return ResponseEntity.status(201).body(Map.of(
                "message",  "Employee registered successfully",
                "id",       dto.getId(),
                "username", dto.getUsername(),
                "role",     dto.getRole()
            ));

        } catch (Exception e) {
            // Log the full exception at ERROR level including stack trace
            logger.error(" Registration failed for {}: {}", request.getUsername(), e.getMessage(), e);
            // Return 500 Internal Server Error with the exception message
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }

    // Handles employee login
    // Validates username exists, verifies password using BCrypt, generates and returns JWT token
    // Returns 200 with token on success, 400 if fields missing, 401 on invalid credentials, 500 on error
    @Override
    public ResponseEntity<?> login(LoginRequest request) {

        // Log method entry at TRACE level
        logger.trace(" login() called");
        // Log the login attempt with the requested username
        logger.info(" Login attempt for username: {}", request.getUsername());
        // Log request details at DEBUG level
        logger.debug(" Login request received for username: {}", request.getUsername());

        try {
            // Null check — return 400 if username or password field is missing from request body
            // Prevents NullPointerException when calling .trim() on null values
            if (request.getUsername() == null || request.getPassword() == null) {
                logger.warn(" Login failed, username or password is missing");
                // Return 400 Bad Request — both fields are required
                return ResponseEntity.status(400).body("Username and password are required.");
            }

            // Trim whitespace from username and password
            // Prevents login failures due to accidental spaces in the request body
            String username = request.getUsername().trim();
            String password = request.getPassword().trim();

            // Check for blank fields after trimming
            // Handles cases where the field is present but contains only spaces
            if (username.isBlank() || password.isBlank()) {
                logger.warn(" Login failed, username or password is blank");
                // Return 400 Bad Request — fields cannot be blank
                return ResponseEntity.status(400).body("Username and password cannot be blank.");
            }

            // Look up the employee in the database by username
            // orElse(null) returns null if no employee found with that username
            Employee employee = employeeRepository.findByUsername(username).orElse(null);

            // If no employee found for the given username — return 401 Unauthorized
            if (employee == null) {
                // Log warning — username not found in database
                logger.warn(" Login failed, username not found: {}", username);
                // Return generic message — do not reveal whether username or password was wrong
                return ResponseEntity.status(401).body("Invalid credentials.");
            }

            // Verify the plain text password against the BCrypt hashed password stored in DB
            // passwordEncoder.matches() hashes the plain text and compares with stored hash
            if (!passwordEncoder.matches(password, employee.getPassword())) {
                // Log warning — password does not match
                logger.warn(" Login failed, incorrect password for: {}", username);
                // Return generic message — same as username not found to prevent user enumeration
                return ResponseEntity.status(401).body("Invalid credentials.");
            }

            // Convert the authenticated employee entity to DTO
            // DTO contains id, username and primary role
            AuthResponseDto dto = toDto(employee);

            // Generate a JWT token using the username as the subject
            // Token contains username and expiry — used for subsequent authenticated requests
            String token = JwtUtil.generateToken(dto.getUsername());

            // Log successful login
            logger.info(" Login successful for username: {}", username);
            // Log token generation at DEBUG level
            logger.debug(" Token generated for username: {}", username);

            // Return 200 OK with the JWT token, username and role in the response body
            return ResponseEntity.ok(new LoginResponse(token, dto.getUsername(), dto.getRole()));

        } catch (Exception e) {
            // Log the full exception at ERROR level including stack trace
            logger.error(" Login failed for {}: {}", request.getUsername(), e.getMessage(), e);
            // Return 500 Internal Server Error with the exception message
            return ResponseEntity.status(500).body("Login failed: " + e.getMessage());
        }
    }
}