
package com.sigmob.sdk.mraid;

import android.webkit.ConsoleMessage;
import android.webkit.JsResult;



public interface MraidWebViewDebugListener {
    /**
     */
    boolean onJsAlert( String message,  JsResult result);

    /**
     */
    boolean onConsoleMessage( ConsoleMessage consoleMessage);
}
