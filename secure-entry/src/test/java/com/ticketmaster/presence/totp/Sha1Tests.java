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

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Sha1Tests {

    private static byte[] messageToEncrypt = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    // SUT
    private TOTP totp;

    @Before
    public void setup() {

        try {
            totp = new TOTP(ByteBuffer.wrap(messageToEncrypt), 8, 30, OTPAlgorithm.SHA1);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void test01() {

        String expected = "94287082";
        int secondsPast1970 = 59;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test04() {

        String expected = "07081804";
        int secondsPast1970 = 1111111109;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test07() {

        String expected = "14050471";
        int secondsPast1970 = 1111111111;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test10() {

        String expected = "89005924";
        int secondsPast1970 = 1234567890;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test13() {

        String expected = "69279037";
        int secondsPast1970 = 2000000000;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test16() {

        String expected = "65353130";
        long secondsPast1970 = 20000000000L;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void kaiCherryTest(){

        try {
            TOTP myTotp = new TOTP(ByteBuffer.wrap("093738027302049".getBytes()), 6, 60, OTPAlgorithm.SHA1);

            String expected = "045849";
            long secondsPast1970 = 1531178841;

            String hashedValue = myTotp.generate(secondsPast1970, true);

            assertEquals(expected, hashedValue);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }

    }

}
