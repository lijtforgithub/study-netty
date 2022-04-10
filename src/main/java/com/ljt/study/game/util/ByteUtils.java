package com.ljt.study.game.util;

import java.util.Objects;

/**
 * @author LiJingTang
 * @date 2022-04-10 9:32
 */
public final class ByteUtils {

    private ByteUtils() {
    }

    public static byte[] intToBytes(int i) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((i >> 24) & 0XFF);
        bytes[1] = (byte) ((i >> 16) & 0XFF);
        bytes[2] = (byte) ((i >> 8) & 0XFF);
        bytes[3] = (byte) (i & 0XFF);

        return bytes;
    }

    public static int bytesToInt(byte[] bytes) {
        if (Objects.isNull(bytes) || bytes.length != 4) {
            throw new IllegalArgumentException();
        }

        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0XFF) << shift;
        }

        return value;
    }

}
