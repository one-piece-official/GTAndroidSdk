# 原生自渲染广告

## 1. 接口 API 说明

### 1.1 原生广告请求类

**WindNativeAdRequest**

| 方法名                                                                                              | 说明                                                                                                                                               |
| --------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------- |
| WindNativeAdRequest(String placementId, String userId, int ad_count, HashMap<String,String> option) | 开屏请求对象，其中 PlacementId 为广告位 必传参数，userId 为用户 ID， 没有可以传入 null，ad_count 为返回最大广告数 ； option 为加载扩展参数，非必传 |

### 1.2 原生广告加载类

**WindNativeUnifiedAd**

| 方法名                                           | 说明                                 |
| ------------------------------------------------ | ------------------------------------ |
| WindNativeUnifiedAd(WindNativeAdRequest request) | 初始化原生广告加载对象               |
| loadAd(NativeAdLoadListener listener)            | 原生广告加载,listener 为广告加载回调 |
| destroy()                                        | 原生广告加载对象销毁                 |

### 1.3 原生广告单元类

**WindNativeAdData**

| 方法名                                                                                                                                                                    | 方法介绍                                                                                                                                                                                            |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| getAdPatternType()                                                                                                                                                        | 获取广告样式 (NativeAdPatternType.NATIVE_VIDEO_AD, NativeAdPatternType.NATIVE_BIG_IMAGE_AD)                                                                                                         |
| getCTAText()                                                                                                                                                              | 获取创意按钮文案                                                                                                                                                                                    |
| getTitle()                                                                                                                                                                | 获取广告的 Tittle                                                                                                                                                                                   |
| getImageList()                                                                                                                                                            | 获取广告图片素材列表（包含图片素材url，图片宽，图片高）                                                                                                                                                                              |
| getDesc()                                                                                                                                                                 | 获取广告的描述                                                                                                                                                                                      |
| getVideoWidth()                                                                                                                                                           | 获取广告视频宽                                                                                                                                                                                      |
| getVideoHeight()                                                                                                                                                          | 获取广告视频高                                                                                                                                                                                      |
| getWidgetView(int width, int height)                                                                                                                                       | 获取广告互动组件View（摇一摇，扭一扭等）注意: 宽高最小值大于等于100显示互动组件UI及互动组件描述,小于100大于60显示互动组件UI，小于60不显示互动组件UI                                                                                                                                                                                   |
| getAdLogo()                                                                                                                                                               | 获取广告的 Logo                                                                                                                                                                                     |
| getIconUrl()                                                                                                                                                              | 获取广告的 Icon                                                                                                                                                                                     |
| startVideo()                                                                                                                                                              | 播放视频广告                                                                                                                                                                                        |
| pauseVideo()                                                                                                                                                              | 暂停视频广告                                                                                                                                                                                        |
| resumeVideo()                                                                                                                                                             | 恢复视频广告                                                                                                                                                                                        |
| stopVideo()                                                                                                                                                               | 停止视频广告                                                                                                                                                                                        |
| destroy()                                                                                                                                                                 | 销毁广告单元对象                                                                                                                                                                                    |
| getAdAppInfo()                                                                                                                                                            | 获取下载类广告六要素信息                                                                                                                                                                            |
| bindImageViews(List&lt;ImageView&gt; imageViews, int defaultImageRes)                                                                                                     | 图片渲染绑定方法。参数说明：context（展示广告的 上下文）、imageViews（需要渲染的 ImageView 集合）、defaultImageRes（图片渲染失败默认展示的资源）。                                                  |
| bindViewForInteraction(View view, List&lt;View&gt; clickableViews, List&lt;View&gt; creativeViewList, View disLikeView,NativeAdInteractionListener adInteractionListener) | 绑定广告交互的方法。参数说明：context（展示广告的 上下文）、clickableViews（可点击的 View 的列表）、creativeViewList（用于下载或者拨打电话的创意 View 集合）、clickableViews（dislike 按钮 View）。 |
| bindMediaView(ViewGroup mediaLayout,NativeADMediaListener nativeADMediaListener)                                                                                          | 绑定视频 Video 方法。参数说明：context（展示广告的 上下文）、mediaLayout（装 video 的容器）,nativeADMediaListener 视频播放监听回调对象                                                              |
| bindMediaViewWithoutAppInfo(ViewGroup mediaLayout,NativeADMediaListener nativeADMediaListener)                                                                            | 接口定义同上,但下载类六要素由开发者自己渲染并显示入口，SDK 不再显示视频下载类六要素入口                                                                                                             |
| setDislikeInteractionCallback(Activity activity, DislikeInteractionCallback dislikeInteractionCallback)                                                                   | 设置 Dislike 监听。参数说明：activity（dislike 弹窗的 acticity）、dislikeInteractionCallback（dislike 监听回调对象）。                                                                              |

### 1.4 原生下载类广告单元应用六要素（非下载类，以下信息为 null）

**注意:**
权限列表，权限列表 URL，隐私协议，隐私协议 URL，功能说明，功能说明 URL 如果部分字段为 null，则说明数据在收集中，

**AdAppInfo**

| 方法名                   | 方法介绍                              |
| ------------------------ | ------------------------------------- |
| getAppName()             | 获取应用名称                          |
| getAuthorName()          | 获取开发者名称                        |
| getVersionName()         | 获取应用版本号                        |
| getPermissions()         | 获取应用权限列表文本                  |
| getPermissionsUrl()      | 获取应用权限列表 URL，以 WebView 渲染 |
| getPrivacyAgreement()    | 获取应用隐私协议文本                  |
| getPrivacyAgreementUrl() | 获取应用隐私协议 URL,以 WebView 渲染  |
| getDescription()         | 获取应用功能介绍文本                  |
| getDescriptionUrl()      | 获取应用功能介绍 URL,以 WebView 渲染  |
| getAppSize()             | 获取应用大小                          |

## 2. 原生回调说明

### 2.1 原生广告加载回调

**NativeAdLoadListener**

| 方法名                                                             | 方法介绍                                                                  |
| ------------------------------------------------------------------ | ------------------------------------------------------------------------- |
| onAdLoad(list&lt;WindNativeAdData&gt; adUnits, String placementId) | 广告加载成功。                                                            |
| onError(WindAdError error)                                         | 广告加载失败。参数说明：error（报错信息，具体可看其内部 code 和 message） |

### 2.2 交互曝光回调

**NativeAdInteractionListener**

| 方法名                       | 方法介绍                                                                    |
| ---------------------------- | --------------------------------------------------------------------------- |
| onADExposed()                | 广告曝光。                                                                  |
| onADError(WindAdError error) | 广告展示失败。参数说明：error（报错信息，具体可看其内部 code 和 message）。 |
| onADClicked()                | 广告点击。                                                                  |

### 2.3 原生视频播放回调

**NativeADMediaListener**

| 方法名                            | 方法介绍                                                                    |
| --------------------------------- | --------------------------------------------------------------------------- |
| onVideoLoad()                     | 视频加载成功。                                                              |
| onVideoError(WindMillError error) | 视频播放失败。参数说明：error（报错信息，具体可看其内部 code 和 message）。 |
| onVideoStart()                    | 视频开始播放。                                                              |
| onVideoPause()                    | 视频暂停播放。                                                              |
| onVideoResume()                   | 视频恢复播放。                                                              |
| onVideoCompleted()                | 视频完成播放。                                                              |

### 2.4 原生广告反馈回调

**DislikeInteractionCallback**

| 方法名                                                  | 方法介绍                                                                                                           |
| ------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| onShow()                                                | Dislike 弹窗显示。                                                                                                 |
| onSelected(int position, String value, boolean enforce) | Dislike 弹窗 Item 点击。参数说明：position（点击的位置）、value（点击的位置的文字选项）、enforce（是否关闭广告）。 |
| onCancel()                                              | Dislike 弹窗取消。                                                                                                 |

## 3. 示例代码

**注意:**
原生自渲染广告具体实现可参考 Demo 中 NativeAdUnifiedActivity、NativeAdUnifiedListActivity、NativeAdUnifiedRecycleActivity 等

### 3.1 原生自渲染广告加载

```java
        private ViewGroup adContainer;

        private WindNativeUnifiedAd windNativeUnifiedAd;

        private List<NativeADData> unifiedADDataList;


        WindNativeAdRequest windNativeAdRequest=new WindNativeAdRequest(placementId,String.valueOf(userID),3,options);

        windNativeUnifiedAd=new WindNativeUnifiedAd(windNativeAdRequest);

        windNativeUnifiedAd.loadAd(new WindNativeUnifiedAd.NativeAdLoadListener(){
                        
                //广告加载失败
                @Override
                public void onError(WindAdError error,String placementId){

                }

                //广告加载成功
                @Override
                public void onAdLoad(list<WindNativeAdData> adUnits,String placementId){

                }
        });
```

### 3.2 原生自渲染广告展示

```java
/**
 * 请在收到onFeedAdLoad回调后再展示广告
 */

     adData.bindViewForInteraction(nativeAdView,clickableViews,creativeViewList,img_dislike,new NativeADEventListener(){

                @Override
                public void onADExposed(){
                        Log.d(TAG,"onADExposed: ");
                }

                @Override
                public void onADClicked(){
                        Log.d(TAG,"onADClicked: ");
                }

                @Override
                public void onADError(WindAdError error){
                        Log.d(TAG,"onADError error code :"+error.toString());
                }

        });

        if(adData.getAdPatternType()==NativeAdPatternType.NATIVE_VIDEO_AD){
                adData.bindMediaView(mMediaViewLayout,new WindNativeAdData.NativeADMediaListener(){

                        @Override
                        public void onVideoStart(){
                                Log.d(TAG,"onVideoStart: ");
                        }

                        @Override
                        public void onVideoPause(){
                                Log.d(TAG,"onVideoPause: ");
                        }

                        @Override
                        public void onVideoResume(){
                                Log.d(TAG,"onVideoResume: ");
                        }

                        @Override
                        public void onVideoCompleted(){
                                Log.d(TAG,"onVideoCompleted: ");
                        }

                        @Override
                        public void onVideoError(WindAdError windAdError){
                                Log.d(TAG,"onVideoError: "+windAdError.toString());
                        }

                        @Override
                        public void onVideoLoad(){
                                Log.d(TAG,"onVideoLoad: ");
                        }
                });
        }else if(adData.getAdPatternType()==NativeAdPatternType.NATIVE_BIG_IMAGE_AD){

                adData.bindImageViews(imageViews,0);
                
        }



```

### 3.3. 原生自渲染广告销毁

```java
        //原生广告单元的销毁
        if(unifiedADDataList!=null&&unifiedADDataList.size()>0){
                        for(NativeADData ad:unifiedADDataList){
                                if(ad!=null){
                                        ad.destroy();
                                }
                        }
        }

        //原生请求广告对象的销毁
        if(windNativeUnifiedAd!=null){

                 windNativeUnifiedAd.destroy();
                 
        }
```
