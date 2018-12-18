package com.ticketmaster.presence.totp;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class Sha256Tests {

    private static byte [] messageToEncrypt = "12345678901234567890123456789012".getBytes(StandardCharsets.US_ASCII);

    // SUT
    private TOTP totp;

    @Before
    public void setup(){
        try {
            totp = new TOTP(ByteBuffer.wrap(messageToEncrypt), 8, 30, OTPAlgorithm.SHA256);
        }catch (InstantiationException ex){
            ex.printStackTrace();
        }
    }

    @Test
    public void test02(){

        String expected = "46119246";
        int secondsPast1970 = 59;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test05(){

        String expected = "68084774";
        int secondsPast1970 = 1111111109;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test08(){

        String expected = "67062674";
        int secondsPast1970 = 1111111111;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test11(){

        String expected = "91819424";
        int secondsPast1970 = 1234567890;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test14(){

        String expected = "90698825";
        int secondsPast1970 = 2000000000;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

    @Test
    public void test17(){

        String expected = "77737706";
        long secondsPast1970 = 20000000000L;

        String hashedValue = totp.generate(secondsPast1970, false);

        assertEquals(expected, hashedValue);
    }

}
