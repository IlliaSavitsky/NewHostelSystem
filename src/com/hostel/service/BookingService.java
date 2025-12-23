package com.hostel.service;

import com.hostel.model.Booking;
import com.hostel.model.Room;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    Booking createBooking(String hostelId, String roomId, String clientId,
                          LocalDateTime checkIn, LocalDateTime checkOut,
                          String firstName, String lastName, String phone, String email);

    boolean cancelBooking(String bookingId, String initiatorId);

    List<Booking> getUserBookings(String userId);

    List<Booking> getHostelBookings(String hostelId);

    List<Booking> getActiveHostelBookings(String hostelId);

    Booking getBookingById(String bookingId);

    double calculatePrice(Room room, LocalDateTime checkIn, LocalDateTime checkOut);

    boolean updateBookingStatus(String bookingId, Booking.BookingStatus status);

    boolean updateBookingPaymentStatus(String bookingId, Booking.PaymentStatus status);
}
