# Sigmob Android SDK 接入说明

## 准备工作

* 1 解压我们提供的压缩包，把 wind-sdk-*.aar , wind-common-*.aar 放入app的libs工程中。

* 2 SDK 支持 Android API Level 18+。

* 3 4.15.0 开始默认仅支持Android X。

## 集成步骤

### 如仅支持Android Support V4环境, 请将一下代码添加到AndroidManifest.xml中

```xml

<manifest>

    <application>
      
        <provider
            android:name="com.sigmob.sdk.SigmobFileV4Provider"
            android:authorities="${applicationId}.sigprovider"
            android:exported="false"
            android:initOrder="200"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/sigmob_provider_paths"/>
        </provider>

    </application>


</manifest>
```

### 添加Sigmob SDK 依赖

```groovy
dependencies {
    	//wind SDK aar文件 放入项目libs中
    	implementation fileTree(include: ['wind-*.aar'], dir: 'libs')


    	//以下依赖二选一即可

    	//AndroidX 项目添加
    	implementation "androidx.core:core:1.0.0"

    	//Android Support V4 项目添加
        implementation 'com.android.support:support-v4:23.0.+'

}
```


### 混淆配置

#### 资源配置

```xml
<resources xmlns:tools="http://schemas.android.com/tools"
    tools:keep="@drawable/sig_*,@layout/sig_*,@id/sig_**,@style/sig_*,@string/sig_*,@anim/sig_*, @xml/sig*,@color/sig_*,@attr/sig_*,@dimen/sig_*"
    tools:ignore="ResourceName" />
```

#### 代码混淆
```java
# 优化 不优化输入的类文件
-dontoptimize

# androidx

-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

# android.support.v4

-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

# WindAd

-keep class com.sigmob.sdk.**{ *;}
-keep interface com.sigmob.sdk.**{ *;}
-keep class com.sigmob.windad.**{ *;}
-keep interface com.sigmob.windad.**{ *;}
-keep class com.czhj.**{ *;}
-keep interface com.czhj.**{ *;}
-keep class com.tan.mark.**{*;}

# miitmdid

-dontwarn com.bun.**
-keep class com.bun.** {*;}
-keep class a.**{*;}
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.vs.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}
```