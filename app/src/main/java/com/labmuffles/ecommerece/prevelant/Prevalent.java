package com.labmuffles.ecommerece.prevelant;

import com.labmuffles.ecommerece.model.Users;

public class Prevalent {

    //Online user data...
    public static Users currentOnlineUser;

    //Ref keys...
    public static final String userPhoneKey = "UserPhone";
    public static final String userPasswordKey = "UserPassword";
    public static final String userTypeKey = "UserType";
    public static final String categoryKey = "category";

    //Products information...
    public static final String productsImagesFolder = "Product Images";
    public static final String productDBRoot = "Products";
    public static final String productDBId = "productId";
    public static final String productDBDate = "date";
    public static final String productDBTime = "time";
    public static final String productDBDescription = "description";
    public static final String productDBImage = "image";
    public static final String productDBCategory = "category";
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
    public static final String cartDBUserView = "User View";
    public static final String cartDBAdminView = "Admin View";
    public static final String cartDBProductsChild = "Products";

    //Order information
    public static final String orderDBRoot = "Orders";
    public static final String orderDBPid = "pid";
    public static final String orderDBPname = "pname";
    public static final String orderDBPprice = "pprice";
    public static final String orderDBPquantity = "pquantity";
    public static final String orderDBPimage = "pimage";
    public static final String orderDBPtotal = "ptotal";
    public static final String orderDBPdate = "pdate";
    public static final String orderDBPtime = "ptime";
    public static final String orderDBPcategory = "pcategory";
    public static final String orderDBPdescription = "pdescription";
    public static final String orderDBPuid = "uid";
    public static final String orderDBPstatus = "pstatus";

}
