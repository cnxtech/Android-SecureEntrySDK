package com.ticketmaster.presence;
/*
    Copyright 2018 Ticketmaster

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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.VisibleForTesting;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ticketmaster.presence.sanetime.Clock;
import com.ticketmaster.presence.sanetime.NTPHost;
import com.ticketmaster.presence.totp.OTPAlgorithm;
import com.ticketmaster.presence.totp.TOTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Locale;

/**
 * View displaying a rotating PDF417 or static PDF417 ticket.
 */

public final class SecureEntryView extends View implements EntryView {

    private static final String TAG = SecureEntryView.class.getSimpleName();

    private static final int BACKGROUND_ANIMATION_DURATION = 700;
    private static final int BACKGROUND_ANIMATION_DELAY_DURATION = 800;
    private static final int FOREGROUND_ANIMATION_DURATION = 600;
    private static final int FOREGROUND_ANIMATION_DELAY_DURATION = 900;
    private static final int ANIMATION_BACKGROUND_WIDTH = 10;
    private static final int ANIMATION_FOREGROUND_WIDTH = 4;
    private static final int INTERNAL_VIEW_PADDING = 4;
    private static final int QR_CODE_MIN_WIDTH = 132;
    private static final int PDF417_MIN_WIDTH = 250;
    private static final int PDF417_MIN_HEIGHT = 100;
    private static final int TEXT_SIZE = 16;

    private static final long ROTATION_INTERVAL = 15000L;
    private static final double TIME_INTERVAL = 15;

    private HandlerThread mHandlerThread = new HandlerThread("SecureEntryViewThread");
    private Handler mWorkerHandler;
    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private BarcodeFormat mBarcodeFormat;
    private Writer mWriter;
    private EntryData mEntryData;
    private String mMessageToEncode;
    private String mStateMessage;

    private float mAspectRatio = (float) PDF417_MIN_HEIGHT / (float) PDF417_MIN_WIDTH;
    private Bitmap mBitmap;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private Paint mViewPaint;
    private Rect mDstRect;

    private TextPaint mTextPaint;

    private Paint mAnimationBackgroundPaint;
    private AnimationRectF mAnimationBackgroundRect;
    private Paint mAnimationForegroundPaint;
    private AnimationRectF mAnimationForegroundRect;

    private boolean mFlipped;
    private int mRetryCount = 3;

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
        mViewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mViewPaint.setColor(Color.WHITE);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setColor(Color.BLACK);

        mStateMessage = context.getString(R.string.error_no_token);

        mAnimationBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimationForegroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mDstRect = new Rect();
        mAnimationBackgroundRect = new AnimationRectF();
        mAnimationForegroundRect = new AnimationRectF();

        if (attrs != null) {
            TypedArray typedArray = context
                    .obtainStyledAttributes(attrs, R.styleable.SecureEntryView, 0, 0);
            try {
                int brandingColor =
                        typedArray.getColor(R.styleable.SecureEntryView_branding_color,
                                getResources().getColor(R.color.default_animation_color));
                setBrandingColor(brandingColor);
            } finally {
                typedArray.recycle();
            }
        }
    }

    @Override
    public void setToken(String token) {
        mStateMessage = getResources().getString(R.string.loading);
        mRetryCount = 3;
        decodeToken(token);
        if (mEntryData != null) {
            setupWriter();
            displayTicket();
        }
    }

    @Override
    public void setBrandingColor(@ColorInt int brandingColor) {
        mAnimationBackgroundPaint.setColor(brandingColor);
        mAnimationBackgroundPaint.setAlpha(70);
        mAnimationForegroundPaint.setColor(brandingColor);
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // setup the threading mechanism
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();
        mWorkerHandler = new Handler(looper);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // clean up any threading
        mHandlerThread.quit();
        mUiHandler.removeCallbacksAndMessages(null);
        mWorkerHandler.removeCallbacksAndMessages(null);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mEntryData != null && mBitmap != null) {
            // static
            if (TextUtils.isEmpty(mEntryData.getToken())) {
                canvas.drawBitmap(mBitmap, null, mDstRect, mViewPaint);
                // rotating
            } else if (!TextUtils.isEmpty(mEntryData.getToken())) {
                canvas.drawBitmap(mBitmap, null, mDstRect, mViewPaint);
                canvas.drawRect(mAnimationBackgroundRect, mAnimationBackgroundPaint);
                canvas.drawRect(mAnimationForegroundRect, mAnimationForegroundPaint);
            } else {
                canvas.drawColor(Color.WHITE);
                float startTextX = (getWidth() / 2) - (mTextPaint.measureText(mStateMessage) / 2);
                float startTextY = (getHeight() / 2) - ((mTextPaint.ascent() + mTextPaint.descent()) / 2);
                canvas.drawText(mStateMessage, startTextX, startTextY, mTextPaint);
            }
        } else {
            canvas.drawColor(Color.WHITE);
            float startTextX = (getWidth() / 2) - (mTextPaint.measureText(mStateMessage) / 2);
            float startTextY = (getHeight() / 2) - ((mTextPaint.ascent() + mTextPaint.descent()) / 2);
            canvas.drawText(mStateMessage, startTextX, startTextY, mTextPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int specMode = MeasureSpec.getMode(widthMeasureSpec);
        final int specSize = MeasureSpec.getSize(widthMeasureSpec);
        int result;
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                if (specSize < mBitmapWidth) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                } else {
                    result = mBitmapWidth + getPaddingLeft() + getPaddingRight();
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = mBitmapWidth;
        }

        if (mEntryData != null) {
            // Height will need to be based on aspect ratio
            float tempHeight = (float) result * mAspectRatio;
            int height = (int) tempHeight;
            setRectDimensions(result, height);
            setMeasuredDimension(result, height);
        } else {
            setErrorRectSize(result);
        }
    }

    private void setErrorRectSize(int width) {

        // error needs to at least fit bounds
        float textWidth = mTextPaint.measureText(mStateMessage);
        float textHeight = mTextPaint.getTextSize() * 2;

        int actualWidth = (int) textWidth + getPaddingLeft() + getPaddingRight();
        int actualHeight = (int) textHeight + getPaddingTop() + getPaddingBottom();

        if (width > actualWidth) {
            actualWidth = width;
        }

        setMeasuredDimension(actualWidth, actualHeight);
    }

    private void setRectDimensions(int width, int height) {
        // by default 4dp is included in the background views padding top & bottom
        mDstRect.left = getPaddingLeft();
        mDstRect.top = getPaddingTop() + getDefaultPadding();
        mDstRect.right = width - getPaddingRight();
        mDstRect.bottom = height - (getPaddingBottom() + getDefaultPadding());

        // background start
        mAnimationBackgroundRect.top = mDstRect.top;
        mAnimationBackgroundRect.bottom = mDstRect.bottom;
        mAnimationBackgroundRect.left = mDstRect.left;
        mAnimationBackgroundRect.right = mDstRect.left + getBackgroundBarWidth();

        // foreground start
        mAnimationForegroundRect.top = getPaddingTop();
        mAnimationForegroundRect.bottom = height - getPaddingBottom();
        float halfForegroundBar = getForegroundBarWidth() / 2;
        float halfBackgroundBar = getBackgroundBarWidth() / 2;
        mAnimationForegroundRect.left = mDstRect.left + (halfBackgroundBar - halfForegroundBar);
        mAnimationForegroundRect.right = mAnimationForegroundRect.left + getForegroundBarWidth();
    }

    private void displayTicket() {

        if (TextUtils.isEmpty(mEntryData.getToken())) {
            mMessageToEncode = mEntryData.getBarcode();
            mWorkerHandler.post(generateQrCodeRunnable);
        } else if (!TextUtils.isEmpty(mEntryData.getToken())) {

            Date now;
            try {
                now = Clock.getInstance(getContext()).now();
            } catch (Exception ex) {
                now = null;
            }

            if (now == null) {
                syncTimeAndShowTicket(isOnline());
            } else {
                mUiHandler.post(moveBackgroundRightRunnable);
                mUiHandler.post(moveForegroundRightRunnable);
                mWorkerHandler.post(changeBitmapRunnable);
            }
        }
    }

    private void syncTimeAndShowTicket(boolean isOnline) {
        if (isOnline) {
            Clock.getInstance(getContext()).sync(NTPHost.NTP_POOL_PROJECT, new Clock.Callback() {
                @Override
                public void onComplete(long offset, Date now) {

                    mUiHandler.post(moveBackgroundRightRunnable);
                    mUiHandler.post(moveForegroundRightRunnable);
                    mWorkerHandler.post(changeBitmapRunnable);
                }

                @Override
                public void onError() {
                    if (mRetryCount != 0) {
                        mRetryCount--;
                        syncTimeAndShowTicket(isOnline());
                    }
                }
            });
        } else {
            mStateMessage = getResources().getString(R.string.network_error);
            postInvalidate();
        }
    }

    private void setupWriter() {

        if (!TextUtils.isEmpty(mEntryData.getToken())) {
            mWriter = new PDF417Writer();
            mBitmapWidth = PDF417_MIN_WIDTH * (int) getResources().getDisplayMetrics().density;
            mBitmapHeight = PDF417_MIN_HEIGHT * (int) getResources().getDisplayMetrics().density;
            mAspectRatio = (float) mBitmapHeight / (float) mBitmapWidth;
            mBarcodeFormat = BarcodeFormat.PDF_417;
        } else {
            mWriter = new QRCodeWriter();
            mBitmapWidth = QR_CODE_MIN_WIDTH * (int) getResources().getDisplayMetrics().density;
            mBitmapHeight = QR_CODE_MIN_WIDTH * (int) getResources().getDisplayMetrics().density;
            mAspectRatio = (float) mBitmapWidth / (float) mBitmapHeight;
            mBarcodeFormat = BarcodeFormat.QR_CODE;
        }

    }

    private void decodeToken(String token) {
        byte[] bytes = Base64.decode(token, Base64.DEFAULT);
        String decoded = new String(bytes);

        try {
            JSONObject jsonObject = new JSONObject(decoded);
            String barcode = jsonObject.optString("b", null);
            String entryToken = jsonObject.optString("t", null);
            String customerKey = jsonObject.optString("ck", null);
            String eventKey = jsonObject.optString("ek", null);
            if (!TextUtils.isEmpty(entryToken)) {
                mEntryData = new EntryData(barcode, entryToken, customerKey, eventKey);
            } else {
                mEntryData = new EntryData(barcode);
            }
        } catch (JSONException e) {
            mStateMessage = getResources().getString(R.string.error_invalid_token);
        }
    }

    private void getNewOTP(Date now) {

        String otpMessage = mEntryData.getToken();
        String keyToUse = mEntryData.getCustomerKey();

        if (TextUtils.isEmpty(otpMessage) || TextUtils.isEmpty(keyToUse)) {
            return;
        }
        byte[] secret = hexStringToByteArray(keyToUse);
        try {
            TOTP totp = new TOTP(ByteBuffer.wrap(secret), 6, (int) TIME_INTERVAL, OTPAlgorithm.SHA1);
            long secondsPast1970 = now.getTime() / 1000;
            String oneTimePassword = totp.generate(secondsPast1970, true);
            mMessageToEncode = String.format("%s::%s", otpMessage, oneTimePassword);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private void generateBitmap() {
        if (mMessageToEncode == null) {
            return;
        }

        try {
            BitMatrix bitMatrix = mWriter.encode(mMessageToEncode, mBarcodeFormat, mBitmapWidth, mBitmapHeight);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap source = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    source.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            // rotate the bitmap vertically
            if (!TextUtils.isEmpty(mEntryData.getToken())) {
                if (mFlipped) {
                    Matrix matrix = new Matrix();
                    matrix.preScale(1, 1);
                    mBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                    mFlipped = false;
                } else {
                    Matrix matrix = new Matrix();
                    matrix.preScale(1, -1);
                    mBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                    mFlipped = true;
                }
            } else {
                mBitmap = source;
                postInvalidate();
            }
        } catch (WriterException e) {
            e.printStackTrace();
            mBitmap = null;
        }
    }

    private int getBackgroundBarWidth() {
        return ANIMATION_BACKGROUND_WIDTH * (int) getResources().getDisplayMetrics().density;
    }

    private int getForegroundBarWidth() {
        return ANIMATION_FOREGROUND_WIDTH * (int) getResources().getDisplayMetrics().density;
    }

    private int getDefaultPadding() {
        return INTERNAL_VIEW_PADDING * (int) getResources().getDisplayMetrics().density;
    }

    private int getTextSize() {
        return TEXT_SIZE * (int) getResources().getDisplayMetrics().density;
    }

    private final Runnable changeBitmapRunnable = new Runnable() {
        @Override
        public void run() {

            Date now;
            try {
                now = Clock.getInstance(getContext()).now();
                if (now == null) {
                    now = new Date(System.currentTimeMillis());
                }
            } catch (Exception ex) {
                // fallback to device time
                now = new Date(System.currentTimeMillis());
            }

            getNewOTP(now);
            generateBitmap();
            mWorkerHandler.postDelayed(changeBitmapRunnable, ROTATION_INTERVAL);
        }
    };

    private final Runnable generateQrCodeRunnable = new Runnable() {
        @Override
        public void run() {
            generateBitmap();
        }
    };

    private final Runnable moveBackgroundRightRunnable = new Runnable() {
        @Override
        public void run() {
            animateBackgroundBarRight();
        }
    };

    private final Runnable moveBackgroundLeftRunnable = new Runnable() {
        @Override
        public void run() {
            animateBackgroundBarLeft();
        }
    };

    private final Runnable moveForegroundRightRunnable = new Runnable() {
        @Override
        public void run() {
            animateForegroundBarRight();
        }
    };

    private final Runnable moveForeGroundLeftRunnable = new Runnable() {
        @Override
        public void run() {
            animateForegroundBarLeft();
        }
    };

    private boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void animateBackgroundBarRight() {
        // compute leftX start & end
        float startBackgroundLeftX = mDstRect.left;
        float endBackgroundLeftX = mDstRect.right - getBackgroundBarWidth();

        // compute rightX start & end
        float startBackgroundRightX = mDstRect.left + getBackgroundBarWidth();
        float endBackgroundRightX = mDstRect.right;

        // animate
        runAnimation(startBackgroundLeftX, endBackgroundLeftX, startBackgroundRightX,
                endBackgroundRightX, BACKGROUND_ANIMATION_DURATION,
                BACKGROUND_ANIMATION_DELAY_DURATION, mAnimationBackgroundRect, moveBackgroundLeftRunnable);
    }

    private void animateBackgroundBarLeft() {
        // compute leftX start & end
        float startBackgroundLeftX = mDstRect.right - getBackgroundBarWidth();
        float endBackgroundLeftX = mDstRect.left;

        // compute rightX start & end
        float startBackgroundRightX = mDstRect.right;
        float endBackgroundRightX = mDstRect.left + getBackgroundBarWidth();

        // animate
        runAnimation(startBackgroundLeftX, endBackgroundLeftX, startBackgroundRightX,
                endBackgroundRightX, BACKGROUND_ANIMATION_DURATION,
                BACKGROUND_ANIMATION_DELAY_DURATION, mAnimationBackgroundRect, moveBackgroundRightRunnable);
    }

    private void animateForegroundBarRight() {

        float halfForegroundBar = getForegroundBarWidth() / 2;
        float halfBackgroundBar = getBackgroundBarWidth() / 2;

        // compute leftX start & end
        float startForegroundLeftX = mDstRect.left + (halfBackgroundBar - halfForegroundBar);
        float endForegroundLeftX = mDstRect.right - (halfBackgroundBar + halfForegroundBar);

        // compute rightX start & end
        float startForegroundRightX = mDstRect.left + (halfBackgroundBar + halfForegroundBar);
        float endForegroundRightX = mDstRect.right - (halfBackgroundBar - halfForegroundBar);

        // animate
        runAnimation(startForegroundLeftX, endForegroundLeftX, startForegroundRightX,
                endForegroundRightX, FOREGROUND_ANIMATION_DURATION, FOREGROUND_ANIMATION_DELAY_DURATION,
                mAnimationForegroundRect, moveForeGroundLeftRunnable);
    }

    private void animateForegroundBarLeft() {

        float halfForegroundBar = getForegroundBarWidth() / 2;
        float halfBackgroundBar = getBackgroundBarWidth() / 2;

        // compute leftX start & end
        float startForegroundLeftX = mDstRect.right - (halfBackgroundBar + halfForegroundBar);
        float endForegroundLeftX = mDstRect.left + (halfBackgroundBar - halfForegroundBar);

        // compute rightX start & end
        float startForegroundRightX = mDstRect.right - (halfBackgroundBar - halfForegroundBar);
        float endForegroundRightX = mDstRect.left + (halfBackgroundBar + halfForegroundBar);

        // animate
        runAnimation(startForegroundLeftX, endForegroundLeftX, startForegroundRightX,
                endForegroundRightX, FOREGROUND_ANIMATION_DURATION, FOREGROUND_ANIMATION_DELAY_DURATION,
                mAnimationForegroundRect, moveForegroundRightRunnable);
    }

    private void runAnimation(float startLeftX, float endLeftX, float startRightX, float endRightX,
                              int duration, final int delayDuration, AnimationRectF animationRect,
                              final Runnable runnable) {

        ObjectAnimator animateLeft =
                ObjectAnimator.ofFloat(animationRect, "left", startLeftX, endLeftX);

        ObjectAnimator animateRight =
                ObjectAnimator.ofFloat(animationRect, "right", startRightX, endRightX);

        animateLeft.addUpdateListener(animatorUpdateListener);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(mInterpolator);
        animatorSet.playTogether(animateLeft, animateRight);
        animatorSet.setDuration(duration);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mUiHandler.postDelayed(runnable, delayDuration);
                animatorSet.removeListener(this);
            }
        });

        animatorSet.start();
    }

    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }
    };

    /**
     * Internal RectF with setters for left & right to animate via ObjectAnimator
     */
    private static class AnimationRectF extends RectF {

        AnimationRectF() {
            super();
        }

        public void setRight(float right) {
            this.right = right;
        }

        public void setLeft(float left) {
            this.left = left;
        }
    }
}
