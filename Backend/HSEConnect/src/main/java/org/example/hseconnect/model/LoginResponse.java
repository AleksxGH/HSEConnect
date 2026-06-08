package org.example.hseconnect.model;

public class LoginResponse {
    private String userId;
    private String email;

    public LoginResponse(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}