package com.hostel.service;

import com.hostel.model.*;
import java.util.List;
import java.util.Scanner;
import com.hostel.server.FileServer;

public class ClientInterface {
    private final Scanner scanner;
    private final HostelService hostelService;
    private final BookingService bookingService;
    private final AuthenticationService authService;
    private final BookingViewMenu bookingViewMenu;
    private final BookingCreationMenu bookingCreationMenu;
    private User currentUser;
    private BookingCache bookingCache;

    public ClientInterface(Scanner scanner, HostelService hostelService,
                           BookingService bookingService, AuthenticationService authService) {
        this.scanner = scanner;
        this.hostelService = hostelService;
        this.bookingService = bookingService;
        this.authService = authService;
        this.bookingCache = new BookingCache();
        this.bookingViewMenu = new BookingViewMenu(scanner, bookingService, authService, bookingCache);
        this.bookingCreationMenu = new BookingCreationMenu(scanner, bookingService, authService);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ІНТЕРФЕЙС КЛІЄНТА " + NetworkMonitor.getNetworkStatus());
        System.out.println("=".repeat(50));

        if (currentUser instanceof Client) {
            Client client = (Client) currentUser;
            System.out.println("Користувач: " + client.getFullName());
        } else if (currentUser != null) {
            System.out.println("Користувач: " + currentUser.getEmail());
        } else {
            System.out.println("Режим: Анонімний перегляд");
        }

        boolean stayInMenu = true;

        while (stayInMenu) {
            showMainOptions();

            System.out.print("\nОберіть опцію: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> searchAndBookHostel();
                case "2" -> {
                    if (currentUser != null && authService.isUserAuthenticated()) {
                        viewMyBookings();
                    } else {
                        System.out.println("\n[ERROR] Для перегляду бронювань потрібно увійти в систему");
                        System.out.println("Будь ласка, виконайте вхід або зареєструйтесь");
                    }
                }
                case "3" -> switchToManagerInterface();
                case "4" -> {
                    if (currentUser != null) {
                        logout();
                    } else {
                        exitAnonymousMode();
                    }
                }
                case "0" -> stayInMenu = false;
                case "stats" -> FileServer.printServerStats(); // Прихована команда
                default -> System.out.println("[ERROR] Невірний вибір");
            }
        }
    }

    private void showMainOptions() {
        System.out.println("\n-- ГОЛОВНЕ МЕНЮ --");
        System.out.println("1. Пошук хостелів та бронювання");

        if (currentUser != null && authService.isUserAuthenticated()) {
            System.out.println("2. Мої бронювання");
        } else {
            System.out.println("2. Мої бронювання (потрібен вхід)");
        }

        System.out.println("3. Перейти до інтерфейсу менеджера");

        if (currentUser != null) {
            System.out.println("4. Вийти з акаунта");
        } else {
            System.out.println("4. Вийти з анонімного режиму");
        }

        System.out.println("0. Повернутись назад");
    }

    public void searchAndBookHostel() {
        System.out.println("\n--- ПОШУК ХОСТЕЛІВ ---");

        System.out.print("Введіть місто для пошуку: ");
        String city = scanner.nextLine().trim();

        System.out.print("Введіть назву хостелу або адресу (Enter для всіх): ");
        String query = scanner.nextLine().trim();

        System.out.println("[INFO] Виконується пошук...");

        List<Hostel> hostels = hostelService.searchHostels(city, query.isEmpty() ? "" : query);

        if (hostels.isEmpty()) {
            System.out.println("[INFO] Хостелів не знайдено");
            System.out.println("Можливі причини:");
            System.out.println("1. Неправильна назва міста");
            System.out.println("2. Хостели в цьому місті призупинено");
            System.out.println("3. Відсутнє мережеве з'єднання");
            return;
        }

        System.out.println("\n[SUCCESS] Знайдено хостелів: " + hostels.size());

        for (int i = 0; i < hostels.size(); i++) {
            Hostel hostel = hostels.get(i);
            System.out.println("\n" + (i + 1) + ". " + hostel.getName());
            System.out.println("   Адреса: " + hostel.getAddress() + ", " + hostel.getCity());
            System.out.println("   Статус: " + (hostel.isActive() ? "✅ Активний" : "⏸️ Призупинений"));

            System.out.print("   Обрати цей хостел? (так/ні): ");
            String select = scanner.nextLine().trim().toLowerCase();

            if (select.equals("так") || select.equals("yes") || select.equals("y")) {
                selectHostelForBooking(hostel);
                return;
            }
        }

        System.out.println("[INFO] Хостел не обрано");
    }

    private void selectHostelForBooking(Hostel hostel) {
        System.out.println("\n--- ВИБІР ХОСТЕЛУ: " + hostel.getName() + " ---");

        if (!hostel.isActive()) {
            System.out.println("[ERROR] Цей хостел тимчасово призупинено");
            System.out.println("Бронювання недоступні");
            return;
        }

        if (!(bookingService instanceof SimpleBookingService)) {
            System.out.println("[ERROR] Неможливо отримати список кімнат");
            return;
        }

        SimpleBookingService simpleService = (SimpleBookingService) bookingService;
        List<Room> availableRooms = simpleService.getRoomsByHostel(hostel.getId());

        if (availableRooms.isEmpty()) {
            System.out.println("[INFO] Наразі немає доступних кімнат у цьому хостелі");
            return;
        }

        System.out.println("\nДоступні кімнати:");
        for (int i = 0; i < availableRooms.size(); i++) {
            Room room = availableRooms.get(i);
            String type = room.getType() == Room.RoomType.PRIVATE ? "Приватна" : "Спільна";
            System.out.println((i + 1) + ". " + type + " кімната");
            System.out.println("   Місць: " + room.getCapacity());
            System.out.println("   Ціна: " + room.getPricePerHour() + " грн/год");
            System.out.println("   Приблизна вартість за добу: " + (room.getPricePerHour() * 24) + " грн");
        }

        System.out.print("\nОберіть номер кімнати (або 0 для відміни): ");

        try {
            int roomChoice = Integer.parseInt(scanner.nextLine().trim());

            if (roomChoice == 0) {
                System.out.println("[INFO] Вибір кімнати відмінено");
                return;
            }

            if (roomChoice < 1 || roomChoice > availableRooms.size()) {
                System.out.println("[ERROR] Невірний вибір кімнати");
                return;
            }

            Room selectedRoom = availableRooms.get(roomChoice - 1);

            // Визначення clientId
            String clientId;
            if (currentUser != null) {
                clientId = currentUser.getId();
            } else {
                clientId = "GUEST_" + System.currentTimeMillis();
            }

            System.out.println("[INFO] Перехід до створення бронювання...");
            Booking booking = bookingCreationMenu.createBookingForHostel(
                    hostel.getId(), clientId, selectedRoom);

            if (booking != null) {
                System.out.println("[SUCCESS] Бронювання створено!");

                // Додавання в чергу синхронізації, якщо потрібно
                if (!NetworkMonitor.isNetworkAvailable()) {
                    System.out.println("[INFO] Бронювання додано до черги синхронізації");
                    System.out.println("Воно буде збережено на сервері після відновлення мережі");
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат числа");
        }
    }

    public void viewMyBookings() {
        if (currentUser == null) {
            System.out.println("[ERROR] Для перегляду бронювань потрібно увійти в систему");
            return;
        }

        System.out.println("\n=== МОЇ БРОНЮВАННЯ ===\n");
        bookingViewMenu.displayUserBookings(currentUser);
    }

    public boolean switchToManagerInterface() {
        System.out.println("\n--- ПЕРЕХІД ДО ІНТЕРФЕЙСУ МЕНЕДЖЕРА ---");
        System.out.println("[INFO] Для доступу потрібна автентифікація менеджера");

        System.out.print("Перейти до входу менеджера? (так/ні): ");
        String response = scanner.nextLine().trim().toLowerCase();

        return response.equals("так") || response.equals("yes") || response.equals("y");
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("\n--- ВИХІД З АКАУНТА ---");
            System.out.print("Ви впевнені, що хочете вийти? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                authService.logout();
                System.out.println("[SUCCESS] Вихід виконано");
                currentUser = null;
            }
        }
    }

    private void exitAnonymousMode() {
        System.out.println("\n--- ВИХІД З АНОНІМНОГО РЕЖИМУ ---");
        System.out.print("Ви впевнені, що хочете вийти? (так/ні): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("так") || response.equals("yes") || response.equals("y")) {
            System.out.println("[INFO] Вихід з анонімного режиму");
            // currentUser вже null
        }
    }
}
