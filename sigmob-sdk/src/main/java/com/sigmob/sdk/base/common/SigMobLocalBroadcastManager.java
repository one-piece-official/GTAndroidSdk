package com.sigmob.sdk.base.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class SigMobLocalBroadcastManager {

    static final int MSG_EXEC_PENDING_BROADCASTS = 1;
    private static final String TAG = "LocalBroadcastManager";
    private static final boolean DEBUG = false;
    private static final Object mLock = new Object();
    private static SigMobLocalBroadcastManager mInstance;
    private final Context mAppContext;
    private final HashMap<BroadcastReceiver, ArrayList<SigMobLocalBroadcastManager.ReceiverRecord>> mReceivers = new HashMap<>();
    private final HashMap<String, ArrayList<SigMobLocalBroadcastManager.ReceiverRecord>> mActions = new HashMap<>();
    private final ArrayList<SigMobLocalBroadcastManager.BroadcastRecord> mPendingBroadcasts = new ArrayList<>();
    private final Handler mHandler;

    private SigMobLocalBroadcastManager(Context context) {
        mAppContext = context;
        mHandler = new Handler(context.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_EXEC_PENDING_BROADCASTS:
                        executePendingBroadcasts();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }

    public static SigMobLocalBroadcastManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new SigMobLocalBroadcastManager(context.getApplicationContext());
            }
            return mInstance;
        }
    }

    /**
     * Register a receive for any local broadcasts that match the given IntentFilter.
     *
     * @param receiver The BroadcastReceiver to handle the broadcast.
     * @param filter   Selects the Intent broadcasts to be received.
     * @see #unregisterReceiver
     */
    public void registerReceiver(BroadcastReceiver receiver,
                                 IntentFilter filter) {
        synchronized (mReceivers) {
            SigMobLocalBroadcastManager.ReceiverRecord entry = new SigMobLocalBroadcastManager.ReceiverRecord(filter, receiver);
            ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> filters = mReceivers.get(receiver);
            if (filters == null) {
                filters = new ArrayList<>(1);
                mReceivers.put(receiver, filters);
            }
            filters.add(entry);
            for (int i = 0; i < filter.countActions(); i++) {
                String action = filter.getAction(i);
                ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> entries = mActions.get(action);
                if (entries == null) {
                    entries = new ArrayList<SigMobLocalBroadcastManager.ReceiverRecord>(1);
                    mActions.put(action, entries);
                }
                entries.add(entry);
            }
        }
    }

    /**
     * Unregister a previously registered BroadcastReceiver.  <em>All</em>
     * filters that have been registered for this BroadcastReceiver will be
     * removed.
     *
     * @param receiver The BroadcastReceiver to unregister.
     * @see #registerReceiver
     */
    public void unregisterReceiver(BroadcastReceiver receiver) {
        synchronized (mReceivers) {
            final ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> filters = mReceivers.remove(receiver);
            if (filters == null) {
                return;
            }
            for (int i = filters.size() - 1; i >= 0; i--) {
                final SigMobLocalBroadcastManager.ReceiverRecord filter = filters.get(i);
                filter.dead = true;
                for (int j = 0; j < filter.filter.countActions(); j++) {
                    final String action = filter.filter.getAction(j);
                    final ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> receivers = mActions.get(action);
                    if (receivers != null) {
                        for (int k = receivers.size() - 1; k >= 0; k--) {
                            final SigMobLocalBroadcastManager.ReceiverRecord rec = receivers.get(k);
                            if (rec.receiver == receiver) {
                                rec.dead = true;
                                receivers.remove(k);
                            }
                        }
                        if (receivers.size() <= 0) {
                            mActions.remove(action);
                        }
                    }
                }
            }
        }
    }

    /**
     * Broadcast the given intent to all interested BroadcastReceivers.  This
     * call is asynchronous; it returns immediately, and you will continue
     * executing while the receivers are run.
     *
     * @param intent The Intent to broadcast; all receivers matching this
     *               Intent will receive the broadcast.
     * @return Returns true if the intent has been scheduled for delivery to one or more
     * broadcast receivers.  (Note tha delivery may not ultimately take place if one of those
     * receivers is unregistered before it is dispatched.)
     * @see #registerReceiver
     */
    public boolean sendBroadcast(Intent intent) {

        return sendBroadcast(intent, 0);
    }

    public boolean sendBroadcast(Intent intent, int delay) {
        synchronized (mReceivers) {
            final String action = intent.getAction();
            final String type = intent.resolveTypeIfNeeded(
                    mAppContext.getContentResolver());
            final Uri data = intent.getData();
            final String scheme = intent.getScheme();
            final Set<String> categories = intent.getCategories();

            final boolean debug = DEBUG ||
                    ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
            if (debug) Log.v(
                    TAG, "Resolving type " + type + " scheme " + scheme
                            + " of intent " + intent);

            ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> entries = mActions.get(intent.getAction());
            if (entries != null) {
                if (debug) Log.v(TAG, "Action list: " + entries);

                ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> receivers = null;
                for (int i = 0; i < entries.size(); i++) {
                    SigMobLocalBroadcastManager.ReceiverRecord receiver = entries.get(i);
                    if (debug) Log.v(TAG, "Matching against filter " + receiver.filter);

                    if (receiver.broadcasting) {
                        if (debug) {
                            Log.v(TAG, "  Filter's target already added");
                        }
                        continue;
                    }

                    int match = receiver.filter.match(action, type, scheme, data,
                            categories, "LocalBroadcastManager");
                    if (match >= 0) {
                        if (debug) Log.v(TAG, "  Filter matched!  match=0x" +
                                Integer.toHexString(match));
                        if (receivers == null) {
                            receivers = new ArrayList<SigMobLocalBroadcastManager.ReceiverRecord>();
                        }
                        receivers.add(receiver);
                        receiver.broadcasting = true;
                    } else {
                        if (debug) {
                            String reason;
                            switch (match) {
                                case IntentFilter.NO_MATCH_ACTION:
                                    reason = "action";
                                    break;
                                case IntentFilter.NO_MATCH_CATEGORY:
                                    reason = "category";
                                    break;
                                case IntentFilter.NO_MATCH_DATA:
                                    reason = "data";
                                    break;
                                case IntentFilter.NO_MATCH_TYPE:
                                    reason = "type";
                                    break;
                                default:
                                    reason = "unknown reason";
                                    break;
                            }
                            Log.v(TAG, "  Filter did not match: " + reason);
                        }
                    }
                }

                if (receivers != null) {
                    for (int i = 0; i < receivers.size(); i++) {
                        receivers.get(i).broadcasting = false;
                    }
                    mPendingBroadcasts.add(new SigMobLocalBroadcastManager.BroadcastRecord(intent, receivers));
                    if (!mHandler.hasMessages(MSG_EXEC_PENDING_BROADCASTS)) {
                        if (delay > 0) {
                            mHandler.sendEmptyMessageDelayed(MSG_EXEC_PENDING_BROADCASTS, delay);
                        } else {
                            mHandler.sendEmptyMessage(MSG_EXEC_PENDING_BROADCASTS);

                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Like {@link #sendBroadcast(Intent)}, but if there are any receivers for
     * the Intent this function will block and immediately dispatch them before
     * returning.
     */
    public void sendBroadcastSync(Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    void executePendingBroadcasts() {
        while (true) {
            final SigMobLocalBroadcastManager.BroadcastRecord[] brs;
            synchronized (mReceivers) {
                final int N = mPendingBroadcasts.size();
                if (N <= 0) {
                    return;
                }
                brs = new SigMobLocalBroadcastManager.BroadcastRecord[N];
                mPendingBroadcasts.toArray(brs);
                mPendingBroadcasts.clear();
            }
            for (int i = 0; i < brs.length; i++) {
                final SigMobLocalBroadcastManager.BroadcastRecord br = brs[i];
                final int nbr = br.receivers.size();
                for (int j = 0; j < nbr; j++) {
                    final SigMobLocalBroadcastManager.ReceiverRecord rec = br.receivers.get(j);
                    if (!rec.dead) {
                        rec.receiver.onReceive(mAppContext, br.intent);
                    }
                }
            }
        }
    }

    private static final class ReceiverRecord {
        final IntentFilter filter;
        final BroadcastReceiver receiver;
        boolean broadcasting;
        boolean dead;

        ReceiverRecord(IntentFilter _filter, BroadcastReceiver _receiver) {
            filter = _filter;
            receiver = _receiver;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(128);
            builder.append("Receiver{");
            builder.append(receiver);
            builder.append(" filter=");
            builder.append(filter);
            if (dead) {
                builder.append(" DEAD");
            }
            builder.append("}");
            return builder.toString();
        }
    }

    private static final class BroadcastRecord {
        final Intent intent;
        final ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> receivers;

        BroadcastRecord(Intent _intent, ArrayList<SigMobLocalBroadcastManager.ReceiverRecord> _receivers) {
            intent = _intent;
            receivers = _receivers;
        }
    }
}