package com.sigmob.sigmob;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.sigmob.sigmob.natives.NativeAdActivity;
import com.sigmob.sigmob.rename.BidInterstitialRenameActivity;
import com.sigmob.sigmob.rename.BidRewardRenameActivity;
import com.sigmob.sigmob.rename.InterstitialRenameActivity;
import com.sigmob.sigmob.rename.NativeAdRenameActivity;
import com.sigmob.sigmob.rename.RewardRenameActivity;
import com.sigmob.sigmob.rename.SplashRenameActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainFragment extends Fragment {

    private TextView logTextView;
    private String[] mLogs;
    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment, container, false);

        return view;
    }

    private void bindButton(@IdRes int id, final Class clz) {
        getMyActivity().findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getMyActivity(), clz);
                //激励视频代码位id
                if (v.getId() == R.id.reward_button) {
//                    intent.putExtra("horizontal_rit", "901121184");
//                    intent.putExtra("vertical_rit", "901121375");
                }
                //全屏视频代码位id
                if (v.getId() == R.id.interstitial_button) {
//                    intent.putExtra("horizontal_rit", "901121430");
//                    intent.putExtra("vertical_rit", "901121365");
                }
                //开屏代码位id
                if (v.getId() == R.id.splash_button) {
                    intent.putExtra("isLoadAndShow", false);
                }
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindButton(R.id.reward_button, RewardVideoActivity.class);
        bindButton(R.id.interstitial_button, InterstitialActivity.class);
        bindButton(R.id.splash_button, SplashActivity.class);
        bindButton(R.id.native_button, NativeAdActivity.class);

        bindButton(R.id.rename_reward_button, RewardRenameActivity.class);
        bindButton(R.id.rename_interstitial_button, InterstitialRenameActivity.class);
        bindButton(R.id.rename_splash_button, SplashRenameActivity.class);
        bindButton(R.id.rename_native_button, NativeAdRenameActivity.class);

        bindButton(R.id.bid_reward_button, BidRewardVideoActivity.class);
        bindButton(R.id.bid_interstitial_button, BidInterstitialActivity.class);
        bindButton(R.id.bid_rename_reward_button, BidRewardRenameActivity.class);
        bindButton(R.id.bid_rename_interstitial_button, BidInterstitialRenameActivity.class);

        Button configurationBtn = getView().findViewById(R.id.configuration_button);
        Button cleanLogBtn = getView().findViewById(R.id.cleanLog_button);

        logTextView = getView().findViewById(R.id.logView);

        configurationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configuration();
            }
        });

        cleanLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanLog();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        logTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        if (mLogs != null && mLogs.length > 0) {
            for (int i = 0; i < mLogs.length; i++) {
                logMessage(mLogs[i]);
            }
        }
    }

    public void setLogs(String[] logs) {
        mLogs = logs;

        if (mLogs != null && mLogs.length > 0 && logTextView != null) {
            for (int i = 0; i < mLogs.length; i++) {
                logMessage(mLogs[i]);
            }
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;//保存Context引用
    }

    private Activity getMyActivity() {
        if (mActivity == null) {
            mActivity = getActivity();
        }
        return mActivity;
    }


    private void configuration() {
        ConfigurationFragment configurationFragment = new ConfigurationFragment();
        FragmentTransaction transaction = getMyActivity().getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, configurationFragment);
        transaction.addToBackStack("configuration");
        transaction.commit();

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


}
