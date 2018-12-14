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

import android.content.Context;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Clock {

    public interface Callback {

        void onComplete(long offset, Date now);
    }

    private static Clock clock;

    private static TimeFreeze stableTime;
    private static IStorage timeStorage;

    private static ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();

    private Clock(Context context) {
        timeStorage = new TimeStorage(context);
    }

    public static Clock getInstance(Context ctx) {
        if (clock == null) {
            clock = new Clock(ctx);
        }
        return clock;
    }

    public Date now() {
        stableTime = timeStorage.getStableTime();
        if (stableTime != null) {
            return new Date(stableTime.getAdjustedTimestamp());
        } else {
            return null;
        }
    }

    public void sync(final NTPHost host, final Callback callback) {

        loadFromDefaults();

        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                NTPClient.query(host, new Callback() {
                    @Override
                    public void onComplete(long offset, Date now) {
                        stableTime = new TimeFreeze(offset);
                        timeStorage.setStableTime(stableTime);
                        callback.onComplete(offset, now);
                    }
                });
            }
        });

    }

    public static void reset() {
        stableTime = null;
        timeStorage.purgeStorage();
    }

    private void loadFromDefaults() {
        TimeFreeze previousStableTime = timeStorage.getStableTime();
        if (previousStableTime == null) {
            stableTime = null;
            return;
        }
        stableTime = previousStableTime;
    }

}
