package com.hostel.service;

import com.hostel.model.*;
import java.util.Scanner;

public class AuthenticationMenu {
    private final Scanner scanner;
    private final AuthenticationService authService;
    private User currentUser;

    public AuthenticationMenu(Scanner scanner, AuthenticationService authService) {
        this.scanner = scanner;
        this.authService = authService;
    }

    public User authenticateManager() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("АВТЕНТИФІКАЦІЯ МЕНЕДЖЕРА");
        System.out.println("=".repeat(50));

        System.out.println("[INFO] Для доступу до панелі управління потрібна автентифікація менеджера.");

        boolean stayInMenu = true;

        while (stayInMenu && currentUser == null) {
            showManagerAuthOptions();

            System.out.print("\nОберіть опцію: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> performManagerLogin();
                case "2" -> performManagerRegistration();
                case "3" -> {
                    System.out.println("[INFO] Повернення до головного меню...");
                    stayInMenu = false;
                }
                case "0" -> {
                    System.out.println("[INFO] Вихід з програми.");
                    System.exit(0);
                }
                default -> System.out.println("[ERROR] Невірний вибір. Спробуйте ще раз.");
            }
        }

        return currentUser;
    }

    private void showManagerAuthOptions() {
        System.out.println("\n-- АВТЕНТИФІКАЦІЯ ДЛЯ МЕНЕДЖЕРА --");
        System.out.println("1. Увійти як менеджер");
        System.out.println("2. Зареєструвати нового менеджера");
        System.out.println("3. Повернутись назад");
        System.out.println("0. Вийти з програми");

        System.out.println("\n[INFO] Тільки менеджери мають доступ до панелі управління.");
        System.out.println("[INFO] Статус мережі: " + NetworkMonitor.getNetworkStatus());
    }

    private void performManagerLogin() {
        System.out.println("\n--- ВХІД ЯК МЕНЕДЖЕР ---");

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[ERROR] Відсутнє мережеве з'єднання");
            System.out.println("[INFO] Автентифікація недоступна в офлайн-режимі");
            return;
        }

        System.out.print("Електронна пошта або телефон менеджера: ");
        String emailOrPhone = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        User user = authService.login(emailOrPhone, password, UserType.MANAGER);

        if (user != null) {
            currentUser = user;
            System.out.println("\n✅ УСПІШНИЙ ВХІД ЯК МЕНЕДЖЕР!");
            System.out.println("Ласкаво просимо, " + user.getEmail() + "!");

            System.out.println("\n[INFO] Завантаження даних менеджера...");
            System.out.println("[INFO] Готово до роботи.");
        } else {
            System.out.println("\n❌ Невірні дані для входу.");

            System.out.print("Бажаєте створити новий акаунт менеджера? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                performManagerRegistration();
            }
        }
    }

    private void performManagerRegistration() {
        System.out.println("\n--- РЕЄСТРАЦІЯ НОВОГО МЕНЕДЖЕРА ---");

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[ERROR] Реєстрація недоступна в офлайн-режимі");
            return;
        }

        System.out.println("[INFO] Для реєстрації менеджера потрібні спеціальні права.");
        System.out.println("[INFO] Якщо ви не маєте прав, зверніться до адміністратора.\n");

        System.out.print("Електронна пошта: ");
        String email = scanner.nextLine().trim();

        System.out.print("Телефон (+380XXXXXXXXX): ");
        String phone = scanner.nextLine().trim();

        System.out.print("Пароль (мінімум 8 символів): ");
        String password = scanner.nextLine().trim();

        if (password.length() < 8) {
            System.out.println("[ERROR] Пароль занадто короткий. Мінімум 8 символів.");
            return;
        }

        System.out.print("Підтвердження пароля: ");
        String confirmPassword = scanner.nextLine().trim();

        if (!password.equals(confirmPassword)) {
            System.out.println("[ERROR] Паролі не співпадають.");
            return;
        }

        User manager = authService.register(email, phone, password, UserType.MANAGER, "Manager", "Account");

        if (manager != null) {
            System.out.println("\n✅ АКАУНТ МЕНЕДЖЕРА СТВОРЕНО!");
            System.out.println("Email: " + email);
            System.out.println("Телефон: " + phone);

            System.out.print("\nБажаєте увійти зараз? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                currentUser = manager;
                System.out.println("✅ Автоматичний вхід виконано!");
                System.out.println("Ласкаво просимо, менеджер " + email + "!");
            }
        } else {
            System.out.println("\n❌ Менеджер з такими даними вже існує.");
            System.out.println("[INFO] Спробуйте інші дані або увійдіть в існуючий акаунт.");
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void logout() {
        if (currentUser != null) {
            authService.logout();
            currentUser = null;
            System.out.println("[INFO] Вихід з акаунта виконано.");
        }
    }

    // Метод для автентифікації клієнта (якщо потрібно)
    public User authenticateClient() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("АВТЕНТИФІКАЦІЯ КЛІЄНТА");
        System.out.println("=".repeat(50));

        System.out.println("[INFO] Для повного доступу до функцій потрібна автентифікація.");

        boolean stayInMenu = true;

        while (stayInMenu && currentUser == null) {
            showClientAuthOptions();

            System.out.print("\nОберіть опцію: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> performClientLogin();
                case "2" -> performClientRegistration();
                case "3" -> {
                    System.out.println("[INFO] Продовження без входу...");
                    stayInMenu = false;
                }
                case "0" -> {
                    System.out.println("[INFO] Вихід з програми.");
                    System.exit(0);
                }
                default -> System.out.println("[ERROR] Невірний вибір.");
            }
        }

        return currentUser;
    }

    private void showClientAuthOptions() {
        System.out.println("\n-- АВТЕНТИФІКАЦІЯ ДЛЯ КЛІЄНТА --");
        System.out.println("1. Увійти як клієнт");
        System.out.println("2. Зареєструвати нового клієнта");
        System.out.println("3. Продовжити без входу");
        System.out.println("0. Вийти з програми");

        System.out.println("\n[INFO] Для бронювання та перегляду історії потрібен акаунт.");
        System.out.println("[INFO] Статус мережі: " + NetworkMonitor.getNetworkStatus());
    }

    private void performClientLogin() {
        System.out.println("\n--- ВХІД ЯК КЛІЄНТ ---");

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[ERROR] Відсутнє мережеве з'єднання");
            System.out.println("[INFO] Автентифікація недоступна в офлайн-режимі");
            return;
        }

        System.out.print("Електронна пошта або телефон: ");
        String emailOrPhone = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        User user = authService.login(emailOrPhone, password, UserType.CLIENT);

        if (user != null) {
            currentUser = user;
            System.out.println("\n✅ УСПІШНИЙ ВХІД!");

            if (user instanceof Client) {
                Client client = (Client) user;
                System.out.println("Ласкаво просимо, " + client.getFullName() + "!");
            }

            System.out.println("\n[INFO] Завантаження ваших бронювань...");
        } else {
            System.out.println("\n❌ Невірні дані для входу.");

            System.out.print("Бажаєте зареєструватися? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                performClientRegistration();
            }
        }
    }

    private void performClientRegistration() {
        System.out.println("\n--- РЕЄСТРАЦІЯ КЛІЄНТА ---");

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[ERROR] Реєстрація недоступна в офлайн-режимі");
            return;
        }

        System.out.println("[INFO] Для реєстрації заповніть всі поля.\n");

        System.out.print("Електронна пошта: ");
        String email = scanner.nextLine().trim();

        System.out.print("Телефон (+380XXXXXXXXX): ");
        String phone = scanner.nextLine().trim();

        System.out.print("Пароль (мінімум 6 символів): ");
        String password = scanner.nextLine().trim();

        if (password.length() < 6) {
            System.out.println("[ERROR] Пароль занадто короткий. Мінімум 6 символів.");
            return;
        }

        System.out.print("Підтвердження пароля: ");
        String confirmPassword = scanner.nextLine().trim();

        if (!password.equals(confirmPassword)) {
            System.out.println("[ERROR] Паролі не співпадають.");
            return;
        }

        System.out.print("Ім'я: ");
        String firstName = scanner.nextLine().trim();

        System.out.print("Прізвище: ");
        String lastName = scanner.nextLine().trim();

        User client = authService.register(email, phone, password, UserType.CLIENT, firstName, lastName);

        if (client != null) {
            System.out.println("\n✅ РЕЄСТРАЦІЯ УСПІШНА!");
            System.out.println("Email: " + email);
            System.out.println("Телефон: " + phone);
            System.out.println("Ім'я: " + firstName + " " + lastName);

            System.out.print("\nБажаєте увійти зараз? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                currentUser = client;
                System.out.println("✅ Автоматичний вхід виконано!");
                System.out.println("Ласкаво просимо, " + firstName + " " + lastName + "!");
            }
        } else {
            System.out.println("\n❌ Користувач з такими даними вже існує.");
            System.out.println("[INFO] Спробуйте інші дані або увійдіть в існуючий акаунт.");
        }
    }

    public boolean isAuthenticated() {
        return currentUser != null && authService.isUserAuthenticated();
    }

    public void clearCurrentUser() {
        this.currentUser = null;
    }
}
