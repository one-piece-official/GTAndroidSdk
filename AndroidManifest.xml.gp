<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.sigmob.sdk"
    android:versionCode="1"
    android:versionName="1.0">


    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <queries>
        <intent>
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.BROWSABLE" />

            <data android:scheme="https" />
        </intent>
    </queries>

    <application>

           <activity
                  android:name="com.sigmob.sdk.base.common.TransparentAdActivity"
                  android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
                  android:multiprocess="true"
                  android:hardwareAccelerated="true"
                  android:screenOrientation="behind"
                  android:theme="@style/sig_transparent_style" />

              <activity
                  android:name="com.sigmob.sdk.base.common.AdActivity"
                  android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
                  android:hardwareAccelerated="true"
                  android:multiprocess="true"
                  android:screenOrientation="behind"
                  android:theme="@android:style/Theme.Light.NoTitleBar" />

              <activity
                  android:name="com.sigmob.sdk.base.common.PortraitAdActivity"
                  android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
                  android:hardwareAccelerated="true"
                  android:multiprocess="true"
                  android:screenOrientation="sensorPortrait"
                  android:theme="@android:style/Theme.Light.NoTitleBar" />

              <activity
                  android:name="com.sigmob.sdk.base.common.LandscapeAdActivity"
                  android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
                  android:hardwareAccelerated="true"
                  android:multiprocess="true"
                  android:screenOrientation="sensorLandscape"
                  android:theme="@android:style/Theme.Light.NoTitleBar" />

              <activity
                  android:name="com.sigmob.sdk.base.common.PortraitTransparentAdActivity"
                  android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
                  android:hardwareAccelerated="true"
                  android:multiprocess="true"
                  android:screenOrientation="sensorPortrait"
                  android:theme="@style/sig_transparent_style" />

              <activity
                  android:name="com.sigmob.sdk.base.common.LandscapeTransparentAdActivity"
                  android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
                  android:hardwareAccelerated="true"
                  android:multiprocess="true"
                  android:screenOrientation="sensorLandscape"
                  android:theme="@style/sig_transparent_style" />

    </application>


</manifest>
