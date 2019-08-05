package com.rd.sh.utils;



/**
 * Created by taojinhou on 2018/3/9.
 */
public class LibUserinfoSo {
    public static String getName(int timestamp, String encode) {
        byte[] as = new byte[18], cp = new byte[18];

        byte[] shuffle = String.format("%08x", timestamp).getBytes();
        byte[] shuffleAs = shuffleAS(shuffle);
        byte[] shuffleCp = shuffleCP(shuffle);
        String md5 = MD5.GetMD5Code(encode);
        if ((timestamp & 1) != 0) {
            md5 = MD5.GetMD5Code(md5);
        }

        byte[] md5Bytes = md5.getBytes();
        byte version = '1';
        as[0] = 'a';
        as[1] = version;
        for (int i = 0; i <= 7; i++){
            as[2 * (i + 1)] = md5Bytes[i];
            as[2 * i + 3] = shuffleAs[i];

            cp[2 * i] = shuffleCp[i];
            cp[2 * i + 1] = md5Bytes[i + 24];
        }

        cp[16] = 'e';
        cp[17] = version;

        return new String(as) + new String(cp);
    }

    private static byte[] shuffleAS(byte[] result) {
        return shuffle(result, "15387264".getBytes());
    }

    private static byte[] shuffleCP(byte[] result) {
        return shuffle(result, "57218436".getBytes());
    }

    private static byte[] shuffle(byte[] result, byte[] code) {
        byte[] tmp = new byte[8];
        for (int i = 0; i < result.length; i++){
            tmp[i] = result[code[i] - 49];
        }
        return tmp;
    }
}
