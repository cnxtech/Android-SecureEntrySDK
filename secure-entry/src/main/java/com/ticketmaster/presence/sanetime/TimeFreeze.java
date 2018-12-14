package com.ticketmaster.presence.sanetime;
/*
    Copyright 2018 Ticketmaster

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

import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;

final class TimeFreeze {

    private static final String KEY_UPTIME = "Uptime";
    private static final String KEY_TIMESTAMP = "Timestamp";
    private static final String KEY_OFFSET = "Offset";

    private long uptime;
    private long timestamp;
    private long offset;

    TimeFreeze(long offset) {
        this.offset = offset;
        this.timestamp = TimeUtil.currentTime();
        this.uptime = TimeFreeze.systemUptime();
    }

    TimeFreeze(Map<String, Long> dictionary) {
        long uptime = dictionary.get(KEY_UPTIME);
        long timestamp = dictionary.get(KEY_TIMESTAMP);
        long offset = dictionary.get(KEY_OFFSET);

        long currentUptime = TimeFreeze.systemUptime();
        long currentTimestamp = TimeUtil.currentTime();
        long currentBoot = currentUptime - currentTimestamp;
        long previousBoot = uptime - timestamp;

        if (Math.round(currentBoot) - Math.round(previousBoot) != 0) {
            this.uptime = currentUptime;
            this.timestamp = currentTimestamp;
        } else {
            this.uptime = uptime;
            this.timestamp = timestamp;
        }
        this.offset = offset;
    }

    /*
      The stable timestamp adjusted by the most accurate offset known so far.
      */
    long getAdjustedTimestamp() {
        return offset + getStableTimestamp();
    }

    /*
      The stable timestamp (calculated based on the uptime); note that this
      doesn't have sub-seconds precision. See `systemUptime()` for more
      information.
      */
    private long getStableTimestamp() {
        return (TimeFreeze.systemUptime() - uptime) + timestamp;
    }

    Map<String, Long> toDictionary() {
        Map<String, Long> map = new HashMap<>();
        map.put(KEY_UPTIME, uptime);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_OFFSET, offset);
        return map;
    }

    /*
        Returns a high-resolution measurement of system uptime,
        that continues ticking through device sleep
      */
    private static long systemUptime() {

        // Returns milliseconds since boot, including time spent in sleep.
        long bootTime = SystemClock.elapsedRealtime();
        if (bootTime == 0) {
            throw new AssertionError("system clock error: device boot time unavailable");
        }

        long now = TimeUtil.currentTime();
        boolean timeError = now >= bootTime;
        if (!timeError) {
            throw new AssertionError("inconsistent clock state: system time precedes boot time");
        }

        return now - bootTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeFreeze that = (TimeFreeze) o;

        // we can't use timestamp here since it will be moving with the clock...
        return uptime == that.uptime &&
                offset == that.offset;
    }

    @Override
    public int hashCode() {
        long result = 0;
        result += offset;
        result += timestamp;
        result += uptime;
        return Long.valueOf(result).hashCode();
    }

}
