package com.hostel.model;

import java.time.LocalDateTime;

public class Booking {
    private String id;
    private String hostelId;
    private String roomId;
    private String clientId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private double totalPrice;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String clientFirstName;
    private String clientLastName;
    private String clientPhone;
    private String clientEmail;
    private LocalDateTime createdAt;
    private Integer rating;
    private String review;

    public enum BookingStatus {
        ACTIVE, IN_SERVICE, COMPLETED, CANCELLED
    }

    public enum PaymentStatus {
        PAID, UNPAID, PARTIALLY_PAID
    }

    public Booking(String hostelId, String roomId, String clientId,
                   LocalDateTime checkInTime, LocalDateTime checkOutTime,
                   double totalPrice, String clientFirstName, String clientLastName,
                   String clientPhone, String clientEmail) {
        this.hostelId = hostelId;
        this.roomId = roomId;
        this.clientId = clientId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalPrice = totalPrice;
        this.status = BookingStatus.ACTIVE;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.clientFirstName = clientFirstName;
        this.clientLastName = clientLastName;
        this.clientPhone = clientPhone;
        this.clientEmail = clientEmail;
        this.createdAt = LocalDateTime.now();
        this.rating = null;
        this.review = null;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHostelId() { return hostelId; }
    public void setHostelId(String hostelId) { this.hostelId = hostelId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public LocalDateTime getCheckInTime() { return checkInTime; }
    public void setCheckInTime(LocalDateTime checkInTime) { this.checkInTime = checkInTime; }

    public LocalDateTime getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(LocalDateTime checkOutTime) { this.checkOutTime = checkOutTime; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getClientFirstName() { return clientFirstName; }
    public void setClientFirstName(String clientFirstName) { this.clientFirstName = clientFirstName; }

    public String getClientLastName() { return clientLastName; }
    public void setClientLastName(String clientLastName) { this.clientLastName = clientLastName; }

    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Рейтинг має бути від 1 до 5");
        }
        this.rating = rating;
    }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public boolean canBeRated() {
        return status == BookingStatus.COMPLETED && rating == null;
    }

    public boolean canBeCancelled() {
        return status == BookingStatus.ACTIVE &&
                checkInTime.isAfter(LocalDateTime.now().plusHours(2));
    }

    public String getStatusDescription() {
        switch (status) {
            case ACTIVE: return "Активне";
            case IN_SERVICE: return "Обслуговується";
            case COMPLETED: return "Завершено";
            case CANCELLED: return "Скасоване";
            default: return "Невідомо";
        }
    }
}
