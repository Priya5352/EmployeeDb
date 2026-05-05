package com.demo.EmployeeDb.advice;

import com.demo.EmployeeDb.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class SuccessResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        String className = returnType.getDeclaringClass().getName();
        return !className.startsWith("org.springdoc")
            && !className.startsWith("org.springframework.boot.actuate");
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        // Already wrapped — don't double-wrap
        if (body instanceof ApiResponse) return body;

        int statusCode = ((ServletServerHttpResponse) response)
                            .getServletResponse().getStatus();

        // Only wrap 2xx success responses
        if (statusCode < 200 || statusCode >= 300) return body;

        String method = "";
        String path = "";

        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            method = attrs.getRequest().getMethod();
            path   = attrs.getRequest().getRequestURI();
        }

        // Detect entity name from path
        String entity = "Record";
        if (path.contains("/employee")) {
            entity = "Employee";
        } else if (path.contains("/department")) {
            entity = "Department";
        } else if (path.contains("/auth")) {
            entity = "User";
        }

        // Build message based on HTTP method and entity
        String message = switch (method) {
            case "POST"   -> entity + " created successfully.";
            case "PUT"    -> entity + " updated successfully.";
            case "DELETE" -> entity + " deleted successfully.";
            default       -> entity + " fetched successfully.";
        };

        if (body instanceof String) {
            return new ApiResponse(statusCode, (String) body, null);
        }

        return new ApiResponse(statusCode, message, body);
    }
}