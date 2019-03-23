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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains the sample token data.
 */
public final class TokenUtils {

  private static final Random RANDOM = new Random();

  static String[] STATIC_TOKENS = {
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
      "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=",
  };

  static String[] ROTATING_TOKENS = {
      "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrbXVtOGRsY3A2IiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiODY3OTU4MjU2NDk0MTU0NWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrMHhsMm1hdzRuIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiMDQ0MjQyODEwNTA2OTM4NmEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBqYnV3eXJxM2RrIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiMjQxMDI0NTE3NDYyNjg1MGEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBpcThwd3hiNzUyIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNTU1MTU1OTIwNTAzMDkxNWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrMDhqdHpkMmZ1IiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNzA0MTk0ODU3MDY2NDUwMWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBqanUxZWw0M285IiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiMTczMDE3Njg5OTM5MTczOGEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBpb2s3OG1ibzhxIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiMjE5MDIxMzE1NDY0OTMxOGEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBpdXFhdWtndjVhIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0=",
      "eyJiIjoiNjAzNjMyODI0ODUxOTkxNmEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrcWZseTZha2VnIiwiY2siOiIyZTM4MzBiZmExMDNiZmEwNDMyNGJiNmViNDEyNjg3NDM3MTU4YTFiIn0="
  };

  static String[] QR_TOKENS = {
      "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEifQ==",
      "eyJiIjoiODY3OTU4MjU2NDk0MTU0NWEifQ==",
      "eyJiIjoiNTUwODkzNDc1NjYyMzQyOGEifQ==",
      "eyJiIjoiMDQ0MjQyODEwNTA2OTM4NmEifQ==",
      "eyJiIjoiMjQxMDI0NTE3NDYyNjg1MGEifQ==",
      "eyJiIjoiNTU1MTU1OTIwNTAzMDkxNWEifQ==",
      "eyJiIjoiNzA0MTk0ODU3MDY2NDUwMWEifQ==",
      "eyJiIjoiMTczMDE3Njg5OTM5MTczOGEifQ==",
      "eyJiIjoiMjE5MDIxMzE1NDY0OTMxOGEifQ=="
  };


  private static String[] EMPTY_TOKENS = {""};
  private static String[] NULL_TOKENS = {null, null, null, null, null, null};

  public static final int[] COLORS = {
      R.color.red,
      R.color.pink,
      R.color.purple,
      R.color.deep_purple,
      R.color.indigo,
      R.color.blue,
      R.color.light_blue,
      R.color.cyan,
      R.color.teal,
      R.color.green,
      R.color.light_green,
      R.color.lime,
      R.color.yellow,
      R.color.amber,
      R.color.orange,
      R.color.deep_orange,
      R.color.brown,
      R.color.grey,
      R.color.blue_grey
  };

  public static List<TokenData> getRotatingTokens() {
    return buildSampleData(ROTATING_TOKENS, true);
  }

  public static List<TokenData> getQrCodeTokens() {
    return buildSampleData(QR_TOKENS, false);
  }

  public static List<TokenData> getStaticTokens() {
    return buildSampleData(STATIC_TOKENS, false);
  }

  public static List<TokenData> getNullTokens() {
    return buildSampleData(NULL_TOKENS, false);
  }

  public static List<TokenData> getEmptyTokens() {
    return buildSampleData(EMPTY_TOKENS, false);
  }

  private static List<TokenData> buildSampleData(String[] tokens,
      boolean includeCustomColor) {
    List<TokenData> items = new ArrayList<>();
    for (String token : tokens) {
      TokenData tokenData = new TokenData();
      tokenData.token = token;
      if (includeCustomColor) {
        tokenData.colorIndex = RANDOM.nextInt(COLORS.length);
      }
      items.add(tokenData);
    }
    return items;
  }

  /**
   * Represents a token and a color value for setting it in the SecureEntryView.
   */
  public static final class TokenData implements Parcelable {

    public int colorIndex;
    public String token;

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(colorIndex);
      dest.writeString(token);
    }

    public static final Creator<TokenUtils.TokenData> CREATOR = new Creator<TokenUtils.TokenData>() {
      @Override
      public TokenUtils.TokenData createFromParcel(Parcel source) {
        TokenUtils.TokenData tokenData = new TokenUtils.TokenData();
        tokenData.colorIndex = source.readInt();
        tokenData.token = source.readString();
        return tokenData;
      }

      @Override
      public TokenUtils.TokenData[] newArray(int size) {
        return new TokenUtils.TokenData[size];
      }
    };
  }

}
