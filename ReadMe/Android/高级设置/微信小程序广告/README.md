# Android SDK 支持微信小程序/小游戏跳转接入方案

## 简介

微信小程序/小游戏广告，是指用户点击广告后，将跳转至微信打开微信小程序/小游戏，在微信内部发生后续的行为转化。

### 适用场景：

适用于Sigmob当前的多种广告样式，接入后可有效提升流量的填充和CPM值。

## 整体接入流程

1. 进入微信开放平台创建移动应用
2. 应用创建完成后，在微信开放平台获取到相应的AppID
3. 在移动端嵌入最新版微信openSDK(>=5.3.1)（仅嵌入即可[openSDK 接入指南](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Launching_a_Mini_Program/Android_Development_example.html)，无需额外开发工作 ）
4. 在Sigmob开发者平台，将微信开放平台填写的AppID 与当前应用进行关联
5. 嵌入更新WindSDK到4.7.0及以上版本。

## 详细步骤

1.在[微信开放平台](https://open.weixin.qq.com/)创建应用

![alt text](http://mn.sigmob.com/supportcenter_v2/sigmob/applets/app_info.jpeg "title")

2.应用创建完成后，在微信开放平台获取相应的AppID
![alt text](http://mn.sigmob.com/supportcenter_v2/sigmob/applets/wechat_appid.jpeg "title")


3.在开发者APP内嵌入Sigmob最新版本SDK，并集成微信openSDK(>=5.3.1)。开发者仅需确认在您的应用中已经成功嵌入OpenSDK(>=5.3.1)，无需额外开发工作，后续逻辑由SDK完成。
嵌入openSDK的详细方法及资源可参考 [微信开放平台](https://developers.weixin.qq.com/doc/oplatform/Mobile_App/Launching_a_Mini_Program/Android_Development_example.html)

4.在Sigmob开发者平台内新建或者修改媒体时,将微信开放平台的AppID和Universal Links（仅iOS需要该字段，安卓应用无需填写）与媒体进行绑定。**若是编辑应用，修改微信平台 AppID 前请先联系运营人员进行审核！** 具体操作如下图所示：

![alt text](http://mn.sigmob.com/supportcenter_v2/sigmob/applets/sigmob_app_info.jpeg "title")
