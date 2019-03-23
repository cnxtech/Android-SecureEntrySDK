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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

final class EntryData implements Parcelable {

  private final String barcode;
  private final String token;
  private final String customerKey;
  private final String eventKey;
  private final String rotatingToken;

  EntryData(String barcode) {
    this(barcode, null, null, null);
  }

  EntryData(String barcode, String token, String customerKey, String eventKey) {
    this(barcode, token, customerKey, eventKey, null);
  }

  EntryData(String barcode, String token, String customerKey, String eventKey,
      String rotatingToken) {
    this.barcode = barcode;
    this.token = token;
    this.customerKey = customerKey;
    this.eventKey = eventKey;
    this.rotatingToken = rotatingToken;
  }

  String getBarcode() {
    return barcode;
  }

  String getToken() {
    return token;
  }

  String getCustomerKey() {
    return customerKey;
  }

  String getEventKey() {
    return eventKey;
  }

  String getRotatingToken() {
    return rotatingToken;
  }

  boolean isRotatingPdf417() {
    return getCustomerKey() != null;
  }

  boolean isQRCode() {
    // v3 format doesn't include anything but barcode
    if (getToken() == null && !TextUtils.isEmpty(getBarcode())) {
      return getRotatingToken() == null || getRotatingToken().equals("barcode");
    } else {
      return false;
    }
  }

  boolean isStaticPdf417() {
    return getToken() == null && !TextUtils.isEmpty(getBarcode()) &&
        getRotatingToken() != null && getRotatingToken().equals("rotating_symbology");
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(barcode);
    dest.writeString(token);
    dest.writeString(customerKey);
    dest.writeString(eventKey);
    dest.writeString(rotatingToken);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    EntryData entryData = (EntryData) o;

    if (barcode != null ? !barcode.equals(entryData.barcode) : entryData.barcode != null) {
      return false;
    }
    if (token != null ? !token.equals(entryData.token) : entryData.token != null) {
      return false;
    }
    if (customerKey != null ? !customerKey.equals(entryData.customerKey)
        : entryData.customerKey != null) {
      return false;
    }
    if (eventKey != null ? !eventKey.equals(entryData.eventKey) : entryData.eventKey != null) {
      return false;
    }
    return rotatingToken != null ? rotatingToken.equals(entryData.rotatingToken)
        : entryData.rotatingToken == null;
  }

  @Override
  public int hashCode() {
    int result = barcode != null ? barcode.hashCode() : 0;
    result = 31 * result + (token != null ? token.hashCode() : 0);
    result = 31 * result + (customerKey != null ? customerKey.hashCode() : 0);
    result = 31 * result + (eventKey != null ? eventKey.hashCode() : 0);
    result = 31 * result + (rotatingToken != null ? rotatingToken.hashCode() : 0);
    return result;
  }

  public static final Parcelable.Creator<EntryData> CREATOR = new Parcelable.Creator<EntryData>() {
    @Override
    public EntryData createFromParcel(Parcel source) {
      String barcode = source.readString();
      String token = source.readString();
      String customerKey = source.readString();
      String eventKey = source.readString();
      String rotatingToken = source.readString();
      EntryData entryData;
      if (!TextUtils.isEmpty(rotatingToken)) {
        entryData = new EntryData(barcode, token, customerKey, eventKey, rotatingToken);
      } else if (!TextUtils.isEmpty(token)) {
        entryData = new EntryData(barcode, token, customerKey, eventKey);
      } else {
        entryData = new EntryData(barcode);
      }
      return entryData;
    }

    @Override
    public EntryData[] newArray(int size) {
      return new EntryData[size];
    }
  };
}
