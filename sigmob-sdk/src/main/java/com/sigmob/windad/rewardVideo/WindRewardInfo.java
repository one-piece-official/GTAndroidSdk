package com.sigmob.windad.rewardVideo;


import java.util.HashMap;

public class WindRewardInfo {

    private final boolean isReward;

    private HashMap<String, String> options;

    public WindRewardInfo(boolean isReward) {
        this.isReward = isReward;
    }
    public WindRewardInfo(boolean isReward,HashMap<String, String> options) {
        this.isReward = isReward;
        this.options = options;
    }
    public void setOptions(HashMap<String, String> options) {
        this.options = options;
    }

    public HashMap<String, String> getOptions() {
        return options;
    }

    public boolean isReward() {
        return isReward;
    }

    @Override
    public String toString() {
        return "WindRewardInfo{" +
                "isReward=" + isReward +
                ", options=" + options +
                '}';
    }
}
