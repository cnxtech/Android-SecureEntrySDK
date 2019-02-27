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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.util.AttributeSet;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/**
 * Tests the SecureEntryView public api using Robolectric
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
public class SecureEntryViewTest {

  private static final String ROTATING_PAYLOAD = "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrbXVtOGRsY3A2IiwiY2siOiJlZTlmOWZjMDA0NjE0MjE5YzY5YmM5ZjA2MzAxOTlkY2I5YjY3N2JmIn0=";
  private static final String STATIC_PAYLOAD = "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEifQ==";

  @Test
  public void initWithDefaultAttributeSet_ShouldSet_DefaultBrandingColor() {

    // given
    Context context = RuntimeEnvironment.application;
    AttributeSet attrs = Robolectric.buildAttributeSet().build();

    // when
    SecureEntryView secureEntryView = new SecureEntryView(context, attrs);

    // then
    assertEquals(secureEntryView.getBrandingColor(),
        context.getResources().getColor(R.color.default_animation_color));

  }

  /*
      This test needs to be revisited later, it is using old application context.
      NOTE: its passing but I haven't moved to new Context because Robolectric docs
      aren't comprehensive when it comes to resources.
   */
  @Test
  @Ignore
  public void initWithCustomAttributeSet_ShouldSet_CustomBrandingColor() {

    // given
    Context context = RuntimeEnvironment.application;
    int resourceIdentifider = context.getResources()
        .getIdentifier("branding_color", "attr", "com.ticketmaster.presence");

    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(resourceIdentifider, "@color/testing_color")
        .build();

    // when
    SecureEntryView secureEntryView = new SecureEntryView(context, attributeSet);

    // then
    assertEquals(secureEntryView.getBrandingColor(),
        context.getResources().getColor(R.color.testing_color));

  }

  @Test
  public void setToken_WithGoodRotatingData_ShouldDecodeToken() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(ROTATING_PAYLOAD);

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertEquals("4868869877751009a", entryData.getBarcode());
    assertEquals("TM::03::7uxb9lagqczspsdbpdjh10n5acxsc2rbw6g0zq0kmum8dlcp6", entryData.getToken());
    assertEquals("ee9f9fc004614219c69bc9f0630199dcb9b677bf", entryData.getCustomerKey());
  }

  @Test
  public void setToken_WithGoodStaticData_ShouldDecodeToken() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(STATIC_PAYLOAD);

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertEquals("4868869877751009a", entryData.getBarcode());
    assertNull(entryData.getToken());
    assertNull(entryData.getCustomerKey());
  }

  @Test
  @Ignore
  public void setToken_WithRotatingData_ShouldUsePDF417Writer() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(ROTATING_PAYLOAD);

    // then
//        assertTrue(secureEntryView.getWriter() instanceof PDF417Writer);

  }

  @Test
  @Ignore
  public void setToken_WithRotatingData_ShouldUseQRCodeWriter() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(STATIC_PAYLOAD);

    // then
//        assertTrue(secureEntryView.getWriter() instanceof QRCodeWriter);

  }

  @Test
  public void setToken_WithNullToken_ShouldSet_ErrorStateMessageNoToken() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken(null);

    // then
    assertEquals(context.getResources().getString(R.string.reload_ticket),
        secureEntryView.getStateMessage());
  }

  @Test
  public void setToken_WithBadToken_ShouldSet_ErrorStateMessageInvalidToken() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("Invalid JSON payload");

    // then
    assertEquals(context.getResources().getString(R.string.reload_ticket),
        secureEntryView.getStateMessage());
  }

  @Test
  public void setToken_With16DigitBarcodeContainingZeros_ShouldSetEntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("4868869877751009a");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("4868869877751009a", entryData.getBarcode());
  }

  @Test
  @Ignore
  public void setToken_WithValidBase64_ErrorStateMessageInvalidToken() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("81948194819481f=");

    // then
    assertEquals(context.getResources().getString(R.string.reload_ticket),
        secureEntryView.getStateMessage());
  }

  @Test
  public void setToken_WithValidBase64TokenAndInvalidJson_ShouldSetEntryData() {

    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("81948194819481f");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("81948194819481f", entryData.getBarcode());
  }

  @Test
  @Ignore
  public void initWithBadBase64EncodedToken_ShouldSet_ErrorStateMessageInvalidToken() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("0");

    // then
    assertEquals(context.getResources().getString(R.string.reload_ticket),
        secureEntryView.getStateMessage());
  }

  // not much value here, but for completeness public api should be checked that its working
  @Test
  public void setBrandingColor_ShouldSet_BrandingColorCorrectly() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setBrandingColor(context.getResources().getColor(R.color.testing_color));

    // then
    assertEquals(context.getResources().getColor(R.color.testing_color),
        secureEntryView.getBrandingColor());
  }

  @Test
  @Ignore
  public void secretDecoded_ShouldMatch_ExpectedValue() {

    byte [] expected = {69, -76, 103, 60, -29, -36, -96, -91, 15, -69, 41, -50, -90, -82, 62, -4, -63, -46, -118, -68};
    String secret = "45b4673ce3dca0a50fbb29cea6ae3efcc1d28abc";

    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    byte [] actual = secureEntryView.hexStringToByteArray(secret);

    assertArrayEquals(expected, actual);
  }
}
