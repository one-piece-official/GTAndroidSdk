package com.gt.adsdk;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class AdRequest {

    private String codeId;
    private int width;
    private int height;
    private int adCount;
    private String userID;
    private int orientation;
    private int adType;
    private Map<String, String> extOption;
    private int rewardAmount;
    private String rewardName;

    private AdRequest() {
        this.orientation = 2;
    }

    public Map<String, String> getExtOption() {
        return this.extOption;
    }

    public String getCodeId() {
        return this.codeId;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }


    public int getAdCount() {
        return this.adCount;
    }

    public void setAdCount(int adCount) {
        this.adCount = adCount;
    }

    public String getUserID() {
        return this.userID;
    }

    public int getOrientation() {
        return this.orientation;
    }

    public int getAdType() {
        return this.adType;
    }


    public String getRewardName() {
        return this.rewardName;
    }

    public int getRewardAmount() {
        return this.rewardAmount;
    }

    public JSONObject toJsonObj() {
        JSONObject var1 = new JSONObject();
        try {
            var1.put("codeId", this.codeId);
            var1.put("adType", this.adType);
            var1.put("width", this.width);
            var1.put("height", this.height);
            var1.put("adCount", this.adCount);
            var1.put("userID", this.userID);
            var1.put("mOrientation", this.orientation);
            var1.put("extOption", this.extOption);
            var1.put("rewardName", this.rewardName);
            var1.put("rewardAmount", this.rewardAmount);
        } catch (Exception var2) {
        }

        return var1;
    }

    public static class Builder {
        private String codeId;
        private int width = 1080;
        private int height = 0;
        private int adCount = 1;
        private String userID = "defaultUser";
        private int orientation = 2;
        private int adType;
        private Map<String, String> extOption;
        private int rewardAmount;
        private String rewardName;

        public Builder() {

        }

        public Builder setAdType(int adType) {
            this.adType = adType;
            return this;
        }

        public Builder setCodeId(String codeId) {
            this.codeId = codeId;
            return this;
        }


        public Builder setExtOption(Map<String, String> option) {
            this.extOption = option;
            return this;
        }


        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }


        public Builder setAdCount(int adCount) {
            if (adCount <= 0) {
                adCount = 1;
            }

            if (adCount > 5) {
                adCount = 5;
            }

            this.adCount = adCount;
            return this;
        }

        public Builder setUserID(String userID) {
            this.userID = userID;
            return this;
        }

        public Builder setOrientation(int orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder setRewardName(String rewardName) {
            this.rewardName = rewardName;
            return this;
        }

        public Builder setRewardAmount(int rewardAmount) {
            this.rewardAmount = rewardAmount;
            return this;
        }

        public AdRequest build() {
            AdRequest request;
            (request = new AdRequest()).codeId = this.codeId;
            request.adCount = this.adCount;
            request.width = this.width;
            request.height = this.height;
            request.userID = this.userID;
            request.orientation = this.orientation;
            request.extOption = this.extOption;
            request.adType = this.adType;
            request.rewardName = this.rewardName;
            request.rewardAmount = this.rewardAmount;
            return request;
        }
    }
}
