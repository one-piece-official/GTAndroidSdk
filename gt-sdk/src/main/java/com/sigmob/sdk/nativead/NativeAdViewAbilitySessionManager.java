package com.sigmob.sdk.nativead;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.SessionManager;
import com.sigmob.sdk.base.models.BaseAdUnit;

import java.util.HashSet;

public class NativeAdViewAbilitySessionManager implements SessionManager {

    private final HashSet<NativeAdViewAbilitySession> mViewAbilitySessions;
    private int mDuration = 0;
    private BaseAdUnit mAdUnit = null;
    private boolean mIsForceShow;
    private int mShow_Skip_Time;
    private int mEndTime = 0;


    public NativeAdViewAbilitySessionManager() {
        mViewAbilitySessions = new HashSet<>();
        mViewAbilitySessions.add(new NativeAdViewAbilitySession());
    }


    public void createDisplaySession(BaseAdUnit adUnit) {

        if (adUnit == null) {
            SigmobLog.e("createDisplaySession() called  mAdUnit is null");
            return;
        }
        for (final NativeAdViewAbilitySession session : mViewAbilitySessions) {

            final boolean successful = session.createDisplaySession(adUnit);
        }
        mAdUnit = adUnit;
        mAdUnit.setSessionManager(this);
    }

    public void recordDisplayEvent(String event, int currentPosition) {
        if (mAdUnit == null) {
            SigmobLog.e("createDisplaySession() called  mAdUnit is null");
            return;
        }

        for (final NativeAdViewAbilitySession session : mViewAbilitySessions) {

            final boolean successful = session.recordDisplayEvent(mAdUnit, event, currentPosition);

        }
    }

    public void endDisplaySession() {
        if (mAdUnit == null) {
            SigmobLog.e("endDisplaySession() called  mAdUnit is null");
            return;
        }
        for (final NativeAdViewAbilitySession session : mViewAbilitySessions) {

            final boolean successful = session.endDisplaySession(mAdUnit);

        }
        mAdUnit.setSessionManager(null);
        mAdUnit.destroy();
        mAdUnit = null;
    }

}
