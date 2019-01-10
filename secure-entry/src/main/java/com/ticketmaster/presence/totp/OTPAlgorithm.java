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

public enum OTPAlgorithm {

    //Hash Algorithm to use, either SHA-1, SHA-256 or SHA-512
    SHA1("SHA-1", 160 / 8, 64),
    SHA256("SHA-256", 256 / 8, 64),
    SHA512("SHA-512", 512 / 8, 128);

    private String digestName;
    private int digestLength;
    private int blockSize;

    OTPAlgorithm(String digestName, int digestLength, int blockSize) {
        this.digestName = digestName;
        this.digestLength = digestLength;
        this.blockSize = blockSize;
    }

    String getDigestName() {
        return digestName;
    }

    int getDigestLength() {
        return digestLength;
    }

    int getBlockSize() {
        return blockSize;
    }

}
