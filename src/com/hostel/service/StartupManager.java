package com.hostel.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.hostel.server.SyncManager;

public class StartupManager {
    private long startTime;

    public void startApplication() {
        startTime = System.currentTimeMillis();

        System.out.println("\n" + "=".repeat(50));
        System.out.println("СИСТЕМА УПРАВЛІННЯ ХОСТЕЛОМ");
        System.out.println("Запуск: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
        System.out.println("=".repeat(50) + "\n");

        // Перевірка мережі
        checkNetworkStatus();

        // Запуск служб
        startServices();

        // Завантаження даних
        loadInitialData();

        long elapsedTime = System.currentTimeMillis() - startTime;

        // Вимога 1: не пізніше 3 секунд
        if (elapsedTime > 3000) {
            System.out.println("[STARTUP WARNING] Запуск зайняв " + elapsedTime + "мс (>3 сек)");
        } else {
            System.out.println("[STARTUP INFO] Система запущена за " + elapsedTime + "мс");
        }

        showWelcomeMessage();
    }

    private void checkNetworkStatus() {
        boolean hasNetwork = NetworkMonitor.checkConnection();
        System.out.println("[NETWORK] Статус: " +
                (hasNetwork ? "Онлайн" : "Офлайн") + " " + NetworkMonitor.getNetworkStatus());

        if (!hasNetwork) {
            System.out.println("[NETWORK WARNING] Робота в офлайн-режимі");
            System.out.println("Деякі функції будуть обмежені до відновлення з'єднання");
        }
    }

    private void startServices() {
        System.out.println("[SERVICES] Запуск служб...");

        // Запуск служби нагадувань
        NotificationService.startReminderService();

        // Ініціалізація сервера (файлової системи)
        com.hostel.server.FileServer.getServerStats();

        System.out.println("[SERVICES] Служби запущено");
    }

    private void loadInitialData() {
        System.out.println("[DATA] Завантаження даних...");
        // Дані завантажуються автоматично в FileServer
        System.out.println("[DATA] Дані завантажено");
    }

    private void showWelcomeMessage() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Ласкаво просимо до системи управління хостелом!");
        System.out.println("Версія: 1.0.0");
        System.out.println("Режим: Консольний інтерфейс");
        System.out.println("=".repeat(50) + "\n");
    }

    public long getStartupTime() {
        return System.currentTimeMillis() - startTime;
    }

    public void shutdown() {
        System.out.println("\n[SHUTDOWN] Завершення роботи системи...");

        // Зупинка служб
        NotificationService.stopService();

        // Синхронізація черги
        if (SyncManager.getQueueSize() > 0 && NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[SHUTDOWN] Синхронізація офлайн-даних...");
            SyncManager.forceSync();
        }

        System.out.println("[SHUTDOWN] Система зупинена");
    }
}
