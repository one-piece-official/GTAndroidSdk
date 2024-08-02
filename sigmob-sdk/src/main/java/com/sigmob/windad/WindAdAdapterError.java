package com.sigmob.windad;

public class WindAdAdapterError {

    int errorCode;
    String message;

    public WindAdAdapterError(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    @Override
    public String toString() {
        return "{" +
                "errorCode:" + errorCode +
                ", message:'" + message + '\'' +
                '}';
    }
}
