package com.wind.demo.natives;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.natives.NativeADEventListener;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;
import com.wind.demo.Constants;
import com.wind.demo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NativeAdUnifiedFragment extends Fragment implements View.OnClickListener {

    private ViewGroup adContainer;
    private TextView logTextView;
    private LinearLayout IdLayout;
    private boolean isNewInstance;
    private int widget_width;
    private int widget_height;

    private List<String> placementIds = new ArrayList<>();


    private Map<String, WindNativeUnifiedAd> nativeUnifiedAdMap = new HashMap<>();
    private Map<String, List<WindNativeAdData>> unifiedADDataMap = new HashMap<>();
    private boolean show_without_appinfo;
    private NativeAdDemoRender adRender;
    private int adWidth;

    //如果是沉浸式的，全屏前就没有状态栏
    public static void hideStatusBar(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    public static void hideSystemUI(Activity context) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        context.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }


    private int dipsToPixels(Context context, float dips) {
        Resources resources = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, resources.getDisplayMetrics());
        return (int) (px+0.5f);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (WindNativeUnifiedAd ad : nativeUnifiedAdMap.values()) {
            ad.destroy();
        }

        for (List<WindNativeAdData> ads : unifiedADDataMap.values()) {
            for (WindNativeAdData data : ads) {
                data.destroy();
            }
        }
    }


    @Override
    public View onCreateView( LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_native_ad_unified, container, false);


        IdLayout = view.findViewById(R.id.ll_placement);

        adContainer = view.findViewById(R.id.native_ad_container);

        View viewById = view.findViewById(R.id.load_all_btn);
        viewById.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                for (String placementId : placementIds) {
                    loadAd(placementId);
                }
            }
        });

        SeekBar seekBar_w = view.findViewById(R.id.widget_width);
        SeekBar seekBar_h = view.findViewById(R.id.widget_height);

        TextView width_et = view.findViewById(R.id.widget_width_value);
        TextView height_et = view.findViewById(R.id.widget_height_value);

        Button button = view.findViewById(R.id.widget_size_update);
        seekBar_w.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                width_et.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar_h.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                height_et.setText(String.valueOf(i));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        button.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {

                try {
                    widget_width = width_et.getText().toString() != null ? Integer.parseInt(width_et.getText().toString()) : 60;
                    widget_height = height_et.getText().toString() != null ? Integer.parseInt(height_et.getText().toString()) : 60;

                }catch (Throwable t){
                    widget_width = 60;
                    widget_height = 60;
                }
                if (adRender != null){
                    View widgetView = adRender.getWidgetView();
                    if (widgetView != null) {
                        ViewGroup.LayoutParams layoutParams = widgetView.getLayoutParams();

                        layoutParams.width = dipsToPixels(getContext(), widget_width);
                        layoutParams.height = dipsToPixels(getContext(), widget_height);
                        widgetView.setLayoutParams(layoutParams);
                    }
                }

            }
        });
        logTextView = view.findViewById(R.id.logView);
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

        view.findViewById(R.id.notice_win_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view);
            }
        });
        view.findViewById(R.id.notice_loss_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view);
            }
        });
        view.findViewById(R.id.cleanLog_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view);
            }
        });
        view.findViewById(R.id.native_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view);
            }
        });
        view.findViewById(R.id.native_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClick(view);
            }
        });
        adWidth = screenWidthAsIntDips(getContext()) - 20;
        return view;
    }

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        hideSystemUI(this);
////        hideStatusBar(this);
//        setContentView(R.layout.fragment_native_ad_unified);
//
//        IdLayout = this.findViewById(R.id.ll_placement);
//
//        adContainer = findViewById(R.id.native_ad_container);
//
//        logTextView = this.findViewById(R.id.logView);
//        logTextView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return true;
//            }
//        });
//        logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//        }
//        updatePlacement();
//
//        adWidth = screenWidthAsIntDips(this) - 20;
//
//        Log.d("WindSDK", "---------screenWidthAsIntDips---------" + adWidth);
//    }

    public static int screenWidthAsIntDips(Context context) {
        int pixels = context.getResources().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        return (int) ((pixels / density) + 0.5f);
    }


    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        String text = button.getText().toString();
        String placementId = text.substring(text.indexOf("-") + 1);
        Log.d("WindSDK", "---------onClick---------" + text);
        if (text.startsWith("LOAD")) {
            loadAd(placementId);
        } else {//SHOW
            showAd(placementId);
        }
    }

    private void loadAd(final String placementId) {
        WindNativeUnifiedAd nativeUnifiedAd = nativeUnifiedAdMap.get(placementId);
        Log.d("WindSDK", (nativeUnifiedAd == null) + "---------loadAd---------" + placementId);
        Map<String, Object> options = new HashMap<>();
        options.put("user_id", Constants.user_id);
        if (nativeUnifiedAd != null) {
            if (isNewInstance) {
                nativeUnifiedAd.destroy();
                nativeUnifiedAdMap.remove(placementId);
                List<WindNativeAdData> windNativeAdData = unifiedADDataMap.get(placementId);
                for (WindNativeAdData data: windNativeAdData) {
                    data.destroy();
                }
                unifiedADDataMap.remove(placementId);
                nativeUnifiedAd = new WindNativeUnifiedAd(new WindNativeAdRequest(placementId, Constants.user_id, Constants.ad_count, options));
            }
        } else {
            nativeUnifiedAd = new WindNativeUnifiedAd(new WindNativeAdRequest(placementId, Constants.user_id, Constants.ad_count, options));

        }

        nativeUnifiedAd.setNativeAdLoadListener(new WindNativeUnifiedAd.WindNativeAdLoadListener() {
            @Override
            public void onAdError(final WindAdError error, final String placementId) {
                Log.d("WindSDK", "----------onAdError----------:" + error.toString() + ":" + placementId);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                    }
                });
            }

            @Override
            public void onAdLoad(List<WindNativeAdData> list, String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logMessage("onFeedAdLoad [ " + placementId + " ]");
                        for (WindNativeAdData data: list) {
                            logMessage("video width "+ data.getVideoWidth() + " height "+ data.getVideoHeight());
                        }
                    }

                });
                for (List<WindNativeAdData> adDataList : unifiedADDataMap.values()) {
                    if (adDataList != null && adDataList.size() > 0) {
                        for (WindNativeAdData ad : adDataList) {
                            if (ad != null) {
                                ad.destroy();
                            }
                        }
                    }
                }


                if (list != null && list.size() > 0) {
                    Log.d("WindSDK", "----------onFeedAdLoad----------" + list.size());
                    unifiedADDataMap.put(placementId, list);
                }
            }


        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("setting", 0);
        String BID_DJ = sharedPreferences.getString(Constants.BID_DJ, "-1");
        String BID_BZ = sharedPreferences.getString(Constants.BID_BZ, "CNY");

        nativeUnifiedAd.setBidFloor(Integer.parseInt(BID_DJ));
        nativeUnifiedAd.setCurrency(BID_BZ);

        nativeUnifiedAd.loadAd();
        nativeUnifiedAdMap.put(placementId, nativeUnifiedAd);
    }

    private void showAd(final String placementId) {
        Log.d("WindSDK", "---------showAd---------" + placementId);
        WindNativeUnifiedAd nativeUnifiedAd = nativeUnifiedAdMap.get(placementId);
        if (nativeUnifiedAd != null && !TextUtils.isEmpty(nativeUnifiedAd.getEcpm())) {
            nativeUnifiedAd.setBidEcpm(Integer.parseInt(nativeUnifiedAd.getEcpm()));
        }
        List<WindNativeAdData> unifiedADDataList = unifiedADDataMap.get(placementId);
        if (unifiedADDataList != null && unifiedADDataList.size() > 0) {
            WindNativeAdData nativeAdData = unifiedADDataList.get(0);

            //创建一个装整个自渲染广告的容器
            adRender = new NativeAdDemoRender();

            adRender.setShowWithoutAppInfo(show_without_appinfo);
            View windContainer = adRender.getNativeAdView(getContext(), nativeAdData, new NativeADEventListener() {
                @Override
                public void onAdExposed() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("AdAppInfo [ " + nativeAdData.getAdAppInfo() + " ]");
                            logMessage("onAdExposed [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onAdClicked() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onAdClicked [ " + placementId + " ]");
                        }
                    });
                }


                @Override
                public void onAdDetailShow() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onAdDetailShow [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onAdDetailDismiss() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onAdDetailDismiss [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onAdError(final WindAdError error) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onAdError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                        }
                    });
                }
            }, new WindNativeAdData.NativeADMediaListener() {
                @Override
                public void onVideoLoad() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoLoad [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoError(final WindAdError error) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoError() called with: error = [" + error + "], placementId = [" + placementId + "]");
                        }
                    });
                }

                @Override
                public void onVideoStart() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoStart [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoPause() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoPause [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoResume() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoResume [ " + placementId + " ]");
                        }
                    });
                }

                @Override
                public void onVideoCompleted() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onVideoCompleted [ " + placementId + " ]");
                        }
                    });
                }
            });

            //设置dislike弹窗
            nativeAdData.setDislikeInteractionCallback(getActivity(), new WindNativeAdData.DislikeInteractionCallback() {
                @Override
                public void onShow() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onShow [ " + placementId + " ]");
                        }
                    });
                    Log.d("WindSDK", "----------onShow----------");
                }

                @Override
                public void onSelected(final int position, final String value, final boolean enforce) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onSelected :" + position + ":" + value + ":" + enforce + " [ " + placementId + " ]");
                        }
                    });
                    Log.d("WindSDK", "----------onSelected----------:" + position + ":" + value + ":" + enforce);
                    if (adContainer != null) {
                        adContainer.removeAllViews();
                    }
                }

                @Override
                public void onCancel() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logMessage("onCancel [ " + placementId + " ]");
                        }
                    });
                    Log.d("WindSDK", "----------onCancel----------");
                }
            });


            //媒体最终将要展示广告的容器
            if (adContainer != null) {
                adContainer.removeAllViews();
                adContainer.addView(windContainer);


            }
        } else {
            logMessage("Ad is not Ready");
            Log.d("WindSDK", "--------请先加载广告--------");
        }
    }

    public void buttonClick(View view) {
        long viewId = view.getId();
        if (viewId == R.id.cleanLog_button) {
            cleanLog();
        } else if (viewId == R.id.notice_win_button) {
            noticeWin();
        } else if (viewId == R.id.notice_loss_button) {
            noticeLoss();
        } else if (viewId == R.id.native_show) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    adContainer.setVisibility(View.VISIBLE);
                }
            });
        } else if (viewId == R.id.native_hide) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    adContainer.setVisibility(View.GONE);
                }
            });
        }
    }

    private void noticeWin() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("setting", 0);

        String AUCTION_PRICE = sharedPreferences.getString(Constants.BID_JS, "-1");
        String HIGHEST_LOSS_PRICE = sharedPreferences.getString(Constants.BID_CGJ, "-1");
        String CURRENCY = sharedPreferences.getString(Constants.BID_BZ, "CNY");

        Log.d("WindSDK", "---------noticeWin---------" + AUCTION_PRICE + ":" + HIGHEST_LOSS_PRICE + ":" + CURRENCY);

        Map<String, Object> map = new HashMap<>();
        map.put(WindAds.AUCTION_PRICE, Integer.parseInt(AUCTION_PRICE));
        map.put(WindAds.HIGHEST_LOSS_PRICE, Integer.parseInt(HIGHEST_LOSS_PRICE));
        map.put(WindAds.CURRENCY, CURRENCY);

        for (WindNativeUnifiedAd nativeUnifiedAd : nativeUnifiedAdMap.values()) {
            nativeUnifiedAd.sendWinNotificationWithInfo(map);
        }
    }

    private void noticeLoss() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("setting", 0);

        String AUCTION_PRICE = sharedPreferences.getString(Constants.BID_JS, "-1");
        String CURRENCY = sharedPreferences.getString(Constants.BID_BZ, "CNY");
        String LOSS_REASON = sharedPreferences.getString(Constants.BID_SB, "-1");
        String ADN_ID = sharedPreferences.getString(Constants.BID_JSF, "-1");

        Log.d("WindSDK", "---------noticeLoss---------" + AUCTION_PRICE + ":" + LOSS_REASON + ":" + CURRENCY + ":" + ADN_ID);

        Map<String, Object> map = new HashMap<>();
        map.put(WindAds.AUCTION_PRICE, Integer.parseInt(AUCTION_PRICE));
        map.put(WindAds.CURRENCY, CURRENCY);
        map.put(WindAds.LOSS_REASON, Integer.parseInt(LOSS_REASON));
        map.put(WindAds.ADN_ID, ADN_ID);

        for (WindNativeUnifiedAd nativeUnifiedAd : nativeUnifiedAdMap.values()) {
            nativeUnifiedAd.sendLossNotificationWithInfo(map);
        }
    }

    private static SimpleDateFormat dateFormat = null;

    private static SimpleDateFormat getDateTimeFormat() {

        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss SSS", Locale.CHINA);
        }
        return dateFormat;
    }

    private void cleanLog() {
        logTextView.setText("");
    }

    private void logMessage(String message) {
        Date date = new Date();
        logTextView.append(getDateTimeFormat().format(date) + " " + message + '\n');
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adRender = null;
        for (List<WindNativeAdData> adDataList : unifiedADDataMap.values()) {
            if (adDataList != null && adDataList.size() > 0) {
                for (WindNativeAdData ad : adDataList) {
                    if (ad != null) {
                        ad.destroy();
                    }
                }
            }
        }

        for (WindNativeUnifiedAd unifiedAd : nativeUnifiedAdMap.values()) {
            if (unifiedAd != null) {
                unifiedAd.destroy();
            }
        }
    }

    private void updatePlacement() {

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("setting", 0);

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");
        show_without_appinfo = sharedPreferences.getBoolean(Constants.CONF_SHOW_WITHOUT_APPINFO, false);

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
                            if (ad_type == 5 && bidType == 0) {

                                String adSlotId = slotId.optString("adSlotId");

                                placementIds.add(adSlotId);
                                LinearLayout ll = new LinearLayout(getContext());
                                ll.setOrientation(LinearLayout.HORIZONTAL);
                                ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                                Button loadB = new Button(getContext());
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0, 5, 0, 5);
                                layoutParams.weight = 1;
                                loadB.setLayoutParams(layoutParams);
                                loadB.setOnClickListener(this);
                                loadB.setText("LOAD-" + adSlotId);
                                loadB.setTextSize(12);
                                ll.addView(loadB);

                                Button playB = new Button(getContext());
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                params.setMargins(0, 5, 0, 5);
                                params.weight = 1;
                                playB.setLayoutParams(params);
                                playB.setOnClickListener(this);
                                playB.setText("PLAY-" + adSlotId);
                                playB.setTextSize(12);
                                ll.addView(playB);

                                IdLayout.addView(ll);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);

        Log.d("WindSDK", "-----------updatePlacement-----------" + isNewInstance);
    }


}