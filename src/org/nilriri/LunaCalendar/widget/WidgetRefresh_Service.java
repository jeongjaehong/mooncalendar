package org.nilriri.LunaCalendar.widget;

import org.nilriri.LunaCalendar.R;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class WidgetRefresh_Service extends Service {

    public NotificationManager mNM;

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 3초후 서비스 자동 종료.
        Thread thr = new Thread(null, mTask, "AlarmService_Service");
        thr.start();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        try {
            WidgetUtil.refreshWidgets(getBaseContext());
        } catch (Exception e) {
        } finally {
            WidgetRefresh_Service.this.stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        mNM.cancel(R.string.alarm_service_started);
    }

    Runnable mTask = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 1000 * 3;
            while (System.currentTimeMillis() < endTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            WidgetRefresh_Service.this.stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}
