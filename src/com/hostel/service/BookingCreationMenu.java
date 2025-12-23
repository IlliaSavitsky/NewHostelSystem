package com.hostel.service;

import com.hostel.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class BookingCreationMenu {
    private final Scanner scanner;
    private final BookingService bookingService;
    private final AuthenticationService authService;

    public BookingCreationMenu(Scanner scanner, BookingService bookingService,
                               AuthenticationService authService) {
        this.scanner = scanner;
        this.bookingService = bookingService;
        this.authService = authService;
    }

    public Booking createBookingForHostel(String hostelId, String clientId, Room selectedRoom) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("СТВОРЕННЯ БРОНЮВАННЯ");
        System.out.println("=".repeat(40));

        User currentUser = authService.getCurrentUser();

        if (currentUser == null) {
            System.out.println("[INFO] Ви не авторизовані. Створюємо бронювання як гість");
        } else {
            System.out.println("[INFO] Ви авторизовані як: " +
                    (currentUser instanceof Client ?
                            ((Client)currentUser).getFullName() : currentUser.getEmail()));
        }

        // Вибір часу заселення
        LocalDateTime checkIn = selectCheckInTime();
        if (checkIn == null) return null;

        // Вибір часу виїзду
        LocalDateTime checkOut = selectCheckOutTime(checkIn);
        if (checkOut == null) return null;

        // Розрахунок ціни
        double price = bookingService.calculatePrice(selectedRoom, checkIn, checkOut);

        System.out.println("\n" + "=".repeat(40));
        System.out.println("РОЗРАХУНОК ВАРТОСТІ");
        System.out.println("=".repeat(40));
        System.out.println("Кімната: " + getRoomDescription(selectedRoom));
        System.out.println("Період: " + formatDateTime(checkIn) + " - " + formatDateTime(checkOut));
        System.out.println("Тривалість: " + getDurationHours(checkIn, checkOut) + " годин");
        System.out.println("ВАРТІСТЬ: " + String.format("%.2f", price) + " грн");

        // Особисті дані
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ОСОБИСТІ ДАНІ");
        System.out.println("=".repeat(40));

        String firstName, lastName, phone, email;

        if (currentUser instanceof Client) {
            // Автоматичне заповнення для зареєстрованих клієнтів
            Client client = (Client) currentUser;
            firstName = client.getFirstName();
            lastName = client.getLastName();
            phone = currentUser.getPhone();
            email = currentUser.getEmail();

            System.out.println("[INFO] Ваші дані з аккаунта:");
            System.out.println("Ім'я: " + firstName);
            System.out.println("Прізвище: " + lastName);
            System.out.println("Телефон: " + phone);
            System.out.println("Email: " + email);

            System.out.print("\nВикористати ці дані для бронювання? (так/ні): ");
            String useExisting = scanner.nextLine().trim().toLowerCase();

            if (!useExisting.equals("так") && !useExisting.equals("yes") && !useExisting.equals("y")) {
                firstName = readInput("Ім'я: ");
                lastName = readInput("Прізвище: ");
                phone = readInput("Номер телефону: ");
                email = readInput("Email: ");
            }
        } else {
            // Запит даних для гостя
            System.out.println("[INFO] Для оформлення бронювання введіть ваші дані:");
            firstName = readInput("Ім'я: ");
            lastName = readInput("Прізвище: ");
            phone = readInput("Номер телефону: ");
            email = readInput("Email: ");
        }

        // Підтвердження
        System.out.println("\n" + "=".repeat(40));
        System.out.println("ПІДТВЕРДЖЕННЯ");
        System.out.println("=".repeat(40));
        System.out.println("Перевірте введені дані:");
        System.out.println("Кімната: " + getRoomDescription(selectedRoom));
        System.out.println("Період: " + formatDateTime(checkIn) + " - " + formatDateTime(checkOut));
        System.out.println("Вартість: " + String.format("%.2f", price) + " грн");
        System.out.println("Клієнт: " + firstName + " " + lastName);
        System.out.println("Телефон: " + phone);
        System.out.println("Email: " + email);

        System.out.print("\nПідтвердити бронювання? (так/ні): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("так") || confirm.equals("yes") || confirm.equals("y")) {
            return finalizeBooking(hostelId, selectedRoom.getId(), clientId,
                    checkIn, checkOut, price, firstName, lastName, phone, email);
        } else {
            System.out.println("[INFO] Бронювання скасовано");
            return null;
        }
    }

    private String readInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private Booking finalizeBooking(String hostelId, String roomId, String clientId,
                                    LocalDateTime checkIn, LocalDateTime checkOut, double price,
                                    String firstName, String lastName, String phone, String email) {

        System.out.println("\n[INFO] Фіналізація бронювання...");

        long startTime = System.currentTimeMillis();

        Booking booking = bookingService.createBooking(
                hostelId, roomId, clientId, checkIn, checkOut,
                firstName, lastName, phone, email
        );

        long elapsedTime = System.currentTimeMillis() - startTime;

        if (booking != null) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("БРОНЮВАННЯ УСПІШНО СТВОРЕНО!");
            System.out.println("=".repeat(50));

            System.out.println("\nДЕТАЛІ БРОНЮВАННЯ:");
            System.out.println("Номер бронювання: " + booking.getId());
            System.out.println("Хостел ID: " + hostelId);
            System.out.println("Кімната ID: " + roomId);
            System.out.println("Дата заїзду: " + formatDateTime(checkIn));
            System.out.println("Дата виїзду: " + formatDateTime(checkOut));
            System.out.println("Загальна вартість: " + String.format("%.2f", booking.getTotalPrice()) + " грн");
            System.out.println("Статус: " + booking.getStatusDescription());

            // Перевірка часу передачі
            if (elapsedTime > 5000) {
                System.out.println("[WARNING] Передача зайняла " + elapsedTime + "мс (>5 сек)");
            } else {
                System.out.println("[INFO] Дані передано за " + elapsedTime + "мс");
            }

            System.out.println("\n" + "-".repeat(50));
            System.out.print("Натисніть Enter для продовження...");
            scanner.nextLine();

            return booking;
        } else {
            System.out.println("[ERROR] Помилка при створенні бронювання");
            return null;
        }
    }

    private String getRoomDescription(Room room) {
        String type = (room.getType() == Room.RoomType.PRIVATE) ? "Приватна" : "Спільна";
        return type + " кімната, " + room.getCapacity() + " місць, " +
                String.format("%.2f", room.getPricePerHour()) + " грн/год";
    }

    private LocalDateTime selectCheckInTime() {
        System.out.println("\n--- ВИБІР ЧАСУ ЗАСЕЛЕННЯ ---");

        while (true) {
            try {
                System.out.println("Формат: РРРР-ММ-ДД ГГ:ХХ");
                System.out.println("Приклад: 2024-12-25 14:00");
                System.out.print("Введіть дату та час заїзду: ");
                String checkInStr = scanner.nextLine().trim();

                if (checkInStr.isEmpty()) {
                    // Замовчування: завтра о 14:00
                    LocalDateTime defaultTime = LocalDateTime.now()
                            .plusDays(1)
                            .withHour(14)
                            .withMinute(0);
                    System.out.println("[INFO] Встановлено за замовчуванням: " + formatDateTime(defaultTime));
                    return defaultTime;
                }

                LocalDateTime checkIn = LocalDateTime.parse(checkInStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                if (checkIn.isBefore(LocalDateTime.now())) {
                    System.out.println("[ERROR] Час заїзду не може бути в минулому");
                    continue;
                }

                return checkIn;

            } catch (DateTimeParseException e) {
                System.out.println("[ERROR] Неправильний формат. Спробуйте ще раз");
            }
        }
    }

    private LocalDateTime selectCheckOutTime(LocalDateTime checkIn) {
        System.out.println("\n--- ВИБІР ЧАСУ ВИЇЗДУ ---");

        while (true) {
            try {
                System.out.println("Формат: РРРР-ММ-ДД ГГ:ХХ");
                System.out.println("Приклад: 2024-12-27 12:00");
                System.out.print("Введіть дату та час виїзду: ");
                String checkOutStr = scanner.nextLine().trim();

                LocalDateTime checkOut = LocalDateTime.parse(checkOutStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                    System.out.println("[ERROR] Час виїзду повинен бути після часу заїзду");
                    continue;
                }

                long hours = getDurationHours(checkIn, checkOut);
                if (hours < 1) {
                    System.out.println("[ERROR] Мінімальна тривалість - 1 година");
                    continue;
                }

                if (hours > 24 * 30) { // Максимум 30 днів
                    System.out.println("[ERROR] Максимальна тривалість - 30 днів");
                    continue;
                }

                return checkOut;

            } catch (DateTimeParseException e) {
                System.out.println("[ERROR] Неправильний формат. Спробуйте ще раз");
            }
        }
    }

    private long getDurationHours(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toHours();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}