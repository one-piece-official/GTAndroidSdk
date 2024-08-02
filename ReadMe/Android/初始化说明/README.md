# SDK接口类介绍与广告接入示例代码
## SDK初始化

```java
WindAds ads = WindAds.sharedAds();

ads.startWithOptions(context, new WindAdOptions(appId, appKey));
```

## 开启/关闭Debug日志

```java
WindAds ads = WindAds.sharedAds();

//true: 开启Debug日志，false: 关闭Debug日志

ads.setDebugEnable(true);

```
## 中国大陆权限授权接口（仅针对中国大陆）

```java
//主动READ_PHONE_STATE，
//ACCESS_FINE_LOCATION 权限授权请求

WindAds.requestPermission(actvity);
```
