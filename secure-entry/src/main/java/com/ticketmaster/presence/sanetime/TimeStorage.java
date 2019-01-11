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

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

final class TimeStorage implements IStorage {

    private SharedPreferences userDefaults;

    TimeStorage(Context context) {
        userDefaults = context.getSharedPreferences("com.ticketmaster.presence.secure_entry_preferences",Context.MODE_PRIVATE);
    }

    @Override
    public TimeFreeze getStableTime() {
        TimeFreeze stableTime = null;
        Map<String, ?> stored = userDefaults.getAll();
        if (stored.size() > 0) {
            TimeFreeze previousStableTime;
            Map<String, Long> stableMap = new HashMap<>();
            for (Map.Entry<String, ?> entry : stored.entrySet()) {
                if (entry.getValue() instanceof Long) {
                    stableMap.put(entry.getKey(), (Long) entry.getValue());
                }
            }
            previousStableTime = new TimeFreeze(stableMap);
            stableTime = previousStableTime;
        }

        return stableTime;
    }

    @Override
    public void setStableTime(TimeFreeze stableTime) {
        Map<String, Long> map = stableTime.toDictionary();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            userDefaults.edit()
                    .putLong(entry.getKey(), entry.getValue())
                    .apply();
        }
    }

    @Override
    public void purgeStorage() {
        userDefaults.edit().clear().apply();
    }

}
