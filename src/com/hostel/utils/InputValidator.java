package com.hostel.utils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class InputValidator {

    // Паттерни для валідації
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+380\\d{9}$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[А-Яа-яA-Za-z\\s'-]{2,50}$");

    /**
     * Валідація email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Валідація номера телефону
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Валідація імені або прізвища
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Валідація пароля
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        // Перевірка на наявність хоча б однієї цифри
        boolean hasDigit = false;
        boolean hasLetter = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (Character.isLetter(c)) {
                hasLetter = true;
            }
        }

        return hasDigit && hasLetter;
    }

    /**
     * Валідація ціни
     */
    public static boolean isValidPrice(double price) {
        return price > 0 && price <= 1000;
    }

    /**
     * Валідація кількості місць
     */
    public static boolean isValidCapacity(int capacity) {
        return capacity >= 1 && capacity <= 20;
    }

    /**
     * Валідація рейтингу
     */
    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    /**
     * Валідація дати заселення
     */
    public static boolean isValidCheckInDate(LocalDateTime checkIn) {
        if (checkIn == null) {
            return false;
        }
        return !checkIn.isBefore(LocalDateTime.now());
    }

    /**
     * Валідація діапазону дат
     */
    public static boolean isValidDateRange(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) {
            return false;
        }
        return checkOut.isAfter(checkIn) &&
                !checkIn.isBefore(LocalDateTime.now());
    }

    /**
     * Валідація мінімальної тривалості
     */
    public static boolean isValidDuration(LocalDateTime checkIn, LocalDateTime checkOut, int minHours) {
        if (checkIn == null || checkOut == null) {
            return false;
        }
        long hours = java.time.Duration.between(checkIn, checkOut).toHours();
        return hours >= minHours;
    }

    /**
     * Валідація адреси
     */
    public static boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        return address.length() >= 5 && address.length() <= 200;
    }

    /**
     * Валідація міста
     */
    public static boolean isValidCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return false;
        }
        return city.length() >= 2 && city.length() <= 50;
    }

    /**
     * Валідація назви хостелу
     */
    public static boolean isValidHostelName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.length() >= 3 && name.length() <= 100;
    }

    /**
     * Валідація коментаря
     */
    public static boolean isValidComment(String comment) {
        if (comment == null) {
            return true; // Коментар не обов'язковий
        }
        return comment.length() <= 500;
    }

    /**
     * Нормалізація email (приведення до нижнього регістру)
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Нормалізація телефону (видалення пробілів)
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.trim().replaceAll("\\s+", "");
    }

    /**
     * Нормалізація імені (видалення зайвих пробілів, перша літера велика)
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        String trimmed = name.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) {
            return "";
        }

        // Перша літера велика, решта малі
        return Character.toUpperCase(trimmed.charAt(0)) +
                trimmed.substring(1).toLowerCase();
    }

    /**
     * Перевірка чи рядок є числом
     */
    public static boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Перевірка чи рядок є цілим числом
     */
    public static boolean isInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Генерація повідомлення про помилку валідації
     */
    public static String getValidationMessage(String field, String value, String type) {
        switch (type) {
            case "email":
                return String.format("Невірний формат email: %s. Приклад: user@example.com", value);
            case "phone":
                return String.format("Невірний формат телефону: %s. Формат: +380XXXXXXXXX", value);
            case "name":
                return String.format("Невірне ім'я: %s. Допустимі лише літери, апостроф та дефіс (2-50 символів)", value);
            case "password":
                return "Пароль має містити мінімум 6 символів, включаючи цифри та літери";
            case "price":
                return "Ціна має бути від 1 до 1000 грн";
            case "capacity":
                return "Кількість місць має бути від 1 до 20";
            case "rating":
                return "Рейтинг має бути від 1 до 5 зірок";
            case "date":
                return "Дата не може бути в минулому";
            case "daterange":
                return "Дата виїзду повинна бути після дати заїзду";
            case "duration":
                return "Мінімальна тривалість перебування - 1 година";
            case "address":
                return "Адреса має бути від 5 до 200 символів";
            case "city":
                return "Назва міста має бути від 2 до 50 символів";
            case "hostelname":
                return "Назва хостелу має бути від 3 до 100 символів";
            case "comment":
                return "Коментар не може перевищувати 500 символів";
            case "required":
                return String.format("Поле '%s' є обов'язковим", field);
            default:
                return String.format("Невірне значення для поля '%s': %s", field, value);
        }
    }

    /**
     * Перевірка всіх полів користувача
     */
    public static boolean validateUserData(String email, String phone, String password,
                                           String firstName, String lastName) {
        return isValidEmail(email) &&
                isValidPhone(phone) &&
                isValidPassword(password) &&
                isValidName(firstName) &&
                isValidName(lastName);
    }

    /**
     * Перевірка всіх полів хостелу
     */
    public static boolean validateHostelData(String name, String address, String city) {
        return isValidHostelName(name) &&
                isValidAddress(address) &&
                isValidCity(city);
    }

    /**
     * Перевірка всіх полів бронювання
     */
    public static boolean validateBookingData(String firstName, String lastName,
                                              String phone, String email,
                                              LocalDateTime checkIn, LocalDateTime checkOut) {
        return isValidName(firstName) &&
                isValidName(lastName) &&
                isValidPhone(phone) &&
                isValidEmail(email) &&
                isValidDateRange(checkIn, checkOut);
    }
}
