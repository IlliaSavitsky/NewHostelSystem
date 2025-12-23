package com.hostel.service;

import com.hostel.model.Review;
import java.util.List;

public interface ReviewService {
    boolean submitReview(String bookingId, String clientId, String hostelId,
                         int rating, String comment);

    List<Review> getReviewsForHostel(String hostelId);

    double getAverageRatingForHostel(String hostelId);
}