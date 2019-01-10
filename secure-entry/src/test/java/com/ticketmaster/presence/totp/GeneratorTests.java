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

import org.apache.commons.codec.binary.Base32;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

public class GeneratorTests {

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Test
    public  void testGenerator6DigitHexSha1(){
        ByteBuffer buffer = ByteBuffer.wrap(GeneratorTests.hexStringToByteArray("3132333435363738393031323334353637383930"));
        assertEquals( "755224", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 0, 6, false));
        assertEquals( "287082", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 1, 6, false));
        assertEquals( "359152", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 2, 6, false));
    }



    @Test
    public  void testGenerator7DigitHexSha1(){
        ByteBuffer buffer = ByteBuffer.wrap(GeneratorTests.hexStringToByteArray("3132333435363738393031323334353637383930"));
        assertEquals( "4755224", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 0, 7, false));
        assertEquals( "4287082", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 1, 7, false));
        assertEquals( "7359152", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 2, 7, false));
    }

    @Test
    public  void testGenerator8DigitHexSha1(){
        ByteBuffer buffer = ByteBuffer.wrap(GeneratorTests.hexStringToByteArray("3132333435363738393031323334353637383930"));
        assertEquals( "84755224", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 0, 8, false));
        assertEquals( "94287082", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 1, 8, false));
        assertEquals( "37359152", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 2, 8, false));
    }

    @Test
    public  void testGenerator6DigitBase32Sha256(){
        Base32 base32 = new Base32();
        ByteBuffer buffer = ByteBuffer.wrap(base32.decode("ABCDEFGHIJKLMNOP".getBytes()));
        assertEquals( "158995", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 0, 6, false));
        assertEquals( "604514", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 1, 6, false));
        assertEquals( "762356", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 2, 6, false));
    }

    @Test
    public  void testGenerator7DigitBase32Sha256(){
        Base32 base32 = new Base32();
        ByteBuffer buffer = ByteBuffer.wrap(base32.decode("ABCDEFGHIJKLMNOP".getBytes()));
        assertEquals( "8158995", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 0, 7, false));
        assertEquals( "1604514", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 1, 7, false));
        assertEquals( "5762356", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 2, 7, false));
    }

    @Test
    public  void testGenerator8DigitBase32Sha256(){
        Base32 base32 = new Base32();
        ByteBuffer buffer = ByteBuffer.wrap(base32.decode("ABCDEFGHIJKLMNOP".getBytes()));
        assertEquals( "38158995", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 0, 8, false));
        assertEquals( "51604514", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 1, 8, false));
        assertEquals( "95762356", GenerateOTP.now(buffer, OTPAlgorithm.SHA256, 2, 8, false));
    }

    @Test
    public  void testGenerator6DigitBase32Sha512(){
        Base32 base32 = new Base32();
        ByteBuffer buffer = ByteBuffer.wrap(base32.decode("ABCDEFGHIJKLMNOP".getBytes()));
        assertEquals( "339279", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 0, 6, false));
        assertEquals( "597655", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 1, 6, false));
        assertEquals( "045732", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 2, 6, false));
    }

    @Test
    public  void testGenerator7DigitBase32Sha512(){
        Base32 base32 = new Base32();
        ByteBuffer buffer = ByteBuffer.wrap(base32.decode("ABCDEFGHIJKLMNOP".getBytes()));
        assertEquals( "7339279", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 0, 7, false));
        assertEquals( "4597655", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 1, 7, false));
        assertEquals( "4045732", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 2, 7, false));
    }

    @Test
    public  void testGenerator8DigitBase32Sha512(){
        Base32 base32 = new Base32();
        ByteBuffer buffer = ByteBuffer.wrap(base32.decode("ABCDEFGHIJKLMNOP".getBytes()));
        assertEquals( "37339279", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 0, 8, false));
        assertEquals( "04597655", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 1, 8, false));
        assertEquals( "34045732", GenerateOTP.now(buffer, OTPAlgorithm.SHA512, 2, 8, false));
    }

    @Test
    public void kaiCherryTest(){
        ByteBuffer buffer = ByteBuffer.wrap("093738027302049".getBytes());
        assertEquals( "783700", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 25519419, 6, true));
    }

    @Test
    public void sha1SecretTest(){
        ByteBuffer buffer = ByteBuffer.wrap(GeneratorTests.hexStringToByteArray("bb166c21cafc0a7aed1ae7a23141f9115556ae07"));
        assertEquals( "632703", GenerateOTP.now(buffer, OTPAlgorithm.SHA1, 102245719, 6, true));
    }

}
