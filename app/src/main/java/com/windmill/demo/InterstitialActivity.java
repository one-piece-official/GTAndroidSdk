package com.windmill.demo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.interstitial.InterstitialAd;
import com.gt.sdk.interstitial.InterstitialAdListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InterstitialActivity extends AppCompatActivity implements InterstitialAdListener, View.OnClickListener {

    private TextView logTextView;
    private String userID;
    private LinearLayout IdLayout;
    private boolean isPlayingLoad;
    private boolean isNewInstance;
    private final Map<String, InterstitialAd> interstitialAdMap = new HashMap<>();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            String placementId;
            switch (msg.what) {
                case 0x001:
                    placementId = (String) msg.obj;
                    Log.d("lance", "-------handleMessage 0x001-------" + placementId);

                    loadAd(placementId);

//                    Message message = Message.obtain();
//                    message.what = 0x002;
//                    message.obj = placementId;
//                    handler.sendMessageDelayed(message, 3000);
                    break;
                case 0x002:
                    placementId = (String) msg.obj;
                    Log.d("lance", "-------handleMessage 0x002-------" + placementId);
                    showAd(placementId);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        IdLayout = this.findViewById(R.id.ll_placement);

        logTextView = this.findViewById(R.id.logView);
        logTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        updatePlacement();
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String text = (String) button.getText();
        String placementId = text.substring(text.indexOf("-") + 1);
        Log.d("lance", "---------onClick---------" + text);
        if (text.startsWith("LOAD")) {
            loadAd(placementId);
        } else {//SHOW
            showAd(placementId);
        }
    }

    private void loadAd(String codeId) {
        InterstitialAd interstitialAd = interstitialAdMap.get(codeId);
        Log.d("lance", (interstitialAd == null) + "---------loadAd---------" + codeId);

        Map<String, String> options = new HashMap<>();
        options.put("user_id", userID);
        AdRequest adRequest = new AdRequest.Builder()
                .setCodeId(codeId)
                .setExtOption(options)
                .build();

        if (interstitialAd != null) {
            if (isNewInstance) {
                interstitialAd.destroyAd();
                interstitialAdMap.remove(codeId);
                interstitialAd = new InterstitialAd(adRequest);
                interstitialAd.setInterstitialAdListener(this);
            }
        } else {
            interstitialAd = new InterstitialAd(adRequest, this);
//            interstitialAd.setInterstitialAdListener(this);
        }

        interstitialAd.loadAd();

        interstitialAdMap.put(codeId, interstitialAd);
    }

    private void showAd(String codeId) {
        InterstitialAd interstitialAd = interstitialAdMap.get(codeId);
        Log.d("lance", "---------showAd---------" + codeId);
        if (interstitialAd != null && interstitialAd.isReady()) {
            interstitialAd.show(this);
        } else {
            logMessage("Ad is not Ready");
            Log.d("lance", "--------请先加载广告--------");
        }
    }

    public void buttonClick(View view) {
        if (view.getId() == R.id.cleanLog_button) {
            cleanLog();
        }
    }

    private void updatePlacement() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        try {
            String adSlotId = "123456";
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            Button loadB = new Button(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 5, 0, 5);
            layoutParams.weight = 1;
            loadB.setLayoutParams(layoutParams);
            loadB.setOnClickListener(this);
            loadB.setText("LOAD-" + adSlotId);
            loadB.setTextSize(12);
            ll.addView(loadB);

            Button playB = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 5, 0, 5);
            params.weight = 1;
            playB.setLayoutParams(params);
            playB.setOnClickListener(this);
            playB.setText("PLAY-" + adSlotId);
            playB.setTextSize(12);
            ll.addView(playB);
            IdLayout.addView(ll);
        } catch (Exception e) {
            e.printStackTrace();
        }

        isPlayingLoad = sharedPreferences.getBoolean(Constants.CONF_PLAYING_LOAD, false);
        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");

        Log.d("lance", isPlayingLoad + "-----------updatePlacement-----------" + isNewInstance);
    }

    private void cleanLog() {
        logTextView.setText("");
    }

    private static SimpleDateFormat dateFormat = null;

    private static SimpleDateFormat getDateTimeFormat() {

        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss SSS", Locale.CHINA);
        }
        return dateFormat;
    }

    private void logMessage(String message) {
        Date date = new Date();
        logTextView.append(getDateTimeFormat().format(date) + " " + message + '\n');
    }

    @Override
    public void onInterstitialAdLoadSuccess(final String codeId) {
        Log.d("lance", "----------onInterstitialAdLoadSuccess----------" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadSuccess [ " + codeId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdCacheSuccess(String codeId) {
        Log.d("lance", "----------onInterstitialAdCacheSuccess----------" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdCacheSuccess [ " + codeId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPlay(String codeId) {
        Log.d("lance", "----------onInterstitialAdShow----------" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdShow [ " + codeId + " ]");
            }
        });

        if (isPlayingLoad) {
            Message message = Message.obtain();
            message.what = 0x001;
            message.obj = codeId;
            handler.sendMessage(message);
        }
    }

    @Override
    public void onInterstitialAdPLayEnd(String codeId) {
        Log.d("lance", "----------onInterstitialAdPLayEnd----------" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPLayEnd [ " + codeId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdSkip(String codeId) {
        Log.d("lance", "----------onInterstitialAdSkip----------" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdSkip [ " + codeId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdClick(String codeId) {
        Log.d("lance", "----------onInterstitialAdClick----------" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClick [ " + codeId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdClosed(String codeId) {
        Log.d("lance", "----------onInterstitialAdClosed----------" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClosed [ " + codeId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdLoadError(String codeId, AdError error) {
        Log.d("lance", "----------onInterstitialAdLoadError----------" + error.toString() + ":" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadError() called with: error = [" + error + "], placementId = [" + codeId + "]");
            }
        });
    }

    @Override
    public void onInterstitialAdShowError(String codeId, AdError error) {
        Log.d("lance", "----------onInterstitialAdShowError----------" + error.toString() + ":" + codeId);
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdShowError() called with: error = [" + error + "], placementId = [" + codeId + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (InterstitialAd interstitialAd : interstitialAdMap.values()) {
            if (interstitialAd != null) {
                interstitialAd.destroyAd();
            }
        }
    }

}