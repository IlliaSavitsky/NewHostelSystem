package com.hostel.model;

public class Hostel {
    private String id;
    private String name;
    private String address;
    private String city;
    private boolean isActive;
    private String managerId;

    public Hostel(String name, String address, String city, String managerId) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.managerId = managerId;
        this.isActive = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
}
