package com.hostel.service;

import com.hostel.model.*;
import com.hostel.server.FileServer;
import java.util.List;
import java.util.Scanner;
import com.hostel.server.SyncManager;

public class ManagerMenu {
    private final Scanner scanner;
    private final HostelService hostelService;
    private final BookingService bookingService;
    private final AuthenticationService authService;
    private final HostelDetailsWindow hostelDetailsWindow;
    private User currentManager;

    public ManagerMenu(Scanner scanner, HostelService hostelService,
                       BookingService bookingService, AuthenticationService authService) {
        this.scanner = scanner;
        this.hostelService = hostelService;
        this.bookingService = bookingService;
        this.authService = authService;
        this.hostelDetailsWindow = new HostelDetailsWindow(scanner, hostelService, bookingService);
    }

    public void setCurrentManager(User manager) {
        this.currentManager = manager;
    }

    public void display() {
        long startTime = System.currentTimeMillis();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("ІНТЕРФЕЙС МЕНЕДЖЕРА " + NetworkMonitor.getNetworkStatus());
        System.out.println("=".repeat(50));

        long loadTime = System.currentTimeMillis() - startTime;
        if (loadTime > 3000) {
            System.out.println("[WARNING] Завантаження зайняло " + loadTime + "мс (>3 сек)");
        }

        if (currentManager != null) {
            System.out.println("Менеджер: " + currentManager.getEmail());
        }

        boolean stayInMenu = true;

        while (stayInMenu && currentManager != null) {
            showMenuOptions();

            System.out.print("\nОберіть опцію: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> createHostel();
                case "2" -> manageHostels();
                case "3" -> viewAllBookings();
                case "4" -> viewServerStats();
                case "5" -> switchToClientInterface();
                case "6" -> logout();
                case "0" -> stayInMenu = false;
                case "sync" -> SyncManager.forceSync(); // Прихована команда
                case "network" -> toggleNetwork(); // Прихована команда
                default -> System.out.println("[ERROR] Невірний вибір");
            }
        }
    }

    private void showMenuOptions() {
        System.out.println("\n-- ОСНОВНІ ОПЦІЇ --");
        System.out.println("1. Створити хостел");
        System.out.println("2. Управління моїми хостелами");
        System.out.println("3. Перегляд всіх бронювань");
        System.out.println("4. Статистика сервера");
        System.out.println("5. Перейти до інтерфейсу клієнта");
        System.out.println("6. Вийти з акаунта");
        System.out.println("0. Повернутись назад");
    }

    private void createHostel() {
        System.out.println("\n--- СТВОРЕННЯ НОВОГО ХОСТЕЛУ ---");

        System.out.print("Назва хостелу: ");
        String name = scanner.nextLine().trim();

        System.out.print("Адреса: ");
        String address = scanner.nextLine().trim();

        System.out.print("Місто: ");
        String city = scanner.nextLine().trim();

        Hostel hostel = hostelService.createHostel(name, address, city, currentManager.getId());

        if (hostel != null) {
            System.out.println("[SUCCESS] Хостел створено!");
            System.out.println("ID: " + hostel.getId());
            System.out.println("Назва: " + hostel.getName());
            System.out.println("Адреса: " + hostel.getAddress());
            System.out.println("Місто: " + hostel.getCity());

            // Меню додавання кімнат
            boolean addRooms = true;
            while (addRooms) {
                System.out.print("\nДодати кімнату до цього хостелу? (так/ні): ");
                String response = scanner.nextLine().trim().toLowerCase();

                if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                    addRoomToHostel(hostel);
                } else {
                    addRooms = false;
                }
            }
        } else {
            System.out.println("[ERROR] Помилка при створенні хостелу");
        }
    }

    private void addRoomToHostel(Hostel hostel) {
        System.out.println("\n--- ДОДАВАННЯ КІМНАТИ ---");

        try {
            System.out.print("Тип кімнати (1 - Приватна, 2 - Спільна): ");
            int typeChoice = Integer.parseInt(scanner.nextLine().trim());
            Room.RoomType roomType = (typeChoice == 1) ? Room.RoomType.PRIVATE : Room.RoomType.SHARED;

            System.out.print("Кількість місць (1-20): ");
            int capacity = Integer.parseInt(scanner.nextLine().trim());

            if (capacity < 1 || capacity > 20) {
                System.out.println("[ERROR] Кількість місць має бути від 1 до 20");
                return;
            }

            System.out.print("Ціна за годину (грн, 1-1000): ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            if (price < 1 || price > 1000) {
                System.out.println("[ERROR] Ціна має бути від 1 до 1000 грн");
                return;
            }

            Room room = new Room(hostel.getId(), roomType, capacity, price);

            if (bookingService instanceof SimpleBookingService) {
                ((SimpleBookingService) bookingService).addRoom(room);
                System.out.println("[SUCCESS] Кімната додана!");
                System.out.println("ID кімнати: " + room.getId());
                System.out.println("Тип: " + (room.getType() == Room.RoomType.PRIVATE ? "Приватна" : "Спільна"));
                System.out.println("Місць: " + room.getCapacity());
                System.out.println("Ціна: " + room.getPricePerHour() + " грн/год");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат даних");
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void manageHostels() {
        System.out.println("\n--- УПРАВЛІННЯ ХОСТЕЛАМИ ---");

        List<Hostel> hostels = hostelService.getHostelsByManager(currentManager.getId());

        if (hostels.isEmpty()) {
            System.out.println("[INFO] У вас немає хостелів");
            System.out.println("[ACTION] Створіть хостел для початку роботи");
            return;
        }

        System.out.println("[INFO] Ваші хостели (" + hostels.size() + "):");

        for (int i = 0; i < hostels.size(); i++) {
            Hostel hostel = hostels.get(i);
            String status = hostel.isActive() ? "✅ Активний" : "⏸️ Призупинений";
            System.out.println((i + 1) + ". " + hostel.getName());
            System.out.println("   Адреса: " + hostel.getAddress() + ", " + hostel.getCity());
            System.out.println("   Статус: " + status);
            System.out.println("   ID: " + hostel.getId());
        }

        System.out.print("\nОберіть хостел для управління (або 0 для відміни): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 0) {
                System.out.println("[INFO] Операцію відмінено");
                return;
            }

            if (choice > 0 && choice <= hostels.size()) {
                Hostel selectedHostel = hostels.get(choice - 1);
                manageSpecificHostel(selectedHostel);
            } else {
                System.out.println("[ERROR] Невірний вибір");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void manageSpecificHostel(Hostel hostel) {
        System.out.println("\n--- УПРАВЛІННЯ ХОСТЕЛОМ: " + hostel.getName() + " ---");

        boolean managing = true;

        while (managing) {
            System.out.println("\nОберіть дію:");
            System.out.println("1. Переглянути детальну інформацію");
            System.out.println("2. Редагувати інформацію");
            System.out.println("3. Переглянути активні бронювання");
            System.out.println("4. Переглянути статистику");
            System.out.println("5. Змінити статус хостелу");
            System.out.println("6. Додати кімнату");
            System.out.println("7. Видалити хостел");
            System.out.println("0. Повернутись назад");

            System.out.print("\nВаш вибір: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> hostelDetailsWindow.displayHostelDetails(hostel);
                case "2" -> editHostelInformation(hostel);
                case "3" -> viewHostelBookings(hostel);
                case "4" -> viewHostelStatistics(hostel);
                case "5" -> changeHostelStatus(hostel);
                case "6" -> addRoomToHostel(hostel);
                case "7" -> deleteHostel(hostel);
                case "0" -> managing = false;
                default -> System.out.println("[ERROR] Невірний вибір");
            }
        }
    }

    private void editHostelInformation(Hostel hostel) {
        System.out.println("\n--- РЕДАГУВАННЯ ХОСТЕЛУ ---");

        System.out.println("Поточна інформація:");
        System.out.println("1. Назва: " + hostel.getName());
        System.out.println("2. Адреса: " + hostel.getAddress());
        System.out.println("3. Місто: " + hostel.getCity());

        System.out.print("\nОберіть поле для редагування (1-3, або 0 для відміни): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            switch (choice) {
                case 0 -> {
                    System.out.println("[INFO] Редагування відмінено");
                    return;
                }
                case 1 -> {
                    System.out.print("Нова назва: ");
                    String newName = scanner.nextLine().trim();
                    hostel.setName(newName);
                }
                case 2 -> {
                    System.out.print("Нова адреса: ");
                    String newAddress = scanner.nextLine().trim();
                    hostel.setAddress(newAddress);
                }
                case 3 -> {
                    System.out.print("Нове місто: ");
                    String newCity = scanner.nextLine().trim();
                    hostel.setCity(newCity);
                }
                default -> {
                    System.out.println("[ERROR] Невірний вибір");
                    return;
                }
            }

            boolean success = hostelService.updateHostel(hostel);

            if (success) {
                System.out.println("[SUCCESS] Зміни збережено");
            } else {
                System.out.println("[ERROR] Не вдалося зберегти зміни");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void viewHostelBookings(Hostel hostel) {
        System.out.println("\n--- БРОНЮВАННЯ ХОСТЕЛУ ---");

        List<Booking> bookings = bookingService.getHostelBookings(hostel.getId());

        if (bookings.isEmpty()) {
            System.out.println("[INFO] Бронювань немає");
            return;
        }

        System.out.println("Всього бронювань: " + bookings.size());

        int active = 0, completed = 0, cancelled = 0;
        double totalRevenue = 0;

        for (Booking booking : bookings) {
            switch (booking.getStatus()) {
                case ACTIVE -> active++;
                case COMPLETED -> {
                    completed++;
                    if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
                        totalRevenue += booking.getTotalPrice();
                    }
                }
                case CANCELLED -> cancelled++;
            }
        }

        System.out.println("\nСтатистика:");
        System.out.println("Активні: " + active);
        System.out.println("Завершені: " + completed);
        System.out.println("Скасовані: " + cancelled);
        System.out.println("Загальний дохід: " + String.format("%.2f", totalRevenue) + " грн");

        // Детальний перегляд
        System.out.print("\nПереглянути детальний список? (так/ні): ");
        String viewDetails = scanner.nextLine().trim().toLowerCase();

        if (viewDetails.equals("так") || viewDetails.equals("yes") || viewDetails.equals("y")) {
            System.out.println("\nДетальний список бронювань:");

            for (int i = 0; i < bookings.size(); i++) {
                Booking booking = bookings.get(i);
                System.out.println("\n" + (i + 1) + ". #" + booking.getId());
                System.out.println("   Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());
                System.out.println("   Період: " + booking.getCheckInTime().toLocalDate() +
                        " - " + booking.getCheckOutTime().toLocalDate());
                System.out.println("   Статус: " + booking.getStatusDescription());
                System.out.println("   Оплата: " + booking.getPaymentStatus());
                System.out.println("   Вартість: " + String.format("%.2f", booking.getTotalPrice()) + " грн");
            }
        }
    }

    private void viewHostelStatistics(Hostel hostel) {
        System.out.println("\n--- СТАТИСТИКА ХОСТЕЛУ ---");
        System.out.println("Хостел: " + hostel.getName());

        // Отримання даних з сервера
        List<Booking> bookings = bookingService.getHostelBookings(hostel.getId());
        List<Review> reviews = FileServer.getReviewsForHostel(hostel.getId());

        // Розрахунок статистики
        int totalBookings = bookings.size();
        int activeBookings = 0;
        int completedBookings = 0;
        double totalRevenue = 0;
        double avgRating = FileServer.getAverageRatingForHostel(hostel.getId());

        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.ACTIVE) {
                activeBookings++;
            } else if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
                completedBookings++;
                if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
                    totalRevenue += booking.getTotalPrice();
                }
            }
        }

        // Виведення статистики
        System.out.println("\nОсновні показники:");
        System.out.println("Всього бронювань: " + totalBookings);
        System.out.println("Активних: " + activeBookings);
        System.out.println("Завершених: " + completedBookings);
        System.out.println("Загальний дохід: " + String.format("%.2f", totalRevenue) + " грн");

        if (completedBookings > 0) {
            double avgBookingValue = totalRevenue / completedBookings;
            System.out.println("Середній чек: " + String.format("%.2f", avgBookingValue) + " грн");
        }

        System.out.println("\nВідгуки та рейтинги:");
        System.out.println("Всього відгуків: " + reviews.size());
        System.out.println("Середній рейтинг: " + String.format("%.1f", avgRating) + " / 5");

        if (!reviews.isEmpty()) {
            System.out.println("\nОстанні відгуки:");
            int limit = Math.min(reviews.size(), 3);
            for (int i = 0; i < limit; i++) {
                Review review = reviews.get(i);
                System.out.println((i + 1) + ". " + review.getRating() + " зірок");
                if (review.getComment() != null && !review.getComment().isEmpty()) {
                    System.out.println("   Коментар: " + review.getComment());
                }
            }
        }
    }

    private void changeHostelStatus(Hostel hostel) {
        System.out.println("\n--- ЗМІНА СТАТУСУ ХОСТЕЛУ ---");

        if (hostel.isActive()) {
            System.out.println("Поточний статус: Активний");
            System.out.println("Призупинення призведе до:");
            System.out.println("• Блокування нових бронювань");
            System.out.println("• Скасування всіх активних бронювань");
            System.out.println("• Надсилання повідомлень клієнтам");

            System.out.print("\nПризупинити роботу хостелу? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                boolean success = hostelService.suspendHostel(hostel.getId());

                if (success) {
                    System.out.println("[SUCCESS] Хостел призупинено");
                } else {
                    System.out.println("[ERROR] Не вдалося призупинити хостел");
                }
            }
        } else {
            System.out.println("Поточний статус: Призупинений");
            System.out.print("Активувати хостел? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                boolean success = hostelService.activateHostel(hostel.getId());

                if (success) {
                    System.out.println("[SUCCESS] Хостел активовано");
                } else {
                    System.out.println("[ERROR] Не вдалося активувати хостел");
                }
            }
        }
    }

    private void deleteHostel(Hostel hostel) {
        System.out.println("\n--- ВИДАЛЕННЯ ХОСТЕЛУ ---");
        System.out.println("[WARNING] Ця дія незворотна!");
        System.out.println("Ви збираєтесь видалити хостел:");
        System.out.println("Назва: " + hostel.getName());
        System.out.println("Адреса: " + hostel.getAddress());
        System.out.println("Місто: " + hostel.getCity());

        System.out.println("\nНаслідки:");
        System.out.println("• Хостел буде повністю видалено з системи");
        System.out.println("• Всі кімнати будуть видалені");
        System.out.println("• Всі бронювання будуть скасовані");
        System.out.println("• Дані неможливо відновити");

        System.out.print("\nДля підтвердження введіть назву хостелу: ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equals(hostel.getName())) {
            System.out.println("[ERROR] Назви не співпадають. Видалення відмінено");
            return;
        }

        // Додаткова перевірка
        System.out.print("\nВи впевнені? (так/ні): ");
        String finalConfirm = scanner.nextLine().trim().toLowerCase();

        if (finalConfirm.equals("так") || finalConfirm.equals("yes") || finalConfirm.equals("y")) {
            System.out.println("[INFO] Видалення... (це може зайняти кілька секунд)");

            boolean success = hostelService.deleteHostel(hostel.getId());

            if (success) {
                System.out.println("[SUCCESS] Хостел видалено");
            } else {
                System.out.println("[ERROR] Не вдалося видалити хостел");
                System.out.println("[INFO] Можливо, є активні бронювання");
            }
        } else {
            System.out.println("[INFO] Видалення відмінено");
        }
    }

    private void viewAllBookings() {
        System.out.println("\n--- ВСІ БРОНЮВАННЯ ---");

        List<Hostel> hostels = hostelService.getHostelsByManager(currentManager.getId());

        if (hostels.isEmpty()) {
            System.out.println("[INFO] У вас немає хостелів");
            return;
        }

        int totalBookings = 0;
        double totalRevenue = 0;

        for (Hostel hostel : hostels) {
            List<Booking> bookings = bookingService.getHostelBookings(hostel.getId());
            totalBookings += bookings.size();

            for (Booking booking : bookings) {
                if (booking.getStatus() == Booking.BookingStatus.COMPLETED &&
                        booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
                    totalRevenue += booking.getTotalPrice();
                }
            }
        }

        System.out.println("Загальна статистика:");
        System.out.println("Хостелів: " + hostels.size());
        System.out.println("Всього бронювань: " + totalBookings);
        System.out.println("Загальний дохід: " + String.format("%.2f", totalRevenue) + " грн");

        // Детальний перегляд по хостелах
        System.out.print("\nПереглянути деталі по хостелах? (так/ні): ");
        String viewDetails = scanner.nextLine().trim().toLowerCase();

        if (viewDetails.equals("так") || viewDetails.equals("yes") || viewDetails.equals("y")) {
            for (Hostel hostel : hostels) {
                System.out.println("\nХостел: " + hostel.getName());
                List<Booking> bookings = bookingService.getHostelBookings(hostel.getId());

                int active = 0, completed = 0, cancelled = 0;
                double revenue = 0;

                for (Booking booking : bookings) {
                    switch (booking.getStatus()) {
                        case ACTIVE -> active++;
                        case COMPLETED -> {
                            completed++;
                            if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
                                revenue += booking.getTotalPrice();
                            }
                        }
                        case CANCELLED -> cancelled++;
                    }
                }

                System.out.println("  Бронювань: " + bookings.size());
                System.out.println("  Активні: " + active);
                System.out.println("  Завершені: " + completed);
                System.out.println("  Скасовані: " + cancelled);
                System.out.println("  Дохід: " + String.format("%.2f", revenue) + " грн");
            }
        }
    }

    private void viewServerStats() {
        FileServer.printServerStats();
    }

    private void switchToClientInterface() {
        System.out.println("\n--- ПЕРЕХІД ДО ІНТЕРФЕЙСУ КЛІЄНТА ---");
        System.out.println("[INFO] Вихід з акаунта менеджера...");

        authService.logout();
        currentManager = null;

        System.out.println("[SUCCESS] Тепер ви можете працювати як клієнт");
    }

    private void logout() {
        System.out.println("\n--- ВИХІД З АКАУНТА ---");
        System.out.print("Ви впевнені, що хочете вийти? (так/ні): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("так") || response.equals("yes") || response.equals("y")) {
            authService.logout();
            currentManager = null;
            System.out.println("[SUCCESS] Вихід виконано");
        }
    }

    private void toggleNetwork() {
        boolean currentStatus = NetworkMonitor.isNetworkAvailable();
        NetworkMonitor.setConnected(!currentStatus);
        System.out.println("[INFO] Статус мережі змінено на: " +
                (NetworkMonitor.isNetworkAvailable() ? "Онлайн" : "Офлайн"));
    }
}
