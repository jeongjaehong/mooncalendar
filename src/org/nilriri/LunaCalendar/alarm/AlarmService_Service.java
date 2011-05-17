package org.nilriri.LunaCalendar.alarm;

import java.util.Calendar;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.schedule.AlarmViewer;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.Music;
import org.nilriri.LunaCalendar.tools.Prefs;
import org.nilriri.LunaCalendar.tools.lunar2solar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class AlarmService_Service extends Service {
    ScheduleDaoImpl dao = null;
    public NotificationManager mNM;

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Thread thr = new Thread(null, mTask, "AlarmService_Service");

        thr.start();
    }

    @Override
    public void onStart(Intent intent, int startId) {

        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));

        try {
            showNotification();
        } catch (CanceledException e) {
            mNM.cancelAll();
            Toast.makeText(this, R.string.alarm_service_finished, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        mNM.cancel(R.string.alarm_service_started);

        dao.close();
    }

    Runnable mTask = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 1000 * 60 * 5;
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }

            AlarmService_Service.this.stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void showNotification() throws CanceledException {

        final Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        String lDay = lunar2solar.s2l(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));

        displayNotify(c, lDay);

    }

    private void displayNotify(Calendar c, String lDay) {

        Cursor cursor = dao.queryAlarm(c, lDay);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            CharSequence title = cursor.getString(1);
            CharSequence content = cursor.getString(2);
            cursor.close();

            Notification notification = new Notification(R.drawable.clock, title, System.currentTimeMillis());

            PendingIntent contentIntent = PendingIntent.getActivity(this, id, new Intent(this, AlarmViewer.class).putExtra("id", new Long(id)), 0);

            notification.setLatestEventInfo(this, title, content, contentIntent);

            try {
                Uri uri = Uri.parse(Prefs.getRingtone(this));

                Ringtone rt = RingtoneManager.getRingtone(this, uri);

                if (null != rt) {
                   if (rt.isPlaying())rt.stop();

                    rt.play();
                } else {
                    Music.play(this, R.raw.ding);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(Common.TAG, "error=" + e.getMessage());
            }

            mNM.notify(id, notification);
        }
        cursor.close();
    }

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
