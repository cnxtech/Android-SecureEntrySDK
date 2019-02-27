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

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * Generates a Time-based one time password.
 */
public final class TOTP {

  private ByteBuffer secret;
  private int digits;
  private int timeInterval;
  private OTPAlgorithm algorithm;

  /**
   * Sets up how we will generate the password
   *
   * @param secret {@link ByteBuffer} wrapping the secret to encode into the password
   * @param digits length of the password; acceptable values are 6, 7 or 8 otherwise an exception
   * will be thrown
   * @param timeInterval time interval for the generation of the password (i.e window in which the
   * same password should occur)
   * @param algorithm {@link OTPAlgorithm} HMAC algorithm to use when hashing the secret (SHA1,
   * SHA256 &amp; SHA512 are supported)
   * @throws InstantiationException If invalid digits length or negative timeInterval is passed in
   */
  public TOTP(ByteBuffer secret, int digits, int timeInterval, OTPAlgorithm algorithm)
      throws InstantiationException {
    this.secret = secret;
    this.digits = digits;
    this.timeInterval = timeInterval;
    this.algorithm = algorithm;

    if (!validateDigits(digits)) {
      throw new InstantiationException("Invalid digits in constructor arguments!");
    }

    if (!validateTime(timeInterval)) {
      throw new InstantiationException("Invalid timeInterval in constructor arguments!");
    }
  }

  /**
   * Generates the one time password.
   *
   * @param time a {@link Date} to use for the one time password
   * @param withPadding <code>true</code>or<code>false</code> if the end should include padded zeros
   * @return 6, 7 or 8 digit password
   */
  public String generate(Date time, boolean withPadding) {
    int secondsPast1970 = (int) Math.floor(time.getTime()) / 1000;
    return generate(secondsPast1970, withPadding);
  }

  /**
   * Generates the one time password.
   *
   * @param secondsPast1970 time since 1970 in seconds
   * @param withPadding <code>true</code>or<code>false</code> if the end should include padded zeros
   * @return 6, 7 or 8 digit password
   */
  public String generate(double secondsPast1970, boolean withPadding) {
    int counterValue = (int) Math.floor(secondsPast1970 / (double) timeInterval);
    return GenerateOTP.now(secret, algorithm, (long) counterValue, digits, withPadding);
  }

  private boolean validateDigits(int digit) {
    int[] validDigits = {6, 7, 8};

    for (int validDigit : validDigits) {
      if (validDigit == digit) {
        return true;
      }
    }
    return false;
  }

  private boolean validateTime(int time) {
    return time > 0;
  }

}
