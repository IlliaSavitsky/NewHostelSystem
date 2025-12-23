package com.hostel.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Review {
    private String id;
    private String bookingId;
    private String clientId;
    private String hostelId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review(String bookingId, String clientId, String hostelId,
                  int rating, String comment) {
        validateRating(rating);

        this.id = "REVIEW_" + UUID.randomUUID().toString().substring(0, 8);
        this.bookingId = bookingId;
        this.clientId = clientId;
        this.hostelId = hostelId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Рейтинг має бути від 1 до 5");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getHostelId() { return hostelId; }
    public void setHostelId(String hostelId) { this.hostelId = hostelId; }

    public int getRating() { return rating; }
    public void setRating(int rating) {
        validateRating(rating);
        this.rating = rating;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
