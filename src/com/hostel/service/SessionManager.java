package com.hostel.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SessionManager {
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    private static final int SESSION_DURATION_DAYS = 30;

    public void startSession() {
        loginTime = LocalDateTime.now();
        lastActivity = loginTime;
        System.out.println("[SESSION] Сесія розпочата: " + loginTime);
    }

    public void updateActivity() {
        if (lastActivity != null) {
            lastActivity = LocalDateTime.now();
        }
    }

    public boolean isSessionValid() {
        if (loginTime == null) {
            return false;
        }

        updateActivity();

        LocalDateTime now = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(loginTime, now);

        if (daysBetween > SESSION_DURATION_DAYS) {
            System.out.println("[SESSION WARNING] Сесія закінчилась (" + daysBetween + " днів тому)");
            return false;
        }

        long daysLeft = SESSION_DURATION_DAYS - daysBetween;
        if (daysLeft <= 3) {
            System.out.println("[SESSION INFO] Залишилось " + daysLeft + " днів до закінчення сесії");
        }

        return true;
    }

    public void endSession() {
        if (loginTime != null) {
            long duration = ChronoUnit.MINUTES.between(loginTime, LocalDateTime.now());
            System.out.println("[SESSION] Тривалість сесії: " + duration + " хвилин");
            loginTime = null;
            lastActivity = null;
        }
    }

    public LocalDateTime getSessionExpiryTime() {
        if (loginTime == null) {
            return null;
        }
        return loginTime.plusDays(SESSION_DURATION_DAYS);
    }

    public long getDaysLeft() {
        if (loginTime == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        long daysBetween = ChronoUnit.DAYS.between(loginTime, now);
        return Math.max(0, SESSION_DURATION_DAYS - daysBetween);
    }
}
