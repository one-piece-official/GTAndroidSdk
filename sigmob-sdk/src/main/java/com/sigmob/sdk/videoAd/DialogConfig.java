package com.sigmob.sdk.videoAd;

public class DialogConfig {
   private String title;
   private String context;
   private String cancel;
   private String close;

    public DialogConfig(String title, String context, String cancel, String close) {
        this.title = title;
        this.context = context;
        this.cancel = cancel;
        this.close = close;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getCancel() {
        return cancel;
    }

    public void setCancel(String cancel) {
        this.cancel = cancel;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    @Override
    public String toString() {
        return "DialogConfig{" +
                "title='" + title + '\'' +
                ", context='" + context + '\'' +
                ", cancelTxt='" + cancel + '\'' +
                ", closeTxtl='" + close + '\'' +
                '}';
    }
}


