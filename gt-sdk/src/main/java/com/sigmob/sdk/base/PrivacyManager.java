package com.sigmob.sdk.base;

import static com.czhj.sdk.common.Constants.FAIL;
import static com.czhj.sdk.common.Constants.SUCCESS;

import android.content.Context;
import android.content.SharedPreferences;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobPrivacy;
import com.sigmob.sdk.base.mta.PointType;
import com.sigmob.sdk.base.utils.WindPrefsUtils;
import com.sigmob.windad.WindAgeRestrictedUserStatus;
import com.sigmob.windad.WindConsentStatus;
import com.sigmob.windad.consent.WindAdConsentInformation;

public class PrivacyManager {


    private boolean mIsAdult = true;
    private int mAge_restricted;
    private boolean mIsPersonalizedAdvertisingOn = true;
    private int mUserAge;
    private static PrivacyManager gInstance;
    private boolean extGDPRRegion;
    private int mGDPRConsentStatus;
    private Boolean init_personalized_state;

    private PrivacyManager() {

        //coppa
        try {
            mAge_restricted = WindPrefsUtils.getPrefs().getInt(Constants.AGE_RESTRICTED_STATUS,
                    WindAgeRestrictedUserStatus.Unknown);
        } catch (Throwable th) {

        }

        try {
            mUserAge = WindPrefsUtils.getPrefs().getInt(Constants.USER_AGE, 0);

        } catch (Throwable t) {
        }

        //gdpr
        try {
            mGDPRConsentStatus = WindPrefsUtils.getPrefs().getInt(Constants.GDPR_CONSENT_STATUS, WindConsentStatus.UNKNOWN);

        } catch (Throwable t) {

        }

        try {
            extGDPRRegion = WindPrefsUtils.getPrefs().getBoolean(Constants.EXT_GDPR_REGION, false);

        } catch (Throwable t) {

        }

    }

    public static synchronized PrivacyManager getInstance() {
        if (gInstance == null) {
            synchronized (PrivacyManager.class) {

                gInstance = new PrivacyManager();
            }
        }
        return gInstance;
    }

    public int getUserAge() {
        return mUserAge;
    }

    public void setUserAge(int userAge, boolean sendTrack) {
        mUserAge = userAge;

        SharedPreferences.Editor editor = WindPrefsUtils.getPrefs().edit();
        editor.putInt(Constants.USER_AGE, mUserAge);
        editor.apply();

        if (sendTrack) {
            trackUserAge(userAge);
        }
    }


    public int getAge_restricted() {
        return mAge_restricted;
    }

    public void setAge_restricted(int age_restricted, boolean sendTrack) {
        mAge_restricted = age_restricted;
        SharedPreferences.Editor editor = WindPrefsUtils.getPrefs().edit();
        editor.putInt(Constants.AGE_RESTRICTED_STATUS, mAge_restricted);
        editor.apply();

        if (sendTrack) {
            trackAgeRestricted(age_restricted);
        }

    }


    public boolean isAdult() {
        return mIsAdult;
    }

    public void setAdult(boolean isAdult, boolean sendTrack) {
        SigmobLog.i("PrivacyManager -> setAdult " + isAdult);
        mIsAdult = isAdult;

        if (sendTrack) {
            trackAdult(isAdult);
        }

    }


    public boolean isPersonalizedAdvertisingOn() {
        return mIsPersonalizedAdvertisingOn;
    }

    public void setPersonalizedAdvertisingOn(boolean isPersonalizedAdvertisingOn, boolean sendTrack) {


        if (init_personalized_state == null) {
            init_personalized_state = isPersonalizedAdvertisingOn;
        }
        SigmobLog.i("PrivacyManager -> setPersonalized " + isPersonalizedAdvertisingOn);

        mIsPersonalizedAdvertisingOn = isPersonalizedAdvertisingOn;

        if (sendTrack) {

            trackPersonalizedOn(isPersonalizedAdvertisingOn);
        }

    }


    public boolean changeRecommendationState() {
        return init_personalized_state == null || init_personalized_state != mIsPersonalizedAdvertisingOn;
    }

    public int getGDPRConsentStatus() {

        return mGDPRConsentStatus;
    }

    public void setGDPRConsentStatus(int gdprConSentStatus, boolean sendtrack) {


        this.mGDPRConsentStatus = gdprConSentStatus;

        SharedPreferences.Editor editor = WindPrefsUtils.getPrefs().edit();
        editor.putInt(Constants.GDPR_CONSENT_STATUS, gdprConSentStatus);
        editor.apply();

        if (sendtrack) {
            WindPrivacyInfoTrack();
        }
    }

    private void trackUserAge(int userAge) {
        PointEntitySigmobPrivacy pointEntityPrivacy = new PointEntitySigmobPrivacy();
        pointEntityPrivacy.setAge(String.valueOf(userAge));
        pointEntityPrivacy.setAc_type(PointType.GDPR_CONSENT);
        pointEntityPrivacy.setSub_category(PointCategory.COPPA);
        pointEntityPrivacy.setCategory(PointCategory.PRIVACY);
        pointEntityPrivacy.commit();
    }

    private void trackAgeRestricted(int age_restricted) {

        PointEntitySigmobPrivacy pointEntityPrivacy = new PointEntitySigmobPrivacy();
        pointEntityPrivacy.setAge_restricted(String.valueOf(age_restricted));
        pointEntityPrivacy.setAc_type(PointType.GDPR_CONSENT);
        pointEntityPrivacy.setSub_category(PointCategory.COPPA);
        pointEntityPrivacy.setCategory(PointCategory.PRIVACY);
        pointEntityPrivacy.commit();
    }

    private void trackAdult(boolean isAdult) {
        PointEntitySigmobPrivacy pointEntityPrivacy = new PointEntitySigmobPrivacy();
        pointEntityPrivacy.setAc_type(PointType.GDPR_CONSENT);
        pointEntityPrivacy.setSub_category(PointCategory.ADULT);
        pointEntityPrivacy.setCategory(PointCategory.PRIVACY);
        pointEntityPrivacy.setIs_minor(isAdult ? FAIL : SUCCESS);
        pointEntityPrivacy.commit();
    }

    private void trackPersonalizedOn(boolean isPersonalizedAdvertisingOn) {
        PointEntitySigmobPrivacy pointEntityPrivacy = new PointEntitySigmobPrivacy();
        pointEntityPrivacy.setAc_type(PointType.GDPR_CONSENT);
        pointEntityPrivacy.setSub_category(PointCategory.PERSONALIZED);
        pointEntityPrivacy.setCategory(PointCategory.PRIVACY);
        pointEntityPrivacy.setIs_unpersonalized(isPersonalizedAdvertisingOn ? FAIL : SUCCESS);
        pointEntityPrivacy.commit();
    }

    private void WindPrivacyInfoTrack() {

        PointEntitySigmobPrivacy pointEntityPrivacy = new PointEntitySigmobPrivacy();
        pointEntityPrivacy.setUser_consent(String.valueOf(getGDPRConsentStatus()));
        pointEntityPrivacy.setGdpr_region(getIsGDPRRegion() ? SUCCESS : FAIL);

        try {
            Context context = SDKContext.getApplicationContext();
            boolean isRequestLocationInEeaOrUnknown = WindAdConsentInformation.getInstance(context)
                    .isRequestLocationInEeaOrUnknown();
            pointEntityPrivacy.setGdpr_dialog_region(isRequestLocationInEeaOrUnknown ? SUCCESS : FAIL);

        } catch (Throwable th) {

        }
        pointEntityPrivacy.setSub_category(PointCategory.CONSENT);
        pointEntityPrivacy.setCategory(PointCategory.GDPR);
        pointEntityPrivacy.setAc_type(PointType.GDPR_CONSENT);
        pointEntityPrivacy.commit();


    }

    public void setExtGDPRRegion(Boolean extGDPRRegion) {

        if (extGDPRRegion != null) {
            SharedPreferences.Editor editor = WindPrefsUtils.getPrefs().edit();
            editor.putBoolean(Constants.EXT_GDPR_REGION, extGDPRRegion);
            editor.apply();
            this.extGDPRRegion = extGDPRRegion;
        }

        if (canCollectPersonalInformation()) {
            WindPrivacyInfoTrack();
            trackPersonalizedOn(mIsPersonalizedAdvertisingOn);
            trackAdult(mIsAdult);
            trackAgeRestricted(mAge_restricted);
            trackUserAge(mUserAge);
        }
    }

    public boolean getIsGDPRRegion() {
//        return extGDPRRegion && WindSDKConfig.isGDPRRegion();
        return extGDPRRegion;
    }

    public boolean canCollectPersonalInformation() {
        boolean isAccept = getGDPRConsentStatus() == 1;
        if (isAccept || !getIsGDPRRegion()) {
            return true;
        }
        return false;
    }


}
