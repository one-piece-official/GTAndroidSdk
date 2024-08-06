package com.gt.sdk.admanager;

import static com.czhj.sdk.common.Constants.FAIL;
import static com.czhj.sdk.common.Constants.SUCCESS;

import android.content.Context;
import android.content.SharedPreferences;

import com.czhj.sdk.common.Constants;
import com.gt.sdk.GtConstants;
import com.gt.sdk.base.point.GtPointCategory;
import com.gt.sdk.base.point.GtPointEntityPrivacy;
import com.gt.sdk.base.point.PointType;
import com.gt.sdk.utils.GtSharedPreUtil;


public class PrivacyDataManager {

    private static volatile PrivacyDataManager sInstance;

    private Context mContext;

    private boolean mIsPersonalizedAdvertisingOn;
    private boolean mIsAdult;
    private int mUserAge = 0;
    private int mAge_restricted = 0;
    private static boolean extGDPRRegion = false;
    private static int mGDPRConsentStatus = 0;

    private Boolean init_personalized_state;

    public static PrivacyDataManager getInstance() {
        if (sInstance == null) {
            synchronized (PrivacyDataManager.class) {
                if (sInstance == null) {
                    sInstance = new PrivacyDataManager();
                }
            }
        }
        return sInstance;
    }

    public static boolean canCollectPersonalInformation() {
        boolean isAccept = (getGDPRConsentStatus() == 1);
        if (isAccept || !PrivacyDataManager.getIsGDPRRegion()) {
            return true;
        }
        return false;
    }

    public int getUserAge() {
        return mUserAge;
    }

    public void setUserAge(int userAge, boolean isPoint) {
        mUserAge = userAge;

        SharedPreferences.Editor editor = GtSharedPreUtil.getSharedPreferences(mContext).edit();
        editor.putInt(Constants.USER_AGE, mUserAge);
        editor.apply();

        if (isPoint) {
            GtPointEntityPrivacy pointEntityPrivacy = new GtPointEntityPrivacy();
            pointEntityPrivacy.setAge(String.valueOf(userAge));
            pointEntityPrivacy.setAc_type(PointType.GT_PRIVACY);
            pointEntityPrivacy.setSub_category(GtPointCategory.AGE);
            pointEntityPrivacy.setCategory(GtPointCategory.PRIVACY);
            pointEntityPrivacy.commit();
        }
    }

    public int getAge_restricted() {
        return mAge_restricted;
    }

    public void setAge_restricted(int age_restricted, boolean isPoint) {
        if (mAge_restricted != age_restricted) {
            notifyPrivacyStatusChange();
        }
        mAge_restricted = age_restricted;
        SharedPreferences.Editor editor = GtSharedPreUtil.getSharedPreferences(mContext).edit();
        editor.putInt(Constants.AGE_RESTRICTED_STATUS, mAge_restricted);
        editor.apply();
        if (isPoint) {
            GtPointEntityPrivacy pointEntityPrivacy = new GtPointEntityPrivacy();
            pointEntityPrivacy.setAge_restricted(String.valueOf(age_restricted));
            pointEntityPrivacy.setAc_type(PointType.GT_PRIVACY);
            pointEntityPrivacy.setSub_category(GtPointCategory.COPPA);
            pointEntityPrivacy.setCategory(GtPointCategory.PRIVACY);
            pointEntityPrivacy.commit();
        }
    }

    public boolean isAdult() {
        return mIsAdult;
    }

    public void setAdult(boolean isAdult, boolean isPoint) {
        mIsAdult = isAdult;
        if (isPoint) {
            GtPointEntityPrivacy pointEntityPrivacy = new GtPointEntityPrivacy();
            pointEntityPrivacy.setIs_minor(mIsAdult ? GtConstants.FAIL : GtConstants.SUCCESS);
            pointEntityPrivacy.setAc_type(PointType.GT_PRIVACY);
            pointEntityPrivacy.setSub_category(GtPointCategory.ADULT);
            pointEntityPrivacy.setCategory(GtPointCategory.PRIVACY);
            pointEntityPrivacy.commit();
        }
    }

    private void notifyPrivacyStatusChange() {

    }

    public boolean isPersonalizedAdvertisingOn() {
        return mIsPersonalizedAdvertisingOn;
    }

    public void setPersonalizedAdvertisingOn(boolean isPersonalizedAdvertisingOn, boolean isPoint) {
        if (init_personalized_state == null) {
            init_personalized_state = isPersonalizedAdvertisingOn;
        }

        if (mIsPersonalizedAdvertisingOn != isPersonalizedAdvertisingOn) {
            notifyPrivacyStatusChange();
        }

        mIsPersonalizedAdvertisingOn = isPersonalizedAdvertisingOn;

        if (isPoint) {
            GtPointEntityPrivacy pointEntityPrivacy = new GtPointEntityPrivacy();
            pointEntityPrivacy.setIs_unpersonalized(mIsPersonalizedAdvertisingOn ? GtConstants.FAIL : GtConstants.SUCCESS);
            pointEntityPrivacy.setAc_type(PointType.GT_PRIVACY);
            pointEntityPrivacy.setSub_category(GtPointCategory.PERSONALIZED);
            pointEntityPrivacy.setCategory(GtPointCategory.PRIVACY);
            pointEntityPrivacy.commit();
        }
    }

    public boolean changeRecommendationState() {
        return init_personalized_state == null || init_personalized_state != mIsPersonalizedAdvertisingOn;
    }

    public static int getGDPRConsentStatus() {
        return mGDPRConsentStatus;
    }

    public void setGDPRConsentStatus(int gdprConSentStatus, boolean isPoint) {
        if (mGDPRConsentStatus != gdprConSentStatus) {
            notifyPrivacyStatusChange();
        }

        mGDPRConsentStatus = gdprConSentStatus;
        SharedPreferences.Editor editor = GtSharedPreUtil.getSharedPreferences(mContext).edit();
        editor.putInt(Constants.GDPR_CONSENT_STATUS, gdprConSentStatus);
        editor.apply();

        if (isPoint) {
            reportGDPRInfoTrack();
        }
    }

    public void reportGDPRInfoTrack() {

        GtPointEntityPrivacy pointEntityPrivacy = new GtPointEntityPrivacy();
        pointEntityPrivacy.setUser_consent(String.valueOf(getGDPRConsentStatus()));
        pointEntityPrivacy.setGdpr_region(getIsGDPRRegion() ? SUCCESS : FAIL);
        pointEntityPrivacy.setSub_category(GtPointCategory.CONSENT);
        pointEntityPrivacy.setCategory(GtPointCategory.GDPR);
        pointEntityPrivacy.setAc_type(PointType.GT_PRIVACY);
        pointEntityPrivacy.commit();
    }

    public void setExtGDPRRegion(boolean extGDPRRegion) {
        SharedPreferences.Editor editor = GtSharedPreUtil.getSharedPreferences(mContext).edit();
        editor.putBoolean(Constants.EXT_GDPR_REGION, extGDPRRegion);
        editor.apply();

        PrivacyDataManager.extGDPRRegion = extGDPRRegion;
        reportGDPRInfoTrack();
    }

    public static boolean getIsGDPRRegion() {
//        return extGDPRRegion && WMSdkConfig.isGDPRRegion();
        return extGDPRRegion;
    }

    public void initialize(Context context) {
        if (context != null) {
            mContext = context.getApplicationContext();
            extGDPRRegion = GtSharedPreUtil.getSharedPreferences(mContext).getBoolean(Constants.EXT_GDPR_REGION, false);
        }
    }

    public Context getContext() {
        return mContext;
    }


}