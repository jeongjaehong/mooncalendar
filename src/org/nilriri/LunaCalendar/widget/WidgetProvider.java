package org.nilriri.LunaCalendar.widget;

import java.util.Calendar;

import org.nilriri.LunaCalendar.LunarCalendar;
import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.Lunar2Solar;
import org.nilriri.LunaCalendar.tools.Prefs;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    // log tag
    private static final String TAG = "WidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        // For each widget that needs an update, get the text that we should display:
        //   - Create a RemoteViews object for it
        //   - Set the text in the RemoteViews object
        //   - Tell the AppWidgetManager to show that views object for the widget.

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "onDeleted");
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            WidgetConfigure.removePrefData(context, appWidgetIds[i]);
        }
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled");
        // When the first widget is created, register for the TIMEZONE_CHANGED and TIME_CHANGED
        // broadcasts.  We don't want to be listening for these if nobody has our widget active.
        // This setting is sticky across reboots, but that doesn't matter, because this will
        // be called after boot if there is a widget instance for this provider.
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("org.nilriri.LunaCalendar", ".widget.WidgetBroadcastReceiver"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // When the first widget is created, stop listening for the TIMEZONE_CHANGED and
        // TIME_CHANGED broadcasts.
        Log.d(TAG, "onDisabled");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName("org.nilriri.LunaCalendar", ".widget.WidgetBroadcastReceiver"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        super.onDisabled(context);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId);
        // Getting the string this way allows the string to be localized.  The format
        // string is filled in using java.util.Formatter-style format strings.

        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        int layout = getWidgetLayout(context, appWidgetManager, appWidgetId);

        RemoteViews views = new RemoteViews(context.getPackageName(), layout);

        ScheduleDaoImpl dao = new ScheduleDaoImpl(context, null, Prefs.getSDCardUse(context));

        String mDday_title = "";
        String mDday_msg = "";
        String mDday_date = "";
        int kind = 6;

        Cursor cursor = dao.queryWidgetByID(WidgetConfigure.getDataPk(context, appWidgetId));
        if (cursor.moveToNext()) {

            String D_dayTitle = cursor.getString(0);
            String D_dayDate = cursor.getString(1);
            int D_Day = cursor.getInt(2);
            kind = cursor.getInt(3);

            switch (WidgetConfigure.getWidgetKind(context, appWidgetId)) {
                case Common.D_DAY_WIDGET: {//dday
                    if (D_Day == 0) {
                        mDday_title += "D day";
                    } else if (D_Day > 0) {
                        mDday_title += "D+" + D_Day + context.getResources().getString(R.string.day_label) + "";
                    } else {
                        mDday_title += "D-" + Math.abs(D_Day - 1) + context.getResources().getString(R.string.day_label) + "";
                    }
                    break;
                }
                case Common.ALLEVENT_WIDGET: {// 모든일정
                    mDday_title = "일정";
                    break;
                }
                case Common.ANNIVERSARY_WIDGET: {//기념일
                    mDday_title = "기념일";
                    break;
                }
            }

            mDday_msg += D_dayTitle.length() >= 18 ? D_dayTitle.substring(0, 18) + "..." : D_dayTitle;
            //mDday_msg += "\n" + D_dayDate;
            mDday_date = D_dayDate;

            mDday_msg = mDday_msg == null ? "" : mDday_msg;

        } else {
            String daynm[] = { "일", "월", "화", "수", "목", "금", "토" };
            Calendar c = Calendar.getInstance();
            mDday_title = daynm[c.get(Calendar.DAY_OF_WEEK) - 1];
            mDday_title += ", W" + c.get(Calendar.WEEK_OF_YEAR);

            mDday_msg = Common.fmtDate(c);
            mDday_date = "음력 : " + Common.fmtDate(Lunar2Solar.s2l(c)).substring(5);
        }

        views.setTextViewText(R.id.text_dday, mDday_title);
        views.setTextViewText(R.id.text_title, mDday_msg);
        views.setTextViewText(R.id.text_contents, mDday_date);

        Bitmap bitmap = null;
        switch (kind) {
            case 3:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.flag3);
                break;
            case 5:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.dday);
                break;
            default:
            case 6:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pen);
                break;
        }

        views.setImageViewBitmap(R.id.widget_icon, bitmap);

        cursor.close();
        dao.close();

        views.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(context, 0, new Intent(context, LunarCalendar.class), 0));

        appWidgetManager.updateAppWidget(appWidgetId, views);
        //ComponentName componentName = new ComponentName(context, WidgetProvider.class);
        //appWidgetManager.updateAppWidget(componentName, views);

    }

    private static int getWidgetColor(Context context, int appWidgetId) {
        return WidgetConfigure.getWidgetColor(context, appWidgetId);
    }

    public static int getWidgetLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        AppWidgetProviderInfo wf = appWidgetManager.getAppWidgetInfo(appWidgetId);

        switch (wf.initialLayout) {

            default:
            case R.layout.widget1x1_transparent:

                switch (getWidgetColor(context, appWidgetId)) {

                    case 0:
                        return R.layout.widget1x1_transparent;
                    case 1:
                        return R.layout.widget1x1_black;
                    case 2:
                        return R.layout.widget1x1_orange;
                    case 3:
                        return R.layout.widget1x1_green;
                    case 4:
                        return R.layout.widget1x1_blue;
                    default:
                        return R.layout.widget1x1_black;
                }

            case R.layout.widget2x2_transparent:

                switch (getWidgetColor(context, appWidgetId)) {

                    case 0:
                        return R.layout.widget2x2_transparent;
                    case 1:
                        return R.layout.widget2x2_black;
                    case 2:
                        return R.layout.widget2x2_orange;
                    case 3:
                        return R.layout.widget2x2_green;
                    case 4:
                        return R.layout.widget2x2_blue;
                    default:
                        return R.layout.widget2x2_transparent;
                }
            case R.layout.widget4x4_transparent:

                switch (getWidgetColor(context, appWidgetId)) {

                    case 0:
                        return R.layout.widget4x4_transparent;

                    default:
                        return R.layout.widget4x4_transparent;
                }
        }

    }

}
