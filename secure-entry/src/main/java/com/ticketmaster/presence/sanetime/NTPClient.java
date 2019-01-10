package com.ticketmaster.presence.sanetime;
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

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

final class NTPClient {

    private static final int DEFAULT_TIMEOUT = 10000;

    private static NTPUDPClient client = new NTPUDPClient();

    static void query(NTPHost ntpHost, Clock.Callback callback) {
        client.setDefaultTimeout(DEFAULT_TIMEOUT);
        try {
            client.open();

            try {
                InetAddress hostAddr = InetAddress.getByName(ntpHost.getHost());

                TimeInfo info = client.getTime(hostAddr);
                NtpV3Packet message = info.getMessage();

                // Time we want
                TimeStamp rcvNtpTime = message.getReceiveTimeStamp();

                info.computeDetails(); // compute offset/delay if not already done
                long offsetValue = info.getOffset();

                callback.onComplete(offsetValue, rcvNtpTime.getDate());

            } catch (IOException ioe) {
                ioe.printStackTrace();
                callback.onError();
            }

        } catch (SocketException e) {
            e.printStackTrace();
            callback.onError();
        }

        client.close();
    }

}
