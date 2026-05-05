package com.demo.EmployeeDb.security;

import com.demo.EmployeeDb.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.demo.EmployeeDb.entity.Employee;
import com.demo.EmployeeDb.repository.EmployeeRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Marks this class as a Spring component so it is auto-detected and registered as a bean
@Component
public class JwtFilter extends OncePerRequestFilter {

    // Logger instance for this class
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    // Repository to fetch employee details from the database
    private final EmployeeRepository employeeRepository;

    // ObjectMapper to write ApiResponse as JSON directly to response
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Constructor injection
    public JwtFilter(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // OncePerRequestFilter ensures this filter runs exactly once per HTTP request
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {

        // Log entry point of the filter at TRACE level
        logger.trace(" doFilterInternal() called");

        try {
            // Get the request URL path to check if it needs JWT validation
            String path = request.getServletPath();
            logger.debug(" Request path: {}", path);

            // Skip JWT validation for /auth endpoints — register and login are public
            if (path.startsWith("/auth")) {
                logger.debug(" Skipping JWT check for auth path");
                filterChain.doFilter(request, response);
                return;
            }

            // Read the Authorization header from the incoming HTTP request
            String header = request.getHeader("Authorization");
            logger.debug(" Authorization header present: {}", header != null);

            // Check if the header exists and starts with "Bearer "
            if (header != null && header.startsWith("Bearer ")) {

                // Extract the JWT token by removing the "Bearer " prefix
                String token = header.substring(7).trim();
                logger.debug("JWT token extracted");

                // Validate the token — checks signature and expiry
                if (JwtUtil.validateToken(token)) {

                    // Extract the username stored inside the JWT token payload
                    String username = JwtUtil.extractUsername(token);
                    logger.info(" Valid token for username: {}", username);

                    // Load the employee from the database using the username from the token
                    Optional<Employee> employeeOpt = employeeRepository.findByUsername(username);

                    if (employeeOpt.isPresent()) {
                        Employee employee = employeeOpt.get();

                        // Map the employee's roles into Spring Security authorities
                        List<SimpleGrantedAuthority> authorities = employee.getRoles()
                                .stream()
                                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                                .collect(Collectors.toList());

                        logger.info(" Roles loaded for {}: {}", username, authorities);

                        // Create and set authentication in SecurityContext
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        username, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(auth);

                    } else {
                        // Token is valid but no employee found in DB for that username
                        logger.warn(" No employee found for username: {}", username);
                        writeErrorResponse(response, 401, "Unauthorized: Employee not found for this token.");
                        return;
                    }

                } else {
                    // ✅ Token failed validation — expired or tampered
                    logger.warn(" Invalid or expired JWT token");
                    writeErrorResponse(response, 401, "Invalid or expired token. Please login again.");
                    return;
                }

            } else if (!path.startsWith("/swagger-ui") && !path.startsWith("/v3/api-docs")) {
                // ✅ No token provided at all for a protected endpoint
                logger.warn(" No Authorization header found for path: {}", path);
                writeErrorResponse(response, 401, "Authorization token is missing. Please login first.");
                return;
            }

        } catch (Exception e) {
            logger.error("JWT Filter error: {}", e.getMessage(), e);
            writeErrorResponse(response, 500, "An unexpected error occurred: " + e.getMessage());
            return;
        }

        // Always pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }

    // ✅ NEW — writes ApiResponse directly to HTTP response as JSON
    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ApiResponse apiResponse = new ApiResponse(status, message, null);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}