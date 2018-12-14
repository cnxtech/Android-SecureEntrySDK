package com.ticketmaster.presence.totp;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Sha512Tests {

    private static byte [] messageToEncrypt = "1234567890123456789012345678901234567890123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

    // SUT
    private TOTP totp;

    @Before
    public void setup(){
        try {
            totp = new TOTP(ByteBuffer.wrap(messageToEncrypt), 8, 30, OTPAlgorithm.SHA512);
        }catch (InstantiationException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void test03(){

        String expected = "90693936";
        int secondsPast1970 = 59;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test06(){

        String expected = "25091201";
        int secondsPast1970 = 1111111109;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test09(){

        String expected = "99943326";
        int secondsPast1970 = 1111111111;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test12(){

        String expected = "93441116";
        int secondsPast1970 = 1234567890;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test15(){

        String expected = "38618901";
        int secondsPast1970 = 2000000000;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test18(){

        String expected = "47863826";
        long secondsPast1970 = 20000000000L;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

}
