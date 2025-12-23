package com.hostel.utils;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileManager {
    private static final String APP_DATA_DIR = "hostel_system_data/";
    private static final String BACKUP_DIR = APP_DATA_DIR + "backups/";
    private static final String EXPORT_DIR = APP_DATA_DIR + "exports/";
    private static final String LOGS_DIR = APP_DATA_DIR + "logs/";

    /**
     * Ініціалізація директорій програми
     */
    public static void initializeDirectories() {
        createDirectoryIfNotExists(APP_DATA_DIR);
        createDirectoryIfNotExists(BACKUP_DIR);
        createDirectoryIfNotExists(EXPORT_DIR);
        createDirectoryIfNotExists(LOGS_DIR);
    }

    private static void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("[FILE] Створено директорію: " + path);
            } else {
                System.err.println("[FILE ERROR] Не вдалося створити директорію: " + path);
            }
        }
    }

    /**
     * Створення резервної копії файлу
     */
    public static boolean createBackup(String sourceFilePath) {
        try {
            File sourceFile = new File(sourceFilePath);
            if (!sourceFile.exists()) {
                return false;
            }

            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = sourceFile.getName() + "_backup_" + timestamp;
            String backupFilePath = BACKUP_DIR + backupFileName;

            Files.copy(sourceFile.toPath(), new File(backupFilePath).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            System.out.println("[BACKUP] Створено резервну копію: " + backupFileName);
            return true;

        } catch (IOException e) {
            System.err.println("[BACKUP ERROR] Помилка створення резервної копії: " + e.getMessage());
            return false;
        }
    }

    /**
     * Створення резервної копії всіх даних
     */
    public static void createFullBackup() {
        System.out.println("[BACKUP] Створення повної резервної копії даних...");

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFolder = BACKUP_DIR + "full_backup_" + timestamp + "/";

        createDirectoryIfNotExists(backupFolder);

        // Копіювання файлів даних
        copyDataFiles(backupFolder);

        // Створення файлу з інформацією про бекап
        createBackupInfoFile(backupFolder, timestamp);

        System.out.println("[BACKUP] Повна резервна копія створена: " + backupFolder);
    }

    private static void copyDataFiles(String backupFolder) {
        String[] dataFiles = {
                "hostel_data/users.json",
                "hostel_data/hostels.json",
                "hostel_data/rooms.json",
                "hostel_data/bookings.json",
                "hostel_data/reviews.json",
                "hostel_data/server.log"
        };

        int copied = 0;
        int failed = 0;

        for (String filePath : dataFiles) {
            File sourceFile = new File(filePath);
            if (sourceFile.exists()) {
                try {
                    String destPath = backupFolder + sourceFile.getName();
                    Files.copy(sourceFile.toPath(), new File(destPath).toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                } catch (IOException e) {
                    System.err.println("[BACKUP ERROR] Не вдалося скопіювати: " + filePath);
                    failed++;
                }
            }
        }

        System.out.println(String.format(
                "[BACKUP] Скопійовано файлів: %d, не вдалося: %d", copied, failed));
    }

    private static void createBackupInfoFile(String backupFolder, String timestamp) {
        String infoFile = backupFolder + "backup_info.txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(infoFile))) {
            writer.println("=== ІНФОРМАЦІЯ ПРО РЕЗЕРВНУ КОПІЮ ===");
            writer.println("Дата створення: " + LocalDateTime.now());
            writer.println("Версія системи: 1.0.0");
            writer.println("Тип бекапу: Повний");
            writer.println("Розмір даних: " + getFolderSize(new File("hostel_data")) + " байт");
            writer.println("================================");
        } catch (IOException e) {
            System.err.println("[BACKUP ERROR] Не вдалося створити файл інформації");
        }
    }

    private static long getFolderSize(File folder) {
        long length = 0;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    length += getFolderSize(file);
                }
            }
        } else {
            length = folder.length();
        }
        return length;
    }

    /**
     * Очищення старих резервних копій
     */
    public static void cleanupOldBackups(int keepDays) {
        System.out.println("[CLEANUP] Очищення старих резервних копій...");

        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return;
        }

        File[] backups = backupDir.listFiles();
        if (backups == null) {
            return;
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(keepDays);
        int deleted = 0;

        for (File backup : backups) {
            if (backup.isDirectory()) {
                // Для папок бекапів
                String folderName = backup.getName();
                if (folderName.startsWith("full_backup_")) {
                    String dateStr = folderName.substring(12, 26); // yyyyMMdd_HHmmss
                    LocalDateTime backupDate = parseBackupDate(dateStr);

                    if (backupDate != null && backupDate.isBefore(cutoffDate)) {
                        if (deleteDirectory(backup)) {
                            deleted++;
                        }
                    }
                }
            } else {
                // Для окремих файлів бекапів
                String fileName = backup.getName();
                if (fileName.contains("_backup_")) {
                    String dateStr = fileName.substring(
                            fileName.lastIndexOf("_backup_") + 8,
                            fileName.lastIndexOf("_backup_") + 22
                    );
                    LocalDateTime backupDate = parseBackupDate(dateStr);

                    if (backupDate != null && backupDate.isBefore(cutoffDate)) {
                        if (backup.delete()) {
                            deleted++;
                        }
                    }
                }
            }
        }

        System.out.println("[CLEANUP] Видалено старих бекапів: " + deleted);
    }

    private static LocalDateTime parseBackupDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr,
                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        return dir.delete();
    }

    /**
     * Експорт даних у текстовий файл
     */
    public static boolean exportToTextFile(String data, String fileName) {
        String filePath = EXPORT_DIR + fileName + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("=== ЕКСПОРТ ДАНИХ ===");
            writer.println("Дата експорту: " + LocalDateTime.now());
            writer.println("=========================");
            writer.println();
            writer.print(data);

            System.out.println("[EXPORT] Дані експортовано у файл: " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("[EXPORT ERROR] Помилка експорту: " + e.getMessage());
            return false;
        }
    }

    /**
     * Експорт даних у CSV файл
     */
    public static boolean exportToCsv(List<String[]> data, String fileName, String[] headers) {
        String filePath = EXPORT_DIR + fileName + ".csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Запис заголовків
            writer.println(String.join(",", headers));

            // Запис даних
            for (String[] row : data) {
                writer.println(String.join(",", row));
            }

            System.out.println("[EXPORT] Дані експортовано у CSV файл: " + filePath);
            return true;

        } catch (IOException e) {
            System.err.println("[EXPORT ERROR] Помилка експорту CSV: " + e.getMessage());
            return false;
        }
    }

    /**
     * Логування подій
     */
    public static void logEvent(String event, String level) {
        String logFile = LOGS_DIR + "system_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                ".log";

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, event);

        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("[LOG ERROR] Помилка запису в лог: " + e.getMessage());
        }
    }

    /**
     * Читання файлу
     */
    public static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            System.err.println("[FILE ERROR] Помилка читання файлу: " + e.getMessage());
            return null;
        }
    }

    /**
     * Запис у файл
     */
    public static boolean writeFile(String filePath, String content) {
        try {
            Files.write(Paths.get(filePath), content.getBytes());
            return true;
        } catch (IOException e) {
            System.err.println("[FILE ERROR] Помилка запису у файл: " + e.getMessage());
            return false;
        }
    }

    /**
     * Перевірка наявності файлу
     */
    public static boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * Отримання розміру файлу
     */
    public static long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.exists() ? file.length() : 0;
    }

    /**
     * Видалення файлу
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    /**
     * Отримання списку файлів у директорії
     */
    public static List<String> listFiles(String directory, String extension) {
        List<String> files = new ArrayList<>();
        File dir = new File(directory);

        if (dir.exists() && dir.isDirectory()) {
            File[] dirFiles = dir.listFiles();
            if (dirFiles != null) {
                for (File file : dirFiles) {
                    if (file.isFile() &&
                            (extension == null || file.getName().endsWith(extension))) {
                        files.add(file.getName());
                    }
                }
            }
        }

        return files;
    }

    /**
     * Отримання інформації про файл
     */
    public static Map<String, Object> getFileInfo(String filePath) {
        Map<String, Object> info = new HashMap<>();
        File file = new File(filePath);

        if (file.exists()) {
            info.put("exists", true);
            info.put("name", file.getName());
            info.put("path", file.getAbsolutePath());
            info.put("size", file.length());
            info.put("lastModified", new Date(file.lastModified()));
            info.put("isDirectory", file.isDirectory());
            info.put("isFile", file.isFile());
            info.put("canRead", file.canRead());
            info.put("canWrite", file.canWrite());
        } else {
            info.put("exists", false);
        }

        return info;
    }

    /**
     * Створення тимчасового файлу
     */
    public static String createTempFile(String prefix, String suffix) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("[TEMP FILE ERROR] Помилка створення тимчасового файлу: " + e.getMessage());
            return null;
        }
    }

    /**
     * Копіювання файлу
     */
    public static boolean copyFile(String sourcePath, String destPath) {
        try {
            Files.copy(Paths.get(sourcePath), Paths.get(destPath),
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("[COPY ERROR] Помилка копіювання файлу: " + e.getMessage());
            return false;
        }
    }

    /**
     * Переміщення файлу
     */
    public static boolean moveFile(String sourcePath, String destPath) {
        try {
            Files.move(Paths.get(sourcePath), Paths.get(destPath),
                    StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("[MOVE ERROR] Помилка переміщення файлу: " + e.getMessage());
            return false;
        }
    }
}
