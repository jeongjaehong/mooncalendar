package org.nilriri.LunaCalendar.widget;

import org.nilriri.LunaCalendar.tools.Common;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class WidgetBroadcastReceiver extends android.content.BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.d(Common.TAG, "intent=" + intent);

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) || //
                action.equals(Intent.ACTION_TIME_CHANGED) || //
                action.equals(Intent.ACTION_TIME_TICK)) {
            WidgetUtil.refreshWidgets(context);
        }

    }
}
