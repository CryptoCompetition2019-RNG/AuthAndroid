package com.auth.Wrapper;

public class SM3Digest {
    /**
     * SM3值的长度
     */
    private static final int BYTE_LENGTH = 32;

    /**
     * SM3分组长度
     */
    private static final int BLOCK_LENGTH = 64;

    /**
     * 缓冲区
     */
    private byte[] xBuf = new byte[BLOCK_LENGTH + 1];

    /**
     * 缓冲区偏移量
     */
    private int xBufOff;

    /**
     * 初始向量
     */
    private byte[] V = SM3Constant.iv.clone();

    private int cntBlock = 0;

    public SM3Digest() {
    }

    public SM3Digest(SM3Digest t) {
        System.arraycopy(t.xBuf, 0, this.xBuf, 0, t.xBuf.length);
        this.xBufOff = t.xBufOff;
        System.arraycopy(t.V, 0, this.V, 0, t.V.length);
    }

    /**
     * SM3结果输出
     *
     * @param out    保存SM3结构的缓冲区
     * @param outOff 缓冲区偏移量
     * @return
     */
    public int doFinal(byte[] out, int outOff) {
        byte[] tmp = doFinal();
        System.arraycopy(tmp, 0, out, 0, tmp.length);
        return BYTE_LENGTH;
    }

    public void reset() {
        xBufOff = 0;
        cntBlock = 0;
        V = SM3Constant.iv.clone();
    }

    /**
     * 明文输入
     *
     * @param in    明文输入缓冲区
     * @param inOff 缓冲区偏移量
     * @param len   明文长度
     */
    public void update(byte[] in, int inOff, int len) {
        int partLen = BLOCK_LENGTH + 1 - xBufOff;
        int inputLen = len;
        int dPos = inOff;
        if (partLen < inputLen) {
            System.arraycopy(in, dPos, xBuf, xBufOff, partLen);
            inputLen -= partLen;
            dPos += partLen;
            doUpdate();
            while (inputLen > BLOCK_LENGTH + 1) {
                System.arraycopy(in, dPos, xBuf, 0, BLOCK_LENGTH + 1);
                inputLen -= BLOCK_LENGTH + 1;
                dPos += BLOCK_LENGTH + 1;
                doUpdate();
            }
        }

        System.arraycopy(in, dPos, xBuf, xBufOff, inputLen);
        xBufOff += inputLen;
    }

    private void doUpdate() {
        byte[] B = new byte[BLOCK_LENGTH + 1];
        for (int i = 0; i < BLOCK_LENGTH + 1; i += BLOCK_LENGTH) {
            System.arraycopy(xBuf, i, B, 0, B.length);
            doHash(B);
        }
        xBufOff = 0;
    }

    private void doHash(byte[] B) {
        byte[] tmp = SM3.CF(V, B);
        System.arraycopy(tmp, 0, V, 0, V.length);
        cntBlock++;
    }

    private byte[] doFinal() {
        byte[] B = new byte[BLOCK_LENGTH];
        byte[] buffer = new byte[xBufOff];
        System.arraycopy(xBuf, 0, buffer, 0, buffer.length);
        byte[] tmp = SM3.padding(buffer, cntBlock);
        for (int i = 0; i < tmp.length; i += BLOCK_LENGTH) {
            System.arraycopy(tmp, i, B, 0, B.length);
            doHash(B);
        }
        return V;
    }

    public void update(byte in) {
        byte[] buffer = new byte[]{in};
        update(buffer, 0, 1);
    }

    public int getDigestSize() {
        return BYTE_LENGTH;
    }


}


