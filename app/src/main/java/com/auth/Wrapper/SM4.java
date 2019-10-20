package com.auth.Wrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class SM4 {
    private long GET_ULONG_BE(byte[] b, int i) {
        long n = (long) (b[i] & 0xff) << 24 | (long) ((b[i + 1] & 0xff) << 16) | (long) ((b[i + 2] & 0xff) << 8) | (long) (b[i + 3] & 0xff) & 0xffffffffL;
        return n;
    }

    private void PUT_ULONG_BE(long n, byte[] b, int i) {
        b[i] = (byte) (int) (0xFF & n >> 24);
        b[i + 1] = (byte) (int) (0xFF & n >> 16);
        b[i + 2] = (byte) (int) (0xFF & n >> 8);
        b[i + 3] = (byte) (int) (0xFF & n);
    }

    private long SHL(long x, int n) {
        return (x & 0xFFFFFFFF) << n;
    }

    private long ROTL(long x, int n) {
        return SHL(x, n) | x >> (32 - n);
    }

    private void SWAP(long[] sk, int i) {
        long t = sk[i];
        sk[i] = sk[(31 - i)];
        sk[(31 - i)] = t;
    }

    private byte sm4Sbox(byte inch) {
        int i = inch & 0xFF;
        byte retVal = SM4Constant.SboxTable[i];
        return retVal;
    }

    private long sm4Lt(long ka) {
        long bb = 0L;
        long c = 0L;
        byte[] a = new byte[4];
        byte[] b = new byte[4];
        PUT_ULONG_BE(ka, a, 0);
        b[0] = sm4Sbox(a[0]);
        b[1] = sm4Sbox(a[1]);
        b[2] = sm4Sbox(a[2]);
        b[3] = sm4Sbox(a[3]);
        bb = GET_ULONG_BE(b, 0);
        c = bb ^ ROTL(bb, 2) ^ ROTL(bb, 10) ^ ROTL(bb, 18) ^ ROTL(bb, 24);
        return c;
    }

    private long sm4F(long x0, long x1, long x2, long x3, long rk) {
        return x0 ^ sm4Lt(x1 ^ x2 ^ x3 ^ rk);
    }

    private long sm4CalciRK(long ka) {
        long bb = 0L;
        long rk = 0L;
        byte[] a = new byte[4];
        byte[] b = new byte[4];
        PUT_ULONG_BE(ka, a, 0);
        b[0] = sm4Sbox(a[0]);
        b[1] = sm4Sbox(a[1]);
        b[2] = sm4Sbox(a[2]);
        b[3] = sm4Sbox(a[3]);
        bb = GET_ULONG_BE(b, 0);
        rk = bb ^ ROTL(bb, 13) ^ ROTL(bb, 23);
        return rk;
    }

    private void sm4_setkey(long[] SK, byte[] key) {
        long[] MK = new long[4];
        long[] k = new long[36];
        int i = 0;
        MK[0] = GET_ULONG_BE(key, 0);
        MK[1] = GET_ULONG_BE(key, 4);
        MK[2] = GET_ULONG_BE(key, 8);
        MK[3] = GET_ULONG_BE(key, 12);
        k[0] = MK[0] ^ (long) SM4Constant.FK[0];
        k[1] = MK[1] ^ (long) SM4Constant.FK[1];
        k[2] = MK[2] ^ (long) SM4Constant.FK[2];
        k[3] = MK[3] ^ (long) SM4Constant.FK[3];
        for (; i < 32; i++) {
            k[(i + 4)] = (k[i] ^ sm4CalciRK(k[(i + 1)] ^ k[(i + 2)] ^ k[(i + 3)] ^ (long) SM4Constant.CK[i]));
            SK[i] = k[(i + 4)];
        }
    }

    private void sm4_one_round(long[] sk, byte[] input, byte[] output) {
        int i = 0;
        long[] ulbuf = new long[36];
        ulbuf[0] = GET_ULONG_BE(input, 0);
        ulbuf[1] = GET_ULONG_BE(input, 4);
        ulbuf[2] = GET_ULONG_BE(input, 8);
        ulbuf[3] = GET_ULONG_BE(input, 12);
        while (i < 32) {
            ulbuf[(i + 4)] = sm4F(ulbuf[i], ulbuf[(i + 1)], ulbuf[(i + 2)], ulbuf[(i + 3)], sk[i]);
            i++;
        }
        PUT_ULONG_BE(ulbuf[35], output, 0);
        PUT_ULONG_BE(ulbuf[34], output, 4);
        PUT_ULONG_BE(ulbuf[33], output, 8);
        PUT_ULONG_BE(ulbuf[32], output, 12);
    }

    private byte[] padding(byte[] input, int mode) {
        if (input == null) {
            return null;
        }

        byte[] ret = (byte[]) null;
        if (mode == SM4Constant.SM4_ENCRYPT) {
            int p = 16 - input.length % 16;
            ret = new byte[input.length + p];
            System.arraycopy(input, 0, ret, 0, input.length);
            for (int i = 0; i < p; i++) {
                ret[input.length + i] = (byte) p;
            }
        } else {
            int p = input[input.length - 1];
            ret = new byte[input.length - p];
            System.arraycopy(input, 0, ret, 0, input.length - p);
        }
        return ret;
    }

    public void sm4_setkey_enc(SM4Context ctx, byte[] key) throws Exception {
        if (ctx == null) {
            throw new Exception("ctx is null!");
        }

        if (key == null || key.length != 16) {
            throw new Exception("key error!");
        }

        ctx.mode = SM4Constant.SM4_ENCRYPT;
        sm4_setkey(ctx.sk, key);
    }

    public void sm4_setkey_dec(SM4Context ctx, byte[] key) throws Exception {
        if (ctx == null) {
            throw new Exception("ctx is null!");
        }

        if (key == null || key.length != 16) {
            throw new Exception("key error!");
        }

        int i = 0;
        ctx.mode = SM4Constant.SM4_DECRYPT;
        sm4_setkey(ctx.sk, key);
        for (i = 0; i < 16; i++) {
            SWAP(ctx.sk, i);
        }
    }

    public byte[] sm4_crypt_ecb(SM4Context ctx, byte[] input) throws Exception {
        if (input == null) {
            throw new Exception("input is null!");
        }

        if ((ctx.isPadding) && (ctx.mode == SM4Constant.SM4_ENCRYPT)) {
            input = padding(input, SM4Constant.SM4_ENCRYPT);
        }

        int length = input.length;
        ByteArrayInputStream bins = new ByteArrayInputStream(input);
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        for (; length > 0; length -= 16) {
            byte[] in = new byte[16];
            byte[] out = new byte[16];
            bins.read(in);
            sm4_one_round(ctx.sk, in, out);
            bous.write(out);
        }

        byte[] output = bous.toByteArray();
        if (ctx.isPadding && ctx.mode == SM4Constant.SM4_DECRYPT) {
            output = padding(output, SM4Constant.SM4_DECRYPT);
        }
        bins.close();
        bous.close();
        return output;
    }

    public byte[] sm4_crypt_cbc(SM4Context ctx, byte[] iv, byte[] input) throws Exception {
        if (iv == null || iv.length != 16) {
            throw new Exception("iv error!");
        }

        if (input == null) {
            throw new Exception("input is null!");
        }

        if (ctx.isPadding && ctx.mode == SM4Constant.SM4_ENCRYPT) {
            input = padding(input, SM4Constant.SM4_ENCRYPT);
        }

        int i = 0;
        int length = input.length;
        ByteArrayInputStream bins = new ByteArrayInputStream(input);
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        if (ctx.mode == SM4Constant.SM4_ENCRYPT) {
            for (; length > 0; length -= 16) {
                byte[] in = new byte[16];
                byte[] out = new byte[16];
                byte[] out1 = new byte[16];

                bins.read(in);
                for (i = 0; i < 16; i++) {
                    out[i] = ((byte) (in[i] ^ iv[i]));
                }
                sm4_one_round(ctx.sk, out, out1);
                System.arraycopy(out1, 0, iv, 0, 16);
                bous.write(out1);
            }
        } else {
            byte[] temp = new byte[16];
            for (; length > 0; length -= 16) {
                byte[] in = new byte[16];
                byte[] out = new byte[16];
                byte[] out1 = new byte[16];

                bins.read(in);
                System.arraycopy(in, 0, temp, 0, 16);
                sm4_one_round(ctx.sk, in, out);
                for (i = 0; i < 16; i++) {
                    out1[i] = ((byte) (out[i] ^ iv[i]));
                }
                System.arraycopy(temp, 0, iv, 0, 16);
                bous.write(out1);
            }
        }

        byte[] output = bous.toByteArray();
        if (ctx.isPadding && ctx.mode == SM4Constant.SM4_DECRYPT) {
            output = padding(output, SM4Constant.SM4_DECRYPT);
        }
        bins.close();
        bous.close();
        return output;
    }
}
