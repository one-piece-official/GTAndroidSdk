package com.sigmob.sigmob.rename;


import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sigmob.sigmob.R;
import com.xmlywind.windad.WindAdError;
import com.xmlywind.windad.WindAds;
import com.xmlywind.windad.rewardedVideo.WindRewardAdRequest;
import com.xmlywind.windad.rewardedVideo.WindRewardInfo;
import com.xmlywind.windad.rewardedVideo.WindRewardedVideoAd;
import com.xmlywind.windad.rewardedVideo.WindRewardedVideoAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.sigmob.sigmob.Constants.CONF_JSON;

public class RewardRenameActivity extends AppCompatActivity implements WindRewardedVideoAdListener {

    private Button loadAdBtn;
    private Button loadAdBtn2;
    private Button playAdBtn;
    private Button playAdBtn2;
    private TextView logTextView;
    private WindRewardedVideoAd windRewardedVideoAd1;
    private WindRewardedVideoAd windRewardedVideoAd2;
    private String placementId;
    private String placementId2;
    private int userID = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("WindSDK", "RewardVideoActivity handleMessage:" + msg.what);
            switch (msg.what) {
                case 0x001:
                    if (windRewardedVideoAd1 != null) {
                        windRewardedVideoAd1.loadAd();
                    }
//                    handler.sendEmptyMessageDelayed(0x002, 3000);
                    break;
                case 0x002:
                    playAd();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_rename);

        loadAdBtn = this.findViewById(R.id.loadAd_button);
        loadAdBtn2 = this.findViewById(R.id.loadAd_button2);
        playAdBtn = this.findViewById(R.id.playAd_button);
        playAdBtn2 = this.findViewById(R.id.playAd_button2);

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

        windRewardedVideoAd1 = new WindRewardedVideoAd(this, new WindRewardAdRequest(placementId, String.valueOf(userID), null));
        windRewardedVideoAd1.setWindRewardedVideoAdListener(new WindRewardedVideoAdListener() {
            @Override
            public void onVideoAdLoadSuccess(final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdLoadSuccessX [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPreLoadSuccess(final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPreLoadSuccessX [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPreLoadFail(final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPreLoadFailX [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPlayEnd(final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPlayEndX [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdPlayStart(final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPlayStartX [ " + placementId + " ]");
                    }
                });
//                handler.sendEmptyMessage(0x001);
            }

            @Override
            public void onVideoAdClicked(final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdClickedX [ " + placementId + " ]");
                    }
                });
            }

            @Override
            public void onVideoAdClosed(final WindRewardInfo info, final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdClosedX() called with: info = [" + info + "], placementId = [" + placementId + "]");
                    }
                });
            }


            @Override
            public void onVideoAdLoadError(final WindAdError error, final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdLoadErrorX() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }

            @Override
            public void onVideoAdPlayError(final WindAdError error, final String placementId) {
                RewardRenameActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onVideoAdPlayErrorX() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }
        });
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.loadAd_button:
                if (windRewardedVideoAd1 != null) {
                    windRewardedVideoAd1.loadAd();
                }
                break;
            case R.id.loadAd_button2:
//                if (windRewardedVideoAd2 != null) {
//                    windRewardedVideoAd2.loadAd();
//                }
                userID++;
                Map<String, Object> options = new HashMap<>();
                options.put("user_id", String.valueOf(userID));
                windRewardedVideoAd2 = new WindRewardedVideoAd(this, new WindRewardAdRequest(placementId2, String.valueOf(userID), options));
                windRewardedVideoAd2.setWindRewardedVideoAdListener(this);
                windRewardedVideoAd2.loadAd();
                break;
            case R.id.playAd_button:
                playAd();
//                handler.sendEmptyMessage(0x001);
//                handler.sendEmptyMessageDelayed(0x001, 3000);
                break;
            case R.id.playAd_button2:
                playAd2();
                break;
            case R.id.cleanLog_button:
                cleanLog();
                break;
        }
    }

    private void updatePlacement() {

        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", 0);

        String configJson = sharedPreferences.getString(CONF_JSON, "");

        if (!TextUtils.isEmpty(configJson)) {
            try {
                JSONObject jsonObject = new JSONObject(configJson);
                JSONObject dataJson = jsonObject.getJSONObject("data");
                JSONArray array = dataJson.optJSONArray("slotIds");
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject slotId = array.getJSONObject(i);
                        if (slotId != null) {
                            int ad_type = slotId.optInt("adType", -1);
                            int bidType = slotId.optInt("bidType", -1);
                            if (ad_type == 1 && bidType == 0) {
                                if (TextUtils.isEmpty(placementId)) {
                                    placementId = slotId.optString("adSlotId");
                                } else if (TextUtils.isEmpty(placementId2)) {
                                    placementId2 = slotId.optString("adSlotId");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        loadAdBtn.setText("load  " + placementId);
        loadAdBtn2.setText("load " + placementId2);
        playAdBtn.setText("play " + placementId);
        playAdBtn2.setText("play " + placementId2);
        Toast.makeText(this, "updatePlacement", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void cleanLog() {
        logTextView.setText("");
    }

    private void playAd() {
        HashMap option = new HashMap();
        option.put(WindAds.AD_SCENE, "menu_1");
        Log.d("WindSDK", "==========playAd=========" + windRewardedVideoAd1.isReady());
        if (windRewardedVideoAd1 != null) {
            windRewardedVideoAd1.show(option);
        }
    }

    private void playAd2() {
        HashMap option = new HashMap();
        option.put(WindAds.AD_SCENE, "menu_2");
        if (windRewardedVideoAd2 != null && windRewardedVideoAd2.isReady()) {
            windRewardedVideoAd2.show(option);
        } else {
            logMessage("Ad is not Ready");
        }
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
    public void onVideoAdLoadSuccess(final String placementId) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdLoadSuccessX2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onVideoAdPreLoadSuccess(final String placementId) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPreLoadSuccessX2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onVideoAdPreLoadFail(final String placementId) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPreLoadFailX2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onVideoAdPlayEnd(final String placementId) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPlayEndX2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onVideoAdPlayError(final WindAdError windAdError, final String s) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPlayErrorX2() called with: error = [" + windAdError + "], placementId = [" + s + "]");
            }
        });
    }

    @Override
    public void onVideoAdPlayStart(final String placementId) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdPlayStartX2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onVideoAdClicked(final String placementId) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdClickedX2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onVideoAdClosed(final WindRewardInfo windRewardInfo, final String s) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdClosedX2() called with: info = [" + windRewardInfo + "], placementId = [" + s + "]");
            }
        });
    }

    @Override
    public void onVideoAdLoadError(final WindAdError windAdError, final String s) {
        RewardRenameActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onVideoAdLoadErrorX2() called with: error = [" + windAdError + "], placementId = [" + s + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (windRewardedVideoAd1 != null) {
            windRewardedVideoAd1.destroy();
            windRewardedVideoAd1 = null;
        }
        if (windRewardedVideoAd2 != null) {
            windRewardedVideoAd2.destroy();
            windRewardedVideoAd2 = null;
        }
    }
}