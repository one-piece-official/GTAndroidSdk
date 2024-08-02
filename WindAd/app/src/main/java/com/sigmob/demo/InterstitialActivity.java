package com.sigmob.demo;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.interstitial.WindInterstitialAd;
import com.sigmob.windad.interstitial.WindInterstitialAdListener;
import com.sigmob.windad.interstitial.WindInterstitialAdRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class InterstitialActivity extends AppCompatActivity {

    private TextView logTextView;
    private Button loadAdBtn;
    private Button playAdBtn;
    private WindInterstitialAd windInterstitialAd1;
    private String placementId;
    private String userID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interstitial);

        loadAdBtn = this.findViewById(R.id.loadAd_button);
        playAdBtn = this.findViewById(R.id.playAd_button);

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

        Map<String, Object> options = new HashMap<>();
        options.put("WindSDK", String.valueOf(userID));
        windInterstitialAd1 = new WindInterstitialAd( new WindInterstitialAdRequest(placementId, userID, options));
        windInterstitialAd1.setWindInterstitialAdListener(new WindInterstitialAdListener() {
            @Override
            public void onInterstitialAdLoadSuccess(final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdLoadSuccess [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onInterstitialAdPreLoadSuccess(final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdPreLoadSuccess [ " + placementId + " ]");
                    }
                });

            }

            @Override
            public void onInterstitialAdPreLoadFail(final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdPreLoadFail [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onInterstitialAdPlayStart(final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdPlayStart [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onInterstitialAdPlayEnd(final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdPlayEnd [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onInterstitialAdClicked(final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdClicked [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onInterstitialAdClosed(final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdClosed [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onInterstitialAdLoadError(final WindAdError error, final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdLoadError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }

            @Override
            public void onInterstitialAdPlayError(final WindAdError error, final String placementId) {
                InterstitialActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onInterstitialAdPlayError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }
        });
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.loadAd_button:
                if (windInterstitialAd1 != null) {
                    windInterstitialAd1.loadAd();
                }
                break;
            case R.id.playAd_button:
                HashMap option = new HashMap();
                option.put(WindAds.AD_SCENE_DESC, "menu_1");
                if (windInterstitialAd1 != null && windInterstitialAd1.isReady()) {
                    windInterstitialAd1.show( option);
                } else {
                    logMessage("Ad is not Ready");
                }
                break;
            case R.id.cleanLog_button:
                cleanLog();
                break;
        }
    }

    private void updatePlacement() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);
        placementId = sharedPreferences.getString(Constants.CONF_FULLSCREEN_PLACEMENTID, Constants.fullScreen_placement_id);
        userID = sharedPreferences.getString(Constants.CONF_USERID, "");

        loadAdBtn.setText("load  " + placementId);
        playAdBtn.setText("play " + placementId);
    }

    @Override
    public void onResume() {
        super.onResume();
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
    protected void onDestroy() {
        super.onDestroy();
        if (windInterstitialAd1 != null) {
            windInterstitialAd1.destroy();
            windInterstitialAd1 = null;
        }

    }
}