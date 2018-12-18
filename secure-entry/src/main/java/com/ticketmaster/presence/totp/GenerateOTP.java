package com.ticketmaster.presence.totp;
/*
    Copyright 2018 Ticketmaster

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

final class GenerateOTP {

    private GenerateOTP() {
    }

    static String now(ByteBuffer secret, OTPAlgorithm algorithm, long counter, int digits, boolean withPadding) {

        //Get byte array of secret key
        byte[] key = secret.array();

        byte[] tmp = new byte[Long.SIZE / Byte.SIZE];
        for (int i = 7; i >= 0; i--) {
            tmp[i] = (byte) (counter & 0xFF);
            counter >>= 8;
        }

        byte[] result;
        if (withPadding) {
            result = new byte[key.length];
            Arrays.fill(result, (byte) 0x00);
            System.arraycopy(tmp, 0, result, 0, tmp.length);
        } else {
            result = Arrays.copyOf(tmp, tmp.length);
        }

        //Generate HMAC message data from counter as big endian
        ByteBuffer counterMessage = ByteBuffer.wrap(result).order(ByteOrder.BIG_ENDIAN);

        //HMAC hash counter data with secret key
        byte[] hmac = null;
        try {
            hmac = new HMAC(key, algorithm).authenticate(counterMessage.array());
        } catch (InvalidObjectException ex) {
            ex.printStackTrace();
        }

        if (hmac == null) {
            return null;
        }

        //Get last 4 bits of hash as offset
        int offset = hmac[hmac.length - 1] == 0 ? 0x00 : hmac[hmac.length - 1] & 0x0f;

        //Get 4 bytes from the hash from [offset] to [offset + 4]
        byte[] truncatedHmac = Arrays.copyOfRange(hmac, offset, offset + 4);

        //Convert byte array of the truncated hash to ByteBuffer
        ByteBuffer data = ByteBuffer.wrap(truncatedHmac);

        //Convert data to int
        String hexString = bytesToHex(data.array());
        int number = (int) Long.parseLong(hexString, 16);

        //Remove most significant bit
        number &= 0x7fffffff;

        //Modulo number by 10^(digits)
        number = number % (int) (Math.pow(10, (float) digits));

        //Convert int to string
        String strNum = String.valueOf(number);
        if (strNum.length() == digits) {
            return strNum;
        } else {
            //Add zeros to start of string if not present
            char[] strings = new char[digits - strNum.length()];
            Arrays.fill(strings, '0');
            String prefixedZeros = String.copyValueOf(strings);
            return prefixedZeros + strNum;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}
