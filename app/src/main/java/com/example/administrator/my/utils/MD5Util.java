package com.example.administrator.my.utils;

/**
 * MD5加密工具
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String strToMD5(String plainText) {
        String cipherText = null;
        String salt = "Grow";
        String oneCiphertext = MD5(plainText) + MD5(salt);
        cipherText = MD5(oneCiphertext);
        return cipherText;
    }

    private static String MD5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return result;
    }
}  