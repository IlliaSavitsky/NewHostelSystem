package com.hostel.service;

import com.hostel.model.Review;
import com.hostel.server.FileServer;
import java.util.List;

public class SimpleReviewService implements ReviewService {

    @Override
    public boolean submitReview(String bookingId, String clientId, String hostelId,
                                int rating, String comment) {
        System.out.println("[REVIEW] Надсилання відгуку для бронювання: " + bookingId);

        if (!NetworkMonitor.isNetworkAvailable()) {
            System.out.println("[REVIEW ERROR] Відсутнє мережеве з'єднання");
            return false;
        }

        // Перевірка рейтингу
        if (rating < 1 || rating > 5) {
            System.out.println("[REVIEW ERROR] Рейтинг має бути від 1 до 5");
            return false;
        }

        // Створення відгуку
        Review review = new Review(bookingId, clientId, hostelId, rating, comment);

        // Збереження на сервер
        boolean saved = FileServer.saveReview(review);

        if (saved) {
            System.out.println("[REVIEW SUCCESS] Відгук збережено: " + review.getId());

            // Імітація передачі даних (Вимога 39b: не пізніше 5 секунд)
            long startTime = System.currentTimeMillis();

            try {
                Thread.sleep(1000); // Імітація затримки мережі
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > 5000) {
                System.out.println("[REVIEW WARNING] Передача зайняла " + elapsedTime + "мс (>5 сек)");
            }

            return true;
        }

        return false;
    }

    @Override
    public List<Review> getReviewsForHostel(String hostelId) {
        return FileServer.getReviewsForHostel(hostelId);
    }

    @Override
    public double getAverageRatingForHostel(String hostelId) {
        return FileServer.getAverageRatingForHostel(hostelId);
    }
}