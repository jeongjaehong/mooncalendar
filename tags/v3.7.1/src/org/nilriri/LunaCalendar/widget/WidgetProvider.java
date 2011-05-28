package org.nilriri.LunaCalendar.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.nilriri.LunaCalendar.LunarCalendar;
import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.gcal.EventEntry;
import org.nilriri.LunaCalendar.gcal.GoogleUtil;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.Lunar2Solar;
import org.nilriri.LunaCalendar.tools.Prefs;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Spinner;

public class WidgetProvider extends AppWidgetProvider {

    private static Context mContext;
    private static AppWidgetManager mAppWidgetManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(Common.TAG, "onUpdate");
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
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(Common.TAG, "onReceive=" + intent);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(Common.TAG, "onDeleted");
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            WidgetConfigure.removePrefData(context, appWidgetIds[i]);
        }
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(Common.TAG, "onEnabled");
        // When the first widget is created, register for the TIMEZONE_CHANGED and TIME_CHANGED
        // broadcasts.  We don't want to be listening for these if nobody has our widget active.
        // This setting is sticky across reboots, but that doesn't matter, because this will
        // be called after boot if there is a widget instance for this provider.
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(//
                new ComponentName("org.nilriri.LunaCalendar", //
                        ".widget.WidgetBroadcastReceiver"), //
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, //
                PackageManager.DONT_KILL_APP);

        super.onEnabled(context);

        Common.srartWidgetRefreshService(context);
    }

    @Override
    public void onDisabled(Context context) {
        // When the first widget is created, stop listening for the TIMEZONE_CHANGED and
        // TIME_CHANGED broadcasts.
        Log.d(Common.TAG, "onDisabled");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(//
                new ComponentName("org.nilriri.LunaCalendar",//
                        ".widget.WidgetBroadcastReceiver"), //
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, //
                PackageManager.DONT_KILL_APP);

        super.onDisabled(context);

        Common.stopWidgetRefreshService(context);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        mContext = context;
        mAppWidgetManager = appWidgetManager;
        try {
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

                Log.d(Common.TAG, "Refresh Target=" + appWidgetId);

                int layout = getWidgetLayout(mContext, mAppWidgetManager, appWidgetId);
                RemoteViews views = new RemoteViews(mContext.getPackageName(), layout);
                //  views.setViewVisibility(R.id.sync, View.VISIBLE);
                //   views.setViewVisibility(R.id.content, View.GONE);
                views.setImageViewResource(R.id.widget_icon, android.R.drawable.stat_notify_sync);
                appWidgetManager.updateAppWidget(appWidgetId, views);

                new ShowOnlineCalendar().execute(appWidgetId);
            }
        } catch (Exception e) {
        }
    }

    private static class ShowOnlineCalendar extends AsyncTask<Integer, Void, Void> {
        private List<EventEntry> todayEvents = new ArrayList<EventEntry>();
        private RemoteViews views;
        private ScheduleDaoImpl dao;
        private int mAppWidgetId;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Integer... params) {

            mAppWidgetId = params[0];

            int layout = getWidgetLayout(mContext, mAppWidgetManager, mAppWidgetId);
            views = new RemoteViews(mContext.getPackageName(), layout);

            try {
                dao = new ScheduleDaoImpl(mContext, null, Prefs.getSDCardUse(mContext));
                try {

                    String mDday_title = "";
                    String mDday_msg = "";
                    String mDday_date = "";
                    int kind = 6;
                    Bitmap bitmap = null;

                    Long dataPK = WidgetConfigure.getDataPk(mContext, mAppWidgetId);

                    if (dataPK == -1) {

                        StringBuilder eventdata = new StringBuilder();

                        String url = WidgetConfigure.getWidgetUrl(mContext, mAppWidgetId);

                        if ("".equals(url)) {
                            url = Prefs.getOnlineCalendar(mContext);
                        }

                        if (url.indexOf("&start-max=") <= 0) {
                            Calendar c = Calendar.getInstance();

                            StringBuilder where = new StringBuilder("?start-min=");
                            where.append(Common.fmtDate(c));
                            where.append("&start-max=");
                            c.add(Calendar.DAY_OF_MONTH, 1);
                            where.append(Common.fmtDate(c));
                            url += where.toString();
                        }

                        WidgetConfigure.setWidgetUrl(mContext, mAppWidgetId, url);
                        Log.d(Common.TAG, "url=" + url);

                        GoogleUtil gu = new GoogleUtil(Prefs.getAuthToken(mContext));
                        todayEvents = gu.getEvents(url);
                        if (todayEvents.size() <= 0) {
                            cancel(true);
                        }

                        String names[] = new String[todayEvents.size()];
                        final String contents[] = new String[todayEvents.size()];

                        for (int i = 0; i < todayEvents.size() && i < 10; i++) {
                            names[i] = todayEvents.get(i).getStartDate().substring(5, 10) + " : " + todayEvents.get(i).title;
                            contents[i] = todayEvents.get(i).content;
                            eventdata.append(names[i]).append("\n");
                        }

                        views.setTextViewText(R.id.text_dday, "OnLine");
                        views.setTextViewText(R.id.text_title2, eventdata.toString());
                        views.setViewVisibility(R.id.text_title, View.GONE);
                        views.setViewVisibility(R.id.text_title2, View.VISIBLE);
                        views.setTextViewText(R.id.text_contents, "");

                        bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.flag4);

                        views.setImageViewBitmap(R.id.widget_icon, bitmap);

                        //Intent intent = new Intent(mContext, LunarCalendar.class);
                        //intent.putExtra("pk", WidgetConfigure.getDataPk(mContext, mAppWidgetId));

                        if (contents.length > 0) {
                            if (contents[0].indexOf("bindex:") >= 0) {

                                Intent intent = new Intent();//mContext, LunarCalendar.class);

                                intent.setAction("org.nilriri.webbibles.VIEW");
                                intent.setType("vnd.org.nilriri/web-bible");

                                intent.putExtra("VERSION", 0);
                                intent.putExtra("VERSION2", 0);
                                intent.putExtra("BOOK", 0);
                                intent.putExtra("CHAPTER", 0);
                                intent.putExtra("VERSE", 0);
                                intent.putExtra("BPLANT", names);
                                intent.putExtra("BPLANI", contents);

                                views.setOnClickPendingIntent(R.id.widget, PendingIntent.getActivity(mContext, mAppWidgetId, intent, 0));

                            }
                        } else {

                            Intent intent = new Intent(mContext, WidgetRefreshService.class);
                            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                            intent.putExtra("WidgetId", mAppWidgetId);

                            views.setOnClickPendingIntent(R.id.widget, PendingIntent.getService(mContext, mAppWidgetId, intent, 0));

                        }

                        // views.setViewVisibility(R.id.sync, View.GONE);
                        // views.setViewVisibility(R.id.content, View.VISIBLE);
                        // mAppWidgetManager.updateAppWidget(mAppWidgetId, views);
                    } else {

                        Cursor cursor = dao.queryWidgetByID(dataPK);
                        if (cursor.moveToNext()) {

                            String D_dayTitle = cursor.getString(0);
                            String D_dayDate = cursor.getString(1);
                            int D_Day = cursor.getInt(2);
                            kind = cursor.getInt(3);

                            switch (kind) {
                                case Common.D_DAY_WIDGET: {//dday
                                    if (D_Day == 0) {
                                        mDday_title += "D day";
                                    } else if (D_Day > 0) {
                                        mDday_title += "D+" + D_Day + mContext.getResources().getString(R.string.day_label) + "";
                                    } else {
                                        mDday_title += "D-" + Math.abs(D_Day - 1) + mContext.getResources().getString(R.string.day_label) + "";
                                    }

                                    if (WidgetConfigure.getReceiver(mContext)) {

                                        Calendar c = Calendar.getInstance();

                                        Time t = new Time();
                                        t.set(c.getTimeInMillis());

                                        //Log.e("@@@@@@@@", "now=" + t.format3339(false));

                                        mDday_date += "\n" + t.format3339(false).substring(11);

                                    } else {
                                        mDday_msg += D_dayTitle.length() >= 18 ? D_dayTitle.substring(0, 18) + "..." : D_dayTitle;

                                        mDday_msg = mDday_msg == null ? "" : mDday_msg;

                                        mDday_date = D_dayDate;
                                    }

                                    break;
                                }
                                default:
                                case Common.ALLEVENT_WIDGET: {// 모든일정
                                    mDday_title = "일정";

                                    mDday_msg += D_dayTitle.length() >= 18 ? D_dayTitle.substring(0, 18) + "..." : D_dayTitle;
                                    //mDday_msg += "\n" + D_dayDate;
                                    mDday_date = D_dayDate;

                                    mDday_msg = mDday_msg == null ? "" : mDday_msg;
                                    break;
                                }
                                case Common.ANNIVERSARY_WIDGET: {//기념일
                                    mDday_title = "기념일";

                                    mDday_msg += D_dayTitle.length() >= 18 ? D_dayTitle.substring(0, 18) + "..." : D_dayTitle;
                                    //mDday_msg += "\n" + D_dayDate;
                                    mDday_date = D_dayDate;

                                    mDday_msg = mDday_msg == null ? "" : mDday_msg;
                                    break;
                                }
                            }

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

                        switch (kind) {
                            case Common.ANNIVERSARY_WIDGET:
                                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.flag3);
                                break;
                            case Common.D_DAY_WIDGET:
                                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dday);
                                break;
                            default:
                            case Common.ALLEVENT_WIDGET:
                                bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.pen);
                                break;
                        }

                        //views.setTextViewText(R.id.text_contents, mDday_date);

                        views.setImageViewBitmap(R.id.widget_icon, bitmap);

                        cursor.close();

                        Log.d(Common.TAG, "Widget dataPK=" + dataPK);

                        Intent intent = new Intent(mContext, LunarCalendar.class).putExtra("DataPk", dataPK);

                        PendingIntent contentIntent = PendingIntent.getActivity(mContext, mAppWidgetId, intent, 0);

                        views.setOnClickPendingIntent(R.id.widget, contentIntent);

                        // views.setViewVisibility(R.id.sync, View.GONE);
                        //  views.setViewVisibility(R.id.content, View.VISIBLE);
                        // mAppWidgetManager.updateAppWidget(mAppWidgetId, views);
                    }
                    dao.close();
                } catch (IOException e) {
                    dao.close();
                    cancel(true);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                cancel(true);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAppWidgetManager.updateAppWidget(mAppWidgetId, views);
        }
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
