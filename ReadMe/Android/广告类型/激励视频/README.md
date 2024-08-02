# 激励视频广告

## 1. 接口 API 说明

### 1.1 激励视频请求类

**WindRewardAdRequest**

| 方法名                                                                                | 说明                                                                                                                       |
| ------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| WindRewardAdRequest(String placementId, String userId, HashMap<String,String> option) | 激励视频请求对象，其中 PlacementId 为广告位 必传参数；userId 为用户 ID，没有可以传入 null，；option 为加载扩展参数，非必传 |
| setEnableScreenLockDisPlayAd(boolean enableScreenLockDisPlayAd )                      | 允许锁屏播放                                                                                                               |
| setEnableKeepOn(boolean enableKeepOn )                                                | 播放中保持屏幕常亮                                                                                                         |

### 1.2 激励视频对象类

**WindRewardVideoAd**

| 方法                                                             | 说明                                             |
| ---------------------------------------------------------------- | ------------------------------------------------ |
| WindRewardVideoAd(WindRewardAdRequest request)                   | 初始化激励广告对象，request 为广告请求对象，必传 |
| setWindRewardVideoAdListener(WindRewardVideoAdListener listener) | 设置激励视频接口回调                             |
| load(String bid_token)                                           | 发起 Header Bidding 广告加载                     |
| load()                                                           | 发起广告加载                                     |
| isReady()                                                        | 判断当前 WindRewardVideoAd 是否存在可展示的广告  |
| show(HashMap<String,String> showOption)                          | 展示广告, showOption 为播放扩展参数              |
| destroy()                                        | 广告对象销毁，如竞价失败，需要再次竞价可以调用destroy销毁后再次进行load竞价     |

### 1.3 播放场景参数

**AD_SCENE_ID, AD_SCENE_DESC** 展示场景扩展参数说明，仅用于 show 接口

| 参数                  | 说明                 |
| --------------------- | -------------------- |
| WindAds.AD_SCENE_ID   | 视频广告播放场景 ID  |
| WindAds.AD_SCENE_DESC | 视频广告播放场景描述 |

### 1.4 奖励信息方法说明

**WindRewardInfo**

| 方法       | 说明     |
| ---------- | -------- |
| isReward() | 完成奖励 |

### 1.5 激励视频回调说明

**WindRewardVideoAdListener** 激励视频回调说明

| 回调方法                                                                | 说明                                                                               |
| ----------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| onRewardAdPreLoadSuccess (String placementId)                           | 广告填充成功，placementId 为回调广告位。收到此回调即可播放广告                     |
| onRewardAdPreLoadFail (String placementId)                              | 广告填充失败 ,placementId 为回调广告位                                             |
| onRewardAdLoadSuccess (String placementId)                              | 广告物料加载成功 ,placementId 为回调广告位                                         |
| onRewardAdLoadError (WindAdError windAdError,String placementId)        | 广告加载物料失败, windAdError 为具体错误信息，placementId 为回调广告位             |
| onRewardAdPlayStart (String placementId)                                | 广告开始展示 ,placementId 为回调广告位                                             |
| onRewardAdPlayError (WindAdError windAdError,String placementId)        | 广告开始展示失败，windAdError 为具体错误信息，placementId 为回调广告位             |
| onRewardAdPlayEnd (String placementId)                                  | 广告视频播放结束, placementId 为回调广告位                                         |
| onRewardAdClicked (String placementId)                                  | 广告被用户点击, placementId 为回调广告位                                           |
| onRewardAdRewarded (WindRewardInfo windRewardInfo,String placementId)     | 用户成功获得广告奖励, windRewardInfo 为广告奖励信息， placementId 为回调广告位     |
| onRewardAdArrived (Map&lt;String,String&gt; extInfo,String placementId) | 服务器奖励已到达, extInfo 为广告服务器奖励 extInfo 信息， placementId 为回调广告位 |
| onRewardAdClosed (String placementId)                                   | 广告关闭， placementId 为回调广告位                                                |

## 2. 示例代码

### 2.1 设置监听回调

```java
private WindRewardVideoAd windRewardVideoAd;

Map<String,String> OPTIONS = new HashMap();

//placementId 必填,USER_ID,OPTIONS可不填，
WindRewardAdRequest request=new WindRewardAdRequest(PLACEMENT_ID,USER_ID,OPTIONS);

windRewardVideoAd=new WindRewardVideoAd(request);

windRewardVideoAd.setWindRewardVideoAdListener(new WindRewardVideoAdListener(){

    //仅sigmob渠道有回调，聚合其他平台无次回调
    @Override
    public void onRewardAdPreLoadSuccess(String placementId){
            Toast.makeText(mContext,"激励视频广告数据返回成功",Toast.LENGTH_SHORT).show();
    }

    //仅sigmob渠道有回调，聚合其他平台无次回调
    @Override
    public void onRewardAdPreLoadFail(String placementId){
            Toast.makeText(mContext,"激励视频广告数据返回失败",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardAdLoadSuccess(String placementId){
            Toast.makeText(mContext,"激励视频广告缓存加载成功,可以播放",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardAdPlayStart(String placementId){
            Toast.makeText(mContext,"激励视频广告播放开始",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardAdPlayEnd(String placementId){
            Toast.makeText(mContext,"激励视频广告播放结束",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardAdClicked(String placementId){
            Toast.makeText(mContext,"激励视频广告CTA点击事件监听",Toast.LENGTH_SHORT).show();
    }

    //获得奖励回调
    @Override
    public void onRewardAdRewarded(WindRewardInfo windRewardInfo,String placementId){
            if(windRewardInfo.isReward()){
                    Toast.makeText(mContext,"激励视频广告完整播放，给予奖励",Toast.LENGTH_SHORT).show();
            }
    }
    //服务器奖励已到达回调
    @Override
    public void onRewardAdArrived(Map<String, Object>extInfo,String placementId){
            Toast.makeText(mContext,"服务器奖励已到达",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRewardAdClosed(String placementId){
            Toast.makeText(mContext,"激励视频广告关闭",Toast.LENGTH_SHORT).show();
    }

    /**
     * 加载广告错误回调
     * WindAdError 激励视频错误内容
     * placementId 广告位
     */
    @Override
    public void onRewardAdLoadError(WindAdError windAdError,String placementId){
            Toast.makeText(mContext,"激励视频广告加载错误",Toast.LENGTH_SHORT).show();
    }

    /**
     * 播放错误回调
     * WindAdError 激励视频错误内容
     * placementId 广告位
     */
    @Override
    public void onRewardAdPlayError(WindAdError windAdError,String placementId){
            Toast.makeText(mContext,"激励视频广告播放错误",Toast.LENGTH_SHORT).show();
    }
});
```

### 2.2 激励视频广告加载

```java
/**
* 同一个windRewardVideoAd不建议在广告playing中重复load
* 同一个windRewardVideoAd在onRewardAdClosed中可以load下一次广告
*/
if(windRewardVideoAd!=null){
    windRewardVideoAd.loadAd();
}
```

### 2.3 激励视频广告播放

```java
try {
  /**
   * 收到onRewardAdLoadSuccess回调代表广告已ready
   */
  if(windRewardVideoAd!=null&&windRewardVideoAd.isReady()){
    HashMap showOption=new HashMap();
    //场景ID
    showOption.put(WindAds.AD_SCENE_ID,"场景ID");
    //场景描述
    showOption.put(WindAds.AD_SCENE_DESC,"场景描述");

    //广告播放
    windRewardVideoAd.show(showOption);
  }
} catch (Exception e) {
        e.printStackTrace();
}
```
