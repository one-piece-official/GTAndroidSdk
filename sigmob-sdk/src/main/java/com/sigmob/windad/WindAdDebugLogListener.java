package com.sigmob.windad;

public interface WindAdDebugLogListener {

    void windAdDebugLog(String msg, WindAdLogLevel logLevel);

    enum WindAdLogLevel {
        WindLogLevelError,
        WindLogLevelWarning,
        WindLogLevelInformation,
        WindLogLevelDebug
    }
}
