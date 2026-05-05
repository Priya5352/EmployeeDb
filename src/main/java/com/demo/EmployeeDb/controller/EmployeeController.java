package com.demo.EmployeeDb.controller;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.demo.EmployeeDb.dto.*;
import com.demo.EmployeeDb.service.EmployeeService;
import com.fasterxml.jackson.annotation.JsonView;
import com.demo.EmployeeDb.dto.Views;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/employee")
public class EmployeeController {

    // service is injected
    private final EmployeeService service;

    // Constructor injection
    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    // Post method for only admins
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> addEmployee(@Valid @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.status(201).body(service.addEmployee(dto));
    }

    @JsonView(Views.Summary.class)
    // Get method for only admins
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees() {
        return ResponseEntity.ok(service.getAllEmployees());
    }

    @JsonView(Views.Full.class)
    // Get by id method for only admins
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEmployeeById(id));
    }

    // Put method for both admins and users
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @Valid
                                            @RequestBody EmployeeRequestDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            try {
                //  Uses service instead of repository — no entity in controller
                EmployeeResponseDto existingEmp = service.getEmployeeByUsername(loggedInUsername);
                if (!existingEmp.getId().equals(id))
                    return ResponseEntity.status(403)
                        .body(Map.of("message", "Access Denied: You can only update your own record. " +
                              "Your employee ID is: " + existingEmp.getId()));
            } catch (RuntimeException e) {
                return ResponseEntity.status(404)
                    .body(Map.of("message", "No employee record found for user: " + loggedInUsername));
            }
        }
        return ResponseEntity.ok(service.updateEmployee(id, dto));
    }

    // Delete method for only admins
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            service.deleteEmployeeById(id);
            return ResponseEntity.ok(Map.of("message", "Employee deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }
    // GET /employee/search?prefix=Jo — Search employees by name prefix — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByNamePrefix(@RequestParam String prefix) {
        return ResponseEntity.ok(service.getEmployeesByNamePrefix(prefix));
    }
}