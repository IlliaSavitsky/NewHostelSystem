package com.hostel.model;

public class Client extends User {
    private String firstName;
    private String lastName;

    public Client(String email, String phone, String password,
                  String firstName, String lastName) {
        super(email, phone, password);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public UserType getUserType() {
        return UserType.CLIENT;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
