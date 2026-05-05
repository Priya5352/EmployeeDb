package com.demo.EmployeeDb.controller;

import com.demo.EmployeeDb.dto.DepartmentRequestDto;
import com.demo.EmployeeDb.dto.DepartmentResponseDto;
import com.demo.EmployeeDb.dto.EmployeeResponseDto;
import com.demo.EmployeeDb.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

// REST controller for department operations
// All endpoints prefixed with /department
@RestController
@RequestMapping("/department")
public class DepartmentController {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    // Only service injected — no repository in controller
    private final DepartmentService departmentService;

    // Constructor injection
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // POST /department — Create a new department — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<DepartmentResponseDto> addDepartment(@Valid @RequestBody DepartmentRequestDto dto) {
        logger.info("department called");
        return ResponseEntity.status(201).body(departmentService.addDepartment(dto));
    }

    // GET /department — Get all departments — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<DepartmentResponseDto>> getAllDepartments() {
        logger.info("department called");
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    // GET /department/{id} — Get department by ID — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDto> getDepartmentById(@PathVariable Long id) {
        logger.info("GET /department/{} called", id);
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    // GET /department/{id}/employee — Get all employees in a department by ID — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}/employee")
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByDepartment(@PathVariable Long id) {
        logger.info(" GET /department/{}/employees called", id);
        return ResponseEntity.ok(departmentService.getEmployeesByDepartmentId(id));
    }

    // GET /department/search?name=Engineering — Get all employees by department name — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDto>> getEmployeesByDepartmentName(@RequestParam String name) {
        logger.info("GET /department/search?name={} called", name);
        return ResponseEntity.ok(departmentService.getEmployeeIdsByDepartmentName(name));
    }

    // PUT /department/{id} — Update department by ID — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @Valid
                                              @RequestBody DepartmentRequestDto dto) {
        logger.info(" PUT /department/{} called", id);

        try {
            DepartmentResponseDto updated = departmentService.updateDepartment(id, dto);
            return ResponseEntity.ok(updated);

        } catch (RuntimeException e) {
            logger.warn(" Department not found for update: {}", id);
            return ResponseEntity.status(404).body("Department not found: " + id);
        }
    }

    // DELETE /department/{id} — Delete department by ID — Admin only
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        logger.info(" DELETE /department/{} called", id);

        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok("Department deleted successfully.");

        } catch (RuntimeException e) {
            logger.warn(" Department not found for deletion: {}", id);
            return ResponseEntity.status(404).body("Department not found: " + id);
        }
    }
}