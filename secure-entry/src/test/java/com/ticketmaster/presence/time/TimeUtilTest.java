package com.ticketmaster.presence.time;
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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TimeUtilTest {

    @Test
    public void testTimeUtilReturnsCurrentTime(){
        // time the function so we can be accurate about the assert
        long startTime = System.currentTimeMillis();
        long actualTime = Math.round(TimeUtil.currentTime());

        // compute the difference
        long diffTime = startTime - System.currentTimeMillis();
        long expectedTime = Math.round(System.currentTimeMillis() - diffTime);

        assertEquals(actualTime, expectedTime);
    }

}
