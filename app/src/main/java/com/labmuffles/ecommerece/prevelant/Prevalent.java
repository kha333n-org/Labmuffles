package com.labmuffles.ecommerece.prevelant;

import com.labmuffles.ecommerece.model.Users;

public class Prevalent {

    //Online user data...
    public static Users currentOnlineUser;

    //Ref keys...
    public static final String userPhoneKey = "UserPhone";
    public static final String userPasswordKey = "UserPassword";
    public static final String userTypeKey = "UserType";
    public static final String date = "date";
    public static final String time = "time";

    //Products information...
    public static final String productsImagesFolder = "Product Images";
    public static final String productDBRoot = "Products";
    public static final String productDBId = "productId";
    public static final String productDBDate = "date";
    public static final String productDBTime = "time";
    public static final String productDBDescription = "description";
    public static final String productDBImage = "image";
    public static final String productDBPrice = "price";
    public static final String productDBName = "name";

    //Users information...
    public static final String userProfileImage = "image";
    public static final String userImagesFolder = "Profile Images";
    public static final String userProfileName = "name";
    public static final String userProfilePassword = "password";
    public static final String userProfileAddress = "address";
    public static final String userProfilePhoneNumber = "phone";
    public static final String userOrderPhone = "orderPhone";
    public static final String userProfileCity = "city";
    public static final String usersWithOrders = "usersWithOrders";


    //Cart information
    public static final String cartDBRoot = "Cart List";
    public static final String cartDBPid = "pid";
    public static final String productDBQuantity = "quantity";
    public static final String cartDBDate = "date";
    public static final String cartDBTime = "time";
    public static final String cartDBTotal = "total";
    public static final String cartDBUid = "uid";
    public static final String cartDBStatus = "status";
    public static final String cartDBDiscount = "discount";
    public static final String productsPerOrder = "productsPerOrder";
    public static final String cartDBProductsChild = "Products";


    //Confirmed Orders information
    public static final String ordersDBRoot = "Orders";
    public static final String orderId = "orderId";
    public static final String totalOrderAmount = "totalAmount";
    public static final String orderState = "State";
    public static final String versionCodeKey = "version_code";
    public static final int updateCheckDelay = 1;
}
