package com.gt.sdk.natives;

public interface SigVideoAdController {

    void setMute(boolean isMute);

    void pause();

    void resume();

    void start();

    void stop();

    void setAdVideoStatusListener(SigAdVideoStatusListener adVideoStatusListener);

    int getVideoDuration();

    int getVideoProgress();

    SigAdVideoStatusListener getSigAdVideoStatusListener();

    void startPreloading();

    void destroy();

    int getCurrentPosition();

}
