/*
 * Copyright (C) 2007 The Android Open Source Project
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

package org.nilriri.LunaCalendar.alarm;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import java.util.Calendar;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.schedule.AlarmViewer;
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
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.Toast;

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an
 * intent receiver.
 * 
 * @see AlarmService
 * @see AlarmService_Alarm
 */
public class AlarmService_Service extends Service {
    public NotificationManager mNM;

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        Thread thr = new Thread(null, mTask, "AlarmService_Service");

        thr.start();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        //Log.i("AlarmService_Service", "onStart #" + startId + ": " + intent.getLongExtra("id", -9));

        // show the icon in the status bar
        try {
            showNotification();
        } catch (CanceledException e) {
            mNM.cancelAll();
            Toast.makeText(this, R.string.alarm_service_finished, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDestroy() {
        // Cancel the notification -- we use the same ID that we had used to start it
        mNM.cancel(R.string.alarm_service_started);

        // Tell the user we stopped.
        // Toast.makeText(this, "R.string.alarm_service_finished", Toast.LENGTH_SHORT).show();
    }

    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        public void run() {
            // Normally we would do some work here...  for our sample, we will
            // just sleep for 10 minute
            long endTime = System.currentTimeMillis() + 1000 * 60 * 5;
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }

            // Done with our work...  stop the service!
            AlarmService_Service.this.stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Show a notification while this service is running.
     * @throws CanceledException 
     */
    private void showNotification() throws CanceledException {
        // In this sample, we'll use the same text for the ticker and the expanded notification

        final Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        String lDay = lunar2solar.s2l(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));

        /*
        displayNotify(dao.queryOnece(c));
        displayNotify(dao.queryEveryDay(c));
        displayNotify(dao.queryEveryWeek(c));
        displayNotify(dao.queryEveryMonth(c, lDay));
        displayNotify(dao.queryEveryYear(c, lDay));
        */

        displayNotify(c, lDay);

    }

    private void displayNotify(Calendar c, String lDay) {
        ScheduleDaoImpl dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));

        Cursor cursor = dao.queryAlarm(c, lDay);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            CharSequence title = cursor.getString(1);
            CharSequence content = cursor.getString(2);

            Notification notification = new Notification(R.drawable.clock, title, System.currentTimeMillis());

            PendingIntent contentIntent = PendingIntent.getActivity(this, id, new Intent(this, AlarmViewer.class).putExtra("id", new Long(id)), 0);

            notification.setLatestEventInfo(this, title, content, contentIntent);

            Music.play(this, R.raw.ding);

            mNM.notify(id, notification);
        }
        cursor.close();
        dao.close();
    }

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
