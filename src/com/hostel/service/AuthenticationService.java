package com.hostel.service;

import com.hostel.model.User;
import com.hostel.model.UserType;

public interface AuthenticationService {
    User login(String emailOrPhone, String password, UserType userType);
    User register(String email, String phone, String password,
                  UserType userType, String firstName, String lastName);
    void logout();
    boolean isUserAuthenticated();
    User getCurrentUser();
}
