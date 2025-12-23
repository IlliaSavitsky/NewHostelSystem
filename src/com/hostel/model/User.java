package com.hostel.model;

public abstract class User {
    private String id;
    private String email;
    private String phone;
    private String password;

    public User(String email, String phone, String password) {
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public abstract UserType getUserType();
}
