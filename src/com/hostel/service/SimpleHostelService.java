package com.hostel.service;

import com.hostel.model.Hostel;
import com.hostel.server.FileServer;
import java.util.List;
import java.util.Random;
import com.hostel.model.*;

public class SimpleHostelService implements HostelService {

    @Override
    public Hostel createHostel(String name, String address, String city, String managerId) {
        System.out.println("[HOSTEL] Створення хостелу: " + name);

        Hostel hostel = new Hostel(name, address, city, managerId);
        hostel.setId("HOSTEL_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000));

        boolean saved = FileServer.saveHostel(hostel);

        if (saved) {
            System.out.println("[HOSTEL SUCCESS] Хостел створено: " + hostel.getId());
            return hostel;
        } else {
            System.out.println("[HOSTEL ERROR] Не вдалося створити хостел");
            return null;
        }
    }

    @Override
    public boolean updateHostel(Hostel hostel) {
        return FileServer.updateHostel(hostel);
    }

    @Override
    public boolean deleteHostel(String hostelId) {
        System.out.println("[HOSTEL] Спроба видалення хостелу: " + hostelId);

        // Перевірка на активні бронювання
        List<Booking> activeBookings = FileServer.getActiveHostelBookings(hostelId);
        if (!activeBookings.isEmpty()) {
            System.out.println("[HOSTEL ERROR] Неможливо видалити хостел з активними бронюваннями");
            return false;
        }

        return FileServer.deleteHostel(hostelId);
    }

    @Override
    public Hostel getHostelById(String hostelId) {
        return FileServer.getHostelById(hostelId);
    }

    @Override
    public List<Hostel> getHostelsByManager(String managerId) {
        return FileServer.getHostelsByManager(managerId);
    }

    @Override
    public List<Hostel> searchHostels(String city, String query) {
        return FileServer.searchHostels(city, query);
    }

    @Override
    public boolean suspendHostel(String hostelId) {
        Hostel hostel = FileServer.getHostelById(hostelId);
        if (hostel != null) {
            hostel.setActive(false);
            boolean updated = FileServer.updateHostel(hostel);

            if (updated) {
                System.out.println("[HOSTEL] Хостел призупинено: " + hostelId);

                // Деактивація активних бронювань
                List<Booking> activeBookings = FileServer.getActiveHostelBookings(hostelId);
                for (Booking booking : activeBookings) {
                    FileServer.updateBookingStatus(booking.getId(), Booking.BookingStatus.CANCELLED);

                    // Повідомлення клієнтам
                    String message = String.format(
                            "Ваше бронювання #%s скасовано через призупинення роботи хостелу '%s'",
                            booking.getId(), hostel.getName()
                    );
                    NotificationService.sendNotificationToClient(booking.getClientId(), message);
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean activateHostel(String hostelId) {
        Hostel hostel = FileServer.getHostelById(hostelId);
        if (hostel != null) {
            hostel.setActive(true);
            boolean updated = FileServer.updateHostel(hostel);

            if (updated) {
                System.out.println("[HOSTEL] Хостел активовано: " + hostelId);
                return true;
            }
        }
        return false;
    }
}