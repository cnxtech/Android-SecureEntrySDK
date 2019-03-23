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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.util.AttributeSet;

import java.util.Calendar;

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

  private static final String V3_ROTATING_TOKEN = "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEiLCJ0IjoiVE06OjAzOjo3dXhiOWxhZ3FjenNwc2RicGRqaDEwbjVhY3hzYzJyYnc2ZzB6cTBrbXVtOGRsY3A2IiwiY2siOiJlZTlmOWZjMDA0NjE0MjE5YzY5YmM5ZjA2MzAxOTlkY2I5YjY3N2JmIn0=";
  private static final String V3_QR_CODE_TOKEN = "eyJiIjoiNDg2ODg2OTg3Nzc1MTAwOWEifQ==";
  private static final String V4_STATIC_PDF417_TOKEN = "eyJiIjoiODMwNTM2NjY1MTU4ayIsInJ0Ijoicm90YXRpbmdfc3ltYm9sb2d5In0=";
  private static final String V4_QR_CODE = "eyJiIjoiMDg2NzM0NjQ3NjA0MTYxNmEiLCJydCI6ImJhcmNvZGUifQ==";
  private static final String V4_ROTATING_TOKEN = "eyJiIjoiODUwMDYxNTcwMjU3USIsInQiOiJCQUlBV0xGYml6dU9FUUFBQUFBQUFBQUFBQUNqdXh3dTlEZXpieFRQbktjOFRhVkxabFpPQ3pYYXh4YWtKMWdWIiwiY2siOiJkN2ZhMGEwZTc4NzJhYzVkNDY2MjhlMmY5YWZkMDExMWVjOGU4N2JmIiwiZWsiOiI5YTE2MDUwOTc3OWU2MDhhZGZlZTg0YmQyN2QwODc3YTVjY2U5MTY2IiwicnQiOiJyb3RhdGluZ19zeW1ib2xvZ3kifQ==";

  @Test
  public void init_With_Default_AttributeSet_Should_Apply_DefaultBrandingColor() {

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
  public void init_With_Custom_AttributeSet_Should_Apply_CustomBrandingColor() {

    // given
    Context context = RuntimeEnvironment.application;
    int resourceIdentifier = context.getResources()
        .getIdentifier("branding_color", "attr", "com.ticketmaster.presence");

    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(resourceIdentifier, "@color/testing_color")
        .build();

    // when
    SecureEntryView secureEntryView = new SecureEntryView(context, attributeSet);

    // then
    assertEquals(secureEntryView.getBrandingColor(),
        context.getResources().getColor(R.color.testing_color));

  }

  // not much value here, but for completeness public api should be checked that its working
  @Test
  public void setBrandingColor_Should_Set_BrandingColor() {
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
  public void setErrorMessage_Should_Set_ErrorMessage() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setErrorText("Custom error text");

    // then
    assertEquals("Custom error text", secureEntryView.getStateMessage());
  }

  @Test
  public void setToken_With_V3RotatingData_Should_Create_EntryData() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(V3_ROTATING_TOKEN);

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertTrue(entryData.isRotatingPdf417());
    assertEquals("4868869877751009a", entryData.getBarcode());
    assertEquals("TM::03::7uxb9lagqczspsdbpdjh10n5acxsc2rbw6g0zq0kmum8dlcp6", entryData.getToken());
    assertEquals("ee9f9fc004614219c69bc9f0630199dcb9b677bf", entryData.getCustomerKey());
  }

  @Test
  public void setToken_With_V3QrData_Should_Create_EntryData() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(V3_QR_CODE_TOKEN);

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertEquals("4868869877751009a", entryData.getBarcode());
    assertTrue(entryData.isQRCode());
    assertNull(entryData.getToken());
    assertNull(entryData.getCustomerKey());
  }

  @Test
  public void setToken_With_V4StaticPdfData_Should_Create_EntryData() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(V4_STATIC_PDF417_TOKEN);

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertTrue(entryData.isStaticPdf417());
    assertEquals("830536665158k", entryData.getBarcode());
    assertEquals("rotating_symbology", entryData.getRotatingToken());
    assertNull(entryData.getToken());
    assertNull(entryData.getCustomerKey());
  }

  @Test
  public void setToken_With_V4RotatingData_Should_Create_EntryData() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(V4_ROTATING_TOKEN);

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertTrue(entryData.isRotatingPdf417());
    assertEquals("850061570257Q", entryData.getBarcode());
    assertEquals("rotating_symbology", entryData.getRotatingToken());
    assertEquals("BAIAWLFbizuOEQAAAAAAAAAAAACjuxwu9DezbxTPnKc8TaVLZlZOCzXaxxakJ1gV",
        entryData.getToken());
    assertEquals("d7fa0a0e7872ac5d46628e2f9afd0111ec8e87bf", entryData.getCustomerKey());
    assertEquals("9a160509779e608adfee84bd27d0877a5cce9166", entryData.getEventKey());
  }

  @Test
  public void setToken_With_V4QrCodeData_Should_Create_EntryData() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.setToken(V4_QR_CODE);

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertTrue(entryData.isQRCode());
    assertEquals("0867346476041616a", entryData.getBarcode());
    assertEquals("barcode", entryData.getRotatingToken());
  }

  @Test
  public void generate_PdfBitmap_With_V3RotatingData_Should_Create_Pdf417Bitmap() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.decodeToken(V3_ROTATING_TOKEN);
    secureEntryView.generatePdfBitmap();

    // then
    assertNotNull(secureEntryView.getPdf417Bitmap());
  }

  @Test
  public void generate_PdfBitmap_With_V4RotatingData_Should_Create_Pdf417Bitmap() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.decodeToken(V4_ROTATING_TOKEN);
    secureEntryView.generatePdfBitmap();

    // then
    assertNotNull(secureEntryView.getPdf417Bitmap());
  }

  @Test
  public void generate_QrCodeBitmap_With_V3StaticData_Should_Create_QrCodeBitmap() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.decodeToken(V3_QR_CODE_TOKEN);
    secureEntryView.generateQrCodeBitmap();

    // then
    assertNotNull(secureEntryView.getQrCodeBitmap());
  }

  @Test
  public void getNewOTP_With_V3RotatingData_Should_Create_Expected_MessageToEncode() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.decodeToken(V3_ROTATING_TOKEN);
    String messageToEncode = secureEntryView.getNewOTP(Calendar.getInstance().getTime());

    // then
    assertNotNull(messageToEncode);
    assertEquals(messageToEncode.split("::").length, 4);
  }

  @Test
  public void getNewOTP_With_V4RotatingData_Should_Create_Expected_MessageToEncode() {
    // given
    SecureEntryView secureEntryView = new SecureEntryView(RuntimeEnvironment.application);

    // when
    secureEntryView.decodeToken(V4_ROTATING_TOKEN);
    String messageToEncode = secureEntryView.getNewOTP(Calendar.getInstance().getTime());

    // then
    assertNotNull(messageToEncode);
    assertEquals(messageToEncode.split("::").length, 3);
  }

  // TODO: check this again, is it useful?
  @Test
  public void setToken_With_Null_Should_Have_DefaultErrorMessage() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken(null);

    // then
    assertEquals(context.getResources().getString(R.string.reload_ticket),
        secureEntryView.getStateMessage());
  }

  // TODO: check this again, is it useful?
  @Test
  public void setToken_With_Null_Should_Have_CustomErrorMessage() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setErrorText("Hello, World!");
    secureEntryView.setToken(null);

    // then
    assertEquals("Hello, World!", secureEntryView.getStateMessage());
  }

  // TODO: check this again, is it useful?
  @Test
  public void setToken_With_InvalidJson_Should_Have_DefaultErrorMessage() {
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
  public void setToken_With_12DigitBarcode_Should_Create_EntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("486886987775a");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("486886987775a", entryData.getBarcode());
  }

  @Test
  public void setToken_With_13DigitBarcode_Should_Create_EntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("4868869877751a");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("4868869877751a", entryData.getBarcode());
  }

  @Test
  public void setToken_With_14DigitBarcode_Should_Create_EntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("48688698777510a");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("48688698777510a", entryData.getBarcode());
  }

  @Test
  public void setToken_With_15DigitBarcode_Should_Create_EntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("486886987775100a");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("486886987775100a", entryData.getBarcode());
  }

  @Test
  public void setToken_With_16DigitBarcode_Should_Create_EntryData() {
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
  public void setToken_With_17DigitBarcode_Should_Create_EntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("48688698777510094a");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("48688698777510094a", entryData.getBarcode());
  }

  @Test
  public void setToken_With_18DigitBarcode_Should_Create_EntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("486886987775100944a");

    // then
    EntryData entryData = secureEntryView.getEntryData();
    assertNotNull(entryData);
    assertEquals("486886987775100944a", entryData.getBarcode());
  }

  @Test
  public void setToken_With_InvalidJson_Should_Not_Create_EntryData() {
    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    // when
    secureEntryView.setToken("81948194819481f=");

    // then
    assertNull(secureEntryView.getEntryData());
  }

  @Test
  public void setToken_With_ValidBase64TokenAndInvalidJson_Should_Create_EntryData() {

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
  public void decodedSecret_Should_Match_ExpectedValue() {

    byte[] expected = {69, -76, 103, 60, -29, -36, -96, -91, 15, -69, 41, -50, -90, -82, 62, -4,
        -63, -46, -118, -68};
    String secret = "45b4673ce3dca0a50fbb29cea6ae3efcc1d28abc";

    // given
    Context context = RuntimeEnvironment.application;
    SecureEntryView secureEntryView = new SecureEntryView(context);

    byte[] actual = secureEntryView.hexStringToByteArray(secret);

    assertArrayEquals(expected, actual);
  }
}
