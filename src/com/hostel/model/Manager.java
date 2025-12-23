package com.hostel.model;

public class Manager extends User {

    public Manager(String email, String phone, String password) {
        super(email, phone, password);
    }

    @Override
    public UserType getUserType() {
        return UserType.MANAGER;
    }
}