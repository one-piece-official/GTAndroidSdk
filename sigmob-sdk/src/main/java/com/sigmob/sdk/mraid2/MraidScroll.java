package com.sigmob.sdk.mraid2;

import android.view.ViewGroup;

/**
 * created by lance on   2022/7/12 : 4:34 下午
 */
public interface MraidScroll {

    ViewGroup getView();

    void onTouchStart(int x, int y);

    void onTouchMove(int x, int y);

    void onTouchEnd(Mraid2WebView view, int x, int y);

    void setPageChangedListener(Mraid2Bridge.PageChangedListener pageChangedListener);
}
