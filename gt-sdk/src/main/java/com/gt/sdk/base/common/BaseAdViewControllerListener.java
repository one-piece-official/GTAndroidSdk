package com.gt.sdk.base.common;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public interface BaseAdViewControllerListener {
    void onSetContentView(final View view);

    void onSetRequestedOrientation(final int requestedOrientation);

    void onFinish();

    void onBackPressed();

    void onStartActivityForResult(final Class<? extends Activity> clazz,
                                  final int requestCode,
                                  final Bundle extras);
}
