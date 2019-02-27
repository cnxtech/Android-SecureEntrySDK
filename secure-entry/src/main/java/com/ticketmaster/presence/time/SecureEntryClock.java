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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class dedicated to syncing the time with an NTP server to get the actual time.
 */
public final class SecureEntryClock {

  /**
   * Callback interface used for receiving a response on the UI thread.
   *
   * Note: This is not required for calling {@link SecureEntryClock#syncTime()} but it is
   * important to note that the call will always be asynchronous.
   */
  public interface Callback {

    void onComplete(long offset, Date now);

    void onError();
  }

  private static SecureEntryClock sSecureEntryClock;

  private static TimeFreeze stableTime;
  private static IStorage timeStorage;

  private static ExecutorService threadPoolExecutor = Executors.newSingleThreadExecutor();

  private SecureEntryClock(Context context) {
    final SharedPreferences sharedPreferences = context
        .getSharedPreferences("com.ticketmaster.presence.secure_entry_preferences",
            Context.MODE_PRIVATE);
    timeStorage = new TimeStorage(sharedPreferences);
  }

  /**
   * Creates and returns the singleton instance of SecureEntryClock
   * @param ctx application context
   * @return this
   */
  public static SecureEntryClock getInstance(Context ctx) {
    if (sSecureEntryClock == null) {
      sSecureEntryClock = new SecureEntryClock(ctx);
    }
    return sSecureEntryClock;
  }

  /**
   * Returns the cached time if there is one.
   * @return {@link Date} stored from {@link TimeStorage} or <code>null</code>
   */
  public Date now() {
    stableTime = timeStorage.getStableTime();
    if (stableTime != null) {
      return new Date(stableTime.getAdjustedTimestamp());
    } else {
      return null;
    }
  }

  /**
   * Syncs time with the NTP server without providing a response.
   */
  public void syncTime() {
    this.syncTime(null);
  }

  /**
   * Syncs time with the NTP server and provides a response via the {@link Callback}.
   * @param callback an interface for receiving a response from the time sync
   */
  public void syncTime(@Nullable final Callback callback) {

    loadFromDefaults();

    threadPoolExecutor.submit(new Runnable() {
      @Override
      public void run() {
        NTPClient.query(NTPHost.NTP_POOL_PROJECT, new Callback() {
          @Override
          public void onComplete(long offset, Date now) {
            stableTime = new TimeFreeze(offset);
            timeStorage.setStableTime(stableTime);
            if (callback != null) {
              callback.onComplete(offset, now);
            }
          }

          @Override
          public void onError() {
            if (callback != null) {
              callback.onError();
            }
          }
        });
      }
    });

  }

  /**
   * Purges the storage in {@link SharedPreferences} for the cached time.
   */
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
