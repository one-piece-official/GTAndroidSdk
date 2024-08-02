package com.sigmob.sdk.base.mta;

import com.czhj.sdk.common.mta.DeviceContext;
import com.sigmob.sdk.SDKContext;

public final class PointEntitySigmobError extends PointEntitySigmob {

    private String error_message;


    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    @Override
    public DeviceContext getDeviceContext() {
        return SDKContext.getDeviceContext();
    }


    public static PointEntitySigmobError SigmobError(String category, int errorCode, String errorMessage) {
        PointEntitySigmobError entityError = new PointEntitySigmobError();
        entityError.setAc_type(PointType.SIGMOB_ERROR);
        entityError.setCategory(category);
        entityError.setError_code(String.valueOf(errorCode));
        entityError.setError_message(errorMessage);


        return entityError;
    }

}
