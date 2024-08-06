package com.gt.sdk.base.point;

import android.text.TextUtils;

public final class GtPointEntityError extends GtPointEntity {

    private String error_message;
    private String error_code;

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public static GtPointEntityError WindError(String category, String event_type, int errorCode, String errorMessage) {
        GtPointEntityError entityError = new GtPointEntityError();
        entityError.setAc_type(PointType.GT_ERROR);
        entityError.setCategory(category);
        entityError.setEvent_type(event_type);
        entityError.setError_code(String.valueOf(errorCode));
        if (!TextUtils.isEmpty(errorMessage)) {
            entityError.setError_message(errorMessage);
        }
        return entityError;
    }

}
