package com.auth.Wrapper;

import junit.framework.Assert;

import java.math.BigInteger;
 
public class ConvertUtil {
    public static String zeroRPad(String src, Integer length) {
        if(src.length() > length) {
            src = src.substring(0, length);
        }
        return String.format("%-" + length + "s", src).replace(" ", "0");
    }
}