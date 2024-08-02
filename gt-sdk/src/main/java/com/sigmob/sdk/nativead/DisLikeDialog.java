package com.sigmob.sdk.nativead;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.network.SigmobRequestQueue;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.VolleyError;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.common.AdActivity;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.common.SigToast;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.BidResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.FeedbackReportRequest;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.natives.WindNativeAdData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Create by lance on 2019/8/14/0014
 */
public class DisLikeDialog extends Dialog implements SigmobAdDislike, DialogInterface.OnShowListener, DialogInterface.OnDismissListener, View.OnClickListener {

    private Context mContext;
    private Window window = null;
    private int mHeight;
    private int mWidth;
    private BaseAdUnit mAdUnit;

    private WindNativeAdData.DislikeInteractionCallback dislikeCallback;
    private List<String> sourceData = new ArrayList<>();
    private TextView dislikeTv, notShowTv, notCloseTv, commitTv;
//    private RelativeLayout whyWatchAdRl;
    private EditText wordEt;
    private SigFlowLayout flowLayout;
    private ViewGroup flowContainer;
    private boolean isShowed;

    public DisLikeDialog(Context context, BaseAdUnit adUnit) {
        super(context, SigmobRes.getSig_custom_dialog());
        this.mContext = context.getApplicationContext();
        this.mAdUnit = adUnit;
        //计算mAdView应该显示的高度
        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        int padding = Dips.dipsToIntPixels(40, mContext);
        if (screenHeight > screenWidth) {//竖屏
            mWidth = screenWidth - padding;
            mHeight = WindowManager.LayoutParams.WRAP_CONTENT;
        } else {//横屏
            mWidth = screenHeight - padding;
            mHeight = screenHeight - padding;
        }
        initData();
    }


    private void initData() {
        sourceData.clear();
        sourceData.add("违法违规");
        sourceData.add("疑似抄袭");
        sourceData.add("虚假欺诈");
        sourceData.add("低俗色情");
        sourceData.add("诱导点击");
    }

    @Override
    public void onClick(View v) {
        String text = "";
        int position = 0;
//        if (v.equals(whyWatchAdRl)) {
//            position = 0;
//            doPoint(PointCategory.DISLIKE, PointCategory.WHY_CLICK, "", "");
//            AdActivity.disLikeDialog(mContext, AdActivity.class, mAdUnit.getUuid());
//        } else
        if (v.equals(notShowTv)) {
            text = (String) notShowTv.getText();
            position = 1;
            doPoint(PointCategory.DISLIKE, PointCategory.ADNORMAL_SHOW, "", "");
        } else if (v.equals(notCloseTv)) {
            text = (String) notCloseTv.getText();
            position = 2;
            doPoint(PointCategory.DISLIKE, PointCategory.CANNOT_CLOSE, "", "");
        } else if (v.equals(dislikeTv)) {
            text = (String) dislikeTv.getText();
            position = 3;
            doPoint(PointCategory.DISLIKE, PointCategory.NOINTEREST_CLICK, "", "");
        } else if (v.equals(commitTv)) {
            if (wordEt != null) {//提交
                text = wordEt.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    position = 5;
                    doPoint(PointCategory.DISLIKE, PointCategory.ADVICE, "", Base64.encodeToString(text.getBytes(), Base64.DEFAULT));
                }
            }
        }
        reportFeedBack();

        if (dislikeCallback != null && position != 0) {
            dislikeCallback.onSelected(position, text, true);
            this.dismiss();
        }
    }

    private boolean textCopyThenPost(Context context, String textCopied) {
        try {
            if (context != null) {
                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied));
                return true;

            }

        } catch (Throwable th) {

        }

        return false;

    }

    private void reportFeedBack() {
        Context context = SDKContext.getApplicationContext();
        if (mAdUnit != null) {
            mAdUnit.dislikeReport();
            if (WindSDKConfig.getInstance().isFeedback_debug()) {
                String requestId = mAdUnit.getRequestId();
                BidResponse bidResponse = AdStackManager.getBidResponse(requestId);
                if (bidResponse != null) {
                    ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
                        @Override
                        public void run() {
                            String feedbackUrl = WindSDKConfig.getInstance().getFeedbackUrl();
                            final FeedbackReportRequest reportRequest = new FeedbackReportRequest(feedbackUrl, bidResponse, new FeedbackReportRequest.Listener() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    WindAds.sharedAds().getHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            boolean result = textCopyThenPost(context, requestId);
                                            if (result) {
                                                SigToast.makeText(context, "反馈上报成功，广告请求ID已复制到剪贴板", Toast.LENGTH_LONG).show();
                                            } else {
                                                SigToast.makeText(context, "反馈上报成功,广告请求ID:" + requestId, Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                }

                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    WindAds.sharedAds().getHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            int statusCode = 0;
                                            if (error != null) {
                                                if (error.networkResponse != null) {
                                                    statusCode = error.networkResponse.statusCode;
                                                }
                                            }
                                            SigToast.makeText(context, "反馈上报失败，错误码: " + statusCode, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            });
                            SigmobRequestQueue sigRequestQueue = Networking.getSigRequestQueue();
                            if (sigRequestQueue != null) {
                                sigRequestQueue.add(reportRequest);
                            }
                        }
                    });
                }
                return;
            }
        }
        SigToast.makeText(context, "感谢反馈", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        RelativeLayout mLayout = new RelativeLayout(this.getContext());
//        mLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        setContentView(mLayout);
        setContentView(ResourceUtil.getLayoutId(mContext, "sig_dislike_layout"));

//        whyWatchAdRl = findViewById(ResourceUtil.getId(mContext, "sig_why_watch_rl"));
        notShowTv = findViewById(ResourceUtil.getId(mContext, "sig_not_show_tv"));
        notCloseTv = findViewById(ResourceUtil.getId(mContext, "sig_not_close_tv"));
        dislikeTv = findViewById(ResourceUtil.getId(mContext, "sig_dislike_tv"));
        commitTv = findViewById(ResourceUtil.getId(mContext, "sig_commit_sl"));
        wordEt = findViewById(ResourceUtil.getId(mContext, "sig_suggest_et"));

//        whyWatchAdRl.setOnClickListener(this);
        dislikeTv.setOnClickListener(this);
        notShowTv.setOnClickListener(this);
        notCloseTv.setOnClickListener(this);
        commitTv.setOnClickListener(this);

        wordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                //text  输入框中改变后的字符串信息
                //start 输入框中改变后的字符串的起始位置
                //before 输入框中改变前的字符串的位置 默认为0
                //count 输入框中改变后的一共输入字符串的数量
                //这个方法是在Text改变过程中触发调用的，它的意思就是说在原有的文本text中，从start开始的count个字符
                //替换长度为before的旧文本，注意这里没有将要之类的字眼，也就是说一句执行了替换动作
                if (commitTv != null) {
                    commitTv.setTextColor(Color.parseColor("#FE7E03"));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
                //text  输入框中改变前的字符串信息
                //start 输入框中改变前的字符串的起始位置
                //count 输入框中改变前后的字符串改变数量一般为0
                //after 输入框中改变后的字符串与起始位置的偏移量

                //这个方法是在Text改变之前被调用，它的意思就是说在原有的文本text中，从start开始的count个字符
                //将会被一个新的长度为after的文本替换，注意这里是将被替换，还没有被替换
            }

            @Override
            public void afterTextChanged(Editable edit) {
                //edit  输入结束呈现在输入框中的信息
                if (TextUtils.isEmpty(edit.toString())) {
                    if (commitTv != null) {
                        commitTv.setTextColor(Color.parseColor("#C2C2C2"));
                    }
                }
            }
        });

        flowContainer = findViewById(ResourceUtil.getId(mContext, "sig_flow_sl"));

        flowLayout = new SigFlowLayout(mContext);
        flowLayout.setList(sourceData);
        flowLayout.setOnItemClickListener(new SigFlowLayout.onItemClickListener() {
            @Override
            public void onItemClick(int position, String text) {
//                List<String> chooseResult = flowLayout.getAllItemSelectedTextWithListArray();
//                if (chooseResult != null && chooseResult.size() > 0) {
//                    String textSelect = "";
//                    for (int i = 0; i < chooseResult.size(); i++) {
//                        textSelect += chooseResult.get(i) + " , ";
//                    }
//                }
                reportFeedBack();

                doPoint(PointCategory.DISLIKE, PointCategory.REPORT, String.valueOf(position), "");

                if (dislikeCallback != null) {
                    dislikeCallback.onSelected(4, text, true);
                    DisLikeDialog.this.dismiss();
                }
            }
        });

        flowContainer.addView(flowLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        this.setOnShowListener(this);
        this.setOnDismissListener(this);
        //点击dialog以外的空白处是否隐藏
        setCanceledOnTouchOutside(true);
        //点击返回键取消
        setCancelable(true);
        //设置窗口显示
        windowDeploy();
        SigmobLog.i("DisLikeDialog onCreate:" + mWidth + ":" + mHeight);
    }


    @Override
    public void showDislikeDialog() {
        if (mAdUnit != null && (!mAdUnit.isDislikeReported() || mAdUnit.getAd_type() == AdFormat.UNIFIED_NATIVE)) {
            this.show();
            doPoint(PointCategory.DISLIKE, PointCategory.CLICK, "", "");
        } else {
            SigToast.makeText(getContext(), "已提交反馈", Toast.LENGTH_LONG).show();

        }

    }

    private void doPoint(String cate, String sub_category, final String reason, final String content) {
        PointEntitySigmobUtils.SigmobTracking(cate, sub_category, mAdUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    PointEntitySigmob entitySigMob = (PointEntitySigmob) pointEntityBase;
                    entitySigMob.setAdtype(String.valueOf(mAdUnit.getAd_type()));
                    entitySigMob.setLoad_id(mAdUnit.getLoad_id());
                    entitySigMob.setRequest_id(mAdUnit.getRequestId());
                    entitySigMob.setReason(reason);
                    entitySigMob.setContent(content);
                    entitySigMob.setVid(mAdUnit.getVid());
                }
            }
        });
    }

    @Override
    public void setDislikeInteractionCallback(WindNativeAdData.DislikeInteractionCallback callback) {
        this.dislikeCallback = callback;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        SigmobLog.i("DisLikeDialog  onShow");
        if (dislikeCallback != null) {
            dislikeCallback.onShow();
        }
        if (flowLayout != null) {
            flowLayout.cancelAllSelectedItems();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        SigmobLog.i("DisLikeDialog  onDismiss");
        if (dislikeCallback != null) {
            dislikeCallback.onCancel();
        }
    }

    public void destroy() {
//        if (whyWatchAdRl != null) {
//            whyWatchAdRl.setOnClickListener(null);
//            whyWatchAdRl = null;
//        }
        if (dislikeTv != null) {
            dislikeTv.setOnClickListener(null);
            dislikeTv = null;

        }
        if (notShowTv != null) {
            notShowTv.setOnClickListener(null);
            notShowTv = null;

        }
        if (notCloseTv != null) {
            notCloseTv.setOnClickListener(null);
            notCloseTv = null;

        }
        if (commitTv != null) {
            commitTv.setOnClickListener(null);
            commitTv = null;
        }

        if (dislikeCallback != null) {
            dislikeCallback = null;
        }
        this.setOnShowListener(null);
        this.setOnDismissListener(null);
        window = null;
    }

    private void windowDeploy() {
        window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.CENTER); //设置窗口显示位置

            int SigMobDialogWindowAnim = SigmobRes.getSig_dialog_window_anim();
            if (SigMobDialogWindowAnim != 0) {
                window.setWindowAnimations(SigMobDialogWindowAnim); //设置窗口弹出动画
            }
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
//            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.width = mWidth;
            lp.height = mHeight;
            window.setAttributes(lp);
        }
    }

}
