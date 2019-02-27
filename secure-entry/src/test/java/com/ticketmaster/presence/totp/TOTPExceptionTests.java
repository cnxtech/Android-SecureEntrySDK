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

import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class TOTPExceptionTests {

  @Test
  public void testInvalidDigitsShouldThrowException() {
    try {
      new TOTP(ByteBuffer.wrap("12345678901234567890".getBytes(StandardCharsets.US_ASCII)), 5, 30,
          OTPAlgorithm.SHA1);
      fail("TOTP didn't throw the exception I expected!");
    } catch (InstantiationException ex) {
      ex.printStackTrace();
    }
  }

  @Test
  public void testNegativeTimeIntervalShouldThrowException() {
    try {
      new TOTP(ByteBuffer.wrap("12345678901234567890".getBytes(StandardCharsets.US_ASCII)), 8, -30,
          OTPAlgorithm.SHA1);
      fail("TOTP didn't throw the exception I expected!");
    } catch (InstantiationException ex) {
      ex.printStackTrace();
    }
  }

}
