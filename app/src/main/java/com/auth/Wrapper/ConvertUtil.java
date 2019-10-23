package com.auth.Wrapper;

import junit.framework.Assert;

import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ConvertUtil {
    public static String zeroRPad(String src, Integer length) {
        if(src.length() > length) {
            return src.substring(0, length);
        } else {
            return String.format("%-" + length + "s", src).replace(" ", "0");
        }
    }

    public static byte[] zeroRPad(byte[] src, Integer length) {
        if(src.length >= length) {
            return Arrays.copyOfRange(src, 0, length);
        } else {
            String srcStr = new String(src, StandardCharsets.US_ASCII);
            String dstStr = String.format("%-" + length + "s", srcStr).replace(" ", "0");
            return dstStr.getBytes(StandardCharsets.US_ASCII);
        }
    }

    public static byte[] zeroRPad(BigInteger src, Integer length) {
        byte[] bintArray = src.toByteArray();
        if (bintArray[0] == 0) {
            bintArray = Arrays.copyOfRange(bintArray, 1, bintArray.length);
        }
        byte[] res =  ConvertUtil.zeroRPad(bintArray, length);
        return res;
    }

    public static String encodeHexString(byte[] raw){
        char[] hex = Hex.encodeHex(raw);
        return String.valueOf(hex);
    }
}