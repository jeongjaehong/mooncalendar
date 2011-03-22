package org.nilriri.LunaCalendar.tools;

import org.nilriri.LunaCalendar.alarm.AlarmService_Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long firstTime = SystemClock.elapsedRealtime();
        PendingIntent mAlarmSender = PendingIntent.getService(context, 0, new Intent(context, AlarmService_Service.class), 0);

        // ScheduleBean the alarm!
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30 * 1000, mAlarmSender);

        /*        
                Intent i = new Intent(context, AlarmService_Service.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
        */
    }

}
