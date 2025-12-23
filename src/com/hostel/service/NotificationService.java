package com.hostel.service;

import com.hostel.model.Booking;
import com.hostel.server.FileServer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NotificationService {
    private static Timer reminderTimer;
    private static Map<String, List<Booking>> pendingReminders = new HashMap<>();

    public static void startReminderService() {
        if (reminderTimer != null) {
            reminderTimer.cancel();
        }

        reminderTimer = new Timer(true);

        // Перевірка кожні 5 хвилин
        reminderTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkBookingReminders();
            }
        }, 0, 5 * 60 * 1000); // 5 хвилин

        System.out.println("[NOTIFICATIONS] Служба нагадувань запущена");
    }

    private static void checkBookingReminders() {
        // Отримуємо всі активні бронювання
        List<Booking> allBookings = new ArrayList<>();

        // Отримуємо всі хостели та їх бронювання
        // (тут спрощено, в реальній системі була б оптимізація)
        FileServer.getServerStats(); // Тільки для ініціалізації

        // Перевіряємо кожне бронювання
        LocalDateTime now = LocalDateTime.now();

        // Спрощена імітація
        System.out.println("[NOTIFICATIONS] Перевірка нагадувань... (" + now + ")");

        // Тут була б реальна логіка перевірки
        // Наразі просто імітуємо
        if (Math.random() > 0.7) {
            System.out.println("[NOTIFICATIONS] Знайдено бронювання для нагадування");
        }
    }

    public static void addBookingReminder(Booking booking) {
        String hostelId = booking.getHostelId();
        pendingReminders.computeIfAbsent(hostelId, k -> new ArrayList<>()).add(booking);
        System.out.println("[NOTIFICATIONS] Додано нагадування для бронювання: " + booking.getId());
    }

    public static void sendNotificationToClient(String clientId, String message) {
        System.out.println("[NOTIFICATION TO CLIENT " + clientId + "] " + message);
        // В реальній системі тут був би email/SMS
    }

    public static void sendNotificationToManager(String managerId, String message) {
        System.out.println("[NOTIFICATION TO MANAGER " + managerId + "] " + message);
        // В реальній системі тут був би email/SMS
    }

    public static void stopService() {
        if (reminderTimer != null) {
            reminderTimer.cancel();
            reminderTimer = null;
        }
        System.out.println("[NOTIFICATIONS] Служба нагадувань зупинена");
    }
}
