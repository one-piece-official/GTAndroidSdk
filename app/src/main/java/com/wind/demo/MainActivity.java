package com.wind.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sigmob.windad.WindAds;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private MainFragment mMainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createMainFragment();

        WindAds.requestPermission(this);

        
//        testJavaCrash();
    }

    public void testJavaCrash() {
        throw new SecurityException("SigMob test Crash from lance!");
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