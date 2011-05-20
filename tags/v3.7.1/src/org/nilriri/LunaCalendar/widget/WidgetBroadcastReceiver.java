/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nilriri.LunaCalendar.widget;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * A BroadcastReceiver that listens for updates for the WidgetProvider.  This
 * BroadcastReceiver starts off disabled, and we only enable it when there is a widget
 * instance created, in order to only receive notifications when we need them.
 */
public class WidgetBroadcastReceiver extends android.content.BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.d("ExmampleBroadcastReceiver", "intent=" + intent);

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIMEZONE_CHANGED) || //
                action.equals(Intent.ACTION_TIME_CHANGED) || //
                action.equals(Intent.ACTION_TIME_TICK)) {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            List<AppWidgetProviderInfo> awp = awm.getInstalledProviders();

            for (int i = 0; i < awp.size(); i++) {
                AppWidgetProviderInfo af = awp.get(i);
                int[] appWidgetIds = awm.getAppWidgetIds(af.provider);
                if (appWidgetIds != null) {
                    for (int ji = 0; ji < appWidgetIds.length; ji++) {
                        WidgetProvider.updateAppWidget(context, awm, appWidgetIds[ji]);

                    }
                }
            }
        }
    }
}
