package com.ticketmaster.presence.secure_entry_demo;
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


import android.app.Application;
import android.util.Log;
import com.squareup.leakcanary.LeakCanary;
import com.ticketmaster.presence.time.SecureEntryClock;

import java.util.Date;

public class SecureEntryApp extends Application {

  private static final String TAG = SecureEntryApp.class.getSimpleName();

  @Override
  public void onCreate() {
    super.onCreate();

    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);


     /*
        You can call this method to syncTime with the NTP server early or let SecureEntryView,
        handle it for you. Note: INTERNET permission is required for this call.
     */
    SecureEntryClock.getInstance(this).syncTime(new SecureEntryClock.Callback() {
      @Override
      public void onComplete(long offset, Date now) {
        Log.d(TAG, "onComplete() called with: offset = [" + offset + "], now = [" + now + "]");
      }

      @Override
      public void onError() {
        Log.d(TAG, "onError() called");
      }
    });
  }
}
