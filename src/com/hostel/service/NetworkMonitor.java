package com.hostel.service;

import java.util.Random;

public class NetworkMonitor {
    private static boolean isConnected = true;
    private static boolean warningShown = false;
    private static Random random = new Random();

    public static boolean checkConnection() {
        // Імітація мережі з 90% ймовірністю успіху
        isConnected = random.nextDouble() > 0.1;
        return isConnected;
    }

    public static void setConnected(boolean connected) {
        boolean previousState = isConnected;
        isConnected = connected;

        if (previousState && !connected) {
            showNetworkWarning();
        } else if (!previousState && connected) {
            System.out.println("[INFO] Мережеве з'єднання відновлено");
            warningShown = false;
        }
    }

    private static void showNetworkWarning() {
        if (!warningShown) {
            System.out.println("\n[WARNING] ВІДСУТНЄ МЕРЕЖЕВЕ З'ЄДНАННЯ!");
            System.out.println("Деякі функції можуть бути обмежені.");
            System.out.println("Будь ласка, перевірте підключення до інтернету.");
            warningShown = true;
        }
    }

    public static boolean isNetworkAvailable() {
        return isConnected;
    }

    public static String getNetworkStatus() {
        return isConnected ? "[ONLINE]" : "[OFFLINE]";
    }

    public static void simulateNetworkIssue() {
        setConnected(false);
        System.out.println("[SIMULATION] Імітація мережевої проблеми");

        // Автоматичне відновлення через 10-30 секунд
        new Thread(() -> {
            try {
                int recoveryTime = 10000 + random.nextInt(20000);
                Thread.sleep(recoveryTime);
                setConnected(true);
                System.out.println("[SIMULATION] Мережа автоматично відновлена");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
