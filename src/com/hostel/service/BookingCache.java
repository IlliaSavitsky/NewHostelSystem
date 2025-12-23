package com.hostel.service;

import com.hostel.model.Booking;
import java.util.*;

public class BookingCache {
    private Map<String, List<Booking>> userCache = new HashMap<>();
    private Map<String, Booking> bookingCache = new HashMap<>();
    private boolean isCacheLoaded = false;

    public List<Booking> getCachedBookings(String userId) {
        return userCache.getOrDefault(userId, new ArrayList<>());
    }

    public void cacheBookings(String userId, List<Booking> bookings) {
        userCache.put(userId, new ArrayList<>(bookings));

        for (Booking booking : bookings) {
            bookingCache.put(booking.getId(), booking);
        }

        isCacheLoaded = true;
        System.out.println("[CACHE] Дані закешовано для користувача: " + userId + " (" + bookings.size() + " бронювань)");
    }

    public Booking getCachedBooking(String bookingId) {
        return bookingCache.get(bookingId);
    }

    public boolean isCacheAvailable(String userId) {
        return userCache.containsKey(userId) && !userCache.get(userId).isEmpty();
    }

    public void clearCache(String userId) {
        if (userCache.containsKey(userId)) {
            List<Booking> bookings = userCache.get(userId);
            for (Booking booking : bookings) {
                bookingCache.remove(booking.getId());
            }
            userCache.remove(userId);
            System.out.println("[CACHE] Кеш очищено для користувача: " + userId);
        }
    }

    public void clearAllCache() {
        userCache.clear();
        bookingCache.clear();
        isCacheLoaded = false;
        System.out.println("[CACHE] Весь кеш очищено");
    }

    public boolean isCacheLoaded() {
        return isCacheLoaded;
    }

    public int getCacheSize() {
        return bookingCache.size();
    }
}
