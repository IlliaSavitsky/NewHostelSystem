package com.hostel.server;

import java.util.*;
import java.util.concurrent.*;
import com.hostel.service.NetworkMonitor;

public class SyncManager {
    private static Queue<Runnable> syncQueue = new ConcurrentLinkedQueue<>();
    private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private static boolean isSyncing = false;

    static {
        // Запускаємо синхронізацію кожні 30 секунд
        executor.scheduleAtFixedRate(() -> {
            if (NetworkMonitor.isNetworkAvailable() && !syncQueue.isEmpty() && !isSyncing) {
                processSyncQueue();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public static void addToSyncQueue(Runnable task) {
        syncQueue.add(task);
        System.out.println("[SYNC] Додано завдання до черги синхронізації. Розмір черги: " + syncQueue.size());
    }

    public static void addToSyncQueue(String description, Runnable task) {
        syncQueue.add(() -> {
            System.out.println("[SYNC] Виконується: " + description);
            task.run();
        });
        System.out.println("[SYNC] Додано: " + description + ". Розмір черги: " + syncQueue.size());
    }

    private static void processSyncQueue() {
        isSyncing = true;
        System.out.println("[SYNC] Початок синхронізації. Завдань в черзі: " + syncQueue.size());

        int processed = 0;
        int failed = 0;

        while (!syncQueue.isEmpty() && NetworkMonitor.isNetworkAvailable()) {
            Runnable task = syncQueue.poll();
            try {
                task.run();
                processed++;

                // Невелика пауза між завданнями для імітації мережі
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            } catch (Exception e) {
                System.err.println("[SYNC ERROR] Помилка виконання завдання: " + e.getMessage());
                failed++;
                // Повертаємо завдання назад в чергу
                syncQueue.add(task);
            }
        }

        System.out.println(String.format(
                "[SYNC] Синхронізація завершена. Оброблено: %d, Не вдалося: %d, Залишилось: %d",
                processed, failed, syncQueue.size()
        ));

        isSyncing = false;

        if (!syncQueue.isEmpty() && !NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[SYNC] Зупинено через відсутність мережі. Очікування відновлення...");
        }
    }

    public static int getQueueSize() {
        return syncQueue.size();
    }

    public static void clearQueue() {
        syncQueue.clear();
        System.out.println("[SYNC] Черга синхронізації очищена");
    }

    public static void forceSync() {
        if (!isSyncing && NetworkMonitor.isNetworkAvailable()) {
            processSyncQueue();
        }
    }
}
