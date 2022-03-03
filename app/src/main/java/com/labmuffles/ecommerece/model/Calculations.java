package com.labmuffles.ecommerece.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Calculations {

    public static int calculateTotal(int price, int quantity) {
        return price * quantity;
    }

    public static int calculateTotal(int price, int quantity, int discount) {
        return price * quantity * (1 - discount);
    }

    public static int calculateDiscount(String price, String discount) {
        return Integer.valueOf(price) - Integer.valueOf(discount);
    }

    public static String getCurrentTime(){
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        return currentTime.format(calendar.getTime());
    }

    public static String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");

        return currentDate.format(calendar.getTime());
    }
}
