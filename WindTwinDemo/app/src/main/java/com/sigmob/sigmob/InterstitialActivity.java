package com.sigmob.sigmob;

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


import androidx.appcompat.app.AppCompatActivity;

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.interstitial.WindInterstitialAd;
import com.sigmob.windad.interstitial.WindInterstitialAdListener;
import com.sigmob.windad.interstitial.WindInterstitialAdRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.sigmob.sigmob.Constants.CONF_JSON;

public class InterstitialActivity extends AppCompatActivity implements WindInterstitialAdListener {

    private TextView logTextView;
    private Button loadAdBtn3;
    private Button loadAdBtn4;
    private Button playAdBtn3;
    private Button playAdBtn4;
    private WindInterstitialAd windInterstitialAd1;
    private WindInterstitialAd windInterstitialAd2;
    private String placementId3;
    private String placementId4;
    private int userID = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("WindSDK", "handleMessage:" + msg.what);
            switch (msg.what) {
                case 0x001:
                    if (windInterstitialAd1 != null) {
                        windInterstitialAd1.loadAd();
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
        setContentView(R.layout.activity_interstitial);

        loadAdBtn3 = this.findViewById(R.id.loadAd_button);
        loadAdBtn4 = this.findViewById(R.id.loadAd_button2);
        playAdBtn3 = this.findViewById(R.id.playAd_button);
        playAdBtn4 = this.findViewById(R.id.playAd_button2);

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

        windInterstitialAd2 = new WindInterstitialAd(this, new WindInterstitialAdRequest(placementId4, String.valueOf(userID), null));
        windInterstitialAd2.setWindInterstitialAdListener(this);

        windInterstitialAd1 = new WindInterstitialAd(this, new WindInterstitialAdRequest(placementId3, String.valueOf(userID), null));
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
                handler.sendEmptyMessage(0x001);
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
            case R.id.loadAd_button2:
                if (windInterstitialAd2 != null) {
                    windInterstitialAd2.loadAd();
                }
                break;
            case R.id.playAd_button:
                playAd();
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
                            if (ad_type == 4 && bidType == 0) {
                                if (TextUtils.isEmpty(placementId3)) {
                                    placementId3 = slotId.optString("adSlotId");
                                } else if (TextUtils.isEmpty(placementId4)) {
                                    placementId4 = slotId.optString("adSlotId");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        loadAdBtn3.setText("load  " + placementId3);
        loadAdBtn4.setText("load " + placementId4);
        playAdBtn3.setText("play " + placementId3);
        playAdBtn4.setText("play " + placementId4);
        Toast.makeText(this, "updatePlacement", Toast.LENGTH_SHORT).show();
    }

    private void cleanLog() {
        logTextView.setText("");
    }

    private void playAd() {
        HashMap option = new HashMap();
        option.put(WindAds.AD_SCENE, "menu_1");
        Log.d("WindSDK", "==========playAd=========" + windInterstitialAd1.isReady());
        if (windInterstitialAd1 != null) {
            windInterstitialAd1.show(option);
        }
    }

    private void playAd2() {
        HashMap option = new HashMap();
        option.put(WindAds.AD_SCENE, "menu_2");
        if (windInterstitialAd2 != null && windInterstitialAd2.isReady()) {
            windInterstitialAd2.show(option);
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
    public void onInterstitialAdLoadSuccess(final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadSuccess2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPreLoadSuccess(final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPreLoadSuccess2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPreLoadFail(final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPreLoadFail2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPlayStart(final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPlayStart2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdPlayEnd(final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPlayEnd2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdClicked(final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClicked2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdClosed(final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdClosed2 [ " + placementId + " ]");
            }
        });
    }

    @Override
    public void onInterstitialAdLoadError(final WindAdError error, final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdLoadError2() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    public void onInterstitialAdPlayError(final WindAdError error, final String placementId) {
        InterstitialActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logMessage("onInterstitialAdPlayError2() called with: error = [" + error + "], placementId = [" + placementId + "]");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (windInterstitialAd1 != null) {
            windInterstitialAd1.destroy();
            windInterstitialAd1 = null;
        }
        if (windInterstitialAd2 != null) {
            windInterstitialAd2.destroy();
            windInterstitialAd2 = null;
        }
    }
}