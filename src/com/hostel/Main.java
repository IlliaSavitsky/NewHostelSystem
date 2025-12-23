package com.hostel;

import com.hostel.utils.FileManager;
import com.hostel.utils.InputValidator;
import com.hostel.model.*;
import com.hostel.service.*;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static StartupManager startupManager;
    private static AuthenticationService authService;
    private static HostelService hostelService;
    private static BookingService bookingService;
    private static ClientInterface clientInterface;
    private static ManagerMenu managerMenu;
    private static AuthenticationMenu authMenu;
    private static User currentUser;
    private static boolean isRunning = true;
    private static boolean isAnonymousMode = false;

    public static void main(String[] args) {
        initializeSystem();
        runMainLoop();
        shutdownSystem();
    }

    private static void initializeSystem() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("СИСТЕМА УПРАВЛІННЯ ХОСТЕЛОМ v1.0");
        System.out.println("Розробник: Савицький Ілля, ПГ-41");
        System.out.println("=".repeat(60) + "\n");

        // Ініціалізація файлової системи
        FileManager.initializeDirectories();

        // Ініціалізація сервісів
        authService = new SimpleAuthService();
        bookingService = new SimpleBookingService();
        hostelService = new SimpleHostelService();

        // Ініціалізація меню
        clientInterface = new ClientInterface(scanner, hostelService, bookingService, authService);
        managerMenu = new ManagerMenu(scanner, hostelService, bookingService, authService);
        authMenu = new AuthenticationMenu(scanner, authService);  // Це вже є

        // Запуск системи
        startupManager = new StartupManager();
        startupManager.startApplication();

        // Ініціалізація тестових даних (якщо потрібно)
        initializeTestData();

        // Створення резервної копії
        FileManager.createFullBackup();
    }

    private static void initializeTestData() {
        // Перевірка, чи є дані на сервері
        var stats = com.hostel.server.FileServer.getServerStats();
        int totalUsers = (int) stats.get("totalUsers");

        if (totalUsers == 0) {
            System.out.println("[SETUP] Сервер порожній. Створення тестових даних...");
            createTestData();
        } else {
            System.out.println("[SETUP] Дані вже існують на сервері (" + totalUsers + " користувачів)");
        }
    }

    private static void createTestData() {
        // Тестовий менеджер
        User manager = authService.register(
                "manager@hostel.com",
                "+380671234567",
                "manager123",
                UserType.MANAGER,
                "Адмін",
                "Менеджер"
        );

        if (manager != null) {
            System.out.println("[SETUP] Створено тестового менеджера");

            // Тестовий хостел
            Hostel hostel = hostelService.createHostel(
                    "Готель 'Центральний'",
                    "вул. Центральна, 1",
                    "Київ",
                    manager.getId()
            );

            if (hostel != null) {
                System.out.println("[SETUP] Створено тестовий хостел");

                // Тестові кімнати
                if (bookingService instanceof SimpleBookingService) {
                    SimpleBookingService simpleService = (SimpleBookingService) bookingService;

                    Room room1 = new Room(hostel.getId(), Room.RoomType.PRIVATE, 2, 150.0);
                    Room room2 = new Room(hostel.getId(), Room.RoomType.SHARED, 4, 80.0);
                    Room room3 = new Room(hostel.getId(), Room.RoomType.PRIVATE, 3, 200.0);

                    simpleService.addRoom(room1);
                    simpleService.addRoom(room2);
                    simpleService.addRoom(room3);

                    System.out.println("[SETUP] Створено 3 тестові кімнати");
                }
            }
        }

        // Тестовий клієнт
        User client = authService.register(
                "client@example.com",
                "+380501234567",
                "client123",
                UserType.CLIENT,
                "Тестовий",
                "Клієнт"
        );

        if (client != null) {
            System.out.println("[SETUP] Створено тестового клієнта");
        }

        System.out.println("[SETUP] Тестові дані створено успішно");
    }

    private static void runMainLoop() {
        while (isRunning) {
            if (currentUser == null && !isAnonymousMode) {
                showMainMenu();
            } else if (isAnonymousMode) {
                showAnonymousMenu();
            } else if (currentUser.getUserType() == UserType.CLIENT) {
                showClientMenu();
            } else {
                showManagerMenu();
            }
        }
    }

    private static void showMainMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ГОЛОВНЕ МЕНЮ " + NetworkMonitor.getNetworkStatus());
        System.out.println("=".repeat(40));

        System.out.println("1. Увійти як клієнт");
        System.out.println("2. Увійти як менеджер");
        System.out.println("3. Зареєструватись як клієнт");
        System.out.println("4. Зареєструватись як менеджер");
        System.out.println("5. Працювати як гість (анонімний режим)");
        System.out.println("6. Перевірити мережеве з'єднання");
        System.out.println("7. Статистика системи");
        System.out.println("0. Вийти з програми");

        System.out.print("\nОберіть опцію: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> loginUser(UserType.CLIENT);
            case "2" -> loginUser(UserType.MANAGER);
            case "3" -> registerUser(UserType.CLIENT);
            case "4" -> registerUser(UserType.MANAGER);
            case "5" -> workAsAnonymous();
            case "6" -> checkNetworkStatus();
            case "7" -> showSystemStats();
            case "0" -> isRunning = false;
            default -> System.out.println("[ERROR] Невірний вибір");
        }
    }

    private static void showAnonymousMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("АНОНІМНИЙ РЕЖИМ " + NetworkMonitor.getNetworkStatus());
        System.out.println("=".repeat(40));

        System.out.println("Ви переглядаєте систему як гість.");
        System.out.println("Деякі функції обмежені (перегляд бронювань).");

        System.out.println("\n1. Пошук хостелів та бронювання");
        System.out.println("2. Перейти до інтерфейсу менеджера");
        System.out.println("3. Увійти в акаунт");
        System.out.println("4. Зареєструватися");
        System.out.println("5. Вийти з анонімного режиму");
        System.out.println("0. Вийти з програми");

        System.out.print("\nОберіть опцію: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                clientInterface.setCurrentUser(null);
                clientInterface.searchAndBookHostel();
            }
            case "2" -> {
                isAnonymousMode = false;
                loginUser(UserType.MANAGER);
            }
            case "3" -> {
                isAnonymousMode = false;
                showLoginOptions();
            }
            case "4" -> {
                isAnonymousMode = false;
                showRegistrationOptions();
            }
            case "5" -> exitAnonymousMode();
            case "0" -> isRunning = false;
            default -> System.out.println("[ERROR] Невірний вибір");
        }
    }

    private static void showClientMenu() {
        clientInterface.setCurrentUser(currentUser);
        clientInterface.displayMainMenu();

        // Якщо користувач вийшов з інтерфейсу клієнта
        if (!authService.isUserAuthenticated()) {
            currentUser = null;
        }
    }

    private static void showManagerMenu() {
        managerMenu.setCurrentManager(currentUser);
        managerMenu.display();

        // Якщо менеджер вийшов з меню
        if (!authService.isUserAuthenticated()) {
            currentUser = null;
        }
    }

    private static void loginUser(UserType userType) {
        System.out.println("\n--- ВХІД У СИСТЕМУ ---");

        System.out.print("Електронна пошта або телефон: ");
        String emailOrPhone = scanner.nextLine().trim();

        System.out.print("Пароль: ");
        String password = scanner.nextLine().trim();

        User user = authService.login(emailOrPhone, password, userType);

        if (user != null) {
            currentUser = user;
            System.out.println("\n✅ Успішний вхід!");

            if (userType == UserType.CLIENT) {
                Client client = (Client) user;
                System.out.println("Ласкаво просимо, " + client.getFullName() + "!");
            } else {
                System.out.println("Ласкаво просимо, менеджер " + user.getEmail() + "!");
            }
        } else {
            System.out.println("\n❌ Невірні дані для входу");
        }
    }

    private static void registerUser(UserType userType) {
        System.out.println("\n--- РЕЄСТРАЦІЯ ---");

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[ERROR] Реєстрація недоступна в офлайн-режимі");
            return;
        }

        String email, phone, password, firstName = "", lastName = "";

        // Email
        System.out.print("Електронна пошта: ");
        email = scanner.nextLine().trim();

        // Телефон
        System.out.print("Телефон (+380XXXXXXXXX): ");
        phone = scanner.nextLine().trim();

        // Пароль
        System.out.print("Пароль (мінімум 6 символів): ");
        password = scanner.nextLine().trim();

        if (password.length() < 6) {
            System.out.println("[ERROR] Пароль має бути не менше 6 символів");
            return;
        }

        System.out.print("Підтвердження пароля: ");
        String confirmPassword = scanner.nextLine().trim();

        if (!password.equals(confirmPassword)) {
            System.out.println("[ERROR] Паролі не співпадають");
            return;
        }

        // Додаткові дані для клієнта
        if (userType == UserType.CLIENT) {
            System.out.print("Ім'я: ");
            firstName = scanner.nextLine().trim();

            System.out.print("Прізвище: ");
            lastName = scanner.nextLine().trim();
        }

        User user = authService.register(email, phone, password, userType, firstName, lastName);

        if (user != null) {
            System.out.println("\n✅ Реєстрація успішна!");

            // Автоматичний вхід
            System.out.print("Увійти зараз? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                currentUser = user;
                System.out.println("[INFO] Автоматичний вхід виконано");
            }
        } else {
            System.out.println("\n❌ Помилка реєстрації");
        }
    }

    private static void workAsAnonymous() {
        isAnonymousMode = true;
        currentUser = null;
        System.out.println("[INFO] Анонімний режим активовано");
    }

    private static void exitAnonymousMode() {
        System.out.print("\nВийти з анонімного режиму? (так/ні): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("так") || response.equals("yes") || response.equals("y")) {
            isAnonymousMode = false;
            System.out.println("[INFO] Анонімний режим вимкнено");
        }
    }

    private static void checkNetworkStatus() {
        boolean isConnected = NetworkMonitor.isNetworkAvailable();

        System.out.println("\n--- СТАТУС МЕРЕЖІ ---");
        if (isConnected) {
            System.out.println("✅ Мережеве з'єднання: АКТИВНЕ");
        } else {
            System.out.println("❌ Мережеве з'єднання: ВІДСУТНЄ");
            System.out.println("💡 Порада: Деякі функції будуть обмежені");
        }

        // Імітація проблеми з мережею (для тестування)
        System.out.print("\nІмітувати проблему з мережею? (так/ні): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("так") || response.equals("yes") || response.equals("y")) {
            NetworkMonitor.simulateNetworkIssue();
        }
    }

    private static void showSystemStats() {
        com.hostel.server.FileServer.printServerStats();

        System.out.println("\n--- СИСТЕМНІ ПОКАЗНИКИ ---");
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        long memoryMax = runtime.maxMemory();

        System.out.println("Використання пам'яті: " +
                String.format("%.2f", memoryUsed / 1024.0 / 1024.0) + " MB / " +
                String.format("%.2f", memoryMax / 1024.0 / 1024.0) + " MB");

        System.out.println("Час роботи системи: " +
                (System.currentTimeMillis() - startupManager.getStartupTime()) + " мс");

        System.out.println("Черга синхронізації: " + com.hostel.server.SyncManager.getQueueSize() + " завдань");
    }

    private static void showLoginOptions() {
        System.out.println("\n--- ВХІД У СИСТЕМУ ---");

        System.out.println("1. Увійти як клієнт");
        System.out.println("2. Увійти як менеджер");
        System.out.println("3. Повернутись назад");

        System.out.print("\nОберіть опцію: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                User user = authMenu.authenticateClient();
                if (user != null) {
                    currentUser = user;
                    isAnonymousMode = false;
                }
            }
            case "2" -> {
                User user = authMenu.authenticateManager();
                if (user != null) {
                    currentUser = user;
                    isAnonymousMode = false;
                }
            }
            case "3" -> {}
            default -> System.out.println("[ERROR] Невірний вибір");
        }
    }

    private static void showRegistrationOptions() {
        System.out.println("\n--- РЕЄСТРАЦІЯ ---");

        System.out.println("1. Зареєструватись як клієнт");
        System.out.println("2. Зареєструватись як менеджер");
        System.out.println("3. Повернутись назад");

        System.out.print("\nОберіть опцію: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                // Викликаємо метод реєстрації клієнта з AuthenticationMenu
                authMenu.performClientRegistration();
                if (authMenu.getCurrentUser() != null) {
                    currentUser = authMenu.getCurrentUser();
                    isAnonymousMode = false;
                }
            }
            case "2" -> {
                // Викликаємо метод реєстрації менеджера з AuthenticationMenu
                authMenu.performManagerRegistration();
                if (authMenu.getCurrentUser() != null) {
                    currentUser = authMenu.getCurrentUser();
                    isAnonymousMode = false;
                }
            }
            case "3" -> {}
            default -> System.out.println("[ERROR] Невірний вибір");
        }
    }

    private static void showRegistrationOptions() {
        System.out.println("\n1. Зареєструватись як клієнт");
        System.out.println("2. Зареєструватись як менеджер");
        System.out.println("3. Повернутись назад");

        System.out.print("\nОберіть опцію: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> registerUser(UserType.CLIENT);
            case "2" -> registerUser(UserType.MANAGER);
            case "3" -> {}
            default -> System.out.println("[ERROR] Невірний вибір");
        }
    }

    private static void shutdownSystem() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ЗАВЕРШЕННЯ РОБОТИ СИСТЕМИ");
        System.out.println("=".repeat(60));

        startupManager.shutdown();

        // Логування завершення роботи
        FileManager.logEvent("Система зупинена", "INFO");

        // Очищення старих бекапів
        FileManager.cleanupOldBackups(7); // Зберігати бекапи за останні 7 днів

        System.out.println("\nДякуємо за використання системи управління хостелом!");
        System.out.println("До побачення!");

        scanner.close();
    }
}
