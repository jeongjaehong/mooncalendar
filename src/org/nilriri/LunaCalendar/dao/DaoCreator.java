package org.nilriri.LunaCalendar.dao;

import java.util.ArrayList;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.Constants.Schedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DaoCreator {

    public void onCreate(Context context, SQLiteDatabase db) {

        onCreateSchedule(db);

        onCreateDays(db);

        onCreateIndex(db);

        onInsertAnniversary(context, db);

        //onInsertBiblePlan(context, db);
    }

    public void onCreateSchedule(SQLiteDatabase db) {

        //Log.d("onCreate", "CurrentVersion=" + db.getVersion());

        StringBuffer query = null;

        query = new StringBuffer();
        query.append("CREATE TABLE " + Schedule.SCHEDULE_TABLE_NAME + " (");
        query.append("    " + Schedule._ID + " INTEGER PRIMARY KEY AUTOINCREMENT ");
        query.append("    ," + Schedule.SCHEDULE_DATE + " TEXT NOT NULL");
        query.append("    ," + Schedule.SCHEDULE_TITLE + " TEXT ");
        query.append("    ," + Schedule.SCHEDULE_CONTENTS + " TEXT ");
        query.append("    ," + Schedule.SCHEDULE_REPEAT + " INTEGER ");
        query.append("    ," + Schedule.SCHEDULE_CHECK + " VARCHAR NOT NULL DEFAULT N ");
        query.append("    ," + Schedule.ALARM_LUNASOLAR + " INTEGER ");
        query.append("    ," + Schedule.ALARM_DATE + " VARCHR ");
        query.append("    ," + Schedule.ALARM_TIME + " VARCHR ");
        query.append("    ," + Schedule.ALARM_DAYS + " INTEGER ");
        query.append("    ," + Schedule.ALARM_DAY + " INTEGER ");
        query.append("    ," + Schedule.DDAY_ALARMYN + " INTEGER ");
        query.append("    ," + Schedule.DDAY_ALARMDAY + " INTEGER ");
        query.append("    ," + Schedule.DDAY_ALARMSIGN + " VARCHAR ");
        query.append("    ," + Schedule.DDAY_DISPLAYYN + " INTEGER ");
        query.append("    ," + Schedule.GID + " VARCHAR ");
        query.append("    ," + Schedule.ANNIVERSARY + " VARCHAR ");
        query.append("    ," + Schedule.LUNARYN + " VARCHAR ");
        query.append("    ," + Schedule.SCHEDULE_LDATE + " VARCHAR ");
        query.append("    ," + Schedule.ALARM_DETAILINFO + " VARCHAR ");
        query.append("    ," + Schedule.DDAY_DETAILINFO + " VARCHAR ");
        query.append("    ," + Schedule.SCHEDULE_TYPE + " VARCHAR ");
        query.append("    ," + Schedule.BIBLE_BOOK + " INTEGER ");
        query.append("    ," + Schedule.BIBLE_CHAPTER + " INTEGER ");
        query.append("    ," + Schedule.ETAG + " VARCHAR ");
        query.append("    ," + Schedule.PUBLISHED + " VARCHAR ");
        query.append("    ," + Schedule.UPDATED + " VARCHAR ");
        query.append("    ," + Schedule.WHEN + " VARCHAR ");
        query.append("    ," + Schedule.WHO + " VARCHAR ");
        query.append("    ," + Schedule.RECURRENCE + " VARCHAR ");
        query.append("    ," + Schedule.SELFURL + " VARCHAR ");
        query.append("    ," + Schedule.EDITURL + " VARCHAR ");
        query.append("    ," + Schedule.ORIGINALEVENT + " VARCHAR ");
        query.append("    ," + Schedule.EVENTSTATUS + " VARCHAR ");
        query.append("    ) ");
        db.execSQL(query.toString());

        //Log.d("onCreate", "Create....schedule Database");

    }

    public void onCreateDays(SQLiteDatabase db) {

        //Log.d("onCreate", "CurrentVersion=" + db.getVersion());

        StringBuffer query = null;

        query = new StringBuffer();
        query.append(" DROP TABLE IF EXISTS days ");
        db.execSQL(query.toString());

        db.execSQL(" CREATE TABLE days ( days integer, dayname varchar) ");

        db.beginTransaction();

        db.execSQL(" insert into days values (1, 'Sun') ");
        db.execSQL(" insert into days values (2, 'Mon') ");
        db.execSQL(" insert into days values (3, 'Thu') ");
        db.execSQL(" insert into days values (4, 'Wed') ");
        db.execSQL(" insert into days values (5, 'Thu') ");
        db.execSQL(" insert into days values (6, 'Fri') ");
        db.execSQL(" insert into days values (7, 'Sat') ");

        db.setTransactionSuccessful();

        db.endTransaction();

        //Log.d("onCreate", "Create....days Database");

    }

    public void onInsertAnniversary(Context context, SQLiteDatabase db) {

        db.beginTransaction();

        String Anniversary[] = context.getResources().getStringArray(R.array.array_anniversary);

        db.execSQL("delete from schedule where schedule_date = '1900-01-01' and schedule_repeat = 9 and alarm_time = '00:00'");
        for (int i = 0; i < Anniversary.length; i++) {
            db.execSQL(Anniversary[i]);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void onInsertBiblePlan(Context context, SQLiteDatabase db) {
/*
        db.beginTransaction();

        ArrayList<String[]> PlanList = new ArrayList<String[]>();

        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan01));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan02));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan03));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan04));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan05));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan06));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan07));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan08));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan09));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan10));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan11));
        PlanList.add(context.getResources().getStringArray(R.array.array_bibleplan12));

        db.execSQL("delete from schedule where schedule_date = '1900-01-01' and schedule_repeat = 8 and alarm_time = '00:00'");
        for (int i = 0; i < PlanList.size(); i++) {
            String BiblePlan[] = (String[]) PlanList.get(i);

            for (int j = 0; j < BiblePlan.length; j++) {

                db.execSQL(BiblePlan[j]);
            }
        }

        db.setTransactionSuccessful();
        db.endTransaction();
*/        
    }

    public void onDeleteBiblePlan(Context context, SQLiteDatabase db) {
        try {
            db.execSQL("delete from schedule where schedule_date = '1900-01-01' and schedule_repeat = 8 and alarm_time = '00:00'");
        } catch (Exception e) {

        }
    }

    public void onAlterTable(SQLiteDatabase db) {

        db.execSQL("alter table schedule add gid varchar");

    }

    public void onAlterTable2(SQLiteDatabase db) {

        db.execSQL("alter table schedule add anniversary varchar");
        db.execSQL("alter table schedule add lunaryn varchar");
        db.execSQL("alter table schedule add schedule_ldate varchar");
    }

    public void onAlterTable3(SQLiteDatabase db) {
        try {
            db.execSQL("alter table schedule add alarm_detailinfo varchar");
            db.execSQL("alter table schedule add dday_detailinfo varchar");
            db.execSQL("alter table schedule add schedule_type varchar");
        } catch (Exception e) {

        }
    }

    public void onAlterTable4(SQLiteDatabase db) {
        try {
            StringBuffer query = null;

            query = new StringBuffer("ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD (");

            query.append(Schedule.ETAG + " VARCHAR, ");
            query.append(Schedule.PUBLISHED + " VARCHAR, ");
            query.append(Schedule.UPDATED + " VARCHAR, ");
            query.append(Schedule.WHEN + " VARCHAR, ");
            query.append(Schedule.WHO + " VARCHAR, ");
            query.append(Schedule.RECURRENCE + " VARCHAR, ");
            query.append(Schedule.ORIGINALEVENT + " VARCHAR, ");
            query.append(Schedule.EVENTSTATUS + " VARCHAR, ");
            query.append(Schedule.EDITURL + " VARCHAR, ");
            query.append(Schedule.SELFURL + " VARCHAR ");
            
            query.append(")");
            
            db.execSQL(query.toString());
            
        } catch (Exception e) {

        }
    }

    public void onCreateIndex(SQLiteDatabase db) {

        StringBuffer query = null;

        query = new StringBuffer();
        query.append("CREATE INDEX idx_ddaydisplayyn ON schedule (dday_displayyn ASC) ");
        db.execSQL(query.toString());

        query = new StringBuffer();
        query.append("CREATE  INDEX idx_scheduledate ON schedule (schedule_date ASC) ");
        db.execSQL(query.toString());

        query = new StringBuffer();
        query.append("CREATE  INDEX idx_alarm_01 ON schedule (schedule_repeat ASC, schedule_check ASC, alarm_date ASC, alarm_time ASC) ");
        db.execSQL(query.toString());

        //Log.d("onCreate", "Create....Index");
    }

    public void onUpgrade(Context context, SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d("onUpgrade_Schedule", "oldVersion=" + oldVersion + ",newVersion=" + newVersion);

        StringBuffer query = new StringBuffer();
        switch (newVersion) {
            case 1:

                onCreate(context, db);
                break;

            case 2:
                db.beginTransaction();

                db.execSQL(" DROP TABLE IF EXISTS sample ");

                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN schedule_check VARCHR NOT NULL DEFAULT N ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN alarm_lunasolar INTEGER ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN alarm_date VARCHR ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN alarm_time VARCHR ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN alarm_days INTEGER ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN alarm_day INTEGER ");

                db.setTransactionSuccessful();
                db.endTransaction();
                break;
            case 3:
                onCreateDays(db);
                break;
            case 4:
            case 5: // date, time 형식 migration...

                db.beginTransaction();

                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN " + Schedule.DDAY_ALARMYN + " INTEGER ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN " + Schedule.DDAY_ALARMDAY + " INTEGER ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN " + Schedule.DDAY_ALARMSIGN + " VARCHR ");
                db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN " + Schedule.DDAY_DISPLAYYN + " INTEGER ");

                query = new StringBuffer();
                query.append(" UPDATE SCHEDULE SET SCHEDULE_CHECK= ");
                query.append(" (DATE( REPLACE(SUBSTR(SCHEDULE_CHECK, 1, 4), '.', '') ||'-'|| ");
                query.append(" (CASE WHEN LENGTH(REPLACE(SUBSTR(SCHEDULE_CHECK, 6, 2), '.', '')) > 1 THEN REPLACE(SUBSTR(SCHEDULE_CHECK, 6, 2), '.', '') ELSE '0'||REPLACE(SUBSTR(SCHEDULE_CHECK, 6, 2), '.', '') END) ||'-'|| ");
                query.append(" (CASE WHEN LENGTH(REPLACE(SUBSTR(SCHEDULE_CHECK, 8), '.', '')) > 1 THEN REPLACE(SUBSTR(SCHEDULE_CHECK, 8), '.', '') ELSE '0'||REPLACE(SUBSTR(SCHEDULE_CHECK, 8), '.', '') END ) , 'LOCALTIME' )) ");
                query.append(" WHERE LENGTH(SCHEDULE_CHECK) >= 8 ");
                db.execSQL(query.toString());

                query = new StringBuffer();
                query.append(" UPDATE SCHEDULE SET SCHEDULE_DATE= ");
                query.append(" (DATE( REPLACE(SUBSTR(SCHEDULE_DATE, 1, 4), '.', '') ||'-'|| ");
                query.append(" (CASE WHEN LENGTH(REPLACE(SUBSTR(SCHEDULE_DATE, 6, 2), '.', '')) > 1 THEN REPLACE(SUBSTR(SCHEDULE_DATE, 6, 2), '.', '') ELSE '0'||REPLACE(SUBSTR(SCHEDULE_DATE, 6, 2), '.', '') END) ||'-'|| ");
                query.append(" (CASE WHEN LENGTH(REPLACE(SUBSTR(SCHEDULE_DATE, 8), '.', '')) > 1 THEN REPLACE(SUBSTR(SCHEDULE_DATE, 8), '.', '') ELSE '0'||REPLACE(SUBSTR(SCHEDULE_DATE, 8), '.', '') END ) , 'LOCALTIME' )) ");
                query.append(" WHERE LENGTH(SCHEDULE_DATE) >= 8 ");
                db.execSQL(query.toString());

                query = new StringBuffer();
                query.append(" UPDATE SCHEDULE SET ALARM_DATE= ");
                query.append(" (DATE( REPLACE(SUBSTR(ALARM_DATE, 1, 4), '.', '') ||'-'|| ");
                query.append(" (CASE WHEN LENGTH(REPLACE(SUBSTR(ALARM_DATE, 6, 2), '.', '')) > 1 THEN REPLACE(SUBSTR(ALARM_DATE, 6, 2), '.', '') ELSE '0'||REPLACE(SUBSTR(ALARM_DATE, 6, 2), '.', '') END) ||'-'|| ");
                query.append(" (CASE WHEN LENGTH(REPLACE(SUBSTR(ALARM_DATE, 8), '.', '')) > 1 THEN REPLACE(SUBSTR(ALARM_DATE, 8), '.', '') ELSE '0'||REPLACE(SUBSTR(ALARM_DATE, 8), '.', '') END ) , 'LOCALTIME' )) ");
                query.append(" WHERE LENGTH(ALARM_DATE) >= 8 ");
                db.execSQL(query.toString());

                query = new StringBuffer();
                query.append("UPDATE SCHEDULE ");
                query.append("   SET ALARM_DATE = ");
                query.append("             (CASE ");
                query.append("                 WHEN LENGTH (REPLACE (SUBSTR ('0000.' || ALARM_DATE, 6, 2),'.','')) > 1 ");
                query.append("                    THEN REPLACE (SUBSTR ('0000.' || ALARM_DATE, 6, 2),'.','') ");
                query.append("                 ELSE     ");
                query.append("                    '0'|| REPLACE (SUBSTR ('0000.' || ALARM_DATE, 6, 2),'.','') ");
                query.append("              END) || '-' ||  ");
                query.append("             (CASE ");
                query.append("                 WHEN LENGTH (REPLACE (SUBSTR ('0000.' || ALARM_DATE, 8),'.','')) > 1 ");
                query.append("                    THEN REPLACE (SUBSTR ('0000.' || ALARM_DATE, 8), '.', '') ");
                query.append("                 ELSE    ");
                query.append("                     '0'|| REPLACE (SUBSTR ('0000.' || ALARM_DATE, 8), '.', '') ");
                query.append("              END) ");
                query.append(" WHERE LENGTH (ALARM_DATE) BETWEEN 3 AND 5 ");
                db.execSQL(query.toString());

                query = new StringBuffer();
                query.append(" UPDATE SCHEDULE SET ALARM_TIME = ");
                query.append(" SUBSTR('0'||(CAST(REPLACE(SUBSTR((REPLACE(REPLACE(REPLACE(ALARM_TIME, 'AM:','0'), 'PM:', '0'),':',':0')),1,3),':','') AS INTEGER) ");
                query.append(" + CASE SUBSTR(ALARM_TIME, 1, 2) WHEN 'PM' THEN 12 WHEN 'AM' THEN 0 ELSE NULL END), -2) ||':'|| ");
                query.append(" SUBSTR( REPLACE(SUBSTR((REPLACE(REPLACE(REPLACE(ALARM_TIME, 'AM:','0'), 'PM:', '0'),':',':0')),4),':','') ,-2) ");
                query.append(" WHERE LENGTH(ALARM_TIME) >= 6 ");
                db.execSQL(query.toString());

                query = new StringBuffer();
                query.append("UPDATE SCHEDULE SET SCHEDULE_DATE = DATE(SCHEDULE_DATE,'1 MONTH', 'LOCALTIME')  ");
                db.execSQL(query.toString());

                query = new StringBuffer();
                query.append("UPDATE SCHEDULE SET ALARM_DATE = DATE(ALARM_DATE,'1 MONTH', 'LOCALTIME') WHERE LENGTH(ALARM_DATE) = 10 ");
                db.execSQL(query.toString());

                query = new StringBuffer();
                query.append("UPDATE SCHEDULE SET ALARM_DATE = STRFTIME('%M-%D', DATE('2010-'||ALARM_DATE, '+1 MONTH', 'LOCALTIME'), 'LOCALTIME') WHERE LENGTH(ALARM_DATE) = 5 ");
                db.execSQL(query.toString());

                db.setTransactionSuccessful();
                db.endTransaction();

                onCreateIndex(db);
                break;
            case 6:
            case 7:
            case 8:
            case 9:
                onAlterTable(db);
                break;
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                onAlterTable2(db);
                onInsertAnniversary(context, db);
                break;
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
                onInsertAnniversary(context, db);
                break;
            case 27:
                // 맥체인 성경읽기 계획표를 추가하기 위한 컬럼추가.
                try {
                    db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN " + Schedule.BIBLE_BOOK + " INTEGER ");
                    db.execSQL(" ALTER TABLE " + Schedule.SCHEDULE_TABLE_NAME + " ADD COLUMN " + Schedule.BIBLE_CHAPTER + " INTEGER ");
                } catch (Exception e) {
                    Log.d("onUpgrade", e.getMessage());
                }
                //onInsertBiblePlan(context, db);
                break;
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
                onAlterTable3(db);
                //google gdata sync를 위한 etag컬럼 추가.
                onAlterTable4(db);
                onDeleteBiblePlan(context, db);
                break;
        }

    }

}
