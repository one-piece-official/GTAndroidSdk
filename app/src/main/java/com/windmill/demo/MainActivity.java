package com.windmill.demo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.windad.consent.ConsentFormListener;
import com.sigmob.windad.consent.ConsentStatus;
import com.sigmob.windad.consent.WindAdConsentForm;
import com.windmill.demo.splash.SplashEyeAdHolder;
import com.windmill.demo.splash.SplashZoomOutManager;
import com.windmill.demo.utils.ViewUtils;
import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillConsentStatus;
import com.windmill.sdk.splash.WMSplashEyeAdListener;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainFragment mMainFragment;
    private WindAdConsentForm form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("lance", "------------startActivity--------onCreate-------" + System.currentTimeMillis());

        createMainFragment();

        WindMillAd.requestPermission(this);

//        testJavaCrash();
    }

    public void testJavaCrash() {
        throw new SecurityException("SigMob test Crash from lance!");
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        showSplashEyeAd();

    }

    private View zoomOutView;

    private void showSplashEyeAd() {

        if (SplashEyeAdHolder.splashEyeAd == null) {
            return;
        }


        SplashEyeAdHolder.splashEyeAd.show(MainActivity.this, null, new WMSplashEyeAdListener() {
            @Override
            public void onAnimationStart(View splashView) {
                Log.i(TAG, "------------onAnimationStart---------");
                SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance(getApplicationContext());

                int[] suggestedSize = SplashEyeAdHolder.splashEyeAd.getSuggestedSize(getApplicationContext());
                if (suggestedSize != null) {
                    zoomOutManager.setSplashEyeAdViewSize(suggestedSize[0], suggestedSize[1]);
                }
                zoomOutView = zoomOutManager.startZoomOutInTwoActivity((ViewGroup) getWindow().getDecorView(), findViewById(android.R.id.content), new SplashZoomOutManager.AnimationCallBack() {

                    @Override
                    public void animationStart(int animationTime) {
                        Log.i(TAG, "------------animationStart---------");
                    }

                    @Override
                    public void animationEnd() {
                        Log.i(TAG, "------------animationEnd---------");
                        SplashEyeAdHolder.splashEyeAd.onFinished();
                    }
                });

                if (zoomOutView != null) {
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onAdDismiss(boolean isSupportEyeSplash) {
                Log.i(TAG, "------------onAdDismiss---------" + isSupportEyeSplash);
                SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance(getApplicationContext());
                zoomOutManager.clearStaticData();
                if (zoomOutView != null) {
                    ViewUtils.removeFromParent(zoomOutView);
                }
                SplashEyeAdHolder.splashEyeAd.destroy();
                SplashEyeAdHolder.splashEyeAd = null;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mMainFragment != null && intent != null) {
            String[] logs = intent.getStringArrayExtra("logs");
            mMainFragment.setLogs(logs);
        }
    }

    private void createMainFragment() {

        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_container) != null && mMainFragment == null) {

            mMainFragment = new MainFragment();
            Intent intent = getIntent();
            String[] logs = intent.getStringArrayExtra("logs");

            mMainFragment.setLogs(logs);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, mMainFragment).commit();

        }
    }

    @Override
    public void onStateNotSaved() {
        super.onStateNotSaved();
        mMainFragment.onResume();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() called " + getFragmentManager().getBackStackEntryCount());
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}