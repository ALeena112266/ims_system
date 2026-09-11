package com.example.imssystem.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String role;
    private String department;
    private String studentId;
    private String password;

    public User() {
    }

    public User(String name, String email, String role, String department, String studentId, String password) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.department = department;
        this.studentId = studentId;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
