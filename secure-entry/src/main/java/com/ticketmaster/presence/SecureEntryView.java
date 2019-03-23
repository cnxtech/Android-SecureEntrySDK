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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ticketmaster.presence.time.SecureEntryClock;
import com.ticketmaster.presence.totp.OTPAlgorithm;
import com.ticketmaster.presence.totp.TOTP;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * View displaying a rotating PDF417 or static QRCode ticket.
 */

public final class SecureEntryView extends FrameLayout implements EntryView, View.OnClickListener {

  private static final String TAG = SecureEntryView.class.getSimpleName();

  private static final AccelerateDecelerateInterpolator ANIMATION_BAR_INTERPOLATOR
      = new AccelerateDecelerateInterpolator();

  private static final int BAR_ANIMATION_DURATION = 1500;
  private static final int BACKGROUND_ANIMATION_DELAY = 100;
  private static final int FOREGROUND_ANIMATION_DELAY = 300;

  private static final long ROTATION_INTERVAL = 15_000L;
  private static final long FALLBACK_DELAY = 10_000L;
  private static final double TIME_INTERVAL = 15;

  private static Pattern regex = Pattern.compile("^[0-9]{12,18}(?:[A-Za-z])?$");

  private HandlerThread handlerThread = new HandlerThread("BackgroundWorker",
      Process.THREAD_PRIORITY_BACKGROUND);
  private Handler workerHandler;
  private Handler uiHandler = new Handler(Looper.getMainLooper());

  // PDF 417
  private Map<EncodeHintType, Object> pdfHints = new EnumMap<>(EncodeHintType.class);
  private Writer pdf417Writer;
  private Bitmap pdf417Bitmap;
  private int pdf417BitmapHeight;
  private int pdf417BitmapWidth;
  private ImageView pdfImageView;
  private View backgroundBarView;
  private View foregroundBarView;
  private ImageButton toggleImageButton;
  private ObjectAnimator backgroundAnimator;
  private ObjectAnimator foregroundAnimator;
  private Animator qrCodeEnterAnimator;
  private Animator qrCodeExitAnimator;
  private Animator pdf417EnterAnimator;
  private Animator pdf417ExitAnimator;

  // QR Code
  private Map<EncodeHintType, Object> qrHints = new EnumMap<>(EncodeHintType.class);
  private Writer qrCodeWriter;
  private Bitmap qrCodeBitmap;
  private int qrCodeBitmapWidth;
  private int qrCodeBitmapHeight;
  private ImageView qrCodeImageView;
  private ImageView loadingView;

  // Errors
  private LinearLayout errorLinearLayout;
  private FrameLayout errorImageFrameLayout;
  private ImageView errorImageView;
  private TextView errorTextView;

  private EntryData entryData;
  private String errorText;
  private String token;
  private int brandingColor;
  private boolean imageFlipped;
  private boolean toggled;
  private boolean viewLoaded;
  private long toggledAtTime;
  private boolean animating;

  private int translationXLimit;
  private boolean attached;

  public SecureEntryView(Context context) {
    this(context, null);
  }

  public SecureEntryView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SecureEntryView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    setClipToPadding(false);

    // view inflation
    inflate(getContext(), R.layout.secure_entry_view, this);
    loadingView = findViewById(R.id.loadingView);
    pdfImageView = findViewById(R.id.pdf417ImageView);
    backgroundBarView = findViewById(R.id.thickRectangleView);
    foregroundBarView = findViewById(R.id.thinRectangleView);
    toggleImageButton = findViewById(R.id.toggleImageButton);
    toggleImageButton.setOnClickListener(this);
    qrCodeImageView = findViewById(R.id.qrImageView);
    errorLinearLayout = findViewById(R.id.errorLinearLayout);
    errorImageFrameLayout = findViewById(R.id.errorImageFrameLayout);
    errorImageView = findViewById(R.id.errorImageView);
    errorTextView = findViewById(R.id.errorTextView);
    Typeface typeface = Typeface
        .createFromAsset(getContext().getAssets(), "fonts/Averta-Regular.otf");
    errorTextView.setTypeface(typeface);

    // hide all views except loading view initially
    resetUI();

    // writers
    pdf417Writer = new PDF417Writer();
    qrCodeWriter = new QRCodeWriter();

    // hints
    qrHints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
    qrHints.put(EncodeHintType.MARGIN, 0);
    pdfHints.put(EncodeHintType.MARGIN, 0);

    // default PDF417 size
    pdf417BitmapWidth = getResources().getDimensionPixelSize(R.dimen.sesdk_pdf_417_min_width);
    pdf417BitmapHeight = getResources().getDimensionPixelSize(R.dimen.sesdk_pdf_417_min_height);

    // default QR Code size
    qrCodeBitmapWidth = getResources().getDimensionPixelSize(R.dimen.sesdk_qr_code_min_width);

    //noinspection SuspiciousNameCombination
    qrCodeBitmapHeight = qrCodeBitmapWidth;

    if (attrs != null) {
      TypedArray typedArray = context
          .obtainStyledAttributes(attrs, R.styleable.SecureEntryView, 0, 0);
      try {
        int brandingColor =
            typedArray.getColor(R.styleable.SecureEntryView_branding_color,
                getResources().getColor(R.color.default_animation_color));
        errorText = typedArray.getString(R.styleable.SecureEntryView_error_text);
        setBrandingColor(brandingColor);
      } finally {
        typedArray.recycle();
      }
    }

    if (errorText == null) {
      errorText = getContext().getString(R.string.reload_ticket);
    }
    errorTextView.setText(errorText);

    // setup the threading mechanism
    handlerThread.start();
    workerHandler = new Handler(handlerThread.getLooper());
  }

  /**
   * Applies a new token to this view, decoding it and displaying either a PDF417 or QR code in an
   * {@link ImageView} or an error if an invalid token is detected.
   *
   * @param token Base64 encoded data mapping to EntryData (below)
   * @see SecureEntryView#setToken(String, String)
   */
  @Override
  public void setToken(String token) {
    setToken(token, null);
  }

  @Override
  public void setToken(String token, @Nullable String errorText) {
    removeCallbacksAndAnimations();
    resetUI();
    this.token = token;
    applyErrorText(errorText);
    this.toggled = false;

    decodeToken(token);
    if (entryData != null) {
      // handle showing regular view state
      if (entryData.isQRCode()) {
        // QR code
        workerHandler.post(generateAndDisplayQRCodeBitmap);
      } else if (entryData.isStaticPdf417()) {
        // static PDF 417
        workerHandler.post(generateAndDisplayStaticPdf417);
        workerHandler.post(generateQRCodeRunnable);
      } else {
        // rotating PDF417
        generateAndDisplayInitialPdf();
        workerHandler.post(generateQRCodeRunnable);
      }
    } else {
      // error
      showError(errorText, BitmapFactory.decodeResource(getResources(), R.drawable.ic_alert));
    }
  }

  private void resetUI() {
    loadingView.setVisibility(View.VISIBLE);
    if (!((AnimationDrawable) loadingView.getDrawable()).isRunning()) {
      ((AnimationDrawable) loadingView.getDrawable()).start();
    }
    qrCodeImageView.setVisibility(View.GONE);
    pdfImageView.setVisibility(View.GONE);
    foregroundBarView.setVisibility(View.GONE);
    backgroundBarView.setVisibility(View.GONE);
    toggleImageButton.setVisibility(View.GONE);
    toggleImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_overflow));
    errorLinearLayout.setVisibility(View.GONE);
  }

  @VisibleForTesting
  void decodeToken(String token) {
    if (TextUtils.isEmpty(token)) {
      return;
    }
    try {
      byte[] bytes = Base64.decode(token, Base64.DEFAULT);
      String decoded = new String(bytes);
      JSONObject jsonObject = new JSONObject(decoded);
      String barcode = jsonObject.optString("b", null);
      String entryToken = jsonObject.optString("t", null);
      String customerKey = jsonObject.optString("ck", null);
      String eventKey = jsonObject.optString("ek", null);
      String rotatingToken = jsonObject.optString("rt", null);
      if (!TextUtils.isEmpty(rotatingToken)) {
        entryData = new EntryData(barcode, entryToken, customerKey, eventKey, rotatingToken);
      } else if (!TextUtils.isEmpty(entryToken)) {
        entryData = new EntryData(barcode, entryToken, customerKey, eventKey);
      } else {
        entryData = new EntryData(barcode);
      }
    } catch (IllegalArgumentException | JSONException ex) {
      Log.e("SecureEntryView", "Error: " + ex.getMessage(), ex);
      entryData = fallbackDecodeToken(token);
    }
  }

  @VisibleForTesting
  EntryData fallbackDecodeToken(String token) {
    Matcher matcher = regex.matcher(token);
    if (matcher.matches()) {
      return new EntryData(token);
    }
    return null;
  }

  /**
   * Applies a new branding color to the animation.
   * Note: If no branding color is supplied a default will be applied to the animation.
   *
   * @param brandingColor color for animation over PDF417
   */
  @Override
  public void setBrandingColor(@ColorInt int brandingColor) {
    final int color = Color
        .rgb(Color.red(brandingColor), Color.green(brandingColor), Color.blue(brandingColor));
    this.brandingColor = color;
    backgroundBarView.setBackgroundColor(color);
    backgroundBarView.setAlpha(0.3f);
    foregroundBarView.setBackgroundColor(color);
    invalidate();
  }

  /**
   * Applies a new error message to display in the event of an invalid token.
   * Note: If no error text is supplied a default will be applied to the error state.
   *
   * @param errorText text to display below the error icon
   */
  @Override
  @Deprecated
  public void setErrorText(@Nullable String errorText) {
    applyErrorText(errorText);
  }

  @Override
  public void showError(@Nullable String errorText, @NonNull Bitmap errorIcon) {
    removeCallbacksAndAnimations();
    applyErrorText(errorText);
    pdfImageView.setAlpha(1f);
    pdfImageView.setTranslationY(0f);
    pdfImageView.setVisibility(View.GONE);
    foregroundBarView.setVisibility(View.GONE);
    backgroundBarView.setVisibility(View.GONE);
    qrCodeImageView.setAlpha(1f);
    qrCodeImageView.setTranslationY(0f);
    qrCodeImageView.setVisibility(View.GONE);
    loadingView.setVisibility(View.GONE);
    ((AnimationDrawable) loadingView.getDrawable()).stop();
    toggleImageButton.setVisibility(View.GONE);
    errorImageView.setImageBitmap(errorIcon);
    errorLinearLayout.setVisibility(View.VISIBLE);
  }

  @VisibleForTesting
  EntryData getEntryData() {
    return entryData;
  }

  @VisibleForTesting
  String getStateMessage() {
    return errorText;
  }

  @VisibleForTesting
  int getBrandingColor() {
    return brandingColor;
  }

  @VisibleForTesting
  Bitmap getPdf417Bitmap() {
    return pdf417Bitmap;
  }

  @VisibleForTesting
  Bitmap getQrCodeBitmap() {
    return qrCodeBitmap;
  }

  @Override
  protected void onDetachedFromWindow() {
    removeCallbacksAndAnimations();
    super.onDetachedFromWindow();
    attached = false;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (toggled) {
      postGenerateFallbackPdf417();
    } else if (pdfImageView.getVisibility() == View.VISIBLE &&
        loadingView.getVisibility() != View.VISIBLE && entryData != null &&
        (entryData.isRotatingPdf417() || entryData.isStaticPdf417())) {
      startAnimation();
      if (viewLoaded) {
        setToken(token);
      }
    }
    attached = true;
  }

  @Override
  protected void onWindowVisibilityChanged(int visibility) {
    super.onWindowVisibilityChanged(visibility);
    boolean viewVisible = visibility == View.VISIBLE;
    boolean loadingHidden = loadingView.getVisibility() != View.VISIBLE;
    boolean pdfVisible = pdfImageView.getVisibility() == View.VISIBLE;

    if (viewVisible && attached && !animating && loadingHidden
        && pdfVisible && entryData != null && !entryData.isQRCode() && !toggled) {
      startAnimation();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    handlerThread.quit();
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    // Force our ancestor class to save its state
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.errorMessage = errorText;
    ss.loaded = viewLoaded;
    ss.toggled = toggled;
    ss.token = token;
    ss.toggledAtTime = toggledAtTime;
    ss.brandingColor = brandingColor;
    ss.entryData = entryData;
    ss.flipped = imageFlipped;
    return ss;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    viewLoaded = ss.loaded;
    toggled = ss.toggled;
    toggledAtTime = ss.toggledAtTime;
    setBrandingColor(ss.brandingColor);
    applyErrorText(ss.errorMessage);
    token = ss.token;
    entryData = ss.entryData;
    imageFlipped = ss.flipped;
  }

  @Override
  protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    /* As we save our own instance state, ensure our children don't save and restore their state as well. */
    dispatchFreezeSelfOnly(container);
  }

  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    /* See comment in {@link #dispatchSaveInstanceState(SparseArray)}  */
    dispatchThawSelfOnly(container);
  }

  @Override
  protected int getSuggestedMinimumHeight() {
    return getResources().getDimensionPixelSize(R.dimen.sesdk_view_min_height);
  }

  @Override
  protected int getSuggestedMinimumWidth() {
    return getResources().getDimensionPixelSize(R.dimen.sesdk_view_min_width);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    measureChildWithMargins(toggleImageButton, widthMeasureSpec, 0, heightMeasureSpec, 0);
    measureChildWithMargins(errorImageFrameLayout, widthMeasureSpec, 0, heightMeasureSpec, 0);
    measureChildWithMargins(errorImageView, widthMeasureSpec, 0, heightMeasureSpec, 0);
    measureChildWithMargins(errorTextView, widthMeasureSpec, 0, heightMeasureSpec, 0);

    // contain the view to the fixed aspect ratio
    final int targetWidth = View.resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
    final int maxWidth = Math.max(getSuggestedMinimumWidth(), targetWidth);
    final float viewAspectRatio = getSuggestedMinimumWidth() / (getSuggestedMinimumHeight() * 1f);
    final int maxHeight = (int) (maxWidth / viewAspectRatio);
    setMeasuredDimension(maxWidth, maxHeight);

    translationXLimit = getMeasuredWidth() - getPaddingLeft() - getPaddingRight()
        - getResources().getDimensionPixelSize(R.dimen.sesdk_background_animation_bar_width);

    // measure children dependent on view dimensions
    measureErrorLinearLayout();
    measureQRCodeImageView(heightMeasureSpec);
    measurePdf417ImageView(widthMeasureSpec);
  }

  private void measurePdf417ImageView(int widthMeasureSpec) {

    final int width = Math.max(0, getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
    final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
    final int pdfMeasuredWidth = View.getDefaultSize(pdf417BitmapWidth, childWidthMeasureSpec);
    final float pdfAspectRatio = pdf417BitmapWidth / (float) pdf417BitmapHeight;
    final int pdfMeasuredHeight = (int) (pdfMeasuredWidth / pdfAspectRatio);
    final int pdfHeightMeasureSpec = MeasureSpec
        .makeMeasureSpec(pdfMeasuredHeight, MeasureSpec.EXACTLY);
    pdfImageView.measure(childWidthMeasureSpec, pdfHeightMeasureSpec);
    loadingView.measure(childWidthMeasureSpec, pdfHeightMeasureSpec);

    measureThickBarView(widthMeasureSpec, pdfHeightMeasureSpec);
    measureThinBarView(widthMeasureSpec, pdfHeightMeasureSpec);
  }

  private void measureThickBarView(int widthMeasureSpec, int pdfHeightMeasureSpec) {
    final MarginLayoutParams lp = (MarginLayoutParams) backgroundBarView.getLayoutParams();
    final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
        getPaddingLeft() + getPaddingRight() +
            lp.leftMargin + lp.rightMargin, lp.width);
    backgroundBarView.measure(childWidthMeasureSpec, pdfHeightMeasureSpec);
  }

  private void measureThinBarView(int widthMeasureSpec, int heightMeasureSpec) {
    final MarginLayoutParams lp = (MarginLayoutParams) foregroundBarView.getLayoutParams();
    final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
        lp.leftMargin + lp.rightMargin, lp.width);
    final int parentHeightMeasureSpec = resolveSize(pdfImageView.getHeight(), heightMeasureSpec);
    final int height = getResources().getDimensionPixelSize(R.dimen.sesdk_extra_rectangle_height)
        + parentHeightMeasureSpec;
    final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
    foregroundBarView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }

  private void measureQRCodeImageView(int heightMeasureSpec) {
    final int height = Math.max(0, getMeasuredHeight()
        - getPaddingTop() - getPaddingBottom());
    final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
        getPaddingTop() + getPaddingBottom(), height);
    qrCodeImageView.measure(childHeightMeasureSpec, childHeightMeasureSpec);
  }

  private void measureErrorLinearLayout() {
    final int parentWidth = Math.max(0, getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
    final int childWidth =
        errorLinearLayout.getMinimumWidth() + errorLinearLayout.getPaddingLeft() + errorLinearLayout
            .getPaddingRight();
    final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
        Math.min(parentWidth, childWidth), MeasureSpec.EXACTLY);

    final int parentHeight = Math
        .max(0, getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
    final int childHeight =
        errorLinearLayout.getMinimumHeight() + errorLinearLayout.getPaddingTop() + errorLinearLayout
            .getPaddingBottom();
    final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
        Math.min(parentHeight, childHeight), MeasureSpec.EXACTLY);
    errorLinearLayout.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }

  @Override
  public void onClick(View v) {
    if (toggled) {
      toggled = false;
      toggleImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_overflow));
      uiHandler.removeCallbacksAndMessages(null);
      workerHandler.removeCallbacksAndMessages(null);
      cancelPdfExitAnimator();
      cancelQrEnterAnimator();
      hideQrCodeViewsWithAnimation();
      showPdf417ViewsWithAnimation();
      if (entryData.isRotatingPdf417()) {
        workerHandler.postDelayed(generateAndDisplayPdf417, ROTATION_INTERVAL);
      }

    } else {
      toggled = true;
      toggledAtTime = System.currentTimeMillis();
      toggleImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_swap));
      uiHandler.removeCallbacksAndMessages(null);
      workerHandler.removeCallbacksAndMessages(null);
      qrCodeImageView.setImageBitmap(qrCodeBitmap);

      cancelPdfEnterAnimator();
      cancelQrExitAnimator();
      showQrCodeViewsWithAnimation();
      hidePdf417ViewsWithAnimation();
      cancelBarAnimators();
      if (entryData.isRotatingPdf417()) {
        workerHandler.postDelayed(fallbackToRotatingPdf417, FALLBACK_DELAY);
      } else {
        uiHandler.postDelayed(fallbackToStaticPdf417, FALLBACK_DELAY);
      }
    }
  }

  private void applyErrorText(String errorText){
    if (!TextUtils.isEmpty(errorText)) {
      this.errorText = errorText;
    }
    if (errorTextView != null) {
      errorTextView.setText(this.errorText);
    }
    requestLayout();
    invalidate();
  }

  private void removeCallbacksAndAnimations() {
    uiHandler.removeCallbacksAndMessages(null);
    workerHandler.removeCallbacksAndMessages(null);

    cancelBarAnimators();
    cancelQrEnterAnimator();
    cancelQrExitAnimator();
    cancelPdfEnterAnimator();
    cancelPdfEnterAnimator();
  }

  private void cancelBarAnimators() {
    animating = false;

    if (backgroundAnimator != null) {
      backgroundAnimator.cancel();
      backgroundAnimator.removeAllListeners();
      backgroundAnimator = null;
    }

    if (foregroundAnimator != null) {
      foregroundAnimator.cancel();
      foregroundAnimator.removeAllListeners();
      foregroundAnimator = null;
    }
  }

  private void startAnimation() {
    if (animating || translationXLimit == 0 || !attached ||
        getVisibility() != VISIBLE || getWindowVisibility() != VISIBLE) {
      return;
    }
    animating = true;
    runBackgroundBarAnimation();
    runForegroundBarAnimation();
  }

  private void runBackgroundBarAnimation() {

    final ObjectAnimator translateXRight =
        ObjectAnimator.ofFloat(backgroundBarView, "translationX", 0, translationXLimit);
    translateXRight.setDuration(BAR_ANIMATION_DURATION);
    translateXRight.setRepeatCount(ValueAnimator.INFINITE);
    translateXRight.setRepeatMode(ValueAnimator.REVERSE);
    translateXRight.setStartDelay(FOREGROUND_ANIMATION_DELAY + BACKGROUND_ANIMATION_DELAY);
    translateXRight.setInterpolator(ANIMATION_BAR_INTERPOLATOR);

    if (backgroundAnimator == null) {
      backgroundAnimator = translateXRight;
    }

    backgroundAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationCancel(Animator animation) {
        backgroundAnimator.removeAllListeners();
      }
    });
    backgroundAnimator.start();
  }

  private void runForegroundBarAnimation() {

    final ObjectAnimator translateXRight =
        ObjectAnimator.ofFloat(foregroundBarView, "translationX", 0, translationXLimit);
    translateXRight.setDuration(BAR_ANIMATION_DURATION);
    translateXRight.setStartDelay(FOREGROUND_ANIMATION_DELAY);
    translateXRight.setRepeatCount(ValueAnimator.INFINITE);
    translateXRight.setRepeatMode(ValueAnimator.REVERSE);
    translateXRight.setInterpolator(ANIMATION_BAR_INTERPOLATOR);

    if (foregroundAnimator == null) {
      foregroundAnimator = translateXRight;
    }

    foregroundAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationCancel(Animator animation) {
        foregroundAnimator.removeAllListeners();
      }
    });
    foregroundAnimator.start();
  }

  private void showPdf417Views() {
    pdfImageView.setAlpha(1f);
    pdfImageView.setTranslationY(0f);
    pdfImageView.setVisibility(View.VISIBLE);
    foregroundBarView.setVisibility(View.VISIBLE);
    backgroundBarView.setVisibility(View.VISIBLE);
    startAnimation();
  }

  private void cancelPdfEnterAnimator() {
    if (pdf417EnterAnimator != null) {
      pdf417EnterAnimator.cancel();
      pdf417EnterAnimator.removeAllListeners();
      pdf417EnterAnimator = null;
    }
  }

  private void cancelPdfExitAnimator() {
    if (pdf417ExitAnimator != null) {
      pdf417ExitAnimator.cancel();
      pdf417ExitAnimator.removeAllListeners();
      pdf417ExitAnimator = null;
    }
  }

  private void showPdf417ViewsWithAnimation() {
    pdfImageView.setAlpha(0f);
    pdfImageView.setVisibility(View.VISIBLE);
    pdf417EnterAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.animate_in);
    pdf417EnterAnimator.setTarget(pdfImageView);
    pdf417EnterAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        pdfImageView.setAlpha(1f);
        foregroundBarView.setTranslationX(0);
        backgroundBarView.setTranslationX(0);
        foregroundBarView.setVisibility(View.VISIBLE);
        backgroundBarView.setVisibility(View.VISIBLE);

        startAnimation();
      }

      @Override
      public void onAnimationCancel(Animator animation) {
        pdfImageView.setAlpha(1f);
        pdfImageView.setTranslationY(0f);
        foregroundBarView.setVisibility(View.VISIBLE);
        backgroundBarView.setVisibility(View.VISIBLE);
        pdf417EnterAnimator.removeAllListeners();
        cancelBarAnimators();
      }
    });
    pdf417EnterAnimator.start();
  }

  private void hidePdf417ViewsWithAnimation() {
    pdfImageView.setAlpha(1f);
    pdfImageView.setVisibility(View.VISIBLE);
    pdf417ExitAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.animate_out);
    pdf417ExitAnimator.setTarget(pdfImageView);
    pdf417ExitAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationStart(Animator animation) {
        foregroundBarView.setVisibility(View.GONE);
        backgroundBarView.setVisibility(View.GONE);
      }

      @Override
      public void onAnimationEnd(Animator animation) {
        pdfImageView.setAlpha(0f);
        pdfImageView.setVisibility(View.GONE);
      }

      @Override
      public void onAnimationCancel(Animator animation) {
        pdfImageView.setAlpha(1f);
        pdfImageView.setVisibility(View.VISIBLE);
        pdf417ExitAnimator.removeAllListeners();
      }
    });
    pdf417ExitAnimator.start();
  }

  private void cancelQrEnterAnimator() {
    if (qrCodeEnterAnimator != null) {
      qrCodeEnterAnimator.cancel();
      qrCodeEnterAnimator.removeAllListeners();
      qrCodeEnterAnimator = null;
    }
  }

  private void cancelQrExitAnimator() {
    if (qrCodeExitAnimator != null) {
      qrCodeExitAnimator.cancel();
      qrCodeExitAnimator.removeAllListeners();
      qrCodeExitAnimator = null;
    }
  }

  private void showQrCodeViewsWithAnimation() {
    qrCodeImageView.setAlpha(0f);
    qrCodeImageView.setVisibility(View.VISIBLE);
    qrCodeEnterAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.animate_in);
    qrCodeEnterAnimator.setTarget(qrCodeImageView);
    qrCodeEnterAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        qrCodeImageView.setAlpha(1f);
      }

      @Override
      public void onAnimationCancel(Animator animation) {
        qrCodeImageView.setAlpha(1f);
        qrCodeImageView.setTranslationY(0f);
        qrCodeImageView.setVisibility(View.VISIBLE);
        cancelBarAnimators();
      }
    });
    qrCodeEnterAnimator.start();
  }

  private void hideQrCodeViewsWithAnimation() {
    qrCodeImageView.setAlpha(1f);
    qrCodeImageView.setVisibility(View.VISIBLE);
    qrCodeExitAnimator = AnimatorInflater.loadAnimator(getContext(), R.animator.animate_out);
    qrCodeExitAnimator.setTarget(qrCodeImageView);
    qrCodeExitAnimator.addListener(new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        qrCodeImageView.setAlpha(0f);
        qrCodeImageView.setVisibility(View.GONE);
      }

      @Override
      public void onAnimationCancel(Animator animation) {
        qrCodeImageView.setAlpha(1f);
        qrCodeImageView.setVisibility(View.VISIBLE);
        qrCodeExitAnimator.removeAllListeners();
      }
    });
    qrCodeExitAnimator.start();
  }

  private Bitmap generateBitmap(Writer writer, String messageToEncode, BarcodeFormat barcodeFormat,
      int bitmapWidth, int bitmapHeight, Map<EncodeHintType, Object> hints) {

    if (messageToEncode == null) {
      return null;
    }

    try {
      final BitMatrix bitMatrix = writer
          .encode(messageToEncode, barcodeFormat, bitmapWidth, bitmapHeight, hints);

      final int width = bitMatrix.getWidth();
      final int height = bitMatrix.getHeight();
      final Bitmap source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
          source.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
        }
      }
      // rotate the bitmap vertically for rotating PDF
      if (entryData.isRotatingPdf417() && writer instanceof PDF417Writer) {
        final Matrix matrix = new Matrix();
        if (imageFlipped) {
          matrix.preScale(1, 1);
          imageFlipped = false;
          return Bitmap
              .createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        } else {
          matrix.preScale(1, -1);
          imageFlipped = true;
          return Bitmap
              .createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }

      } else {
        return source;
      }
    } catch (WriterException e) {
      return null;
    }
  }

  private void generateAndDisplayInitialPdf() {
    final ConnectivityManager connectivityManager =
        (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    if (getNow() == null && networkInfo != null && networkInfo.isConnected()) {

      SecureEntryClock.getInstance(getContext())
          .syncTime(new SecureEntryClock.Callback() {
            @Override
            public void onComplete(long offset, Date now) {
              workerHandler.post(generateAndDisplayInitialPdf417);
            }

            @Override
            public void onError() {
              workerHandler.post(generateAndDisplayInitialPdf417);
            }
          });
    } else {
      workerHandler.post(generateAndDisplayInitialPdf417);
    }
  }

  @VisibleForTesting
  void generatePdfBitmap() {

    Date now = getNow();
    final String messageToEncode;
    if (now == null) {
      messageToEncode = getNewOTP(Calendar.getInstance().getTime());
    } else {
      messageToEncode = getNewOTP(now);
    }
    pdf417Bitmap = generateBitmap(pdf417Writer, messageToEncode,
        BarcodeFormat.PDF_417,
        pdf417BitmapWidth,
        pdf417BitmapHeight,
        pdfHints);
  }

  private Date getNow() {
    try {
      return SecureEntryClock.getInstance(getContext()).now();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  @VisibleForTesting
  String getNewOTP(Date now) {

    String otpMessage = entryData.getToken();
    String customerKey = entryData.getCustomerKey();
    String eventKey = entryData.getEventKey();

    if (otpMessage == null || otpMessage.length() == 0 ||
        customerKey == null || customerKey.length() == 0) {
      return null;
    }

    try {

      long secondsPast1970 = now.getTime() / 1000;
      byte[] customerSecret = hexStringToByteArray(customerKey);
      TOTP customerTotp = new TOTP(ByteBuffer.wrap(customerSecret), 6, (int) TIME_INTERVAL,
          OTPAlgorithm.SHA1);
      String otpCustomer = customerTotp.generate(secondsPast1970, true);

      if (eventKey == null || eventKey.length() == 0) {
        return String.format("%s::%s", otpMessage, otpCustomer);
      } else {
        byte[] eventSecret = hexStringToByteArray(eventKey);
        TOTP eventTotp = new TOTP(ByteBuffer.wrap(eventSecret), 6, (int) TIME_INTERVAL,
            OTPAlgorithm.SHA1);
        String otpEvent = eventTotp.generate(secondsPast1970, true);
        return String.format("%s::%s::%s", otpMessage, otpEvent, otpCustomer);
      }

    } catch (InstantiationException e) {
      e.printStackTrace();
    }
    return null;
  }

  @VisibleForTesting
  byte[] hexStringToByteArray(String hex) {
    int l = hex.length();
    byte[] data = new byte[l / 2];
    for (int i = 0; i < l; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character
          .digit(hex.charAt(i + 1), 16));
    }
    return data;
  }

  /**
   * Posts a runnable either immediately or based on the <code>toggledAtTime</code> and
   * <code>FALLBACK_DELAY</code> to fallback to QR Code barcode from either static or rotating PDF.
   *
   * @see SecureEntryView#toggledAtTime
   * @see SecureEntryView#FALLBACK_DELAY
   */
  private void postGenerateFallbackPdf417() {
    final long toggledDiff = System.currentTimeMillis() - toggledAtTime;
    if (toggledDiff < FALLBACK_DELAY) {
      if (entryData.isStaticPdf417()) {
        uiHandler.postDelayed(fallbackToStaticPdf417, FALLBACK_DELAY - toggledDiff);
      } else {
        workerHandler.postDelayed(fallbackToRotatingPdf417, FALLBACK_DELAY - toggledDiff);
      }
    } else {
      if (entryData.isStaticPdf417()) {
        uiHandler.post(fallbackToStaticPdf417);
      } else {
        workerHandler.post(fallbackToRotatingPdf417);
      }
    }
  }

  private final Runnable generateAndDisplayInitialPdf417 = new Runnable() {
    @Override
    public void run() {
      Date now = getNow();
      if (now == null) {
        now = new Date();
      }
      String messageToEncode = getNewOTP(now);
      pdf417Bitmap = generateBitmap(pdf417Writer, messageToEncode,
          BarcodeFormat.PDF_417,
          pdf417BitmapWidth,
          pdf417BitmapHeight,
          pdfHints);
      uiHandler.post(displayInitialPdf417);
      workerHandler.postDelayed(generateAndDisplayPdf417, ROTATION_INTERVAL);
    }
  };

  private final Runnable displayInitialPdf417 = new Runnable() {
    @Override
    public void run() {
      loadingView.setVisibility(View.GONE);
      ((AnimationDrawable) loadingView.getDrawable()).stop();
      toggleImageButton.setVisibility(View.VISIBLE);
      pdfImageView.setImageBitmap(pdf417Bitmap);
      if (!viewLoaded) {
        viewLoaded = true;
        showPdf417ViewsWithAnimation();
      } else {
        showPdf417Views();
      }
    }
  };

  private final Runnable fallbackToRotatingPdf417 = new Runnable() {
    @Override
    public void run() {
      generatePdfBitmap();
      uiHandler.post(displayRotatingFallback);
      workerHandler.postDelayed(generateAndDisplayPdf417, ROTATION_INTERVAL);
    }
  };

  private final Runnable displayRotatingFallback = new Runnable() {
    @Override
    public void run() {
      toggled = false;
      pdfImageView.setImageBitmap(pdf417Bitmap);
      toggleImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_overflow));
      hideQrCodeViewsWithAnimation();
      showPdf417ViewsWithAnimation();
    }
  };

  private final Runnable generateAndDisplayPdf417 = new Runnable() {
    @Override
    public void run() {
      generatePdfBitmap();
      uiHandler.post(displayPdf417);
      workerHandler.postDelayed(generateAndDisplayPdf417, ROTATION_INTERVAL);
    }
  };

  private final Runnable displayPdf417 = new Runnable() {
    @Override
    public void run() {
      loadingView.setVisibility(View.GONE);
      ((AnimationDrawable) loadingView.getDrawable()).stop();
      pdfImageView.setImageBitmap(pdf417Bitmap);
      if (!toggled) {
        toggleImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_overflow));
        if (!viewLoaded) {
          showPdf417ViewsWithAnimation();
        } else {
          showPdf417Views();
        }
      } else {
        /*
          it's possible on rotation, displayPdf417 is called but the view is still toggled
          we need to explicitly handle this case here and fallback to PDF afterward.
        */
        toggleImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_swap));
        if (qrCodeBitmap == null) {
          workerHandler.post(generateAndDisplayQRCodeBitmap);
        } else {
          qrCodeImageView.setImageBitmap(qrCodeBitmap);
          qrCodeImageView.setAlpha(1f);
          qrCodeImageView.setTranslationY(0f);
          qrCodeImageView.setVisibility(View.VISIBLE);
        }
        postGenerateFallbackPdf417();
      }
    }
  };

  private final Runnable generateAndDisplayStaticPdf417 = new Runnable() {
    @Override
    public void run() {
      if (pdf417Bitmap == null) {
        pdf417Bitmap = generateBitmap(pdf417Writer, entryData.getBarcode(),
            BarcodeFormat.PDF_417,
            pdf417BitmapWidth,
            pdf417BitmapHeight,
            pdfHints);
      }
      uiHandler.post(displayInitialPdf417);
    }
  };

  private final Runnable fallbackToStaticPdf417 = new Runnable() {
    @Override
    public void run() {
      pdfImageView.setImageBitmap(pdf417Bitmap);
      toggleImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_overflow));
      showPdf417ViewsWithAnimation();
      hideQrCodeViewsWithAnimation();
      toggled = false;
    }
  };

  @VisibleForTesting
  void generateQrCodeBitmap() {
    if (qrCodeBitmap == null) {
      qrCodeBitmap = generateBitmap(qrCodeWriter, entryData.getBarcode(),
          BarcodeFormat.QR_CODE,
          qrCodeBitmapWidth,
          qrCodeBitmapHeight, qrHints);
    }
  }

  private final Runnable generateAndDisplayQRCodeBitmap = new Runnable() {
    @Override
    public void run() {
      generateQrCodeBitmap();
      uiHandler.post(displayQRCode);
      viewLoaded = true;
    }
  };

  private final Runnable generateQRCodeRunnable = new Runnable() {
    @Override
    public void run() {
      generateQrCodeBitmap();
    }
  };

  private final Runnable displayQRCode = new Runnable() {
    @Override
    public void run() {
      loadingView.setVisibility(View.GONE);
      ((AnimationDrawable) loadingView.getDrawable()).stop();
      qrCodeImageView.setImageBitmap(qrCodeBitmap);
      qrCodeImageView.setAlpha(1f);
      qrCodeImageView.setTranslationY(0f);
      qrCodeImageView.setVisibility(View.VISIBLE);
    }
  };

  /**
   * Saves the state of the view on orientation changes
   */
  protected static class SavedState extends BaseSavedState {

    private String token;
    private int brandingColor;
    private EntryData entryData;
    private String errorMessage;
    private boolean loaded;
    private boolean toggled;
    private long toggledAtTime;
    private boolean flipped;

    SavedState(Parcelable superState) {
      super(superState);
    }

    private SavedState(Parcel in) {
      super(in);
      token = in.readString();
      brandingColor = in.readInt();
      entryData = in.readParcelable(EntryData.class.getClassLoader());
      errorMessage = in.readString();
      loaded = in.readInt() == 1;
      toggled = in.readInt() == 1;
      toggledAtTime = in.readLong();
      flipped = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeString(token);
      out.writeInt(brandingColor);
      out.writeParcelable(entryData, flags);
      out.writeString(errorMessage);
      out.writeInt(loaded ? 1 : 0);
      out.writeInt(toggled ? 1 : 0);
      out.writeLong(toggledAtTime);
      out.writeInt(flipped ? 1 : 0);
    }

    public static final Parcelable.Creator<SavedState> CREATOR
        = new Parcelable.Creator<SavedState>() {
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };

  }
}
