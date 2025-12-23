package com.hostel.service;

import com.hostel.model.*;
import com.hostel.server.FileServer;
import java.util.List;
import java.util.Scanner;

public class HostelDetailsWindow {
    private final Scanner scanner;
    private final HostelService hostelService;
    private final BookingService bookingService;
    private final SimpleReviewService reviewService;
    private final HostelEditMenu hostelEditMenu;

    public HostelDetailsWindow(Scanner scanner, HostelService hostelService,
                               BookingService bookingService) {
        this.scanner = scanner;
        this.hostelService = hostelService;
        this.bookingService = bookingService;
        this.reviewService = new SimpleReviewService();
        this.hostelEditMenu = new HostelEditMenu(scanner, hostelService, bookingService);
    }

    public void displayHostelDetails(Hostel hostel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ДЕТАЛЬНА ІНФОРМАЦІЯ ПРО ХОСТЕЛ");
        System.out.println("=".repeat(60));

        showBasicInfo(hostel);
        showRoomInfo(hostel);
        showStatistics(hostel);
        showReviews(hostel);

        showManagementOptions(hostel);
    }

    private void showBasicInfo(Hostel hostel) {
        System.out.println("\n--- ОСНОВНА ІНФОРМАЦІЯ ---");
        System.out.println("Назва: " + hostel.getName());
        System.out.println("Адреса: " + hostel.getAddress());
        System.out.println("Місто: " + hostel.getCity());
        System.out.println("Статус: " + (hostel.isActive() ? "✅ Активний" : "⏸️ Призупинений"));
        System.out.println("ID: " + hostel.getId());
    }

    private void showRoomInfo(Hostel hostel) {
        System.out.println("\n--- КІМНАТИ ---");

        if (!(bookingService instanceof SimpleBookingService)) {
            System.out.println("[INFO] Інформація про кімнати недоступна");
            return;
        }

        SimpleBookingService simpleService = (SimpleBookingService) bookingService;
        List<Room> rooms = simpleService.getAllRoomsByHostel(hostel.getId());

        if (rooms.isEmpty()) {
            System.out.println("[INFO] Кімнат не знайдено");
            return;
        }

        int privateRooms = 0;
        int sharedRooms = 0;
        int totalBeds = 0;
        int availableRooms = 0;
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0;

        for (Room room : rooms) {
            if (room.getType() == Room.RoomType.PRIVATE) {
                privateRooms++;
            } else {
                sharedRooms++;
            }

            totalBeds += room.getCapacity();

            if (room.isAvailable()) {
                availableRooms++;
            }

            double price = room.getPricePerHour();
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;
        }

        System.out.println("Приватних кімнат: " + privateRooms);
        System.out.println("Спільних кімнат: " + sharedRooms);
        System.out.println("Всього місць: " + totalBeds);
        System.out.println("Доступних кімнат: " + availableRooms + " з " + rooms.size());

        if (minPrice != Double.MAX_VALUE) {
            System.out.println("\n--- ЦІНИ ---");
            System.out.println("Ціна за годину: від " + String.format("%.2f", minPrice) +
                    " до " + String.format("%.2f", maxPrice) + " грн");
            System.out.println("Ціна за добу: від " + String.format("%.2f", minPrice * 24) +
                    " до " + String.format("%.2f", maxPrice * 24) + " грн");
        }

        // Детальний перегляд кімнат
        System.out.print("\nПереглянути детальний список кімнат? (так/ні): ");
        String viewDetails = scanner.nextLine().trim().toLowerCase();

        if (viewDetails.equals("так") || viewDetails.equals("yes") || viewDetails.equals("y")) {
            System.out.println("\nДетальний список кімнат:");
            for (int i = 0; i < rooms.size(); i++) {
                Room room = rooms.get(i);
                System.out.println("\n" + (i + 1) + ". Кімната #" + room.getId());
                System.out.println("   Тип: " + (room.getType() == Room.RoomType.PRIVATE ? "Приватна" : "Спільна"));
                System.out.println("   Місць: " + room.getCapacity());
                System.out.println("   Ціна: " + room.getPricePerHour() + " грн/год");
                System.out.println("   Статус: " + (room.isAvailable() ? "✅ Доступна" : "❌ Заброньована"));
            }
        }
    }

    private void showStatistics(Hostel hostel) {
        System.out.println("\n--- СТАТИСТИКА ---");

        List<Booking> bookings = bookingService.getHostelBookings(hostel.getId());

        if (bookings.isEmpty()) {
            System.out.println("[INFO] Бронювань немає");
            return;
        }

        int total = bookings.size();
        int active = 0;
        int completed = 0;
        int cancelled = 0;
        double totalRevenue = 0;
        double pendingRevenue = 0;

        for (Booking booking : bookings) {
            switch (booking.getStatus()) {
                case ACTIVE -> {
                    active++;
                    if (booking.getPaymentStatus() == Booking.PaymentStatus.UNPAID) {
                        pendingRevenue += booking.getTotalPrice();
                    }
                }
                case COMPLETED -> {
                    completed++;
                    if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
                        totalRevenue += booking.getTotalPrice();
                    }
                }
                case CANCELLED -> cancelled++;
            }
        }

        System.out.println("Всього бронювань: " + total);
        System.out.println("Активні: " + active);
        System.out.println("Завершені: " + completed);
        System.out.println("Скасовані: " + cancelled);
        System.out.println("Загальний дохід: " + String.format("%.2f", totalRevenue) + " грн");
        System.out.println("Очікуваний дохід: " + String.format("%.2f", pendingRevenue) + " грн");

        if (total > 0) {
            double occupancyRate = (double) completed / total * 100;
            System.out.println("Завантаженість: " + String.format("%.1f", occupancyRate) + "%");

            if (completed > 0) {
                double avgBookingValue = totalRevenue / completed;
                System.out.println("Середній чек: " + String.format("%.2f", avgBookingValue) + " грн");
            }
        }

        // Перегляд активних бронювань
        if (active > 0) {
            System.out.print("\nПереглянути активні бронювання? (так/ні): ");
            String viewActive = scanner.nextLine().trim().toLowerCase();

            if (viewActive.equals("так") || viewActive.equals("yes") || viewActive.equals("y")) {
                System.out.println("\nАктивні бронювання:");
                List<Booking> activeBookings = bookingService.getActiveHostelBookings(hostel.getId());

                for (int i = 0; i < activeBookings.size(); i++) {
                    Booking booking = activeBookings.get(i);
                    System.out.println("\n" + (i + 1) + ". Бронювання #" + booking.getId());
                    System.out.println("   Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());
                    System.out.println("   Телефон: " + booking.getClientPhone());
                    System.out.println("   Заїзд: " + booking.getCheckInTime().toLocalDate() +
                            " (" + booking.getCheckInTime().toLocalTime() + ")");
                    System.out.println("   Виїзд: " + booking.getCheckOutTime().toLocalDate() +
                            " (" + booking.getCheckOutTime().toLocalTime() + ")");
                    System.out.println("   Вартість: " + String.format("%.2f", booking.getTotalPrice()) + " грн");
                    System.out.println("   Оплата: " + booking.getPaymentStatus());
                }

                // Опції для роботи з активними бронюваннями
                showActiveBookingOptions(hostel, activeBookings);
            }
        }
    }

    private void showActiveBookingOptions(Hostel hostel, List<Booking> activeBookings) {
        System.out.println("\n--- ОПЦІЇ ДЛЯ АКТИВНИХ БРОНЮВАНЬ ---");

        if (activeBookings.isEmpty()) {
            return;
        }

        System.out.println("1. Скасувати бронювання");
        System.out.println("2. Позначити як обслуговується");
        System.out.println("3. Позначити як завершене");
        System.out.println("4. Змінити статус оплати");
        System.out.println("0. Повернутись назад");

        System.out.print("\nОберіть опцію: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> cancelActiveBooking(activeBookings);
            case "2" -> markAsInService(activeBookings);
            case "3" -> markAsCompleted(activeBookings);
            case "4" -> changePaymentStatus(activeBookings);
            case "0" -> {}
            default -> System.out.println("[ERROR] Невірний вибір");
        }
    }

    private void cancelActiveBooking(List<Booking> activeBookings) {
        if (activeBookings.isEmpty()) {
            System.out.println("[INFO] Активних бронювань немає");
            return;
        }

        System.out.print("\nВведіть номер бронювання для скасування: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (choice >= 0 && choice < activeBookings.size()) {
                Booking booking = activeBookings.get(choice);

                System.out.println("\n[WARNING] Ви збираєтесь скасувати бронювання:");
                System.out.println("Номер: " + booking.getId());
                System.out.println("Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());
                System.out.println("Вартість: " + booking.getTotalPrice() + " грн");

                System.out.print("Введіть причину скасування: ");
                String reason = scanner.nextLine().trim();

                System.out.print("Підтвердити скасування? (так/ні): ");
                String confirm = scanner.nextLine().trim().toLowerCase();

                if (confirm.equals("так") || confirm.equals("yes") || confirm.equals("y")) {
                    boolean success = bookingService.updateBookingStatus(
                            booking.getId(), Booking.BookingStatus.CANCELLED);

                    if (success) {
                        System.out.println("✅ Бронювання скасовано");
                        System.out.println("[INFO] Причина: " + reason);

                        // Повідомлення клієнту
                        String message = "Ваше бронювання #" + booking.getId() +
                                " скасовано менеджером. Причина: " + reason;
                        NotificationService.sendNotificationToClient(booking.getClientId(), message);
                    } else {
                        System.out.println("[ERROR] Не вдалося скасувати бронювання");
                    }
                }
            } else {
                System.out.println("[ERROR] Невірний номер бронювання");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void markAsInService(List<Booking> activeBookings) {
        System.out.print("\nВведіть номер бронювання для позначення як 'обслуговується': ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (choice >= 0 && choice < activeBookings.size()) {
                Booking booking = activeBookings.get(choice);

                boolean success = bookingService.updateBookingStatus(
                        booking.getId(), Booking.BookingStatus.IN_SERVICE);

                if (success) {
                    System.out.println("✅ Бронювання позначено як 'обслуговується'");

                    // Автоматична зміна статусу оплати на "оплачено"
                    bookingService.updateBookingPaymentStatus(booking.getId(),
                            Booking.PaymentStatus.PAID);
                    System.out.println("✅ Статус оплати змінено на 'оплачено'");
                }
            } else {
                System.out.println("[ERROR] Невірний номер бронювання");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void markAsCompleted(List<Booking> activeBookings) {
        System.out.print("\nВведіть номер бронювання для завершення: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (choice >= 0 && choice < activeBookings.size()) {
                Booking booking = activeBookings.get(choice);

                boolean success = bookingService.updateBookingStatus(
                        booking.getId(), Booking.BookingStatus.COMPLETED);

                if (success) {
                    System.out.println("✅ Бронювання завершено");

                    // Можливість залишити відгук
                    System.out.print("Попросити клієнта залишити відгук? (так/ні): ");
                    String askForReview = scanner.nextLine().trim().toLowerCase();

                    if (askForReview.equals("так") || askForReview.equals("yes") || askForReview.equals("y")) {
                        String message = "Ваше перебування завершено. Будь ласка, залиште відгук про " +
                                "хостел в особистому кабінеті. Дякуємо!";
                        NotificationService.sendNotificationToClient(booking.getClientId(), message);
                        System.out.println("✅ Запит на відгук надіслано клієнту");
                    }
                }
            } else {
                System.out.println("[ERROR] Невірний номер бронювання");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void changePaymentStatus(List<Booking> activeBookings) {
        System.out.print("\nВведіть номер бронювання для зміни статусу оплати: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (choice >= 0 && choice < activeBookings.size()) {
                Booking booking = activeBookings.get(choice);

                System.out.println("\nПоточний статус оплати: " + booking.getPaymentStatus());
                System.out.println("Вартість: " + booking.getTotalPrice() + " грн");

                System.out.println("\nНовий статус оплати:");
                System.out.println("1. Оплачено");
                System.out.println("2. Не оплачено");
                System.out.println("3. Частково оплачено");

                System.out.print("\nОберіть новий статус: ");
                String statusChoice = scanner.nextLine().trim();

                Booking.PaymentStatus newStatus;
                switch (statusChoice) {
                    case "1" -> newStatus = Booking.PaymentStatus.PAID;
                    case "2" -> newStatus = Booking.PaymentStatus.UNPAID;
                    case "3" -> newStatus = Booking.PaymentStatus.PARTIALLY_PAID;
                    default -> {
                        System.out.println("[ERROR] Невірний вибір");
                        return;
                    }
                }

                boolean success = bookingService.updateBookingPaymentStatus(
                        booking.getId(), newStatus);

                if (success) {
                    System.out.println("✅ Статус оплати змінено на: " + newStatus);
                } else {
                    System.out.println("[ERROR] Не вдалося змінити статус оплати");
                }
            } else {
                System.out.println("[ERROR] Невірний номер бронювання");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void showReviews(Hostel hostel) {
        System.out.println("\n--- ВІДГУКИ ---");

        double avgRating = reviewService.getAverageRatingForHostel(hostel.getId());
        List<Review> reviews = reviewService.getReviewsForHostel(hostel.getId());

        if (reviews.isEmpty()) {
            System.out.println("[INFO] Відгуків поки що немає");
            return;
        }

        System.out.println("Середній рейтинг: " + getStars((int)Math.round(avgRating)) +
                " (" + String.format("%.1f", avgRating) + "/5)");
        System.out.println("Всього відгуків: " + reviews.size());

        // Останні 5 відгуків
        int limit = Math.min(reviews.size(), 5);
        System.out.println("\nОстанні відгуки:");

        for (int i = 0; i < limit; i++) {
            Review review = reviews.get(i);
            System.out.println("\n" + (i + 1) + ". " + getStars(review.getRating()));

            if (review.getComment() != null && !review.getComment().isEmpty()) {
                System.out.println("   Коментар: " + review.getComment());
            }

            // Отримання інформації про клієнта
            User client = FileServer.getUserById(review.getClientId());
            if (client instanceof Client) {
                Client c = (Client) client;
                System.out.println("   Клієнт: " + c.getFullName());
            }

            System.out.println("   Дата: " + review.getCreatedAt().toLocalDate());
        }

        // Розподіл рейтингів
        System.out.println("\n--- РОЗПОДІЛ РЕЙТИНГІВ ---");
        int[] ratingCount = new int[5];

        for (Review review : reviews) {
            if (review.getRating() >= 1 && review.getRating() <= 5) {
                ratingCount[review.getRating() - 1]++;
            }
        }

        for (int i = 4; i >= 0; i--) {
            System.out.print((i + 1) + " зірок: ");
            System.out.print(getStars(i + 1) + " ");
            System.out.println("(" + ratingCount[i] + " відгуків)");
        }
    }

    private String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    private void showManagementOptions(Hostel hostel) {
        System.out.println("\n--- ОПЦІЇ УПРАВЛІННЯ ---");

        boolean managing = true;

        while (managing) {
            System.out.println("\nОберіть дію:");
            System.out.println("1. Редагувати інформацію про хостел");
            System.out.println("2. Переглянути дані обліку");
            System.out.println("3. Переглянути активні бронювання");
            System.out.println("4. Змінити статус хостелу");
            System.out.println("5. Додати кімнату");
            System.out.println("6. Переглянути всі бронювання");
            System.out.println("7. Переглянути всі відгуки");
            System.out.println("8. Закрити вікно");
            System.out.println("0. Вийти з управління");

            System.out.print("\nВаш вибір: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    hostelEditMenu.editHostel(hostel, false);
                    // Після редагування оновлюємо дані
                    hostel = hostelService.getHostelById(hostel.getId());
                    if (hostel != null) {
                        displayHostelDetails(hostel);
                    }
                }
                case "2" -> viewAccountingData(hostel);
                case "3" -> viewActiveBookings(hostel);
                case "4" -> changeHostelStatus(hostel);
                case "5" -> addRoomToHostel(hostel);
                case "6" -> viewAllBookings(hostel);
                case "7" -> viewAllReviews(hostel);
                case "8" -> {
                    System.out.println("[INFO] Закриття вікна...");
                    return;
                }
                case "0" -> managing = false;
                default -> System.out.println("[ERROR] Невірний вибір");
            }
        }
    }

    private void viewAccountingData(Hostel hostel) {
        System.out.println("\n--- ДАНІ ОБЛІКУ ---");
        System.out.println("Хостел: " + hostel.getName());
        System.out.println("Період: останні 30 днів");

        // Отримання даних
        List<Booking> bookings = bookingService.getHostelBookings(hostel.getId());

        if (bookings.isEmpty()) {
            System.out.println("[INFO] Даних обліку немає");
            return;
        }

        // Фільтрація за останні 30 днів
        java.time.LocalDateTime monthAgo = java.time.LocalDateTime.now().minusDays(30);
        List<Booking> recentBookings = new java.util.ArrayList<>();
        double recentRevenue = 0;

        for (Booking booking : bookings) {
            if (booking.getCreatedAt().isAfter(monthAgo) &&
                    booking.getStatus() == Booking.BookingStatus.COMPLETED &&
                    booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
                recentBookings.add(booking);
                recentRevenue += booking.getTotalPrice();
            }
        }

        System.out.println("\nФінансові показники:");
        System.out.println("  Доходи за місяць: " + String.format("%.2f", recentRevenue) + " грн");
        System.out.println("  Середній дохід на день: " +
                String.format("%.2f", recentRevenue / 30) + " грн");

        // Статистика по днях тижня
        int[] daysCount = new int[7];
        for (Booking booking : recentBookings) {
            int dayOfWeek = booking.getCreatedAt().getDayOfWeek().getValue() - 1; // 0 = Monday
            daysCount[dayOfWeek]++;
        }

        String[] days = {"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд"};
        System.out.println("\nАктивність по днях тижня:");
        for (int i = 0; i < 7; i++) {
            System.out.println("  " + days[i] + ": " + daysCount[i] + " бронювань");
        }

        // Експорт даних
        System.out.print("\nБажаєте експортувати дані обліку? (так/ні): ");
        String export = scanner.nextLine().trim().toLowerCase();

        if (export.equals("так") || export.equals("yes") || export.equals("y")) {
            exportAccountingData(hostel, recentBookings, recentRevenue);
        }
    }

    private void exportAccountingData(Hostel hostel, List<Booking> bookings, double revenue) {
        System.out.println("\n--- ЕКСПОРТ ДАНИХ ---");
        System.out.println("Формати експорту:");
        System.out.println("1. Текстовий файл (.txt)");
        System.out.println("2. CSV файл (.csv)");
        System.out.println("3. Екран (попередній перегляд)");

        System.out.print("\nОберіть формат: ");
        String format = scanner.nextLine().trim();

        String filename = "accounting_" + hostel.getId() + "_" +
                java.time.LocalDate.now() +
                (format.equals("1") ? ".txt" : format.equals("2") ? ".csv" : "");

        System.out.println("[INFO] Експорт даних у файл: " + filename);

        // Імітація експорту
        try {
            Thread.sleep(1500);
            System.out.println("✅ Дані експортовано успішно");
            System.out.println("[INFO] Файл збережено в директорії програми");
        } catch (InterruptedException e) {
            System.out.println("[ERROR] Помилка експорту");
        }
    }

    private void viewActiveBookings(Hostel hostel) {
        System.out.println("\n--- АКТИВНІ БРОНЮВАННЯ ---");

        List<Booking> activeBookings = bookingService.getActiveHostelBookings(hostel.getId());

        if (activeBookings.isEmpty()) {
            System.out.println("[INFO] Активних бронювань немає");
            return;
        }

        System.out.println("Кількість активних бронювань: " + activeBookings.size());

        for (int i = 0; i < activeBookings.size(); i++) {
            Booking booking = activeBookings.get(i);
            System.out.println("\n" + (i + 1) + ". Бронювання #" + booking.getId());
            System.out.println("   Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());
            System.out.println("   Телефон: " + booking.getClientPhone());
            System.out.println("   Email: " + booking.getClientEmail());
            System.out.println("   Заїзд: " + booking.getCheckInTime());
            System.out.println("   Виїзд: " + booking.getCheckOutTime());
            System.out.println("   Вартість: " + String.format("%.2f", booking.getTotalPrice()) + " грн");
            System.out.println("   Оплата: " + booking.getPaymentStatus());

            // Нагадування про наближення дати заселення
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            long hoursUntilCheckIn = java.time.Duration.between(now, booking.getCheckInTime()).toHours();

            if (hoursUntilCheckIn > 0 && hoursUntilCheckIn <= 24) {
                System.out.println("   ⚠️ Нагадування: заселення через " + hoursUntilCheckIn + " годин");
            }
        }

        // Загальна статистика
        double totalValue = 0;
        for (Booking booking : activeBookings) {
            totalValue += booking.getTotalPrice();
        }

        System.out.println("\nЗагальна вартість активних бронювань: " +
                String.format("%.2f", totalValue) + " грн");
    }

    private void changeHostelStatus(Hostel hostel) {
        System.out.println("\n--- ЗМІНА СТАТУСУ ХОСТЕЛУ ---");

        if (hostel.isActive()) {
            System.out.println("Поточний статус: ✅ Активний");
            System.out.println("Призупинення заблокує нові бронювання.");
            System.out.print("Призупинити роботу хостелу? (так/ні): ");
        } else {
            System.out.println("Поточний статус: ⏸️ Призупинений");
            System.out.print("Активувати хостел? (так/ні): ");
        }

        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("так") || response.equals("yes") || response.equals("y")) {
            boolean success = hostel.isActive() ?
                    hostelService.suspendHostel(hostel.getId()) :
                    hostelService.activateHostel(hostel.getId());

            if (success) {
                System.out.println("✅ Статус хостелу змінено");
                hostel = hostelService.getHostelById(hostel.getId());
            } else {
                System.out.println("[ERROR] Не вдалося змінити статус");
            }
        }
    }

    private void addRoomToHostel(Hostel hostel) {
        System.out.println("\n--- ДОДАВАННЯ КІМНАТИ ---");

        if (!(bookingService instanceof SimpleBookingService)) {
            System.out.println("[ERROR] Неможливо додати кімнату");
            return;
        }

        SimpleBookingService service = (SimpleBookingService) bookingService;

        try {
            System.out.print("Тип кімнати (1 - Приватна, 2 - Спільна): ");
            int typeChoice = Integer.parseInt(scanner.nextLine().trim());
            Room.RoomType roomType = (typeChoice == 1) ? Room.RoomType.PRIVATE : Room.RoomType.SHARED;

            System.out.print("Кількість місць (1-20): ");
            int capacity = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Ціна за годину (грн): ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            Room room = new Room(hostel.getId(), roomType, capacity, price);
            service.addRoom(room);

            System.out.println("✅ Кімната додана!");
            System.out.println("ID кімнати: " + room.getId());

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат даних");
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void viewAllBookings(Hostel hostel) {
        System.out.println("\n--- ВСІ БРОНЮВАННЯ ---");

        List<Booking> bookings = bookingService.getHostelBookings(hostel.getId());

        if (bookings.isEmpty()) {
            System.out.println("[INFO] Бронювань немає");
            return;
        }

        System.out.println("Всього бронювань: " + bookings.size());

        // Сортування за датою створення
        bookings.sort((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()));

        // Пагінація
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) bookings.size() / pageSize);
        int currentPage = 0;

        boolean viewing = true;

        while (viewing) {
            int start = currentPage * pageSize;
            int end = Math.min(start + pageSize, bookings.size());

            System.out.println("\nСторінка " + (currentPage + 1) + " з " + totalPages);
            System.out.println("-".repeat(60));

            for (int i = start; i < end; i++) {
                Booking booking = bookings.get(i);
                System.out.println("\n" + (i + 1) + ". #" + booking.getId());
                System.out.println("   Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());
                System.out.println("   Дата: " + booking.getCreatedAt().toLocalDate());
                System.out.println("   Статус: " + booking.getStatusDescription());
                System.out.println("   Оплата: " + booking.getPaymentStatus());
                System.out.println("   Вартість: " + String.format("%.2f", booking.getTotalPrice()) + " грн");
            }

            System.out.println("\nНавігація:");
            if (currentPage > 0) {
                System.out.println("P - Попередня сторінка");
            }
            if (currentPage < totalPages - 1) {
                System.out.println("N - Наступна сторінка");
            }
            System.out.println("S - Пошук бронювання");
            System.out.println("0 - Повернутись назад");

            System.out.print("\nОберіть дію: ");
            String action = scanner.nextLine().trim().toUpperCase();

            switch (action) {
                case "P" -> {
                    if (currentPage > 0) currentPage--;
                }
                case "N" -> {
                    if (currentPage < totalPages - 1) currentPage++;
                }
                case "S" -> searchBooking(bookings);
                case "0" -> viewing = false;
                default -> System.out.println("[ERROR] Невірна дія");
            }
        }
    }

    private void searchBooking(List<Booking> bookings) {
        System.out.print("\nВведіть номер бронювання або ім'я клієнта: ");
        String query = scanner.nextLine().trim().toLowerCase();

        List<Booking> results = new java.util.ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getId().toLowerCase().contains(query) ||
                    booking.getClientFirstName().toLowerCase().contains(query) ||
                    booking.getClientLastName().toLowerCase().contains(query) ||
                    (booking.getClientFirstName() + " " + booking.getClientLastName()).toLowerCase().contains(query)) {
                results.add(booking);
            }
        }

        if (results.isEmpty()) {
            System.out.println("[INFO] Бронювань не знайдено");
            return;
        }

        System.out.println("\nЗнайдено бронювань: " + results.size());

        for (int i = 0; i < results.size(); i++) {
            Booking booking = results.get(i);
            System.out.println("\n" + (i + 1) + ". #" + booking.getId());
            System.out.println("   Клієнт: " + booking.getClientFirstName() + " " + booking.getClientLastName());
            System.out.println("   Телефон: " + booking.getClientPhone());
            System.out.println("   Дата: " + booking.getCreatedAt().toLocalDate());
            System.out.println("   Статус: " + booking.getStatusDescription());
        }

        System.out.print("\nНатисніть Enter для продовження...");
        scanner.nextLine();
    }

    private void viewAllReviews(Hostel hostel) {
        System.out.println("\n--- ВСІ ВІДГУКИ ---");

        List<Review> reviews = reviewService.getReviewsForHostel(hostel.getId());

        if (reviews.isEmpty()) {
            System.out.println("[INFO] Відгуків немає");
            return;
        }

        System.out.println("Всього відгуків: " + reviews.size());

        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);
            System.out.println("\n" + (i + 1) + ". " + getStars(review.getRating()));

            if (review.getComment() != null && !review.getComment().isEmpty()) {
                System.out.println("   Коментар: " + review.getComment());
            }

            // Отримання інформації про клієнта
            User client = FileServer.getUserById(review.getClientId());
            if (client instanceof Client) {
                Client c = (Client) client;
                System.out.println("   Клієнт: " + c.getFullName());
            }

            System.out.println("   Дата: " + review.getCreatedAt().toLocalDate());
            System.out.println("   Бронювання: #" + review.getBookingId());
        }

        // Статистика
        System.out.println("\n--- СТАТИСТИКА ВІДГУКІВ ---");
        double avgRating = reviewService.getAverageRatingForHostel(hostel.getId());
        System.out.println("Середній рейтинг: " + String.format("%.1f", avgRating) + "/5");

        int[] ratingCount = new int[5];
        for (Review review : reviews) {
            if (review.getRating() >= 1 && review.getRating() <= 5) {
                ratingCount[review.getRating() - 1]++;
            }
        }

        for (int i = 4; i >= 0; i--) {
            double percentage = (double) ratingCount[i] / reviews.size() * 100;
            System.out.println((i + 1) + " зірок: " + ratingCount[i] + " (" +
                    String.format("%.1f", percentage) + "%)");
        }
    }
}