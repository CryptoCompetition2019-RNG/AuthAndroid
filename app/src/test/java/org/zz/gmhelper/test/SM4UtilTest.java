package org.zz.gmhelper.test;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.Assert;
import org.junit.Test;
import org.zz.gmhelper.SM4Util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static org.junit.Assert.*;

public class SM4UtilTest extends GMBaseTest {
    @Test
    public void testWithPython(){
        String plainHex = "7a90adbc2df844d694dea1404c09926d100314d6e35b8bcff7ea68a2c0327208100314d6e35b8bcff7ea68a2c0327208100314d6e35b8bcff7ea68a2c0327208";
        String keyHex = "0baaebf8cacdc77c1f09d91e0ccc8132";
        String cipherHex = "4ef97098810447b3f5206183a46c7ff3896ecb2b61993db0fd579441602600c0896ecb2b61993db0fd579441602600c0896ecb2b61993db0fd579441602600c0";
        try{
            byte[] plainBytes = Hex.decodeHex(plainHex);
            byte[] keyBytes = Hex.decodeHex(keyHex);
            byte[] cipherBytes = Hex.decodeHex(cipherHex);
            assertEquals(plainBytes.length, 64);
            assertEquals(keyBytes.length, 16);
            assertEquals(cipherBytes.length, 64);

            byte[] solvedCipher = SM4Util.encrypt_Ecb_NoPadding(keyBytes, plainBytes);
            assertArrayEquals(solvedCipher, cipherBytes);
            byte[] solvedPlain = SM4Util.decrypt_Ecb_NoPadding(keyBytes, cipherBytes);
            assertArrayEquals(solvedPlain, plainBytes);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testEncryptAndDecrypt() {
        try {
            byte[] key = SM4Util.generateKey();
            byte[] iv = SM4Util.generateKey();
            byte[] cipherText = null;
            byte[] decryptedData = null;

            cipherText = SM4Util.encrypt_Ecb_NoPadding(key, SRC_DATA_16B);
            System.out.println("SM4 ECB NoPadding encrypt result:\n" + Arrays.toString(cipherText));
            decryptedData = SM4Util.decrypt_Ecb_NoPadding(key, cipherText);
            System.out.println("SM4 ECB NoPadding decrypt result:\n" + Arrays.toString(decryptedData));
            if (!Arrays.equals(decryptedData, SRC_DATA_16B)) {
                Assert.fail();
            }

            cipherText = SM4Util.encrypt_Ecb_Padding(key, SRC_DATA);
            System.out.println("SM4 ECB Padding encrypt result:\n" + Arrays.toString(cipherText));
            decryptedData = SM4Util.decrypt_Ecb_Padding(key, cipherText);
            System.out.println("SM4 ECB Padding decrypt result:\n" + Arrays.toString(decryptedData));
            if (!Arrays.equals(decryptedData, SRC_DATA)) {
                Assert.fail();
            }

            cipherText = SM4Util.encrypt_Cbc_Padding(key, iv, SRC_DATA);
            System.out.println("SM4 CBC Padding encrypt result:\n" + Arrays.toString(cipherText));
            decryptedData = SM4Util.decrypt_Cbc_Padding(key, iv, cipherText);
            System.out.println("SM4 CBC Padding decrypt result:\n" + Arrays.toString(decryptedData));
            if (!Arrays.equals(decryptedData, SRC_DATA)) {
                Assert.fail();
            }

            cipherText = SM4Util.encrypt_Cbc_NoPadding(key, iv, SRC_DATA_16B);
            System.out.println("SM4 CBC NoPadding encrypt result:\n" + Arrays.toString(cipherText));
            decryptedData = SM4Util.decrypt_Cbc_NoPadding(key, iv, cipherText);
            System.out.println("SM4 CBC NoPadding decrypt result:\n" + Arrays.toString(decryptedData));
            if (!Arrays.equals(decryptedData, SRC_DATA_16B)) {
                Assert.fail();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testMac() throws Exception {
        byte[] key = SM4Util.generateKey();
        byte[] iv = SM4Util.generateKey();

        byte[] mac = SM4Util.doCMac(key, SRC_DATA_24B);
        System.out.println("CMAC:\n" + ByteUtils.toHexString(mac).toUpperCase());

        mac = SM4Util.doGMac(key, iv, 16, SRC_DATA_24B);
        System.out.println("GMAC:\n" + ByteUtils.toHexString(mac).toUpperCase());

        byte[] cipher = SM4Util.encrypt_Cbc_NoPadding(key, iv, SRC_DATA_32B);
        byte[] cipherLast16 = Arrays.copyOfRange(cipher, cipher.length - 16, cipher.length);
        mac = SM4Util.doCBCMac(key, iv, null, SRC_DATA_32B);
        if (!Arrays.equals(cipherLast16, mac)) {
            Assert.fail();
        }
        System.out.println("CBCMAC:\n" + ByteUtils.toHexString(mac).toUpperCase());

        cipher = SM4Util.encrypt_Cbc_Padding(key, iv, SRC_DATA_32B);
        cipherLast16 = Arrays.copyOfRange(cipher, cipher.length - 16, cipher.length);
        mac = SM4Util.doCBCMac(key, iv, SRC_DATA_32B);
        if (!Arrays.equals(cipherLast16, mac)) {
            Assert.fail();
        }
        System.out.println("CBCMAC:\n" + ByteUtils.toHexString(mac).toUpperCase());
    }
}
