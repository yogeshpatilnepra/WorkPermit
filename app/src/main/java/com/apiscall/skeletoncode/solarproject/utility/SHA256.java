package com.apiscall.skeletoncode.solarproject.utility;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SHA256 {
    private static final String ALGORISM = "HmacSHA256";
    private static final String S = "CtMMVTspdFP9JEdvNGYchqdj3ZV6BZXd";

    public String hmacDigest(String str) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(S.getBytes(), ALGORISM);
        try {
            Mac mac = Mac.getInstance(ALGORISM);
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal(str.getBytes());
            return byteToString(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return str;
    }

    private static String byteToString(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            int d = b[i];
            d += (d < 0) ? 256 : 0;
            if (d < 16) {
                buffer.append("0");
            }
            buffer.append(Integer.toString(d, 16));
        }
        return buffer.toString();
    }

}