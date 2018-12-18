package com.ticketmaster.presence.sanetime;

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
