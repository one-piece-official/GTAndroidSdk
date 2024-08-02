package com.sigmob.sdk.base.views;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.common.SigToast;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.windad.WindAds;


/**
 * Create by lance on 2019/8/14/0014
 */
public class AdInfoDialog extends Dialog implements DialogInterface.OnShowListener, DialogInterface.OnDismissListener, View.OnClickListener {

    private Context mContext;
    private Window window = null;
    private int mHeight;
    private int mWidth;
    private int mScreenWidth;
    private int mScreenHeight;
    private String request_id;
    private String placement_id;
    private ImageView closeView;


    public AdInfoDialog(Context context, String requestId, String placement_id) {
        super(context, SigmobRes.getSig_custom_dialog());
        this.mContext = context.getApplicationContext();
        this.request_id = requestId;
        this.placement_id = placement_id;
        closeView = getCloseView();

        mWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        mHeight = Dips.dipsToIntPixels(120, context);
    }


    private ImageView getCloseView() {

        closeView = new ImageView(mContext);
        closeView.setImageBitmap(Drawables.CLOSE.getBitmap());
        closeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        closeView.setImageAlpha((int) (0.5f * 255.0f));
        closeView.setClickable(true);
        closeView.setOnClickListener(this);

        return closeView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout mLayout = new RelativeLayout(this.getContext());
        mLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(Color.WHITE);
        int corner = Dips.dipsToIntPixels(20, mContext);
        float[] radii = new float[]{
                corner, corner,
                corner, corner,
                0F, 0F,
                0F, 0F
        };
        gd.setCornerRadii(radii);
        int w = Dips.dipsToIntPixels(10, mContext);
        mLayout.setPadding(w, w, w, w);
        mLayout.setBackground(gd);

        setContentView(mLayout);
        this.setOnShowListener(this);
        this.setOnDismissListener(this);

        SigmobLog.i("AdInfoDialog onCreate:" + mWidth + ":" + mHeight);

        int width = Dips.dipsToIntPixels(18, mContext);

        if (closeView != null) {
            final RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(width, width);
            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            iconLayoutParams.setMargins(width, width, 0, 0);
            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            iconLayoutParams.setMargins(0, width / 4, width / 2, 0);
            mLayout.addView(closeView, iconLayoutParams);
        }

        TextView copyButton = getCopyButton();
        copyButton.setId(ClientMetadata.generateViewId());
        RelativeLayout.LayoutParams LayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width);
        LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            iconLayoutParams.setMargins(width, width, 0, 0);
        LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        LayoutParams.setMargins(0, width / 4, width / 2, 0);
        mLayout.addView(copyButton, LayoutParams);


        LayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LayoutParams.addRule(RelativeLayout.BELOW, copyButton.getId());
//            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            iconLayoutParams.setMargins(width, width, 0, 0);
        LayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        LayoutParams.setMargins(0, 0, 0, width / 2);
        mLayout.addView(getAdInfoView(), LayoutParams);

        //点击dialog以外的空白处是否隐藏
        setCanceledOnTouchOutside(true);
        //点击返回键取消
        setCancelable(true);
        //设置窗口显示
        windowDeploy();
    }


    private boolean textCopyThenPost(Context context, String textCopied) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            // When setting the clip board text.
            clipboardManager.setPrimaryClip(ClipData.newPlainText("", textCopied));
            // Only show a toast for SigmobAndroid 12 and lower.
            return true;
        } catch (Throwable th) {

        }

        return false;

    }

    private TextView getCopyButton() {


        TextView textView = new TextView(getContext());
        textView.setText("复制广告信息");
        textView.setTextColor(Color.BLUE);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sig_id = "appid:" + WindAds.sharedAds().getAppId();
                String p_id = "p_id:" + placement_id;
                String req_id = "req_id:" + request_id;
                boolean result = textCopyThenPost(view.getContext(), String.format("%s,%s,%s", sig_id, p_id, req_id));

                if (result) {
                    SigToast.makeText(view.getContext(), "广告信息复制完成", Toast.LENGTH_SHORT).show();
                } else {
                    SigToast.makeText(view.getContext(), "广告信息复制失败，请检查权限", Toast.LENGTH_SHORT).show();
                }

            }
        });
        return textView;
    }

    private View getAdInfoView() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        int height = Dips.dipsToIntPixels(20, getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT
                , height);
        layoutParams.setMargins(0, height / 4, 0, 0);
        TextView suid = new TextView(getContext());
        String sig_id = "appid: " + WindAds.sharedAds().getAppId();
        suid.setText(sig_id);
        suid.setTextColor(Color.BLACK);
        suid.setTextSize(12);
        linearLayout.addView(suid, layoutParams);

        TextView placementIdTV = new TextView(getContext());
        String p_id = "p_id: " + placement_id;

        placementIdTV.setText(p_id);
        placementIdTV.setTextColor(Color.BLACK);
        placementIdTV.setTextSize(12);

        linearLayout.addView(placementIdTV, layoutParams);

        TextView requestIDTV = new TextView(getContext());
        String req_id = "req_id: " + request_id;
        requestIDTV.setTextSize(12);
        requestIDTV.setText(req_id);
        requestIDTV.setTextColor(Color.BLACK);

        linearLayout.addView(requestIDTV, layoutParams);


        return linearLayout;
    }

    private void windowDeploy() {
        window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM); //设置窗口显示位置

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


    @Override
    public void onShow(DialogInterface dialog) {
        SigmobLog.i("AdInfoDialog  onShow");

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        SigmobLog.i("AdInfoDialog  onDismiss");

    }

    public void destroy() {

        if (closeView != null) {
            ViewUtil.removeFromParent(closeView);
            closeView = null;
        }
        if (mContext != null) {
            mContext = null;
        }

    }

    @Override
    public void onClick(View view) {
        dismiss();
    }
}
