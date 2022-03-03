package com.labmuffles.ecommerece.model;

public class UsersWithOrders {
    private String state, name, phone, address, dateTime;

    public UsersWithOrders(){}

    public UsersWithOrders(String state, String name, String phone, String address, String dateTime) {
        this.state = state;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.dateTime = dateTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
