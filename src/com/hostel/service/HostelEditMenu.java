package com.hostel.service;

import com.hostel.model.*;
import com.hostel.server.FileServer;
import java.util.List;
import java.util.Scanner;

public class HostelEditMenu {
    private final Scanner scanner;
    private final HostelService hostelService;
    private final BookingService bookingService;
    private Hostel originalHostel;
    private Hostel workingCopy;

    public HostelEditMenu(Scanner scanner, HostelService hostelService,
                          BookingService bookingService) {
        this.scanner = scanner;
        this.hostelService = hostelService;
        this.bookingService = bookingService;
    }

    public void editHostel(Hostel hostel, boolean isNewHostel) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(isNewHostel ? "СТВОРЕННЯ НОВОГО ХОСТЕЛУ" : "РЕДАГУВАННЯ ХОСТЕЛУ");
        System.out.println("=".repeat(60));

        this.originalHostel = hostel;
        this.workingCopy = cloneHostel(hostel);

        if (isNewHostel) {
            System.out.println("[INFO] Створення нового хостелу");
            System.out.println("[INFO] Всі поля порожні. Заповніть інформацію.");
        } else {
            System.out.println("[INFO] Редагування хостелу: " + hostel.getName());
            System.out.println("[INFO] Поля заповнені поточними даними.");
        }

        boolean editing = true;

        while (editing) {
            showEditMenu(isNewHostel);

            System.out.print("\nОберіть опцію: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> editBasicInfo();
                case "2" -> manageRooms();
                case "3" -> manageServices();
                case "4" -> managePrices();
                case "5" -> {
                    if (!isNewHostel) {
                        suspendHostel();
                    } else {
                        System.out.println("[ERROR] Ця опція недоступна для нового хостелу");
                    }
                }
                case "6" -> {
                    if (!isNewHostel) {
                        deleteHostel();
                        editing = false;
                    } else {
                        System.out.println("[ERROR] Ця опція недоступна для нового хостелу");
                    }
                }
                case "7" -> {
                    saveChanges(isNewHostel);
                    editing = false;
                }
                case "8" -> {
                    cancelChanges();
                    editing = false;
                }
                case "0" -> {
                    System.out.println("[INFO] Вихід без збереження");
                    editing = false;
                }
                case "info" -> showCurrentInfo();
                default -> System.out.println("[ERROR] Невірний вибір");
            }
        }
    }

    private void showEditMenu(boolean isNewHostel) {
        System.out.println("\n-- ОПЦІЇ РЕДАГУВАННЯ --");
        System.out.println("Хостел: " + (workingCopy.getName() != null ? workingCopy.getName() : "[Немає назви]"));
        System.out.println("Статус: " + (workingCopy.isActive() ? "✅ Активний" : "⏸️ Призупинений"));

        System.out.println("\n1. Основна інформація");
        System.out.println("2. Керування кімнатами");
        System.out.println("3. Додаткові послуги");
        System.out.println("4. Ціни та тарифи");

        if (!isNewHostel) {
            System.out.println("5. Призупинити/активувати хостел");
            System.out.println("6. Видалити хостел");
        }

        System.out.println("7. Зберегти зміни");
        System.out.println("8. Відмінити зміни");
        System.out.println("0. Вийти");

        // Перевірка заповненості обов'язкових полів
        if (!areRequiredFieldsFilled()) {
            System.out.println("\n⚠️ УВАГА: Не всі обов'язкові поля заповнені!");
            System.out.println("Поля 'Зберегти зміни' буде недоступна.");
        }
    }

    private boolean areRequiredFieldsFilled() {
        return workingCopy.getName() != null && !workingCopy.getName().trim().isEmpty() &&
                workingCopy.getAddress() != null && !workingCopy.getAddress().trim().isEmpty() &&
                workingCopy.getCity() != null && !workingCopy.getCity().trim().isEmpty();
    }

    private void editBasicInfo() {
        System.out.println("\n--- ОСНОВНА ІНФОРМАЦІЯ ---");

        System.out.println("1. Назва хостелу: " +
                (workingCopy.getName() != null ? workingCopy.getName() : "[не встановлено]"));
        System.out.println("2. Адреса: " +
                (workingCopy.getAddress() != null ? workingCopy.getAddress() : "[не встановлено]"));
        System.out.println("3. Місто: " +
                (workingCopy.getCity() != null ? workingCopy.getCity() : "[не встановлено]"));

        System.out.print("\nОберіть поле для редагування (1-3): ");

        try {
            int field = Integer.parseInt(scanner.nextLine().trim());

            switch (field) {
                case 1 -> {
                    System.out.print("Назва хостелу: ");
                    String name = scanner.nextLine().trim();
                    if (!name.isEmpty()) {
                        workingCopy.setName(name);
                        System.out.println("✅ Назву оновлено");
                    }
                }
                case 2 -> {
                    System.out.print("Адреса: ");
                    String address = scanner.nextLine().trim();
                    if (!address.isEmpty()) {
                        workingCopy.setAddress(address);
                        System.out.println("✅ Адресу оновлено");
                    }
                }
                case 3 -> {
                    System.out.print("Місто: ");
                    String city = scanner.nextLine().trim();
                    if (!city.isEmpty()) {
                        workingCopy.setCity(city);
                        System.out.println("✅ Місто оновлено");
                    }
                }
                default -> System.out.println("[ERROR] Невірний вибір поля");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void manageRooms() {
        System.out.println("\n--- КЕРУВАННЯ КІМНАТАМИ ---");

        if (!(bookingService instanceof SimpleBookingService)) {
            System.out.println("[ERROR] Сервіс бронювання не підтримує управління кімнатами");
            return;
        }

        SimpleBookingService simpleService = (SimpleBookingService) bookingService;

        System.out.println("Оберіть дію:");
        System.out.println("1. Переглянути всі кімнати");
        System.out.println("2. Додати нову кімнату");
        System.out.println("3. Редагувати кімнату");
        System.out.println("4. Видалити кімнату");
        System.out.println("0. Повернутись назад");

        System.out.print("\nВаш вибір: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> viewAllRooms(simpleService);
            case "2" -> addNewRoom(simpleService);
            case "3" -> editExistingRoom(simpleService);
            case "4" -> deleteRoom(simpleService);
            case "0" -> {}
            default -> System.out.println("[ERROR] Невірний вибір");
        }
    }

    private void viewAllRooms(SimpleBookingService service) {
        System.out.println("\n--- ВСІ КІМНАТИ ---");

        List<Room> rooms = service.getAllRoomsByHostel(workingCopy.getId());

        if (rooms.isEmpty()) {
            System.out.println("[INFO] Кімнат не знайдено");
            return;
        }

        System.out.println("Кількість кімнат: " + rooms.size());

        for (int i = 0; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            System.out.println("\n" + (i + 1) + ". Кімната #" + room.getId());
            System.out.println("   Тип: " + (room.getType() == Room.RoomType.PRIVATE ? "Приватна" : "Спільна"));
            System.out.println("   Місць: " + room.getCapacity());
            System.out.println("   Ціна: " + room.getPricePerHour() + " грн/год");
            System.out.println("   Статус: " + (room.isAvailable() ? "✅ Доступна" : "❌ Заброньована"));
        }
    }

    private void addNewRoom(SimpleBookingService service) {
        System.out.println("\n--- ДОДАВАННЯ НОВОЇ КІМНАТИ ---");

        try {
            System.out.print("Тип кімнати (1 - Приватна, 2 - Спільна): ");
            int typeChoice = Integer.parseInt(scanner.nextLine().trim());
            Room.RoomType roomType = (typeChoice == 1) ? Room.RoomType.PRIVATE : Room.RoomType.SHARED;

            System.out.print("Кількість місць (1-20): ");
            int capacity = Integer.parseInt(scanner.nextLine().trim());

            if (capacity < 1 || capacity > 20) {
                System.out.println("[ERROR] Кількість місць має бути від 1 до 20");
                return;
            }

            System.out.print("Ціна за годину (грн, 1-1000): ");
            double price = Double.parseDouble(scanner.nextLine().trim());

            if (price < 1 || price > 1000) {
                System.out.println("[ERROR] Ціна має бути від 1 до 1000 грн");
                return;
            }

            Room room = new Room(workingCopy.getId(), roomType, capacity, price);
            service.addRoom(room);

            System.out.println("✅ Нова кімната додана!");
            System.out.println("ID: " + room.getId());

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат даних");
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private void editExistingRoom(SimpleBookingService service) {
        System.out.println("\n--- РЕДАГУВАННЯ КІМНАТИ ---");

        List<Room> rooms = service.getAllRoomsByHostel(workingCopy.getId());

        if (rooms.isEmpty()) {
            System.out.println("[INFO] Кімнат для редагування не знайдено");
            return;
        }

        System.out.println("Оберіть кімнату для редагування:");
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println((i + 1) + ". " + rooms.get(i).getId());
        }

        System.out.print("\nВаш вибір: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim()) - 1;

            if (choice < 0 || choice >= rooms.size()) {
                System.out.println("[ERROR] Невірний вибір");
                return;
            }

            Room room = rooms.get(choice);
            editRoomDetails(room, service);

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void editRoomDetails(Room room, SimpleBookingService service) {
        System.out.println("\nРедагування кімнати " + room.getId());

        boolean editing = true;

        while (editing) {
            System.out.println("\nПоточні дані:");
            System.out.println("1. Тип: " + room.getType());
            System.out.println("2. Кількість місць: " + room.getCapacity());
            System.out.println("3. Ціна: " + room.getPricePerHour() + " грн/год");
            System.out.println("4. Доступність: " + (room.isAvailable() ? "Так" : "Ні"));
            System.out.println("0. Завершити редагування");

            System.out.print("\nОберіть поле для редагування: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.print("Новий тип (1 - Приватна, 2 - Спільна): ");
                    try {
                        int newType = Integer.parseInt(scanner.nextLine().trim());
                        room.setType(newType == 1 ? Room.RoomType.PRIVATE : Room.RoomType.SHARED);
                        System.out.println("✅ Тип кімнати змінено");
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Неправильний формат");
                    }
                }
                case "2" -> {
                    System.out.print("Нова кількість місць: ");
                    try {
                        int newCapacity = Integer.parseInt(scanner.nextLine().trim());
                        room.setCapacity(newCapacity);
                        System.out.println("✅ Кількість місць змінено");
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Неправильний формат");
                    } catch (IllegalArgumentException e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "3" -> {
                    System.out.print("Нова ціна за годину (грн): ");
                    try {
                        double newPrice = Double.parseDouble(scanner.nextLine().trim());
                        room.setPricePerHour(newPrice);
                        System.out.println("✅ Ціну змінено");
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Неправильний формат");
                    } catch (IllegalArgumentException e) {
                        System.out.println("[ERROR] " + e.getMessage());
                    }
                }
                case "4" -> {
                    System.out.print("Доступність (1 - Так, 2 - Ні): ");
                    try {
                        int availability = Integer.parseInt(scanner.nextLine().trim());
                        room.setAvailable(availability == 1);
                        System.out.println("✅ Доступність змінено");
                    } catch (NumberFormatException e) {
                        System.out.println("[ERROR] Неправильний формат");
                    }
                }
                case "0" -> editing = false;
                default -> System.out.println("[ERROR] Невірний вибір");
            }

            // Оновлення кімнати на сервері
            FileServer.saveRoom(room);
        }
    }

    private void deleteRoom(SimpleBookingService service) {
        System.out.println("\n--- ВИДАЛЕННЯ КІМНАТИ ---");

        List<Room> rooms = service.getAllRoomsByHostel(workingCopy.getId());

        if (rooms.isEmpty()) {
            System.out.println("[INFO] Кімнат для видалення не знайдено");
            return;
        }

        System.out.println("Оберіть кімнату для видалення:");
        for (int i = 0; i < rooms.size(); i++) {
            System.out.println((i + 1) + ". " + rooms.get(i).getId());
        }

        System.out.print("\nВаш вибір (або 0 для відміни): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());

            if (choice == 0) {
                System.out.println("[INFO] Видалення відмінено");
                return;
            }

            if (choice > 0 && choice <= rooms.size()) {
                Room room = rooms.get(choice - 1);

                // Перевірка на активні бронювання
                List<Booking> activeBookings = service.getActiveHostelBookings(workingCopy.getId());
                boolean hasActiveBookings = false;

                for (Booking booking : activeBookings) {
                    if (booking.getRoomId().equals(room.getId())) {
                        hasActiveBookings = true;
                        break;
                    }
                }

                if (hasActiveBookings) {
                    System.out.println("[ERROR] Неможливо видалити кімнату з активними бронюваннями");
                    return;
                }

                System.out.println("\n[WARNING] Ви збираєтесь видалити кімнату:");
                System.out.println("ID: " + room.getId());
                System.out.println("Тип: " + (room.getType() == Room.RoomType.PRIVATE ? "Приватна" : "Спільна"));
                System.out.println("Місць: " + room.getCapacity());
                System.out.println("Ціна: " + room.getPricePerHour() + " грн/год");

                System.out.print("\nВи впевнені? (так/ні): ");
                String confirm = scanner.nextLine().trim().toLowerCase();

                if (confirm.equals("так") || confirm.equals("yes") || confirm.equals("y")) {
                    // В реальній системі тут було б видалення з сервера
                    System.out.println("[INFO] Функція видалення кімнат буде реалізована в наступних версіях");
                    System.out.println("[INFO] Наразі можна лише змінити статус доступності");
                } else {
                    System.out.println("[INFO] Видалення відмінено");
                }
            } else {
                System.out.println("[ERROR] Невірний вибір");
            }
        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Неправильний формат");
        }
    }

    private void manageServices() {
        System.out.println("\n--- ДОДАТКОВІ ПОСЛУГИ ---");
        System.out.println("[INFO] Ця функція буде повністю реалізована в наступних версіях");

        System.out.println("\nЗаплановані послуги:");
        System.out.println("1. Wi-Fi: буде доступний");
        System.out.println("2. Сніданок: буде доступний");
        System.out.println("3. Пральня: буде доступна");
        System.out.println("4. Кухня: буде доступна");
        System.out.println("5. Парковка: буде доступна");

        System.out.println("\n[INFO] Для налаштування послуг зверніться до адміністратора");
    }

    private void managePrices() {
        System.out.println("\n--- ЦІНИ ТА ТАРИФИ ---");

        if (!(bookingService instanceof SimpleBookingService)) {
            System.out.println("[ERROR] Не вдалося отримати інформацію про ціни");
            return;
        }

        SimpleBookingService service = (SimpleBookingService) bookingService;
        List<Room> rooms = service.getAllRoomsByHostel(workingCopy.getId());

        if (rooms.isEmpty()) {
            System.out.println("[INFO] Кімнат не знайдено");
            System.out.println("[ACTION] Спочатку додайте кімнати");
            return;
        }

        System.out.println("Поточні ціни:");

        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0;
        double totalDailyRevenue = 0;

        for (Room room : rooms) {
            double price = room.getPricePerHour();
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;

            // Розрахунок потенційного доходу (при 100% завантаженні)
            totalDailyRevenue += price * 24;
        }

        System.out.println("Ціна за годину: від " + String.format("%.2f", minPrice) +
                " до " + String.format("%.2f", maxPrice) + " грн");
        System.out.println("Ціна за добу: від " + String.format("%.2f", minPrice * 24) +
                " до " + String.format("%.2f", maxPrice * 24) + " грн");
        System.out.println("Потенційний дохід за добу (100% завантаження): " +
                String.format("%.2f", totalDailyRevenue) + " грн");

        System.out.print("\nБажаєте змінити ціни? (так/ні): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("так") || response.equals("yes") || response.equals("y")) {
            System.out.println("[INFO] Для зміни цін використовуйте опцію 'Редагувати кімнату'");
        }
    }

    private void suspendHostel() {
        System.out.println("\n--- ПРИЗУПИНЕННЯ РОБОТИ ХОСТЕЛУ ---");

        if (workingCopy.isActive()) {
            System.out.println("Поточний статус: ✅ Активний");
            System.out.println("\n[WARNING] Призупинення призведе до:");
            System.out.println("• Блокування нових бронювань");
            System.out.println("• Автоматичного скасування всіх активних бронювань");
            System.out.println("• Надсилання повідомлень клієнтам");
            System.out.println("• Тимчасової недоступності хостелу");

            System.out.print("\nПризупинити роботу хостелу? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                workingCopy.setActive(false);
                System.out.println("✅ Статус змінено на 'Призупинений'");
                System.out.println("[INFO] Зміни буде збережено при збереженні всіх змін");
            }
        } else {
            System.out.println("Поточний статус: ⏸️ Призупинений");
            System.out.print("Активувати хостел? (так/ні): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("так") || response.equals("yes") || response.equals("y")) {
                workingCopy.setActive(true);
                System.out.println("✅ Статус змінено на 'Активний'");
                System.out.println("[INFO] Зміни буде збережено при збереженні всіх змін");
            }
        }
    }

    private void deleteHostel() {
        System.out.println("\n--- ВИДАЛЕННЯ ХОСТЕЛУ ---");
        System.out.println("[WARNING] Ця дія незворотна!");

        System.out.println("\nВи збираєтесь видалити хостел:");
        System.out.println("Назва: " + workingCopy.getName());
        System.out.println("Адреса: " + workingCopy.getAddress());
        System.out.println("Місто: " + workingCopy.getCity());

        System.out.println("\n[ВАЖЛИВО] Ця дія призведе до:");
        System.out.println("• Повного видалення хостелу з системи");
        System.out.println("• Втрати всіх даних про кімнати");
        System.out.println("• Скасування всіх активних бронювань");
        System.out.println("• Неможливості відновлення даних!");

        // Додаткова перевірка
        System.out.print("\nДля підтвердження введіть назву хостелу: ");
        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equals(workingCopy.getName())) {
            System.out.println("[ERROR] Назви не співпадають");
            System.out.println("[INFO] Видалення відмінено");
            return;
        }

        // Таймер підтвердження (Вимога 59b: 10 секунд)
        System.out.println("\n[WARNING] ОСТАННЄ ПОПЕРЕДЖЕННЯ!");
        System.out.println("Видалення почнеться через 10 секунд...");
        System.out.println("Для скасування натисніть Enter.");

        for (int i = 10; i > 0; i--) {
            System.out.print(i + "... ");

            try {
                // Перевірка натискання Enter для скасування
                if (System.in.available() > 0) {
                    scanner.nextLine(); // Очистити буфер
                    System.out.println("\n[INFO] Видалення скасовано користувачем");
                    return;
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                // Продовжити лічильник
            }
        }

        System.out.println("\n[INFO] Виконується видалення...");

        // Перевірка активних бронювань
        if (bookingService instanceof SimpleBookingService) {
            SimpleBookingService service = (SimpleBookingService) bookingService;
            List<Booking> activeBookings = service.getActiveHostelBookings(workingCopy.getId());

            if (!activeBookings.isEmpty()) {
                System.out.println("[ERROR] Неможливо видалити хостел з активними бронюваннями");
                System.out.println("[INFO] Спочатку скасуйте або завершіть всі активні бронювання");
                return;
            }
        }

        boolean success = hostelService.deleteHostel(workingCopy.getId());

        if (success) {
            System.out.println("✅ Хостел успішно видалено!");
            System.out.println("[INFO] Всі пов'язані дані очищено");
        } else {
            System.out.println("[ERROR] Не вдалося видалити хостел");
        }
    }

    private void saveChanges(boolean isNewHostel) {
        System.out.println("\n--- ЗБЕРЕЖЕННЯ ЗМІН ---");

        // Перевірка заповненості обов'язкових полів (Вимога 63)
        if (!areRequiredFieldsFilled()) {
            System.out.println("[ERROR] Не всі обов'язкові поля заповнені!");
            System.out.println("[INFO] Заповніть наступні поля:");

            if (workingCopy.getName() == null || workingCopy.getName().trim().isEmpty()) {
                System.out.println("  • Назва хостелу");
            }
            if (workingCopy.getAddress() == null || workingCopy.getAddress().trim().isEmpty()) {
                System.out.println("  • Адреса");
            }
            if (workingCopy.getCity() == null || workingCopy.getCity().trim().isEmpty()) {
                System.out.println("  • Місто");
            }

            System.out.println("[ACTION] Поверніться та заповніть обов'язкові поля");
            return;
        }

        System.out.println("[INFO] Перевірка даних...");

        if (isNewHostel) {
            // Для нового хостелу
            System.out.println("[INFO] Створення нового хостелу...");
            Hostel createdHostel = hostelService.createHostel(
                    workingCopy.getName(),
                    workingCopy.getAddress(),
                    workingCopy.getCity(),
                    workingCopy.getManagerId()
            );

            if (createdHostel != null) {
                System.out.println("✅ Новий хостел створено!");
                System.out.println("ID: " + createdHostel.getId());
            } else {
                System.out.println("[ERROR] Не вдалося створити хостел");
            }
        } else {
            // Для існуючого хостелу
            System.out.println("[INFO] Оновлення даних хостелу...");

            // Копіювання ID та managerId з оригіналу
            workingCopy.setId(originalHostel.getId());
            workingCopy.setManagerId(originalHostel.getManagerId());

            boolean success = hostelService.updateHostel(workingCopy);

            if (success) {
                System.out.println("✅ Зміни збережено!");
                System.out.println("[INFO] Дані синхронізовано з сервером");
            } else {
                System.out.println("[ERROR] Не вдалося зберегти зміни");
                System.out.println("[INFO] Залишаємось у меню редагування");
            }
        }
    }

    private void cancelChanges() {
        System.out.println("\n--- ВІДМІНА ЗМІН ---");

        System.out.println("[INFO] Всі незбережені зміни будуть втрачені");
        System.out.print("Ви впевнені, що хочете відмінити зміни? (так/ні): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("так") || response.equals("yes") || response.equals("y")) {
            System.out.println("[INFO] Зміни відмінено");
            System.out.println("[INFO] Повернення до попередніх значень...");
        } else {
            System.out.println("[INFO] Відміну скасовано");
        }
    }

    private void showCurrentInfo() {
        System.out.println("\n--- ПОТОЧНА ІНФОРМАЦІЯ ---");
        System.out.println("Назва: " + workingCopy.getName());
        System.out.println("Адреса: " + workingCopy.getAddress());
        System.out.println("Місто: " + workingCopy.getCity());
        System.out.println("Статус: " + (workingCopy.isActive() ? "Активний" : "Призупинений"));
        System.out.println("Manager ID: " + workingCopy.getManagerId());
        System.out.println("ID: " + workingCopy.getId());
    }

    private Hostel cloneHostel(Hostel original) {
        Hostel clone = new Hostel(
                original.getName(),
                original.getAddress(),
                original.getCity(),
                original.getManagerId()
        );
        clone.setId(original.getId());
        clone.setActive(original.isActive());
        return clone;
    }
}
