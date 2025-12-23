package com.hostel.service;

import com.hostel.model.*;
import com.hostel.server.FileServer;
import com.hostel.server.SyncManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SimpleBookingService implements BookingService {
    private BookingCache bookingCache = new BookingCache();

    @Override
    public Booking createBooking(String hostelId, String roomId, String clientId,
                                 LocalDateTime checkIn, LocalDateTime checkOut,
                                 String firstName, String lastName, String phone, String email) {

        System.out.println("[BOOKING] Створення бронювання для кімнати: " + roomId);

        long startTime = System.currentTimeMillis();

        // Перевірка мережі
        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[BOOKING ERROR] Відсутнє мережеве з'єднання");

            // Додаємо в чергу синхронізації
            SyncManager.addToSyncQueue("Створення бронювання", () -> {
                createBookingSync(hostelId, roomId, clientId, checkIn, checkOut,
                        firstName, lastName, phone, email);
            });

            System.out.println("[BOOKING] Бронювання додано до черги синхронізації");
            return createOfflineBooking(hostelId, roomId, clientId, checkIn, checkOut,
                    firstName, lastName, phone, email);
        }

        // Отримання кімнати
        Room room = FileServer.getRoomById(roomId);
        if (room == null) {
            System.out.println("[BOOKING ERROR] Кімната не знайдена: " + roomId);
            return null;
        }

        if (!room.isAvailable()) {
            System.out.println("[BOOKING ERROR] Кімната недоступна");
            return null;
        }

        if (!room.getHostelId().equals(hostelId)) {
            System.out.println("[BOOKING ERROR] Кімната не належить вказаному хостелу");
            return null;
        }

        // Розрахунок ціни
        double price = calculatePrice(room, checkIn, checkOut);

        // Створення бронювання
        Booking booking = new Booking(hostelId, roomId, clientId, checkIn, checkOut,
                price, firstName, lastName, phone, email);
        booking.setId("BOOK_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000));

        // Збереження на сервер
        boolean saved = FileServer.saveBooking(booking);

        long elapsedTime = System.currentTimeMillis() - startTime;

        if (!saved) {
            System.out.println("[BOOKING ERROR] Не вдалося зберегти бронювання на сервері");

            // Додаємо в чергу синхронізації
            SyncManager.addToSyncQueue("Повторна спроба збереження бронювання",
                    () -> FileServer.saveBooking(booking));

            return null;
        }

        System.out.println("[BOOKING SUCCESS] Бронювання створено: " + booking.getId());

        // Перевірка часу виконання (Вимога 23: не пізніше 5 секунд)
        if (elapsedTime > 5000) {
            System.out.println("[BOOKING WARNING] Операція зайняла " + elapsedTime + "мс (>5 сек)");
        } else {
            System.out.println("[BOOKING INFO] Операція виконана за " + elapsedTime + "мс");
        }

        // Повідомлення менеджеру (Вимога 79)
        notifyManager(hostelId, booking);

        return booking;
    }

    private Booking createOfflineBooking(String hostelId, String roomId, String clientId,
                                         LocalDateTime checkIn, LocalDateTime checkOut,
                                         String firstName, String lastName, String phone, String email) {
        // Створення тимчасового бронювання для офлайн режиму
        Booking booking = new Booking(hostelId, roomId, clientId, checkIn, checkOut,
                0, firstName, lastName, phone, email);
        booking.setId("OFFLINE_BOOK_" + System.currentTimeMillis());
        booking.setStatus(Booking.BookingStatus.ACTIVE);

        // Кешуємо локально
        List<Booking> cached = bookingCache.getCachedBookings(clientId);
        cached.add(booking);
        bookingCache.cacheBookings(clientId, cached);

        System.out.println("[BOOKING OFFLINE] Створено офлайн-бронювання: " + booking.getId());
        return booking;
    }

    private void createBookingSync(String hostelId, String roomId, String clientId,
                                   LocalDateTime checkIn, LocalDateTime checkOut,
                                   String firstName, String lastName, String phone, String email) {
        // Синхронна версія для черги
        Room room = FileServer.getRoomById(roomId);
        if (room == null || !room.isAvailable()) {
            System.out.println("[SYNC ERROR] Кімната недоступна для синхронізації");
            return;
        }

        double price = calculatePrice(room, checkIn, checkOut);
        Booking booking = new Booking(hostelId, roomId, clientId, checkIn, checkOut,
                price, firstName, lastName, phone, email);
        booking.setId("SYNC_BOOK_" + System.currentTimeMillis());

        if (FileServer.saveBooking(booking)) {
            System.out.println("[SYNC SUCCESS] Бронювання синхронізовано: " + booking.getId());
        } else {
            System.out.println("[SYNC ERROR] Не вдалося синхронізувати бронювання");
        }
    }

    private void notifyManager(String hostelId, Booking booking) {
        Hostel hostel = FileServer.getHostelById(hostelId);
        if (hostel != null) {
            String message = String.format(
                    "Нове бронювання #%s для хостелу '%s'. Клієнт: %s %s",
                    booking.getId(), hostel.getName(),
                    booking.getClientFirstName(), booking.getClientLastName()
            );
            NotificationService.sendNotificationToManager(hostel.getManagerId(), message);
        }
    }

    @Override
    public boolean cancelBooking(String bookingId, String initiatorId) {
        System.out.println("[BOOKING] Скасування бронювання: " + bookingId);

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[BOOKING ERROR] Відсутнє мережеве з'єднання");
            return false;
        }

        // Перевірка бронювання
        Booking booking = FileServer.getBookingById(bookingId);
        if (booking == null) {
            System.out.println("[BOOKING ERROR] Бронювання не знайдено");
            return false;
        }

        // Зміна статусу
        boolean updated = FileServer.updateBookingStatus(bookingId, Booking.BookingStatus.CANCELLED);

        if (updated) {
            System.out.println("[BOOKING SUCCESS] Бронювання скасовано: " + bookingId);

            // Повідомлення клієнту (Вимога 80)
            if (initiatorId.startsWith("MANAGER")) {
                String message = String.format(
                        "Ваше бронювання #%s скасовано менеджером. Причина: за запитом адміністрації",
                        bookingId
                );
                NotificationService.sendNotificationToClient(booking.getClientId(), message);
            }

            return true;
        }

        return false;
    }

    @Override
    public List<Booking> getUserBookings(String userId) {
        System.out.println("[BOOKING] Завантаження бронювань користувача: " + userId);

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[BOOKING INFO] Офлайн режим - використовуються кешовані дані");
            return bookingCache.getCachedBookings(userId);
        }

        // Завантаження з сервера
        List<Booking> bookings = FileServer.getUserBookings(userId);

        // Кешування
        bookingCache.cacheBookings(userId, bookings);

        return bookings;
    }

    @Override
    public List<Booking> getHostelBookings(String hostelId) {
        return FileServer.getHostelBookings(hostelId);
    }

    @Override
    public List<Booking> getActiveHostelBookings(String hostelId) {
        return FileServer.getActiveHostelBookings(hostelId);
    }

    @Override
    public Booking getBookingById(String bookingId) {
        return FileServer.getBookingById(bookingId);
    }

    @Override
    public double calculatePrice(Room room, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null || checkOut.isBefore(checkIn)) {
            throw new IllegalArgumentException("Некоректний діапазон дат");
        }

        Duration duration = Duration.between(checkIn, checkOut);
        long hours = duration.toHours();

        if (hours < 1) {
            hours = 1;
        }

        return room.getPricePerHour() * hours;
    }

    @Override
    public boolean updateBookingStatus(String bookingId, Booking.BookingStatus status) {
        return FileServer.updateBookingStatus(bookingId, status);
    }

    @Override
    public boolean updateBookingPaymentStatus(String bookingId, Booking.PaymentStatus status) {
        return FileServer.updateBookingPaymentStatus(bookingId, status);
    }

    // Додаткові методи для кімнат
    public List<Room> getRoomsByHostel(String hostelId) {
        return FileServer.getRoomsByHostel(hostelId);
    }

    public List<Room> getAllRoomsByHostel(String hostelId) {
        return FileServer.getAllRoomsByHostel(hostelId);
    }

    public void addRoom(Room room) {
        room.setId("ROOM_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000));
        FileServer.saveRoom(room);
    }
}