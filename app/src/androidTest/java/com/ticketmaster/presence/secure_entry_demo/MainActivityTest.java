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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This UI test is a bit flaky. It should only be run on a large device (Pixel 2XL).
 * The reason being, if espresso finds a view that is not displayed at least 90% on screen then
 * it will not interact with that view.
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Test
  public void click_TryIt_Should_Display_Error() {
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.errorButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction errorLayout = onView(withId(R.id.errorLinearLayout));
    errorLayout.check(matches(isDisplayed()));

    ViewInteraction errorImage = onView(withId(R.id.errorImageView));
    errorImage.check(matches(isDisplayed()));

    ViewInteraction errorTextView = onView(
        allOf(
            withId(R.id.errorTextView),
            withText("Invalid Barcode")
        ));
    errorTextView.check(matches(isDisplayed()));
  }

  @Test
  public void click_TryIt_Should_Display_QrCode() {
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.qrButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction qrImageView = onView(withId(R.id.qrImageView));
    qrImageView.check(matches(isDisplayed()));
  }

  @Test
  public void click_TryIt_Should_Display_StaticPdf417() {
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.staticPdfButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction backgroundAnimationBar = onView(withId(R.id.thickRectangleView));
    backgroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction foregroundAnimationBar = onView(withId(R.id.thinRectangleView));
    foregroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction pdf417ImageView = onView(withId(R.id.pdf417ImageView));
    pdf417ImageView.check(matches(isDisplayed()));

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.check(matches(isDisplayed()));
  }

  @Test
  public void click_TryIt_Should_Display_RotatingPdf417(){

    ViewInteraction materialButton = onView(
        allOf(withId(R.id.rotatingButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction backgroundAnimationBar = onView(withId(R.id.thickRectangleView));
    backgroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction foregroundAnimationBar = onView(withId(R.id.thinRectangleView));
    foregroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction pdf417ImageView = onView(withId(R.id.pdf417ImageView));
    pdf417ImageView.check(matches(isDisplayed()));

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.check(matches(isDisplayed()));

  }

  @Test
  public void toggle_RotatingPdf417_Should_Display_QRCode(){
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.rotatingButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.perform(click());

    ViewInteraction qrImageView = onView(withId(R.id.qrImageView));
    qrImageView.check(matches(isDisplayed()));
  }

  @Test
  public void toggle_RotatingPdf417_Should_Fallback_To_Pdf417_After_10_Seconds(){
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.rotatingButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.perform(click());

    try {
      Thread.sleep(10_000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction backgroundAnimationBar = onView(withId(R.id.thickRectangleView));
    backgroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction foregroundAnimationBar = onView(withId(R.id.thinRectangleView));
    foregroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction pdf417ImageView = onView(withId(R.id.pdf417ImageView));
    pdf417ImageView.check(matches(isDisplayed()));

  }

  @Test
  public void toggle_StaticPdf417_Should_Display_QRCode(){
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.staticPdfButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.perform(click());

    ViewInteraction qrImageView = onView(withId(R.id.qrImageView));
    qrImageView.check(matches(isDisplayed()));
  }

  @Test
  public void toggle_StaticPdf417_Should_Fallback_To_Pdf417_After_10_Seconds(){
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.staticPdfButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.perform(click());

    try {
      Thread.sleep(10_000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction backgroundAnimationBar = onView(withId(R.id.thickRectangleView));
    backgroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction foregroundAnimationBar = onView(withId(R.id.thinRectangleView));
    foregroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction pdf417ImageView = onView(withId(R.id.pdf417ImageView));
    pdf417ImageView.check(matches(isDisplayed()));
  }

  @Test
  public void scroll_For_QRCode_Should_Display_Another_QRCode(){
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.qrButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction recyclerView = onView(withId(R.id.recyclerViewTickets));
    recyclerView.perform(RecyclerViewActions.scrollToPosition(TokenUtils.QR_TOKENS.length - 1));

    // need to sleep for a short time
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction qrImageView = onView(withId(R.id.qrImageView));
    qrImageView.check(matches(isDisplayed()));
  }

  @Test
  public void scroll_For_StaticPdf417_Should_Display_Another_Pdf417(){
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.staticPdfButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction recyclerView = onView(withId(R.id.recyclerViewTickets));
    recyclerView.perform(RecyclerViewActions.scrollToPosition(TokenUtils.STATIC_TOKENS.length - 1));

    // need to sleep for a short time
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction pdf417ImageView = onView(withId(R.id.pdf417ImageView));
    pdf417ImageView.check(matches(isDisplayed()));

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.check(matches(isDisplayed()));
  }

  @Test
  public void scroll_For_RotatingPdf417_Should_Display_Another_Pdf417(){
    ViewInteraction materialButton = onView(
        allOf(withId(R.id.rotatingButton), withText("Try It!")));
    materialButton.perform(click());

    ViewInteraction recyclerView = onView(withId(R.id.recyclerViewTickets));
    recyclerView.perform(RecyclerViewActions.scrollToPosition(TokenUtils.ROTATING_TOKENS.length - 1));

    // need to sleep for a short time
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    ViewInteraction backgroundAnimationBar = onView(withId(R.id.thickRectangleView));
    backgroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction foregroundAnimationBar = onView(withId(R.id.thinRectangleView));
    foregroundAnimationBar.check(matches(isDisplayed()));

    ViewInteraction pdf417ImageView = onView(withId(R.id.pdf417ImageView));
    pdf417ImageView.check(matches(isDisplayed()));

    ViewInteraction toggleImageButton = onView(withId(R.id.toggleImageButton));
    toggleImageButton.check(matches(isDisplayed()));
  }
}
