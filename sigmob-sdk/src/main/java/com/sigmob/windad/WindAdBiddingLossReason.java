package com.sigmob.windad;

/**
 * created by lance on   2022/8/18 : 5:31 下午
 */
public enum WindAdBiddingLossReason {

    LOSS_REASON_LOW_PRICE(2, "出价过低"),

    LOSS_REASON_RETURN_TIMEOUT(2000, "返回超时"),

    LOSS_REASON_RETURN_ERROR(2001, "广告素材格式错误");

    private int code;

    private String message;

    WindAdBiddingLossReason(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
