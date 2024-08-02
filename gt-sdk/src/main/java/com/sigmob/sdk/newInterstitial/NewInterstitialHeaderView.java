package com.sigmob.sdk.newInterstitial;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.sigmob.sdk.base.views.Drawables;
import com.sigmob.sdk.base.views.OvalButton;

public class NewInterstitialHeaderView extends RelativeLayout {


    private ImageView mCloseView;
    private TextView mTimerTextView;
    private ImageView mSoundImageView;
    private int mTimer;
    private Runnable timerRunnable;

    private OvalButton feedBack;
    private boolean closeAd;
    private View mCloseViewRL;


    public interface AdHeaderViewStateListener {
        void onShowClose();

        void onShowSkip();
    }

    private AdHeaderViewStateListener listener;

    public NewInterstitialHeaderView(Context context) {
        super(context);
        init(context);
    }

    public NewInterstitialHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NewInterstitialHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = inflate(context, ResourceUtil.getLayoutId(context, "sig_new_interstitial_header_layout"), this);
        mCloseView = view.findViewById(ResourceUtil.getId(context, "sig_ad_close"));
        mCloseViewRL = view.findViewById(ResourceUtil.getId(context, "sig_ad_rl_root"));
        if (mCloseView != null) {
            mCloseView.setImageBitmap(Drawables.CLOSE_OLD.getBitmap());
        }

        mSoundImageView = view.findViewById(ResourceUtil.getId(context, "sig_ad_sound"));
        mTimerTextView = view.findViewById(ResourceUtil.getId(context, "sig_ad_timer"));
        mTimerTextView.setClickable(false);
//        mTimerTextView.setId(ClientMetadata.generateViewId());
        timerRunnable = new Runnable() {

            @Override
            public void run() {
                if (mTimer > 0) {
                    mTimerTextView.setText(String.valueOf(mTimer--));
                    mTimerTextView.postDelayed(timerRunnable, 1000);
                } else {
                    if (closeAd) {
                        if (listener != null) {
                            listener.onShowClose();
                        }
                        mTimerTextView.setVisibility(GONE);
                        mCloseView.setVisibility(VISIBLE);
                    } else {
                        if (listener != null) {
                            listener.onShowSkip();
                        }
                        mTimerTextView.setClickable(true);
                        mTimerTextView.setText("   跳过   ");
                    }
                    removeCallbacks(this);
                }
            }
        };
    }

    public void showFeedback(boolean isLeft, View.OnClickListener onClickListener) {
        if (feedBack == null) {
            Context context = getContext();
            feedBack = new OvalButton(context);
            feedBack.setText("反馈");
            feedBack.setOnClickListener(onClickListener);
            feedBack.setId(ClientMetadata.generateViewId());
            int padding = Dips.dipsToIntPixels(5, context);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(45, context), Dips.dipsToIntPixels(30, context));
            if (isLeft) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                RelativeLayout.LayoutParams layoutParams1 = (RelativeLayout.LayoutParams) mSoundImageView.getLayoutParams();
                layoutParams1.addRule(RelativeLayout.RIGHT_OF, feedBack.getId());
                layoutParams.setMargins(padding, 0, 0, 0);

            } else {
                layoutParams.addRule(RelativeLayout.LEFT_OF, mCloseViewRL.getId());
                layoutParams.setMargins(0, 0, padding, 0);
            }
            layoutParams.addRule(RelativeLayout.ALIGN_BASELINE, mCloseViewRL.getId());
            ViewParent parent = mCloseViewRL.getParent();
            if (parent != null) {
                ((ViewGroup) parent).addView(feedBack, layoutParams);
            }
        }
    }

    public void setAdHeaderViewStateListener(AdHeaderViewStateListener listener) {
        this.listener = listener;
    }

    public void showSoundIcon() {
        mSoundImageView.setVisibility(View.VISIBLE);
//        addFeedBack(getContext(), View.VISIBLE);
    }

    public void hideSoundIcon() {
        mSoundImageView.setVisibility(View.GONE);

    }

    public void showCloseView() {
        mCloseView.setVisibility(View.VISIBLE);
    }


    public void startAdTimer(int seconds, boolean closeAd) {

        this.closeAd = closeAd;

        if (seconds > 0) {
            mTimerTextView.setVisibility(VISIBLE);
            mTimerTextView.setText(String.valueOf(seconds));
            mTimer = seconds - 1;
            mTimerTextView.setClickable(false);
            mTimerTextView.postDelayed(timerRunnable, 1000);
        } else {
            if (closeAd) {
                showCloseView();
                if (listener != null) {
                    listener.onShowClose();
                }
                mTimerTextView.setVisibility(GONE);
            } else {
                if (listener != null) {
                    listener.onShowSkip();
                }
                mTimerTextView.setClickable(true);
                mTimerTextView.setText("   跳过   ");
            }

        }


    }


    public void setSoundClickListener(View.OnClickListener onClickListener) {
        mSoundImageView.setOnClickListener(onClickListener);
    }

    public void setCloseClickListener(View.OnClickListener onClickListener) {
        mCloseView.setOnClickListener(onClickListener);
    }

    public void setSkipClickListener(View.OnClickListener onClickListener) {
        mTimerTextView.setOnClickListener(onClickListener);
    }


    public void setSoundStatus(boolean mIsMute) {
        if (mIsMute) {
            mSoundImageView.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_mute"));
        } else {
            mSoundImageView.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_unmute"));
        }
    }
}
