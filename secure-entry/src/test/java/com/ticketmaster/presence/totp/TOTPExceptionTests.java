package com.ticketmaster.presence.totp;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.fail;

public class TOTPExceptionTests {

    @Test
    public void testInvalidDigitsShouldThrowException() {
        try {
            new TOTP(ByteBuffer.wrap("12345678901234567890".getBytes(StandardCharsets.US_ASCII)), 5, 30, OTPAlgorithm.SHA1);
            fail("TOTP didn't throw the exception I expected!");
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testNegativeTimeIntervalShouldThrowException() {
        try {
            new TOTP(ByteBuffer.wrap("12345678901234567890".getBytes(StandardCharsets.US_ASCII)), 8, -30, OTPAlgorithm.SHA1);
            fail("TOTP didn't throw the exception I expected!");
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        }
    }

}
