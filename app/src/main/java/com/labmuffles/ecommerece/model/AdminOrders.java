package com.labmuffles.ecommerece.model;

public class AdminOrders {
    private String name, phone,address, State, date, time, totalAmount, discount;

    public AdminOrders() {
    }

    public AdminOrders(String name, String phone, String address, String state, String date, String time, String totalAmount, String discount) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        State = state;
        this.date = date;
        this.time = time;
        this.totalAmount = totalAmount;
        this.discount = discount;
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

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
