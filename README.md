## Introduction

Clone the repo using git:

```
git clone https://github.com/ticketmaster/Android-SecureEntrySDK.git
```

or download the zip file.

### SecureEntryView

The main view integrating the PDF417 into 3rd party applications is the `SecureEntryView`. It is based on Android View class and gives access to methods for controlling the underlying data and animation color.

Class definition for the view:

```java
/**
 * View displaying a rotating PDF417 or static PDF417 ticket.
 */
public final class SecureEntryView extends View implements EntryView {}
```

#### Usage:

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

#### Initializing the view:

**Note:** You must call this method first otherwise the view will remain in the loading state.

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

##### Gradle (Coming Soon)
```groovy
dependencies {
    implementation 'com.ticketmaster.presence:secure-entry:1.0.0'
}
```

#### Library Dependencies

* ZXing (for rending PDF417 symbology)
* commons-net (for communicating with NTP servers to get time)