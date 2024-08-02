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
import com.sigmob.windad.rewardedVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardedVideo.WindRewardInfo;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAd;
import com.sigmob.windad.rewardedVideo.WindRewardedVideoAdListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RewardVideoActivity extends AppCompatActivity {
    private Button loadAdBtn;
    private Button playAdBtn;
    private TextView logTextView;
    private WindRewardedVideoAd windRewardedVideoAd;
    private String placementId;
    private String userID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_video);

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
        windRewardedVideoAd = new WindRewardedVideoAd(new WindRewardAdRequest(placementId, userID, options));
        windRewardedVideoAd.setWindRewardedVideoAdListener(new WindRewardedVideoAdListener() {
            @Override
            public void onVideoAdLoadSuccess(final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdLoadSuccess [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPreLoadSuccess(final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPreLoadSuccess [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPreLoadFail(final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPreLoadFail [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPlayEnd(final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPlayEnd [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPlayStart(final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPlayStart [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdClicked(final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdClicked [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdClosed(final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdClosed [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdReward(final WindRewardInfo windRewardInfo, final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdReward() called with: info = [" + windRewardInfo + "], placementId = [" + placementId + "]");
                    }
                });
            }



            @Override
            public void onVideoAdLoadError(final WindAdError error, final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdLoadError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }

            @Override
            public void onVideoAdPlayError(final WindAdError error, final String placementId) {
                RewardVideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPlayError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }
        });
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.loadAd_button:
                if (windRewardedVideoAd != null) {
                    windRewardedVideoAd.loadAd();
                }
                break;
            case R.id.playAd_button:
                HashMap option = new HashMap();
                option.put(WindAds.AD_SCENE_DESC, "menu_1");
                if (windRewardedVideoAd != null && windRewardedVideoAd.isReady()) {
                    windRewardedVideoAd.show( option);
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

        placementId = sharedPreferences.getString(Constants.CONF_REWARD_PLACEMENTID, Constants.reward_placement_id);
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
        if (windRewardedVideoAd != null) {
            windRewardedVideoAd.destroy();
            windRewardedVideoAd = null;
        }
    }
}