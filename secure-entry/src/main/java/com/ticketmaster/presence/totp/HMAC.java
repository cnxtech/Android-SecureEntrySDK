package com.ticketmaster.presence.totp;
/*
    Copyright 2019 Ticketmaster

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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

final class HMAC {

    private byte[] key;
    private OTPAlgorithm algorithm;

    HMAC(byte[] key, OTPAlgorithm algorithm) {
        this.key = key;
        this.algorithm = algorithm;

        if (key.length > algorithm.getBlockSize()) {
            this.key = generateHash(key);
        }

        if (key.length < algorithm.getBlockSize()) {
            ZeroPadding zeroPadding = new ZeroPadding();
            byte[] result = zeroPadding.add(key, algorithm.getBlockSize());
            System.arraycopy(result, 0, this.key, 0, this.key.length);
        }

    }

    byte[] authenticate(byte[] bytes) throws InvalidObjectException {
        byte[] opad = new byte[algorithm.getBlockSize()];
        Arrays.fill(opad, (byte) 0x5c);
        for (int i = 0; i < key.length; i++) {
            opad[i] = (byte) (key[i] ^ opad[i]);
        }

        byte[] ipad = new byte[algorithm.getBlockSize()];
        Arrays.fill(ipad, (byte) 0x36);
        for (int i = 0; i < key.length; i++) {
            ipad[i] = (byte) (key[i] ^ ipad[i]);
        }

        byte[] mergedArray = new byte[ipad.length + bytes.length];
        System.arraycopy(ipad, 0, mergedArray, 0, ipad.length);
        System.arraycopy(bytes, 0, mergedArray, ipad.length, bytes.length);
        byte[] ipadAndMessageHash = generateHash(mergedArray);

        if (ipadAndMessageHash == null) {
            throw new InvalidObjectException("ipadAndMessageHash was null!");
        }

        byte[] mergedArray2 = new byte[opad.length + ipadAndMessageHash.length];
        System.arraycopy(opad, 0, mergedArray2, 0, opad.length);
        System.arraycopy(ipadAndMessageHash, 0, mergedArray2, opad.length, ipadAndMessageHash.length);
        byte[] result = generateHash(mergedArray2);

        if (result == null) {
            throw new InvalidObjectException("result hash was null!");
        }

        return result;
    }

    private byte[] generateHash(byte[] bytes) {
        switch (algorithm) {
            case SHA1:
                return hash(bytes);
            case SHA256:
                return hash(bytes);
            case SHA512:
                return hash(bytes);
        }
        return null;
    }

    private byte[] hash(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm.getDigestName());
            md.reset();
            md.update(bytes);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
