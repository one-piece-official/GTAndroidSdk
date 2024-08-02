package com.sigmob.sdk.base.common;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class SigToast extends Toast {
    public SigToast(Context context) {
        super(context);
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER,0,0);
        return toast;
    }
}
