package com.labmuffles.ecommerece;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Hash {
    public static String compute(String data){
        try
        {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(data.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer md5Hash = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++){
                String h = Integer.toHexString(0xff & messageDigest[i]);
                while (h.length() < 2){
                    h = "0" + h;
                }
                md5Hash.append(h);
            }

            return md5Hash.toString();
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }
}
