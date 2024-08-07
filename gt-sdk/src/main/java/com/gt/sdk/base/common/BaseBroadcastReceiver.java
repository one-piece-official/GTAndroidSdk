package com.gt.sdk.base.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.utils.Preconditions;
import com.gt.sdk.GtAdSdk;

import java.util.Map;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {

    private final String mBroadcastIdentifier;

    public BaseBroadcastReceiver(String broadcastIdentifier) {
        mBroadcastIdentifier = broadcastIdentifier;
    }

    public static void broadcastAction(final Context context, final String broadcastIdentifier, final String action) {
        broadcastAction(context, broadcastIdentifier, null, action, 0);
    }

    public static void broadcastAction(final Context context, final String broadcastIdentifier, final String action, final int delay) {
        broadcastAction(context, broadcastIdentifier, null, action, delay);
    }

    public static void broadcastAction(final Context context, final String broadcastIdentifier, final Map<String, Object> extras, final String action) {
        broadcastAction(context, broadcastIdentifier, extras, action, 0);
    }

    public static void broadcastAction(final Context context, final String broadcastIdentifier, final Map<String, Object> extras, final String action, final int delay) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(action);
        Intent intent = new Intent(action);
        intent.putExtra(Constants.BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        if (extras != null) {
            for (String key : extras.keySet()) {
                try {
                    Object value = extras.get(key);
                    if (value instanceof Number) {
                        intent.putExtra(key, (Number) value);
                    } else {
                        intent.putExtra(key, (String) value);
                    }
                } catch (Throwable ignored) {

                }
            }
        }

        GtLocalBroadcastManager localBroadcastManager = GtLocalBroadcastManager.getInstance(context);
        if (localBroadcastManager != null) {
            try {
                localBroadcastManager.sendBroadcast(intent, delay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract IntentFilter getIntentFilter();

    public void register(final BroadcastReceiver broadcastReceiver) {
        GtLocalBroadcastManager localBroadcastManager = GtLocalBroadcastManager.getInstance(GtAdSdk.sharedAds().getContext());
        if (localBroadcastManager != null) {
            try {
                localBroadcastManager.registerReceiver(broadcastReceiver, getIntentFilter());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void unregister(final BroadcastReceiver broadcastReceiver) {
        if (broadcastReceiver != null) {
            GtLocalBroadcastManager localBroadcastManager = GtLocalBroadcastManager.getInstance(GtAdSdk.sharedAds().getContext());
            if (localBroadcastManager != null) {
                try {
                    localBroadcastManager.unregisterReceiver(broadcastReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Only consume this broadcast if the identifier on the received Intent and this broadcast
     * match up. This allows us to target broadcasts to the ad that spawned them. We include
     * this here because there is no appropriate IntentFilter condition that can recreate this
     * behavior.
     */
    protected boolean shouldConsumeBroadcast(final Intent intent) {
        Preconditions.NoThrow.checkNotNull(intent);
        final String receivedIdentifier = intent.getStringExtra(Constants.BROADCAST_IDENTIFIER_KEY);
        return !TextUtils.isEmpty(receivedIdentifier) && receivedIdentifier.equalsIgnoreCase(mBroadcastIdentifier);
    }
}