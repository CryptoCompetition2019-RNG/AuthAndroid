package com.auth.Wrapper;

import java.io.InputStream;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileInputStream;

/*
 * byte[] msg;
 * String filePath = new String("E:/Poem.txt");
 * byte[] hash1 = (new SM3Hash()).fileSM3(msg);
 * byte[] hash2 = (new SM3Hash()).fileSM3(filePath);
 *
 */
public class SM3Hash {
    public SM3Hash() {

    }

    public byte[] bytesSM3(byte[] msg) {
        byte[] md = new byte[32];
        SM3Digest sm3 = new SM3Digest();
        sm3.update(msg, 0, msg.length);
        sm3.doFinal(md, 0);
        return md;
    }

    /**
     * byte[] hash1 = (new SM3Hash()).stringSM3(msg);
     */
    public String stringSM3(String input) {
        byte[] bs = new byte[0];
        bs = bytesSM3(input.getBytes());
        String cipherText = ConvertUtil.getHexString(bs);
        if (cipherText != null && cipherText.trim().length() > 0) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(cipherText);
            cipherText = m.replaceAll("");
        }
        return cipherText;
    }

}

