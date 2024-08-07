package com.gt.sdk;

public enum AdError {

    ERROR_LOAD_FILTER_FOR_PROVIDER_ERROR(600201, "Provider声明错误,请检查manifest文件"),

    ERROR_LOAD_FILTER_FOR_PROVIDER_XML_ERROR(600200, "Provider的Xml配置错误,请检查Xml文件内容"),

    ERROR_LOAD_FILTER_FOR_PROGUARD_ERROR(600202, "SDK混淆配置错误"),

    ERROR_LOAD_FILTER_FOR_ACTIVITY_ERROR(600203, "AdActivity未声明,请检查manifest文件"),

    ERROR_LOAD_FILTER_FOR_INSTALL_PERMISSION_ERROR(600204, " 缺少REQUEST_INSTALL_PACKAGES,请检查manifest文件"),

    ERROR_AD_LOAD_FAIL_LOADING(610012, "广告正在加载中，请稍后再加载"), // 广告在加载间隔中加载

    ERROR_AD_LOAD_FAIL_INTERVAL(610013, "广告加载频繁，请稍后在试"),

    ERROR_AD_PERSONALIZED_OFF(600400, "广告个性化开关被关闭，请开启个性化开关"), //网络出错

    ERROR_AD_NETWORK(600100, "网络错误"),

    //请求出错
    ERROR_AD_REQUEST(600101, "广告请求出错"),

    //文件下载错误
    ERROR_AD_FILE_DOWNLOAD(600104, "文件下载错误"),

    ERROR_AD_BAD_REQUEST(600150, "内部错误，请联系运营或技术人员"),

    //下载广告超时
    ERROR_AD_DOWNLOAD_TIME_OUT(600105, "下载广告超时"),

    ERROR_AD_PLAY_VIDEO(600106, "视频播放失败"),

    ERROR_AD_NOT_INIT(600900, "SDK未初始化"),

    ERROR_AD_PLACEMENT_ID_EMPTY(600901, "广告位为空"),

    ERROR_AD_INSTALL_FAIL(600903, "安装失败"),

    ERROR_AD_DB_INSERT(600904, "插入数据库失败"),

    ERROR_AD_GDPR_DENIED(600905, "GDPR 未授权"),

    ERROR_AD_INIT_FAIL(600906, "SDK 初始化失败"),

    ERROR_AD_CONTAINER_IS_NULL(600907, "广告容器不能为NULL"),

    ERROR_OTHER(600999, "未知错误"),

    //激励视频播放出错
    ERROR_AD_PLAY(610002, "广告播放出错"),

    //激励视频未准备好
    ERROR_AD_NOT_READY(610003, "开屏广告未准备好"),

    //server下发的广告信息缺失关键信息
    ERROR_AD_INFORMATION_LOSE(610004, "server下发的广告信息缺失关键信息"),

    //下载的文件校验md5出错
    ERROR_AD_FILE_MD5(610005, "下载的文件校验md5出错"),

    ERROR_AD_VIDEO_FILE(610008, "下载的视频文件出错"),

    //开屏广告加载超时
    ERROR_AD_LOAD_TIMEOUT(620001, "广告加载超时"),

    //开屏广告不支持当前方向
    ERROR_AD_UN_SUPPORT_ORIENTATION(620002, "广告不支持当前方向"),

    ERROR_SPLASH_ADBLOCK(620900, "AD BLOCK"),

    RENDER_PROCESS_GONE_WITH_CRASH(620901, "RENDER PROCESS GONE WITH CRASH"),

    RENDER_PROCESS_GONE_UNSPECIFIED(620902, "RENDER PROCESS GONE UNSPECIFIED"),

    VIDEO_CACHE_ERROR(620904, "VIDEO CACHE ERROR"),

    ERROR_NO_AD(200000, "无广告填充");

    private int errorCode;
    private String message;


    AdError(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public static AdError getAdError(int error_code) {
        for (AdError error : AdError.values()) {
            if (error.getErrorCode() == error_code) {
                return error;
            }
        }
        return null;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorMessage(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("{ \"error_code\":%d, \"message\": %s }", this.errorCode, this.message);
    }
}
