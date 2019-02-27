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

import java.util.Arrays;
import java.util.Collections;

final class ZeroPadding {

  byte[] add(byte[] toBytes, int blockSize) {
    int paddingCount = blockSize - (toBytes.length % blockSize);
    if (paddingCount > 0) {
      byte[] mergedArray = new byte[toBytes.length + paddingCount];
      Arrays.fill(mergedArray, (byte) 0);
      System.arraycopy(toBytes, 0, mergedArray, 0, toBytes.length);
      return mergedArray;
    }

    return toBytes;
  }


  byte[] remove(byte[] fromBytes, int blockSize) {
    Collections.reverse(Arrays.asList(fromBytes));
    for (int i = 0; i < fromBytes.length; i++) {

      if (fromBytes[i] != 0) {
        return Arrays.copyOfRange(fromBytes, 0, fromBytes.length - i);
      }
    }
    return fromBytes;
  }
}
