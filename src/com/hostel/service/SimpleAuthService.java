package com.hostel.service;

import com.hostel.model.*;
import com.hostel.server.FileServer;
import java.util.*;

public class SimpleAuthService implements AuthenticationService {
    private User currentUser;
    private SessionManager sessionManager;
    private int clientCounter = 1;
    private int managerCounter = 1;

    public SimpleAuthService() {
        this.sessionManager = new SessionManager();
    }

    @Override
    public User login(String emailOrPhone, String password, UserType userType) {
        System.out.println("[AUTH] Спроба входу для " + userType + ": " + emailOrPhone);

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[AUTH ERROR] Відсутнє мережеве з'єднання");
            return null;
        }

        // Пошук користувача на сервері
        User user = FileServer.findUserByEmailOrPhone(emailOrPhone);

        if (user == null) {
            System.out.println("[AUTH ERROR] Користувача з такими даними не знайдено");
            return null;
        }

        // Перевірка пароля та типу користувача
        boolean passwordMatch = user.getPassword().equals(password);
        boolean typeMatch = user.getUserType() == userType;

        if (passwordMatch && typeMatch) {
            currentUser = user;
            sessionManager.startSession();

            System.out.println("[AUTH SUCCESS] Успішний вхід: " +
                    (user instanceof Client ? ((Client)user).getFullName() : user.getEmail()));

            return user;
        } else {
            System.out.println("[AUTH ERROR] Невірний пароль або тип користувача");
            return null;
        }
    }

    @Override
    public User register(String email, String phone, String password,
                         UserType userType, String firstName, String lastName) {

        System.out.println("[AUTH] Спроба реєстрації " + userType + ": " + email);

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[AUTH ERROR] Відсутнє мережеве з'єднання");
            return null;
        }

        // Перевірка унікальності email та телефону
        if (FileServer.findUserByEmailOrPhone(email) != null) {
            System.out.println("[AUTH ERROR] Користувач з email " + email + " вже існує");
            return null;
        }

        if (FileServer.findUserByEmailOrPhone(phone) != null) {
            System.out.println("[AUTH ERROR] Користувач з телефоном " + phone + " вже існує");
            return null;
        }

        // Перевірка пароля
        if (password.length() < 6) {
            System.out.println("[AUTH ERROR] Пароль має бути не менше 6 символів");
            return null;
        }

        // Створення користувача
        User newUser;
        if (userType == UserType.CLIENT) {
            newUser = new Client(email, phone, password, firstName, lastName);
            newUser.setId("CLIENT_" + clientCounter++);
        } else {
            newUser = new Manager(email, phone, password);
            newUser.setId("MANAGER_" + managerCounter++);
        }

        // Збереження на сервер
        boolean saved = FileServer.saveUser(newUser);
        if (!saved) {
            System.out.println("[AUTH ERROR] Не вдалося зберегти користувача на сервері");
            return null;
        }

        System.out.println("[AUTH SUCCESS] Користувач успішно зареєстрований: " + newUser.getId());
        return newUser;
    }

    @Override
    public void logout() {
        if (currentUser != null) {
            System.out.println("[AUTH] Завершення сесії для " + currentUser.getEmail());
            sessionManager.endSession();
            currentUser = null;
        }
    }

    @Override
    public boolean isUserAuthenticated() {
        return currentUser != null && sessionManager.isSessionValid();
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
