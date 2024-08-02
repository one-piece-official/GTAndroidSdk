# 新插屏广告集成相关
## 1. 接口API说明
注意：此广告类型需要更新至 Sigmob Android SDK v4.12.0及其以上版本

### 1.1 新插屏视频请求类

**WindNewInterstitialAdRequest** 

| 方法名                 | 说明                                                         |
| ---------------------  | ------------------------------------------------------------ |
| WindNewInterstitialAdRequest(String placementId, String userId, HashMap<String,String> option) | 新插屏视频请求对象，其中PlacementId 为广告位 必传参数；userId 为用户ID，没有可以传入null； option 为加载扩展参数，非必传 |
| setEnableScreenLockDisPlayAd(boolean enableScreenLockDisPlayAd )                                            | 允许锁屏播放                                                 |
| setEnableKeepOn(boolean enableKeepOn )    | 播放中保持屏幕常亮                                                 |


### 1.2 新插屏视频对象类

**WindNewInterstitialAd**

| 方法                  | 说明                                                         |
| --------------------- | ------------------------------------------------------------ |
| WindNewInterstitialAd(WindNewInterstitialAdRequest  request)   | 初始化新插屏广告对象，request 为广告请求对象、必传。此构造方法为全屏新插屏 |
| setWindNewInterstitialAdListener(WindNewInterstitialAdListener listener)  | 设置新插屏视频接口回调 |
| load(String bid_token)              | 发起Header Bidding广告加载        |
| load()              | 发起广告加载                                                 |
| isReady()             | 判断当前WindNewInterstitialAd是否存在可展示的广告                 |
| show(HashMap<String,String> showOption)                | 展示广告, showOption 为播放扩展参数        |
| destroy()                                        | 广告对象销毁，如竞价失败，需要再次竞价可以调用destroy销毁后再次进行load竞价       |


### 1.3 播放场景参数

 **AD_SCENE_ID, AD_SCENE_DESC**  展示场景扩展参数说明，仅用于show接口
 
| 参数                    | 说明                                                         |
| ---------------------  | ------------------------------------------------------------ |
| WindAds.AD_SCENE_ID    |  视频广告播放场景ID |
| WindAds.AD_SCENE_DESC  |  视频广告播放场景描述 |


### 1.4 新插屏回调说明 

**WindNewInterstitialAdListener**

| 回调方法                  | 参数                                             | 说明                                                         |
| --------------------- | ------------------------------------------------ | ------------------------------------------------------------ |
| onInterstitialAdPreLoadSuccess(String placementId) |  广告服务填充成功，placementId 为回调广告位 |
| onInterstitialAdPreLoadFail(String placementId) |  广告填充失败 ,placementId 为回调广告位|
| onInterstitialAdLoadSuccess(String placementId) |  广告加载成功 ,placementId 为回调广告位 |
| onInterstitialAdLoadError(WindAdError windAdError,String placementId) |  广告加载失败, windAdError 为具体错误信息，placementId 为回调广告位 |
| onInterstitialAdShow(String placementId) |  广告开始展示 ,placementId 为回调广告位 |
| onInterstitialAdShowError(WindAdError windAdError,String placementId) |  广告开始展示失败，windAdError 为具体错误信息，placementId 为回调广告位 |
| onInterstitialAdClicked(String placementId) |  广告被用户点击, placementId 为回调广告位 |
| onInterstitialAdClosed(String placementId) |  广告关闭， placementId 为回调广告位 |


## 2. 示例代码

### 2.1 设置监听回调

```java
private WindNewInterstitialAd windNewInterstitialAd;

//placementId 必填,USER_ID,OPTIONS可不填，
WindNewInterstitialAdRequest request = new WindNewInterstitialAdRequest(PLACEMENT_ID, USER_ID, OPTIONS);

WindNewInterstitialAd windNewInterstitialAd = new WindNewInterstitialAd(request);

windNewInterstitialAd.setWindNewInterstitialAdListener(new WindNewInterstitialAdListener() {

    //仅sigmob渠道有回调，聚合其他平台无次回调
    @Override
    public void onInterstitialAdPreLoadSuccess(String placementId) {
        Toast.makeText(mContext, "新插屏广告数据返回成功", Toast.LENGTH_SHORT).show();
    }

    //仅sigmob渠道有回调，聚合其他平台无次回调
    @Override
    public void onInterstitialAdPreLoadFail(String placementId) {
        Toast.makeText(mContext, "新插屏广告数据返回失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterstitialAdLoadSuccess(String placementId) {
        Toast.makeText(mContext, "新插屏广告缓存加载成功,可以播放", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterstitialAdShow(String placementId) {
        Toast.makeText(mContext, "新插屏广告播放开始", Toast.LENGTH_SHORT).show();
    }
    

    @Override
    public void onInterstitialAdClicked(String placementId) {
        Toast.makeText(mContext, "新插屏广告点击事件监听", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterstitialAdClosed(String placementId) {
        Toast.makeText(mContext, "新插屏广告关闭", Toast.LENGTH_SHORT).show();
    }

    /**
     * 加载广告错误回调
     * WindAdError 新插屏错误内容
     * placementId 广告位
     */
    @Override
    public void onInterstitialAdLoadError(WindAdError windAdError, String placementId) {
        Toast.makeText(mContext, "新插屏广告加载错误", Toast.LENGTH_SHORT).show();
    }

    /**
     * 播放错误回调
     * WindAdError 新插屏错误内容
     * placementId 广告位
     */
    @Override
    public void onInterstitialAdShowError(WindAdError windAdError, String placementId) {
        Toast.makeText(mContext, "新插屏广告播放错误", Toast.LENGTH_SHORT).show();
    }
});
```

### 2.2 新插屏广告加载

```java
/**
 *同一个WindNewInterstitialAd不建议在广告playing中重复load
  *同一个WindNewInterstitialAd在onInterstitialAdClosed中可以load下一次广告
  */
if (windNewInterstitialAd != null) {
    windNewInterstitialAd.loadAd();
}
```

### 2.3 新插屏广告播放

```java
try {
    /**
     *收到onInterstitialAdLoadSuccess回调代表广告已ready
      */
    if (windNewInterstitialAd != null && windNewInterstitialAd.isReady()) {
        HashMap option = new HashMap();
        //场景ID
        option.put(WindAds.AD_SCENE_ID, "场景ID");
        //场景描述
        option.put(WindAds.AD_SCENE_DESC, "场景描述");
        //广告播放
        windNewInterstitialAd.show(option);
    }
} catch (Exception e) {
    e.printStackTrace();
}
```