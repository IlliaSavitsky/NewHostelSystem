package com.hostel.server;

import com.hostel.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileServer {
    private static final String DATA_DIR = "hostel_data/";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    // Кеші даних
    private static Map<String, User> usersCache = new ConcurrentHashMap<>();
    private static Map<String, Hostel> hostelsCache = new ConcurrentHashMap<>();
    private static Map<String, Room> roomsCache = new ConcurrentHashMap<>();
    private static Map<String, Booking> bookingsCache = new ConcurrentHashMap<>();
    private static Map<String, Review> reviewsCache = new ConcurrentHashMap<>();

    // Статистика
    private static int totalOperations = 0;
    private static LocalDateTime lastSyncTime = LocalDateTime.now();

    static {
        initializeDataDirectory();
        loadAllData();
    }

    private static void initializeDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            Files.createDirectories(Paths.get(DATA_DIR + "users"));
            Files.createDirectories(Paths.get(DATA_DIR + "hostels"));
            Files.createDirectories(Paths.get(DATA_DIR + "rooms"));
            Files.createDirectories(Paths.get(DATA_DIR + "bookings"));
            Files.createDirectories(Paths.get(DATA_DIR + "reviews"));
            Files.createDirectories(Paths.get(DATA_DIR + "logs"));

            System.out.println("[SERVER] Директорії даних створено");
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Помилка створення директорій: " + e.getMessage());
        }
    }

    private static void loadAllData() {
        System.out.println("[SERVER] Завантаження даних з файлів...");

        // Завантажуємо Client та Manager окремо
        Map<String, Client> clients = loadFromFile(DATA_DIR + "clients.json",
                new TypeToken<Map<String, Client>>(){}.getType());

        Map<String, Manager> managers = loadFromFile(DATA_DIR + "managers.json",
                new TypeToken<Map<String, Manager>>(){}.getType());

        // Об'єднуємо в один кеш
        usersCache = new ConcurrentHashMap<>();
        if (clients != null) {
            clients.forEach((id, client) -> usersCache.put(id, client));
        }
        if (managers != null) {
            managers.forEach((id, manager) -> usersCache.put(id, manager));
        }

        hostelsCache = loadFromFile(DATA_DIR + "hostels.json",
                new TypeToken<Map<String, Hostel>>(){}.getType());
        if (hostelsCache == null) hostelsCache = new ConcurrentHashMap<>();

        roomsCache = loadFromFile(DATA_DIR + "rooms.json",
                new TypeToken<Map<String, Room>>(){}.getType());
        if (roomsCache == null) roomsCache = new ConcurrentHashMap<>();

        bookingsCache = loadFromFile(DATA_DIR + "bookings.json",
                new TypeToken<Map<String, Booking>>(){}.getType());
        if (bookingsCache == null) bookingsCache = new ConcurrentHashMap<>();

        reviewsCache = loadFromFile(DATA_DIR + "reviews.json",
                new TypeToken<Map<String, Review>>(){}.getType());
        if (reviewsCache == null) reviewsCache = new ConcurrentHashMap<>();

        System.out.println(String.format(
                "[SERVER] Дані завантажено: %d користувачів, %d хостелів, %d кімнат, %d бронювань, %d відгуків",
                usersCache.size(), hostelsCache.size(), roomsCache.size(),
                bookingsCache.size(), reviewsCache.size()
        ));
    }

    private static <T> T loadFromFile(String filePath, Type type) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            try (Reader reader = new FileReader(file)) {
                return gson.fromJson(reader, type);
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Помилка завантаження з " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    private static synchronized void saveToFile(String filePath, Object data) {
        try {
            try (Writer writer = new FileWriter(filePath)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Помилка збереження в " + filePath + ": " + e.getMessage());
        }
    }

    private static void logEvent(String event) {
        String logEntry = LocalDateTime.now() + " - " + event;
        try {
            Files.write(Paths.get(DATA_DIR + "server.log"),
                    (logEntry + "\n").getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Помилка логування: " + e.getMessage());
        }
    }

    // ========== API МЕТОДИ ==========

    // Користувачі
    public static User findUserByEmailOrPhone(String emailOrPhone) {
        for (User user : usersCache.values()) {
            if (user.getEmail().equals(emailOrPhone) || user.getPhone().equals(emailOrPhone)) {
                return user;
            }
        }
        return null;
    }

    private static void saveUsers() {
        Map<String, Client> clients = new HashMap<>();
        Map<String, Manager> managers = new HashMap<>();

        for (User user : usersCache.values()) {
            if (user instanceof Client) {
                clients.put(user.getId(), (Client) user);
            } else if (user instanceof Manager) {
                managers.put(user.getId(), (Manager) user);
            }
        }

        saveToFile(DATA_DIR + "clients.json", clients);
        saveToFile(DATA_DIR + "managers.json", managers);
    }

    public static boolean saveUser(User user) {
        usersCache.put(user.getId(), user);
        saveUsers(); // Використовуємо новий метод
        totalOperations++;
        logEvent("Збережено користувача: " + user.getId());
        return true;
    }

    public static User getUserById(String userId) {
        return usersCache.get(userId);
    }

    // Хостели
    public static boolean saveHostel(Hostel hostel) {
        hostelsCache.put(hostel.getId(), hostel);
        saveToFile(DATA_DIR + "hostels.json", hostelsCache);
        totalOperations++;
        logEvent("Збережено хостел: " + hostel.getId());
        return true;
    }

    public static Hostel getHostelById(String hostelId) {
        return hostelsCache.get(hostelId);
    }

    public static List<Hostel> getHostelsByManager(String managerId) {
        List<Hostel> result = new ArrayList<>();
        for (Hostel hostel : hostelsCache.values()) {
            if (hostel.getManagerId().equals(managerId)) {
                result.add(hostel);
            }
        }
        return result;
    }

    public static List<Hostel> searchHostels(String city, String query) {
        List<Hostel> result = new ArrayList<>();
        for (Hostel hostel : hostelsCache.values()) {
            if (hostel.getCity().equalsIgnoreCase(city) && hostel.isActive()) {
                if (query.isEmpty() ||
                        hostel.getName().toLowerCase().contains(query.toLowerCase()) ||
                        hostel.getAddress().toLowerCase().contains(query.toLowerCase())) {
                    result.add(hostel);
                }
            }
        }
        return result;
    }

    public static boolean updateHostel(Hostel hostel) {
        if (hostelsCache.containsKey(hostel.getId())) {
            hostelsCache.put(hostel.getId(), hostel);
            saveToFile(DATA_DIR + "hostels.json", hostelsCache);
            logEvent("Оновлено хостел: " + hostel.getId());
            return true;
        }
        return false;
    }

    public static boolean deleteHostel(String hostelId) {
        Hostel removed = hostelsCache.remove(hostelId);
        if (removed != null) {
            saveToFile(DATA_DIR + "hostels.json", hostelsCache);
            logEvent("Видалено хостел: " + hostelId);

            // Видаляємо всі кімнати цього хостела
            roomsCache.entrySet().removeIf(entry -> entry.getValue().getHostelId().equals(hostelId));
            saveToFile(DATA_DIR + "rooms.json", roomsCache);

            return true;
        }
        return false;
    }

    // Кімнати
    public static boolean saveRoom(Room room) {
        roomsCache.put(room.getId(), room);
        saveToFile(DATA_DIR + "rooms.json", roomsCache);
        totalOperations++;
        logEvent("Збережено кімнату: " + room.getId());
        return true;
    }

    public static List<Room> getRoomsByHostel(String hostelId) {
        List<Room> result = new ArrayList<>();
        for (Room room : roomsCache.values()) {
            if (room.getHostelId().equals(hostelId) && room.isAvailable()) {
                result.add(room);
            }
        }
        return result;
    }

    public static List<Room> getAllRoomsByHostel(String hostelId) {
        List<Room> result = new ArrayList<>();
        for (Room room : roomsCache.values()) {
            if (room.getHostelId().equals(hostelId)) {
                result.add(room);
            }
        }
        return result;
    }

    public static Room getRoomById(String roomId) {
        return roomsCache.get(roomId);
    }

    // Бронювання
    public static boolean saveBooking(Booking booking) {
        bookingsCache.put(booking.getId(), booking);
        saveToFile(DATA_DIR + "bookings.json", bookingsCache);
        totalOperations++;

        // Оновлення доступності кімнати
        Room room = roomsCache.get(booking.getRoomId());
        if (room != null && booking.getStatus() == Booking.BookingStatus.ACTIVE) {
            room.setAvailable(false);
            saveRoom(room);
        }

        logEvent("Збережено бронювання: " + booking.getId());
        return true;
    }

    public static List<Booking> getUserBookings(String userId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookingsCache.values()) {
            if (booking.getClientId().equals(userId)) {
                result.add(booking);
            }
        }
        result.sort((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()));
        return result;
    }

    public static List<Booking> getHostelBookings(String hostelId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookingsCache.values()) {
            if (booking.getHostelId().equals(hostelId)) {
                result.add(booking);
            }
        }
        return result;
    }

    public static List<Booking> getActiveHostelBookings(String hostelId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookingsCache.values()) {
            if (booking.getHostelId().equals(hostelId) &&
                    booking.getStatus() == Booking.BookingStatus.ACTIVE) {
                result.add(booking);
            }
        }
        return result;
    }

    public static Booking getBookingById(String bookingId) {
        return bookingsCache.get(bookingId);
    }

    public static boolean updateBookingStatus(String bookingId, Booking.BookingStatus status) {
        Booking booking = bookingsCache.get(bookingId);
        if (booking != null) {
            booking.setStatus(status);

            // Якщо бронювання скасовано або завершено - звільняємо кімнату
            if (status == Booking.BookingStatus.CANCELLED ||
                    status == Booking.BookingStatus.COMPLETED) {
                Room room = roomsCache.get(booking.getRoomId());
                if (room != null) {
                    room.setAvailable(true);
                    saveRoom(room);
                }
            }

            saveToFile(DATA_DIR + "bookings.json", bookingsCache);
            logEvent("Оновлено статус бронювання " + bookingId + " на " + status);
            return true;
        }
        return false;
    }

    public static boolean updateBookingPaymentStatus(String bookingId, Booking.PaymentStatus status) {
        Booking booking = bookingsCache.get(bookingId);
        if (booking != null) {
            booking.setPaymentStatus(status);
            saveToFile(DATA_DIR + "bookings.json", bookingsCache);
            logEvent("Оновлено статус оплати бронювання " + bookingId + " на " + status);
            return true;
        }
        return false;
    }

    // Відгуки
    public static boolean saveReview(Review review) {
        reviewsCache.put(review.getId(), review);
        saveToFile(DATA_DIR + "reviews.json", reviewsCache);
        totalOperations++;

        // Оновлення рейтингу в бронюванні
        Booking booking = bookingsCache.get(review.getBookingId());
        if (booking != null) {
            booking.setRating(review.getRating());
            booking.setReview(review.getComment());
            saveToFile(DATA_DIR + "bookings.json", bookingsCache);
        }

        logEvent("Збережено відгук: " + review.getId());
        return true;
    }

    public static List<Review> getReviewsForHostel(String hostelId) {
        List<Review> result = new ArrayList<>();
        for (Review review : reviewsCache.values()) {
            if (review.getHostelId().equals(hostelId)) {
                result.add(review);
            }
        }
        result.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        return result;
    }

    public static double getAverageRatingForHostel(String hostelId) {
        List<Review> reviews = getReviewsForHostel(hostelId);
        if (reviews.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    // Статистика
    public static Map<String, Object> getServerStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", usersCache.size());
        stats.put("totalHostels", hostelsCache.size());
        stats.put("totalRooms", roomsCache.size());
        stats.put("totalBookings", bookingsCache.size());
        stats.put("totalReviews", reviewsCache.size());
        stats.put("totalOperations", totalOperations);
        stats.put("lastSyncTime", lastSyncTime);
        stats.put("currentTime", LocalDateTime.now());
        return stats;
    }

    public static void printServerStats() {
        Map<String, Object> stats = getServerStats();
        System.out.println("\n=== СТАТИСТИКА СЕРВЕРА ===");
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            System.out.printf("%-20s: %s%n", entry.getKey(), entry.getValue());
        }
        System.out.println("=========================\n");
    }
}
