package com.hostel.model;

public class Room {
    private String id;
    private String hostelId;
    private RoomType type;
    private int capacity;
    private double pricePerHour;
    private boolean isAvailable;

    public enum RoomType {
        SHARED, PRIVATE
    }

    public Room(String hostelId, RoomType type, int capacity, double pricePerHour) {
        validateCapacity(capacity);
        validatePrice(pricePerHour);

        this.hostelId = hostelId;
        this.type = type;
        this.capacity = capacity;
        this.pricePerHour = pricePerHour;
        this.isAvailable = true;
    }

    private void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Місткість має бути більше 0");
        }
        if (capacity > 20) {
            throw new IllegalArgumentException("Місткість не може перевищувати 20 місць");
        }
    }

    private void validatePrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Ціна за годину має бути більше 0");
        }
        if (price > 1000) {
            throw new IllegalArgumentException("Ціна за годину не може перевищувати 1000 грн");
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHostelId() { return hostelId; }
    public void setHostelId(String hostelId) { this.hostelId = hostelId; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) {
        validateCapacity(capacity);
        this.capacity = capacity;
    }

    public double getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(double pricePerHour) {
        validatePrice(pricePerHour);
        this.pricePerHour = pricePerHour;
    }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}
