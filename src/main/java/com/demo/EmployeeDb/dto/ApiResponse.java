package com.demo.EmployeeDb.dto;

import com.fasterxml.jackson.annotation.JsonView;

public class ApiResponse {

    @JsonView({Views.Summary.class, Views.Full.class})
    private int status;

    @JsonView({Views.Summary.class, Views.Full.class})
    private String message;

    @JsonView({Views.Summary.class, Views.Full.class})
    private Object data;

    // Constructor
    public ApiResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Getters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public Object getData() { return data; }
}