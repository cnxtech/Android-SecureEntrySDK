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

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

public class Sha256Tests {

  private static byte[] messageToEncrypt = "12345678901234567890123456789012"
      .getBytes(StandardCharsets.US_ASCII);

  // SUT
  private TOTP totp;

  @Before
  public void setup() {
    try {
      totp = new TOTP(ByteBuffer.wrap(messageToEncrypt), 8, 30, OTPAlgorithm.SHA256);
    } catch (InstantiationException ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void test02() {

    String expected = "46119246";
    int secondsPast1970 = 59;

    String hashedValue = totp.generate(secondsPast1970, false);

    assertEquals(expected, hashedValue);
  }

  @Test
  public void test05() {

    String expected = "68084774";
    int secondsPast1970 = 1111111109;

    String hashedValue = totp.generate(secondsPast1970, false);

    assertEquals(expected, hashedValue);
  }

  @Test
  public void test08() {

    String expected = "67062674";
    int secondsPast1970 = 1111111111;

    String hashedValue = totp.generate(secondsPast1970, false);

    assertEquals(expected, hashedValue);
  }

  @Test
  public void test11() {

    String expected = "91819424";
    int secondsPast1970 = 1234567890;

    String hashedValue = totp.generate(secondsPast1970, false);

    assertEquals(expected, hashedValue);
  }

  @Test
  public void test14() {

    String expected = "90698825";
    int secondsPast1970 = 2000000000;

    String hashedValue = totp.generate(secondsPast1970, false);

    assertEquals(expected, hashedValue);
  }

  @Test
  public void test17() {

    String expected = "77737706";
    long secondsPast1970 = 20000000000L;

    String hashedValue = totp.generate(secondsPast1970, false);

    assertEquals(expected, hashedValue);
  }

}
