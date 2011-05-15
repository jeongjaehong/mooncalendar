package org.nilriri.LunaCalendar.tools;

import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class DataManager {
    public static ScheduleDaoImpl daoSource;
    public static ScheduleDaoImpl daoTarget;
    public static ProgressDialog pd;
    public static String Error = "";

    public static Context mContext;

    public static void StartCopy(Context context, boolean work) {
        mContext = context;

        daoSource = new ScheduleDaoImpl(mContext, null, !work);
        daoTarget = new ScheduleDaoImpl(mContext, null, work);

        pd = new ProgressDialog(mContext);

        pd.setTitle("Move!");
        pd.setMessage("Move to external storage...");

        //pd.setCancelable(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //pd.setIndeterminate(true);
        pd.show();

        Thread thread = new Thread(new Runnable() {

            public void run() {

                try {
                    
                    Log.d(Common.TAG, "########    Start     #######");

                    Cursor cursor = daoSource.queryAll();

                    Log.d(Common.TAG, "########    "+cursor.getCount()+"     #######");

                    if (!daoSource.exportdata(cursor, pd)) {
                        Error = "백업이 실패하였습니다.";
                    } else {

                        if (cursor.getCount() > 0) {
                            if (!daoTarget.copy(cursor))
                                Error = "저장위치 변경 실패!";
                        }
                    }
                    
                    Log.d(Common.TAG, "########   End    #######");


                    cursor.close();
                    daoSource.close();
                    daoTarget.close();

                    handler.sendEmptyMessage(0);

                } catch (Exception e) {
                    Log.d(Common.TAG, "########    "+e.getMessage()+"     #######");
                    handler.sendEmptyMessage(0);

                    Error = e.getMessage();

                }

            }

        });

        thread.start();

    }

    public static void StartRestore(Context context) {
        mContext = context;

        daoTarget = new ScheduleDaoImpl(mContext, null, Prefs.getSDCardUse(context));

        pd = new ProgressDialog(mContext);

        pd.setTitle("Restore!");
        pd.setMessage("Restore from backup file...");

        //pd.setCancelable(true);
        //pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //pd.setIndeterminate(true);
        pd.show();

        Thread thread = new Thread(new Runnable() {

            public void run() {

                try {

                    if (!daoTarget.importdata()) {
                        Error = "복구 실패!";
                    } else {
                        Error = "";
                    }
                    daoTarget.close();

                    handler.sendEmptyMessage(0);

                } catch (Exception e) {
                    handler.sendEmptyMessage(0);
                    Error = e.getMessage();

                }

            }

        });

        thread.start();
    }

    public static void StartBackup(Context context) {
        mContext = context;

        daoSource = new ScheduleDaoImpl(mContext, null, Prefs.getSDCardUse(context));

        pd = new ProgressDialog(mContext);

        pd.setTitle("Backup!");
        pd.setMessage("Backup schedule data...");

        //pd.setCancelable(true);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        //pd.setIndeterminate(true);
        pd.show();

        Thread thread = new Thread(new Runnable() {

            public void run() {

                try {
                    Log.d(Common.TAG, "########    Start     #######");
                    Cursor cursor = daoSource.queryAll();
                    
                    Log.d(Common.TAG, "########    "+cursor.getCount()+"     #######");

                    if (!daoSource.exportdata(cursor, pd)) {
                        Log.d(Common.TAG, "########   Backup Fail     #######");
                        Error = "백업이 실패하였습니다.";
                    } else {
                        Log.d(Common.TAG, "########    Succ     #######");
                        Error = "";
                    }

                    Log.d(Common.TAG, "########    close     #######");
                    cursor.close();
                    daoSource.close();

                    handler.sendEmptyMessage(0);

                } catch (Exception e) {
                    handler.sendEmptyMessage(0);
                    
                    Log.d(Common.TAG, "########    Exception="+e.getMessage()+"     #######");

                    Error = e.getMessage();

                }

            }

        });

        thread.start();
    }

    public static Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            pd.dismiss();

            if ("".equals(Error)) {
                Toast.makeText(mContext, "작업이 정상적으로 완료 되었습니다.", Toast.LENGTH_LONG).show();
            } else {
                Log.e("Copy", "Error = " + Error);
                Toast.makeText(mContext, Error, Toast.LENGTH_LONG).show();
            }

        }

    };

}