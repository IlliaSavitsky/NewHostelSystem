package com.hostel.service;

import com.hostel.model.*;
import com.hostel.server.FileServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookingViewMenu {
    private final Scanner scanner;
    private final BookingService bookingService;
    private final AuthenticationService authService;
    private final BookingCache bookingCache;
    private final SimpleReviewService reviewService;

    public BookingViewMenu(Scanner scanner, BookingService bookingService,
                           AuthenticationService authService, BookingCache bookingCache) {
        this.scanner = scanner;
        this.bookingService = bookingService;
        this.authService = authService;
        this.bookingCache = bookingCache;
        this.reviewService = new SimpleReviewService();
    }

    public void displayUserBookings(User user) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("МОЇ БРОНЮВАННЯ");
        System.out.println("=".repeat(50));

        if (user == null) {
            System.out.println("[ERROR] Користувач не знайдений");
            return;
        }

        System.out.println("[INFO] Завантаження бронювань...");

        List<Booking> bookings = bookingService.getUserBookings(user.getId());

        if (bookings.isEmpty()) {
            System.out.println("\n[INFO] У вас немає бронювань");
            System.out.println("Скористайтесь опцією 'Пошук хостелів та бронювання'");
            return;
        }

        System.out.println("\n[SUCCESS] Знайдено бронювань: " + bookings.size());

        boolean stayInMenu = true;

        while (stayInMenu) {
            showBookingsMenu(bookings);

            System.out.print("\nОберіть опцію: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> showAllBookings(bookings);
                case "2" -> showActiveBookings(bookings);
                case "3" -> showCompletedBookings(bookings);
                case "4" -> showCancelledBookings(bookings);
                case "5" -> cancelBooking(bookings);
                case "6" -> rateBooking(bookings);
                case "0" -> stayInMenu = false;
                default -> System.out.println("[ERROR] Невірний вибір");
            }
        }
    }

    private void showBookingsMenu(List<Booking> bookings) {
        int active = 0, completed = 0, cancelled = 0;

        for (Booking booking : bookings) {
            switch (booking.getStatus()) {
                case ACTIVE -> active++;
                case COMPLETED -> completed++;
                case CANCELLED -> cancelled++;
            }
        }

        System.out.println("\n-- СТАТУС БРОНЮВАНЬ --");
        System.out.println("Активні: " + active);
        System.out.println("Завершені: " + completed);
        System.out.println("Скасовані: " + cancelled);

        System.out.println("\n-- ОПЦІЇ --");
        System.out.println("1. Всі бронювання");
        System.out.println("2. Активні бронювання");
        System.out.println("3. Завершені бронювання");
        System.out.println("4. Скасовані бронювання");
        System.out.println("5. Скасувати бронювання");
        System.out.println("6. Залишити відгук");
        System.out.println("0. Повернутись назад");
    }

    private void showAllBookings(List<Booking> bookings) {
        System.out.println("\n--- ВСІ БРОНЮВАННЯ ---");

        for (int i = 0; i < bookings.size(); i++) {
            displayBookingDetails(bookings.get(i), i + 1);
        }
    }

    private void showActiveBookings(List<Booking> bookings) {
        System.out.println("\n--- АКТИВНІ БРОНЮВАННЯ ---");

        List<Booking> activeBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.ACTIVE) {
                activeBookings.add(booking);
            }
        }

        if (activeBookings.isEmpty()) {
            System.out.println("[INFO] Активних бронювань немає");
            return;
        }

        for (int i = 0; i < activeBookings.size(); i++) {
            displayBookingDetails(activeBookings.get(i), i + 1);
            System.out.println("   [ДІЯ] Можна скасувати");
        }
    }

    private void showCompletedBookings(List<Booking> bookings) {
        System.out.println("\n--- ЗАВЕРШЕНІ БРОНЮВАННЯ ---");

        List<Booking> completedBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
                completedBookings.add(booking);
            }
        }

        if (completedBookings.isEmpty()) {
            System.out.println("[INFO] Завершених бронювань немає");
            return;
        }

        for (int i = 0; i < completedBookings.size(); i++) {
            displayBookingDetails(completedBookings.get(i), i + 1);

            if (bookingCanBeRated(completedBookings.get(i))) {
                System.out.println("   [ДІЯ] Можна залишити відгук");
            }
        }
    }

    private void showCancelledBookings(List<Booking> bookings) {
        System.out.println("\n--- СКАСОВАНІ БРОНЮВАННЯ ---");

        List<Booking> cancelledBookings = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
                cancelledBookings.add(booking);
            }
        }

        if (cancelledBookings.isEmpty()) {
            System.out.println("[INFO] Скасованих бронювань немає");
            return;
        }

        for (int i = 0; i < cancelledBookings.size(); i++) {
            displayBookingDetails(cancelledBookings.get(i), i + 1);
        }
    }

    private void cancelBooking(List<Booking> bookings) {
        List<Booking> cancellableBookings = new ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getStatus() == Booking.BookingStatus.ACTIVE &&
                    booking.canBeCancelled()) {
                cancellableBookings.add(booking);
            }
        }

        if (cancellableBookings.isEmpty()) {
            System.out.println("\n[INFO] Немає бронювань для скасування");
            System.out.println("Можна скасувати тільки активні бронювання");
            System.out.println("за 2 години до заселення");
            return;
        }

        System.out.println("\n--- СКАСУВАННЯ БРОНЮВАННЯ ---");
        System.out.println("Оберіть бронювання для скасування:");

        for (int i = 0; i < cancellableBookings.size(); i++) {
            Booking booking = cancellableBookings.get(i);
            System.out.println((i + 1) + ". #" + booking.getId() +
                    " - " + booking.getCheckInTime().toLocalDate() +
                    " (заїзд о " + booking.getCheckInTime().toLocalTime() + ")");
        }

        System.out.print("\nВаш вибір (або 0 для відміни): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 0) {
                System.out.println("[INFO] Скасування відмінено");
                return;
            }

            if (choice > 0 && choice <= cancellableBookings.size()) {
                Booking toCancel = cancellableBookings.get(choice - 1);
                confirmCancelBooking(toCancel);
            } else {
                System.out.println("[ERROR] Невірний вибір");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void confirmCancelBooking(Booking booking) {
        System.out.println("\n--- ПІДТВЕРДЖЕННЯ СКАСУВАННЯ ---");
        System.out.println("[WARNING] Ви збираєтесь скасувати бронювання:");
        System.out.println("Номер: " + booking.getId());
        System.out.println("Дата заїзду: " + booking.getCheckInTime());
        System.out.println("Вартість: " + String.format("%.2f", booking.getTotalPrice()) + " грн");
        System.out.println("Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());

        System.out.print("\nВведіть причину скасування: ");
        String reason = scanner.nextLine().trim();

        System.out.print("\nВи впевнені, що хочете скасувати? (так/ні): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("так") || confirm.equals("yes") || confirm.equals("y")) {
            System.out.println("[INFO] Виконується скасування...");

            long startTime = System.currentTimeMillis();

            String initiatorId = authService.getCurrentUser() != null ?
                    authService.getCurrentUser().getId() : "CLIENT";

            boolean success = bookingService.cancelBooking(booking.getId(), initiatorId);

            long elapsedTime = System.currentTimeMillis() - startTime;

            if (success) {
                System.out.println("[SUCCESS] Бронювання скасовано!");

                if (elapsedTime > 5000) {
                    System.out.println("[WARNING] Операція зайняла " + elapsedTime + "мс (>5 сек)");
                }

                // Запис причини в логи
                System.out.println("[INFO] Причина скасування: " + reason);
            } else {
                System.out.println("[ERROR] Не вдалося скасувати бронювання");
                System.out.println("[INFO] Спробуйте пізніше або зверніться до менеджера");
            }
        } else {
            System.out.println("[INFO] Скасування відмінено");
        }
    }

    private void rateBooking(List<Booking> bookings) {
        List<Booking> rateableBookings = new ArrayList<>();

        for (Booking booking : bookings) {
            if (bookingCanBeRated(booking)) {
                rateableBookings.add(booking);
            }
        }

        if (rateableBookings.isEmpty()) {
            System.out.println("\n[INFO] Немає бронювань для оцінки");
            System.out.println("Можна оцінити тільки завершені бронювання");
            System.out.println("які ще не мають оцінки");
            return;
        }

        System.out.println("\n--- ОЦІНКА ХОСТЕЛУ ---");
        System.out.println("Оберіть бронювання для оцінки:");

        for (int i = 0; i < rateableBookings.size(); i++) {
            Booking booking = rateableBookings.get(i);
            System.out.println((i + 1) + ". #" + booking.getId() +
                    " - " + booking.getCheckInTime().toLocalDate() +
                    " до " + booking.getCheckOutTime().toLocalDate());
        }

        System.out.print("\nВаш вибір (або 0 для відміни): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 0) {
                System.out.println("[INFO] Оцінку відмінено");
                return;
            }

            if (choice > 0 && choice <= rateableBookings.size()) {
                Booking toRate = rateableBookings.get(choice - 1);
                submitReview(toRate);
            } else {
                System.out.println("[ERROR] Невірний вибір");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void submitReview(Booking booking) {
        System.out.println("\n--- ЗАЛИШИТИ ВІДГУК ---");

        // Введення рейтингу
        int rating;
        while (true) {
            System.out.print("Оцініть хостел (1-5 зірок, де 5 - найвищий): ");
            String input = scanner.nextLine().trim();

            try {
                rating = Integer.parseInt(input);
                if (rating >= 1 && rating <= 5) {
                    break;
                }
                System.out.println("[ERROR] Введіть число від 1 до 5");
            } catch (NumberFormatException e) {
                System.out.println("[ERROR] Введіть коректне число");
            }
        }

        // Введення коментаря
        System.out.print("Залишити коментар (не обов'язково): ");
        String comment = scanner.nextLine().trim();

        // Підтвердження
        System.out.println("\nВаша оцінка: " + rating + " зірок");
        if (!comment.isEmpty()) {
            System.out.println("Коментар: " + comment);
        }

        System.out.print("\nНадіслати відгук? (так/ні): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("так") || confirm.equals("yes") || confirm.equals("y")) {
            boolean success = reviewService.submitReview(
                    booking.getId(), booking.getClientId(),
                    booking.getHostelId(), rating, comment
            );

            if (success) {
                System.out.println("[SUCCESS] Дякуємо за ваш відгук!");
                System.out.println("[INFO] Ваша оцінка допомагає покращувати сервіс");
            } else {
                System.out.println("[ERROR] Не вдалося зберегти відгук");
                System.out.println("[INFO] Спробуйте пізніше");
            }
        } else {
            System.out.println("[INFO] Відгук скасовано");
        }
    }

    private boolean bookingCanBeRated(Booking booking) {
        return booking.getStatus() == Booking.BookingStatus.COMPLETED &&
                booking.getRating() == null;
    }

    private void displayBookingDetails(Booking booking, int number) {
        System.out.println("\n" + number + ". БРОНЮВАННЯ #" + booking.getId());
        System.out.println("   Статус: " + booking.getStatusDescription());
        System.out.println("   Оплата: " + booking.getPaymentStatus());
        System.out.println("   Вартість: " + String.format("%.2f", booking.getTotalPrice()) + " грн");
        System.out.println("   Заїзд: " + booking.getCheckInTime());
        System.out.println("   Виїзд: " + booking.getCheckOutTime());
        System.out.println("   Створено: " + booking.getCreatedAt().toLocalDate());
        System.out.println("   Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());
        System.out.println("   Телефон: " + booking.getClientPhone());

        if (booking.getRating() != null) {
            System.out.println("   Рейтинг: " + booking.getRating() + " зірок");
            if (booking.getReview() != null && !booking.getReview().isEmpty()) {
                System.out.println("   Відгук: " + booking.getReview());
            }
        }
    }
}
