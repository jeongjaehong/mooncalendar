package org.nilriri.LunaCalendar;

import org.nilriri.LunaCalendar.alarm.AlarmService_Service;
import org.nilriri.LunaCalendar.schedule.AlarmViewer;
import org.nilriri.LunaCalendar.tools.Common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(Common.TAG, "======== BroadcastReceiver ======");
        try {
            //long firstTime = SystemClock.elapsedRealtime();
            long firstTime = System.currentTimeMillis();
            PendingIntent mAlarmSender = PendingIntent.getService(context, 0, new Intent(context, AlarmService_Service.class), PendingIntent.FLAG_UPDATE_CURRENT);
            int interval = 1000 * 60;

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            //am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, interval, mAlarmSender);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstTime, interval, mAlarmSender);

        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent i = new Intent(context, AlarmViewer.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);

    }

}
