package com.sigmob.sdk.base.common;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.models.BaseAdUnit;

import java.util.HashSet;
import java.util.Set;

public class ExternalViewabilitySessionManager implements SessionManager {

    private final Set<ExternalViewabilitySession> mViewabilitySessions;
    private BaseAdUnit mAdUnit;

    public ExternalViewabilitySessionManager() {
        mViewabilitySessions = new HashSet<>();
        mViewabilitySessions.add(new RewardVideoViewAbilitySession());

    }

    public void onVideoShowSkip(boolean isForceShow, int currentPosition) {

        if (mAdUnit == null) {
            SigmobLog.e("onVideoShowSkip() called  mAdUnit is null");
            return;
        }
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final boolean successful = session.ondDisplayShowSkip(mAdUnit, isForceShow, currentPosition);
        }
    }


    public void onVideoPrepared(int duration, int endTime) {

        if (mAdUnit == null) {
            SigmobLog.e("onVideoPrepared() called  mAdUnit is null");
            return;
        }
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final boolean successful = session.onVideoPrepared(mAdUnit, duration, endTime);

        }

    }


    public void createDisplaySession(BaseAdUnit adUnit) {

        if (adUnit == null) {
            SigmobLog.e("createDisplaySession() called  adUnit is null");
            return;
        }
        mAdUnit = adUnit;
        adUnit.setSessionManager(this);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {

            final boolean successful = session.createDisplaySession(adUnit);

        }
    }


    public void endDisplaySession() {
        SigmobLog.d("endDisplaySession() called");
        if (mAdUnit == null) {
            SigmobLog.e("endDisplaySession() called  mAdUnit is null");
            return;
        }
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final boolean successful = session.endDisplaySession(mAdUnit);
        }
        mAdUnit.setSessionManager(null);
        mAdUnit.destroy();
        mAdUnit = null;

    }

    public void recordDisplayEvent(String event, int currentPosition) {
        if (mAdUnit == null) {
            SigmobLog.e("recordDisplayEvent() called  mAdUnit is null");
            return;
        }
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final boolean successful = session.recordDisplayEvent(mAdUnit, event, currentPosition);
        }
    }
}
