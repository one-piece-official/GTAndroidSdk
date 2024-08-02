# 隐私设置

> 初始化前设置生效

### GDPR授权（仅针对海外市场）

```java
WindAds ads=WindAds.sharedAds();

/* 欧盟区域请设置GDPR相关授权状态
 * WindConsentStatus 值说明:
 * UNKNOW,  //未知,默认值，根据服务器判断是否在欧盟区，若在欧盟区则判断为拒绝GDPR授权
 * ACCEPT,  //用户同意GDPR授权
 * DENIED;  //用户拒绝GDPR授权
 */

ads.setUserGDPRConsentStatus(WindConsentStatus.ACCEPT);
```

### 开发者传入设备Id

```java
WindAds ads=WindAds.sharedAds();
WindAdOptions options=new WindAdOptions(appId,appKey);
options.setCustomController(new WindCustomController() {
    @Override
    public boolean isCanUseLocation(){
        return false;
    }

    @Override
    public Location getLocation(){
        return customLocation;
    }

    @Override
    public boolean isCanUsePhoneState(){
        return false;
    }

    @Override
    public String getDevImei(){
        return"customIMEI"
    }

    @Override
    public boolean isCanUseAndroidId(){
        return false;

    }

    @Override
    public String getAndroidId(){
        return"customAndroid"
    }

    @Override
    public String getDevOaid(){
        return"customOaid"
    }
})

ads.startWithOptions(context,options);
```

### COPPA授权（仅针对海外市场）

```java
WindAds ads=WindAds.sharedAds();

/*
 * 如若涉及儿童隐私保护相关协议，请设置是否为受限制用户
 * UNKNOWN 未知，默认值
 * NO 不限制
 * YES 有限制
 *
 */
ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.YES);

/*
 * setUserAge 设置用户年龄
 */
ads.setUserAge(28);
```

### 是否成年人(仅中国大陆)

```java
/*
 * 是否成年
 * true 成年, false 未成年，默认值为true
 */
WindAds.sharedAds().setAdult(true);
```

### 是否开启个性化推荐（仅中国大陆）

```java
/*
 * 是否开启个性化推荐接口
 * true 开启, false 关闭,默认值为true
 */
WindAds.sharedAds().setPersonalizedAdvertisingOn(true); 
```