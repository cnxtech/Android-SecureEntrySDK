## Introduction

### SecureEntryView

The main view integrating the PDF417 into 3rd party applications is the `SecureEntryView`. It is based on Android FrameLayout ViewGroup and gives access to methods for controlling the underlying data, animation color, and error handling text.

Class definition for the view:

```java
/**
 * View displaying a rotating PDF417 or static QRCode ticket.
 */
public final class SecureEntryView extends FrameLayout implements EntryView, View.OnClickListener {}
```

### Installation

#### jCenter 

Add to root `build.gradle`:
```groovy
allprojects {
    repositories {
	      //...
        jcenter()
    }
}
```

Add to project `build.gradle` dependencies:
```groovy
dependencies {
    //...
    implementation ('com.ticketmaster.presence:secure-entry:1.0.4@aar') {
        transitive = true
    }
}
```

#### Jitpack

Add to root `build.gradle`:
```groovy
allprojects {
    repositories {
	//...
        maven { url 'https://jitpack.io' }
    }
}
```

Add to project `build.gradle` dependencies:
```groovy
dependencies {
    //...
    implementation 'com.github.ticketmaster:Android-SecureEntrySDK:1.0.4'
}
```

#### Manually

1. Clone Android-SecureEntrySDK from the git repository: https://github.com/ticketmaster/Android-SecureEntrySDK

```
git clone https://github.com/ticketmaster/Android-SecureEntrySDK.git
```

2. Import the project as a New Module, when importing import only include secure-entry in the import
3. Once, imported your settings.gradle should have new module `:secure-entry` add this to your dependencies block of your `app/build.gradle`:

```groovy
dependencies {
    //...
    implementation project(":secure-entry")
}
```

### Usage:

Simply include the following layout in XML:

```xml
<com.ticketmaster.presence.SecureEntryView
    android:id="@+id/secureEntryView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

or add it to a ViewGroup programmatically:

```java
SecureEntryView secureEntryView = new SecureEntryView(mContext);
myViewGroup.addView(secureEntryView);
```

#### Sizing

If `android:layout_width="wrap_content"` and `android:layout_height="wrap_content"` the view will be 216x160 (dp).

When supplying a width such as `android:layout_width="300dp"` the view will size its height automatically using a 5:1 ratio for PDF417 and a 1:1 ratio for QRCode but the parent view max width would be `300dp`.

Finally, if `android:layout_height="wrap_content"` and `android:layout_width="match_parent"` are used the view will use width to calculate the height using the same ratios above however, the width would be as large as the devices screen width (not recommended).

Note: if using the view in `ConstraintLayout` do not supply `app:layout_constraintDimensionRatio=""` for any ratio. It is okay however to use `android:layout_width="0dp"` and `android:layout_height="wrap_content"` in this ViewGroup as it will size based on the above aspect ratio.

#### Initializing the view:

**Note:** You must call `setToken(String token)` first otherwise the view will remain in the error state.

```java
// find the view or call constructor
SecureEntryView secureEntryView = findViewById(R.id.secureEntryView);

// set the token on the view
secureEntryView.setToken(mNewToken);
```

#### Change the branding color:
```java
secureEntryView.setBrandingColor(getResources().getColor(R.color.anotherBrandingColor));
```

or via xml:
```xml
<com.ticketmaster.presence.SecureEntryView
    android:id="@+id/secureEntryView"
    app:branding_color="@color/your_branding_color"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>

```

#### Change the error text:
```java
secureEntryView.setErrorText(getString(R.string.custom_error_text));
```

or via xml:
```xml
<com.ticketmaster.presence.SecureEntryView
    android:id="@+id/secureEntryView"
    app:error_text="@string/custom_error_text"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>

```

### Syncing the time

#### `SecureEntryClock.getInstance(context).syncTime()`

This method is used to begin a background time sync. This method is provided to allow apps to initiate a time sync before a `SecureEntryView` is ever instantiated/displayed.

It can be called in a few ways:

1. It can be called with no params `SecureEntryClock.getInstance(context).syncTime()` which will asynchronously sync the time but provide no callback of completion or error.
2. It can also be called with a SecureEntryClock.Callback to know whether it completed or an error occurred.

```java
SecureEntryClock.getInstance(this).syncTime(new SecureEntryClock.Callback() {
      @Override
      public void onComplete(long offset, Date now) {
        //.. handle completion
        Log.d(TAG, "onComplete() called with: offset = [" + offset + "], now = [" + now + "]");
      }

      @Override
      public void onError() {
        //.. handle error
        Log.d(TAG, "onError() called");
      }
    });

```

Note: Both calls will happen in their own thread and the response will be received on whichever thread has created the callback.

### Permissions included in AndroidManifest.xml for this library
```xml
 <uses-permission android:name="android.permission.INTERNET"/>
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### Library Dependencies

* ZXing (for rending PDF417 symbology)
* commons-net (for communicating with NTP servers to get time)