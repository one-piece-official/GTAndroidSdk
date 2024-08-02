# 开屏广告集成相关

> 目前开屏广告仅支持竖屏

## 1. 接口API说明

### 1.1 开屏请求类

**WindSplashAdRequest** 

| 方法名                 | 说明                                                         |
| ---------------------  | ------------------------------------------------------------ |
| WindSplashAdRequest(String placementId, String userId, HashMap<String,String> option) | 开屏请求对象，其中PlacementId 为广告位 必传参数，userId 为用户ID，没有可以传入null， option 为加载扩展参数，没有可以传入参数 |
| setFetchDelay(int fetchDelay )                                            | 设置开屏加载时长                                                |
| setDisableAutoHideAd(boolean disableAutoHideAd )       | 禁止开屏广告展示完毕自动隐藏      亮                                                 |


### 1.2 开屏对象类

**WindSplashAD** 

| 方法                   | 说明                                                         |
| --------------------- | ------------------------------------------------------------ |
| WindSplashAD(WindSplashAdRequest  request,SplashADListener listener)             | 初始化开屏广告对象，request 为广告请求对象，必传, listener 为开屏广告接口回调 |
| loadAd()                                | 发起广告加载                          |
| loadAndShow(View adContainer)       | 发起广告加载,加载完成自动展示广告 adContainer 为展示的广告容器                                                 |
| isReady()                                       | 判断当前WindSplashAD是否存在可展示的广告                 |
| show()               | 展示广告        |
| destroy()               | 销毁广告对象, 如果不再使用需要销毁，防止可能导致的内存泄露。   |


### 1.3 开屏回调说明 

**SplashADListener**

| 回调方法                   | 说明                                                         |
| ---------------------  | ------------------------------------------------------------ |
| onSplashAdLoadSuccess(String placement) |  广告加载成功 ,placementId 为回调广告位 |
| onSplashAdLoadFail(WindAdError error, String placementId) |  广告加载失败, error 错误信息，placementId 为回调广告位  |
| onSplashAdShow(String placement) |  广告开始展示成功，placementId 为回调广告位 |
| onSplashAdShowError(WindAdError error,String placement) |  广告开始展示失败, error 为错误码，placementId 为错误信息 |
| onSplashAdSkip(String placement) |  广告被跳过，placementId 为回调广告位 |
| onSplashAdClick(String placement) |  广告被用户点击 ，placementId 为回调广告位|
| onSplashClose(String placement) |  广告关闭，placementId 为回调广告位 |


## 2. 示例代码
### 2.1 设置监听回调

```java
// 开屏广告成功展示
@Override
public void onSplashAdShow() {

}

/**
*  开屏广告成功加载
*  如果不是LoadAndShow模式,则需要调用showAd()方法展示广告
*  adContainer 开屏内容展示容器，必传，非空
*/
@Override
public void onSplashAdLoadSuccess() {
    if (!isLoadAndShow && mWindSplashAD.isReady()) {
        mWindSplashAD.showAd(adContainer);
    }
}
/**
* 开屏广告展示失败
* WindAdError 开屏广告错误内容
* placementId 广告位
*/
@Override
public void onSplashAdShowError(WindAdError error, String placementId) {
    logs.add("onSplashAdShowError: " + error + " placementId: " + placementId);
}

/**
* 开屏广告加载失败
* WindAdError 开屏广告错误内容
* placementId 广告位
*/
@Override
public void onSplashAdLoadFail(WindAdError error, String placementId) {
    //广告失败直接进入主页面
    jumpMainActivity();
}

// 开屏广告点击
@Override
public void onSplashAdClick() {

}

// 开屏广告关闭
@Override
public void onSplashClose() {
    //需要判断是否能进入主页面
    jumpWhenCanClick();
}
```


### 2.2 开屏加载及播放接口


```java
//PLACEMENT_ID必填
WindSplashAdRequest splashAdRequest = new WindSplashAdRequest(PLACEMENT_ID,USER_ID,OPTIONS);

/**
 * 广告结束，广告内容是否自动隐藏.默认是false
 * 若开屏和应用共用Activity，建议false。
 * 若开屏是单独Activity ，建议true。
*/
adRequest.setDisableAutoHideAd(true);

//广告允许最大等待返回时间
splashAdRequest.setFetchDelay(5);


/**
 * 方法:
 *   WindSplashAD(Activity activity,WindSplashAdRequest splashAdRequest, WindSplashADListener adListener)
 * 参数说明:
 *   activity 开屏展示Activity
 *   adRequest WindAdRequest广告请求
 *   adListener 开屏事件监听
*/

WindSplashAD mWindSplashAD =  new WindSplashAD(splashAdRequest,this);

/**
 * 建议默认实时加载并展示广告
*/
private boolean isLoadAndShow = true;

/**
 * adContainer 开屏内容展示容器,若传null，则默认进行全屏展示
*/
if (isLoadAndShow) {
    mWindSplashAD.loadAdAndShow(adContainer);//不需要再调用mWindSplashAD.showAd();
} else {
    mWindSplashAD.loadAd();//需要在onSplashAdSuccessLoad回调里调用mWindSplashAD.showAd();
}

```


### 2.3开屏广告点击注意事项

```java
/**
 * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
 * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
 */

public boolean canJumpImmediately = false;

private void jumpWhenCanClick() {
    if (canJumpImmediately) {
        jumpMainActivity();
    } else {
        canJumpImmediately = true;
    }
}

/**
 * 不可点击的开屏，使用该jump方法，而不是用jumpWhenCanClick
 */
private void jumpMainActivity() {
    Intent intent = new Intent(this, MainActivity.class);
    this.startActivity(intent);
    this.finish();
}

@Override
protected void onPause() {
    canJumpImmediately = false;
}

@Override
protected void onResume() {
    if (canJumpImmediately) {
        jumpWhenCanClick();
    }
    canJumpImmediately = true;
}
```