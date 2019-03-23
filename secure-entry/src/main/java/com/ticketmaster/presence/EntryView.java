package com.ticketmaster.presence;
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


import android.graphics.Bitmap;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Contract defining the public contract for the {@link SecureEntryView}.
 */
interface EntryView {

  /**
   * Call to change the underlying data in the PDF417 ticket.
   * Note: this will cause the view to be redrawn.
   *
   * @param token Base64 encoded data mapping to EntryData (below)
   * @see EntryData
   */
  void setToken(String token);

  /**
   * Call to change the underlying data in the PDF417 ticket.
   *
   * @param token Base64 encoded data mapping to EntryData (below)
   * @param errorText optional error message for token parsing
   */
  void setToken(String token, @Nullable String errorText);

  /**
   * Call to change the color of the animation displaying over the ticket.
   * Note: this will cause the view to be redrawn.
   *
   * @param brandingColor color for animation over PDF417
   */
  void setBrandingColor(@ColorRes int brandingColor);


  /**
   * Call to change the error state of a bad token.
   * Note: a bad token is either bad json, an invalid Base64 String or an unrecognized barcode.
   *
   * @param errorText text to display below the error icon
   */
  void setErrorText(@Nullable String errorText);


  /**
   * Call to change the error state when an error should be explicitly set.
   * Note: calling this hides all other views except the error views
   * @param errorText text to display below the error icon
   * @param errorIcon icon to show as the error icon
   */
  void showError(@Nullable String errorText, @NonNull Bitmap errorIcon);
}
