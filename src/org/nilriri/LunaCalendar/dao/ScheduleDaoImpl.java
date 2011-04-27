package org.nilriri.LunaCalendar.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.Constants.Schedule;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.Prefs;
import org.nilriri.LunaCalendar.tools.WhereClause;
import org.nilriri.LunaCalendar.tools.lunar2solar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class ScheduleDaoImpl extends AbstractDao {

    private SQLiteDatabase db;

    private Context mContext;

    public ScheduleDaoImpl(Context context, CursorFactory factory, boolean sdcarduse) {
        super(context, factory, sdcarduse);

        mContext = context;

        db = getWritableDatabase();
    }

    public void delete(Long id) {
        String sql = "DELETE FROM " + Schedule.SCHEDULE_TABLE_NAME + " WHERE " + Schedule._ID + "=" + id;
        getWritableDatabase().execSQL(sql);
    }

    public void deleteAll() {
        String sql = "DELETE FROM " + Schedule.SCHEDULE_TABLE_NAME;
        getWritableDatabase().execSQL(sql);
    }

    public void insert(ScheduleBean scheduleBean) {
        ContentValues val = new ContentValues();
        WhereClause whereClause = new WhereClause();

        whereClause.put(Schedule.GID, scheduleBean.getGID());

        int gCnt = 0;

        val.put(Schedule.SCHEDULE_DATE, scheduleBean.getDate());

        val.put(Schedule.SCHEDULE_LDATE, scheduleBean.getLDate());
        val.put(Schedule.LUNARYN, scheduleBean.getLunarYN() == true ? "Y" : "N");
        val.put(Schedule.ANNIVERSARY, scheduleBean.getAnniversary() == true ? "Y" : "N");

        val.put(Schedule.SCHEDULE_TITLE, scheduleBean.getTitle());
        val.put(Schedule.SCHEDULE_CONTENTS, scheduleBean.getContents());
        val.put(Schedule.SCHEDULE_REPEAT, scheduleBean.getRepeat());

        val.put(Schedule.ALARM_LUNASOLAR, scheduleBean.getLunaSolar());

        switch (scheduleBean.getRepeat()) {
            case 5:
                // 매년주기 알람인경우 년도를 빼고 월과 일만 저장한다.
                String alarmdate = scheduleBean.getAlarmDate();
                if (alarmdate.length() > 5)
                    alarmdate = alarmdate.substring(5);
                val.put(Schedule.ALARM_DATE, alarmdate);
                break;
            default:
                val.put(Schedule.ALARM_DATE, scheduleBean.getAlarmDate());
        }

        val.put(Schedule.ALARM_TIME, scheduleBean.getAlarmTime());
        val.put(Schedule.ALARM_DAYS, scheduleBean.getAlarmDays());
        val.put(Schedule.ALARM_DAY, scheduleBean.getAlarmDay());

        val.put(Schedule.DDAY_ALARMYN, scheduleBean.getDday_alarmyn());
        val.put(Schedule.DDAY_ALARMDAY, scheduleBean.getDday_alarmday());
        val.put(Schedule.DDAY_ALARMSIGN, scheduleBean.getDday_alarmsign());
        val.put(Schedule.DDAY_DISPLAYYN, scheduleBean.getDday_displayyn());
        val.put(Schedule.GID, scheduleBean.getGID());

        Log.d("DaoImpl-insert", "val=" + val.toString());

        if ("".equals(scheduleBean.getGID())) {
            gCnt = 0;
        } else {

            String sql = "SELECT _id FROM " + Schedule.SCHEDULE_TABLE_NAME + " WHERE " + whereClause;

            Cursor c = getReadableDatabase().rawQuery(sql, null);

            Log.d("DaoImpl-insert", "sql=" + sql);

            if (c.moveToNext()) {

                gCnt = c.getCount();
                scheduleBean.setId(c.getInt(Schedule.COL_ID));
                val.put(Schedule._ID, c.getString(Schedule.COL_ID));
            }
            c.close();
        }
        if (gCnt > 0) {
            // db = getWritableDatabase();

            // int cnt = db.update(Schedule.SCHEDULE_TABLE_NAME, val, whereClause.toString(), null);

            // Log.d("DaoImpl-update", "update count is " + cnt);

            // db.close();

            update(scheduleBean);

        } else if (gCnt == 0) {

            // db = getWritableDatabase();

            db.beginTransaction();
            if (1 == scheduleBean.getDday_displayyn()) {
                // 기존에 상단에 표시하도록 되어있던 D-day정보는 목록표시로 변경한다.
                db.execSQL("update schedule set dday_displayyn = 0 where dday_displayyn = 1");
            }

            db.insert(Schedule.SCHEDULE_TABLE_NAME, null, val);

            db.setTransactionSuccessful();
            db.endTransaction();

            Log.d("DaoImpl-insert", "succ.");
        }

        //getWritableDatabase().insert(Schedule.SCHEDULE_TABLE_NAME, null, val);
    }

    public void update(ScheduleBean scheduleBean) {

        String[] args = new String[] { scheduleBean.getId() + "" };
        ContentValues val = new ContentValues();

        val.put(Schedule._ID, scheduleBean.getId());
        val.put(Schedule.SCHEDULE_DATE, scheduleBean.getDate());

        val.put(Schedule.SCHEDULE_LDATE, scheduleBean.getLDate());
        val.put(Schedule.LUNARYN, scheduleBean.getLunarYN() == true ? "Y" : "N");
        val.put(Schedule.ANNIVERSARY, scheduleBean.getAnniversary() == true ? "Y" : "N");

        val.put(Schedule.SCHEDULE_TITLE, scheduleBean.getTitle());
        val.put(Schedule.SCHEDULE_CONTENTS, scheduleBean.getContents());
        val.put(Schedule.SCHEDULE_REPEAT, scheduleBean.getRepeat());
        val.put(Schedule.SCHEDULE_CHECK, scheduleBean.getCheck());

        val.put(Schedule.ALARM_LUNASOLAR, scheduleBean.getLunaSolar());

        switch (scheduleBean.getRepeat()) {
            case 5:
                // 매년주기 알람인경우 년도를 빼고 월과 일만 저장한다.
                String alarmdate = scheduleBean.getAlarmDate();
                if (alarmdate.length() > 5)
                    alarmdate = alarmdate.substring(5);
                val.put(Schedule.ALARM_DATE, alarmdate);
                break;
            default:
                val.put(Schedule.ALARM_DATE, scheduleBean.getAlarmDate());
        }

        val.put(Schedule.ALARM_TIME, scheduleBean.getAlarmTime());
        val.put(Schedule.ALARM_DAYS, scheduleBean.getAlarmDays());
        val.put(Schedule.ALARM_DAY, scheduleBean.getAlarmDay());

        val.put(Schedule.DDAY_ALARMYN, scheduleBean.getDday_alarmyn());
        val.put(Schedule.DDAY_ALARMDAY, scheduleBean.getDday_alarmday());
        val.put(Schedule.DDAY_ALARMSIGN, scheduleBean.getDday_alarmsign());
        val.put(Schedule.DDAY_DISPLAYYN, scheduleBean.getDday_displayyn());

        Log.d("DaoImpl-update", val.toString());

        // db = getWritableDatabase();

        db.beginTransaction();
        if (1 == scheduleBean.getDday_displayyn()) {
            // 기존에 상단에 표시하도록 되어있던 D-day정보는 목록표시로 변경한다.
            db.execSQL("update schedule set dday_displayyn = 0 where dday_displayyn = 1");
        }

        db.update(Schedule.SCHEDULE_TABLE_NAME, val, Schedule._ID + "=?", args);

        db.setTransactionSuccessful();
        db.endTransaction();

    }

    public Cursor query(String date, String lDay) {

        String sday = date.substring(5);
        String lday = lDay.substring(4, 6) + "-" + lDay.substring(6);

        /*
        String selection = Schedule.SCHEDULE_DATE + " = ? ";
        String selectionArgs[] = new String[] { date };
        String groupBy = null;
        String having = null;
        String orderBy = "_id desc";

        // db = getReadableDatabase();

        return db.query(Schedule.SCHEDULE_TABLE_NAME, mColumns, selection, selectionArgs, groupBy, having, orderBy);
        */

        StringBuffer query = new StringBuffer();

        //양력, 음력 일반 일정
        query.append("SELECT   ");
        query.append("    _id ");
        query.append("    ,'Schedule' schedule_type ");
        query.append("    ,schedule_title ");
        query.append("    ,6 kind ");
        query.append("    ,0 bible_book ");
        query.append("    ,0 bible_chapter ");
        query.append("FROM  schedule ");
        query.append("WHERE schedule_date = ? and lunaryn <> 'Y' and  anniversary <> 'Y' ");
        query.append(" OR ( schedule_ldate = '" + Common.fmtDate(lDay) + "' and lunaryn = 'Y' and anniversary <> 'Y' )");
        /*
         query.append("union all  ");
         query.append("SELECT   ");
         query.append("    _id ");
         query.append("    ,'D-day' schedule_type ");
         query.append("    ,substr(schedule_title, 1, 15) ");
         query.append("    ||'('|| ");
         query.append("case when cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) > 0  ");
         query.append("then 'D + ' || cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer)  ");
         query.append(" when cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) = 0  ");
         query.append("then 'D day' else 'D ' ||  cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) end ");
         query.append("    ||')' schedule_title ");
         query.append("FROM  schedule ");
         query.append("WHERE 1=1 ");
         query.append("and dday_displayyn = 2  ");
        */

        //사용자 양력기념일, 음력기념일
        query.append("union all  ");
        query.append("select _id, 'Anniversary' schedule_type, schedule_title ");
        query.append("    ,3 kind ");
        query.append("    ,0 bible_book ");
        query.append("    ,0 bible_chapter ");
        query.append("from schedule ");
        query.append("where 1=1 ");
        query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + date + "', 'LOCALTIME')||'-12-31' ");
        query.append(" and  schedule_date like '%" + date.substring(5) + "' and lunaryn <> 'Y' and  anniversary = 'Y' ");
        query.append(" OR ( schedule_date <= STRFTIME('%Y', '" + date + "', 'LOCALTIME')||'-12-31' ");
        query.append(" and schedule_ldate like '%" + Common.fmtDate(lDay).substring(5) + "' and lunaryn = 'Y' and anniversary = 'Y' )");

        // system 기념일
        query.append("union all  ");
        query.append("select _id, 'Anniversary' schedule_type, schedule_title ");
        query.append("    ,alarm_day kind "); //붉은깃발 혹은 녹색깃발.
        query.append("    ,0 bible_book ");
        query.append("    ,0 bible_chapter ");
        query.append("from schedule ");
        query.append("where schedule_repeat = 9 ");
        query.append("and schedule_date = '1900-01-01' ");
        query.append("and (   (alarm_lunasolar = 0 and alarm_date = '" + sday + "') ");
        query.append("     or (alarm_lunasolar = 1 and alarm_date = '" + lday + "') ) ");
        query.append("and alarm_time = '00:00' ");

        /*
                if (Prefs.getBplan(mContext) && (Prefs.getBplanFamily(mContext) || Prefs.getBplanPersonal(mContext))) {
                    // 성경읽기 플랜
                    query.append("union all  ");
                    query.append("select _id, 'B-Plan' schedule_type, schedule_title ");
                    query.append("    ,alarm_day kind "); //붉은깃발 혹은 녹색깃발.
                    query.append("    ,bible_book ");
                    query.append("    ,bible_chapter ");
                    query.append("from schedule ");
                    query.append("where 1 = 1 ");
                    if (Prefs.getBplanFamily(mContext) && Prefs.getBplanPersonal(mContext))
                        query.append(" and schedule_repeat in ('F','P') ");
                    else if (Prefs.getBplanFamily(mContext))
                        query.append(" and schedule_repeat = 'F' ");
                    else if (Prefs.getBplanPersonal(mContext))
                        query.append(" and schedule_repeat = 'P' ");
                    query.append("and schedule_date = '1900-01-01' ");
                    query.append("and (   (alarm_lunasolar = 0 and alarm_date = '" + sday + "') ");
                    query.append("     or (alarm_lunasolar = 1 and alarm_date = '" + lday + "') ) ");
                    query.append("and alarm_time = '00:00' ");
                }
        */
        //d-day
        query.append("union all  ");
        query.append("SELECT   ");
        query.append("    _id ");
        query.append("    ,'D-day' schedule_type ");
        query.append("    ,substr(schedule_title, 1, 15) ");
        query.append("    ||'('|| ");
        query.append("case when cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) > 0  ");
        query.append("then 'D + ' || cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer)  ");
        query.append(" when cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) = 0  ");
        query.append("then 'D day' else 'D ' ||  cast(JULIANDAY(?, 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) end ");
        query.append("    ||')' schedule_title ");
        query.append("    ,5 kind ");
        query.append("    ,0 bible_book ");
        query.append("    ,0 bible_chapter ");
        query.append("FROM  schedule ");
        query.append("WHERE 1=1 ");
        query.append("and dday_alarmyn = 1  ");
        query.append("and dday_displayyn = 2  ");
        query.append("or (dday_alarmyn = 1  ");
        query.append("and dday_displayyn in (0, 1)  ");
        query.append("and strftime('%Y-%m-%d', DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'localtime') = ? ) ");

        query.append("ORDER BY 1 ");

        String selectionArgs[] = new String[] { date, date, date, date, date, date, };

        Log.d("DaoImpl-query", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryAll() {

        StringBuffer query = new StringBuffer();

        query.append("SELECT   ");
        query.append(" _id,schedule_date,schedule_title,schedule_contents,schedule_repeat,schedule_check,alarm_lunasolar,alarm_date,alarm_time,alarm_days,alarm_day,dday_alarmyn,dday_alarmday,dday_alarmsign,dday_displayyn,gid,anniversary,lunaryn,schedule_ldate ");
        query.append("FROM  schedule ");
        query.append("WHERE schedule_date > '1900-01-01' ");

        query.append("  AND schedule_repeat not in ('F','P', '9') ");
        /*
                if (Prefs.getBplan(mContext) && (Prefs.getBplanFamily(mContext) || Prefs.getBplanPersonal(mContext))) {
                    query.append("  AND schedule_repeat not in ( '9') ");
                } else {

                    query.append("  AND schedule_repeat not in ('F','P', '9') ");
                }
        */
        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public boolean export(Cursor cursor) {

        String path = "/sdcard/";
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        String datetime = Common.fmtDateTime(c);
        File file = new File(path + "lunarcalendar.auto.bak");
        try {
            FileOutputStream fos = new FileOutputStream(file);

            StringBuffer buf = new StringBuffer();

            buf.append("schedule_date||schedule_title||schedule_contents||schedule_repeat||schedule_check||alarm_lunasolar||alarm_date||alarm_time||alarm_days||alarm_day||dday_alarmyn||dday_alarmday||dday_alarmsign||dday_displayyn||gid||anniversary||lunaryn||schedule_ldate").append("\n");

            while (cursor.moveToNext()) {

                buf.append(cursor.getString(Schedule.COL_SCHEDULE_DATE)).append("||");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_TITLE)).append("||");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_CONTENTS)).append("||");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_REPEAT)).append("||");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_CHECK)).append("||");
                buf.append(cursor.getString(Schedule.COL_ALARM_LUNASOLAR)).append("||");
                buf.append(cursor.getString(Schedule.COL_ALARM_DATE)).append("||");
                buf.append(cursor.getString(Schedule.COL_ALARM_TIME)).append("||");
                buf.append(cursor.getString(Schedule.COL_ALARM_DAYS)).append("||");
                buf.append(cursor.getString(Schedule.COL_ALARM_DAY)).append("||");
                buf.append(cursor.getString(Schedule.COL_DDAY_ALARMYN)).append("||");
                buf.append(cursor.getString(Schedule.COL_DDAY_ALARMDAY)).append("||");
                buf.append(cursor.getString(Schedule.COL_DDAY_ALARMSIGN)).append("||");
                buf.append(cursor.getString(Schedule.COL_DDAY_DISPLAYYN)).append("||");
                buf.append(cursor.getString(Schedule.COL_GID)).append("||");
                buf.append(cursor.getString(Schedule.COL_ANNIVERSARY)).append("||");
                buf.append(cursor.getString(Schedule.COL_LUNARYN)).append("||");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_LDATE)).append("\n");

            }

            fos.write(buf.toString().getBytes());

            fos.close();

            return true;
        } catch (IOException e) {
            Log.i("Export", e.getMessage());
            return false;
        }
    }

    public boolean exportdata(Cursor cursor) {

        String path = android.os.Environment.getExternalStorageDirectory().toString() + "/";

        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        String datetime = Common.fmtDateTime(c);
        File file = new File(path + "lunarcalendar.backup");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);

            StringBuffer buf = new StringBuffer();

            buf.append("schedule_date\tschedule_title\tschedule_contents\tschedule_repeat\tschedule_check\talarm_lunasolar\talarm_date\talarm_time\talarm_days\talarm_day\tdday_alarmyn\tdday_alarmday\tdday_alarmsign\tdday_displayyn\tgid\tanniversary\tlunaryn\tschedule_ldate").append("\n\r");

            while (cursor.moveToNext()) {

                buf.append(cursor.getString(Schedule.COL_SCHEDULE_DATE)).append("\t");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_TITLE)).append("\t");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_CONTENTS)).append("\t");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_REPEAT)).append("\t");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_CHECK)).append("\t");
                buf.append(cursor.getString(Schedule.COL_ALARM_LUNASOLAR)).append("\t");
                buf.append(cursor.getString(Schedule.COL_ALARM_DATE)).append("\t");
                buf.append(cursor.getString(Schedule.COL_ALARM_TIME)).append("\t");
                buf.append(cursor.getString(Schedule.COL_ALARM_DAYS)).append("\t");
                buf.append(cursor.getString(Schedule.COL_ALARM_DAY)).append("\t");
                buf.append(cursor.getString(Schedule.COL_DDAY_ALARMYN)).append("\t");
                buf.append(cursor.getString(Schedule.COL_DDAY_ALARMDAY)).append("\t");
                buf.append(cursor.getString(Schedule.COL_DDAY_ALARMSIGN)).append("\t");
                buf.append(cursor.getString(Schedule.COL_DDAY_DISPLAYYN)).append("\t");
                buf.append(cursor.getString(Schedule.COL_GID)).append("\t");
                buf.append(cursor.getString(Schedule.COL_ANNIVERSARY)).append("\t");
                buf.append(cursor.getString(Schedule.COL_LUNARYN)).append("\t");
                buf.append(cursor.getString(Schedule.COL_SCHEDULE_LDATE)).append("\n\r");

            }

            fos.write(buf.toString().getBytes());

            fos.close();

            return true;
        } catch (Exception e) {

            Log.i("Export", e.getMessage());
            return false;
        }
    }

    public boolean importdata() {

        String path = android.os.Environment.getExternalStorageDirectory().toString() + "/";
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        String datetime = Common.fmtDateTime(c);
        File file = new File(path + "lunarcalendar.backup");

        if (!file.exists()) {
            return true;
        }

        // db = getWritableDatabase();
        try {

            //DataInputStream fis = new DataInputStream(new FileInputStream(file));

            String[] CheckData = null;
            FileInputStream fis = new FileInputStream(file);
            int size = fis.available();
            byte[] buf = new byte[size];

            fis.read(buf);
            String filedata = new String(buf);
            fis.close();

            Log.d("DaoImpl-import", "filedata=" + filedata);

            CheckData = filedata.split("\n\r");
            Log.d("DaoImpl-import", "CheckData=" + CheckData.toString());
            Log.d("DaoImpl-import", "CheckData=" + CheckData.length);

            db.beginTransaction();
            ContentValues val = null;
            for (int row = 0; row < CheckData.length; row++) {
                if (row == 0)
                    continue;

                String data[] = CheckData[row].toString().split("\t");
                Log.d("DaoImpl-import", "data=" + data.length);
                if (data.length != 18)
                    continue;

                val = new ContentValues();

                val.put(Schedule.SCHEDULE_DATE, "null".equals(data[0]) ? "" : data[0]);
                val.put(Schedule.SCHEDULE_TITLE, "null".equals(data[1]) ? "" : data[1]);
                val.put(Schedule.SCHEDULE_CONTENTS, "null".equals(data[2]) ? "" : data[2]);
                val.put(Schedule.SCHEDULE_REPEAT, "null".equals(data[3]) ? "" : data[3]);
                val.put(Schedule.SCHEDULE_CHECK, "null".equals(data[4]) ? "" : data[4]);
                val.put(Schedule.ALARM_LUNASOLAR, "null".equals(data[5]) ? "" : data[5]);
                val.put(Schedule.ALARM_DATE, "null".equals(data[6]) ? "" : data[6]);
                val.put(Schedule.ALARM_TIME, "null".equals(data[7]) ? "" : data[7]);
                val.put(Schedule.ALARM_DAYS, "null".equals(data[8]) ? "" : data[8]);
                val.put(Schedule.ALARM_DAY, "null".equals(data[9]) ? "" : data[9]);
                val.put(Schedule.DDAY_ALARMYN, "null".equals(data[10]) ? "" : data[10]);
                val.put(Schedule.DDAY_ALARMDAY, "null".equals(data[11]) ? "" : data[11]);
                val.put(Schedule.DDAY_ALARMSIGN, "null".equals(data[12]) ? "" : data[12]);
                val.put(Schedule.DDAY_DISPLAYYN, "null".equals(data[13]) ? "" : data[13]);
                val.put(Schedule.GID, "null".equals(data[14]) ? "" : data[14]);
                val.put(Schedule.ANNIVERSARY, "null".equals(data[15]) ? "" : data[15]);
                val.put(Schedule.LUNARYN, "null".equals(data[16]) ? "" : data[16]);
                val.put(Schedule.SCHEDULE_LDATE, "null".equals(data[17]) ? "" : data[17]);

                if (!queryExists(data[0], data[1])) {

                    Log.d("DaoImpl-import", "val=" + val.toString());

                    db.insert(Schedule.SCHEDULE_TABLE_NAME, null, val);
                }

            }

            db.setTransactionSuccessful();
            db.endTransaction();

            return true;
        } catch (Exception e) {
            db.endTransaction();
            Log.i("Import", e.getMessage());
            return false;
        }
    }

    public boolean copy(Cursor cursor) {

        ContentValues val = null;
        // db = getWritableDatabase();

        if (!export(cursor)) {
            return false;
        }

        db.beginTransaction();

        boolean isFirst = true;

        while (cursor.moveToNext()) {

            if (isFirst) {
                StringBuffer query = new StringBuffer();
                query.append("DELETE FROM schedule ");
                db.execSQL(query.toString());
            }

            val = new ContentValues();
            val.put("schedule_date", cursor.getString(Schedule.COL_SCHEDULE_DATE));
            val.put("schedule_title", cursor.getString(Schedule.COL_SCHEDULE_TITLE));
            val.put("schedule_contents", cursor.getString(Schedule.COL_SCHEDULE_CONTENTS));
            val.put("schedule_repeat", cursor.getString(Schedule.COL_SCHEDULE_REPEAT));
            val.put("schedule_check", cursor.getString(Schedule.COL_SCHEDULE_CHECK));
            val.put("alarm_lunasolar", cursor.getString(Schedule.COL_ALARM_LUNASOLAR));
            val.put("alarm_date", cursor.getString(Schedule.COL_ALARM_DATE));
            val.put("alarm_time", cursor.getString(Schedule.COL_ALARM_TIME));
            val.put("alarm_days", cursor.getString(Schedule.COL_ALARM_DAYS));
            val.put("alarm_day", cursor.getString(Schedule.COL_ALARM_DAY));
            val.put("dday_alarmyn", cursor.getString(Schedule.COL_DDAY_ALARMYN));
            val.put("dday_alarmday", cursor.getString(Schedule.COL_DDAY_ALARMDAY));
            val.put("dday_alarmsign", cursor.getString(Schedule.COL_DDAY_ALARMSIGN));
            val.put("dday_displayyn", cursor.getString(Schedule.COL_DDAY_DISPLAYYN));
            val.put("gid", cursor.getString(Schedule.COL_GID));
            val.put("anniversary", cursor.getString(Schedule.COL_ANNIVERSARY));
            val.put("lunaryn", cursor.getString(Schedule.COL_LUNARYN));
            val.put("schedule_ldate", cursor.getString(Schedule.COL_SCHEDULE_LDATE));

            db.insert("schedule", null, val);

            isFirst = false;
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        return true;
    }

    public Cursor queryExistsAnniversary(String month, String lfromday, String ltoday) {

        StringBuffer query = new StringBuffer();

        lfromday = lfromday.substring(4, 8);
        ltoday = ltoday.substring(4, 8);

        boolean isChange = (lfromday.compareTo(ltoday) > 0);

        lfromday = String.format("%2s-%2s", lfromday.substring(0, 2), lfromday.substring(2));
        ltoday = String.format("%2s-%2s", ltoday.substring(0, 2), ltoday.substring(2));

        query.append("select case when alarm_lunasolar = 0 then cast(substr(alarm_date, -2) as integer)  ");
        query.append("      else cast(replace(substr(alarm_date, -5), '-', '') as integer) end lday ");
        query.append("      , cast(alarm_day as integer) flag ");
        query.append("from schedule ");
        query.append("where schedule_repeat = 9 ");
        query.append("and schedule_date = '1900-01-01' ");
        query.append("and (   (alarm_lunasolar = 0 and alarm_date like '" + month.substring(5, 7) + "%') ");
        if (isChange) {
            query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + lfromday + "' and '" + lfromday.substring(0, 2) + "-31')  ");
            query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + ltoday.substring(0, 2) + "-01' and '" + ltoday + "') ) ");
        } else {
            query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + lfromday + "' and '" + ltoday + "') ) ");
        }
        query.append("and alarm_time = '00:00' ");

        Log.d("DaoImpl-queryExistsAnniversary", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public boolean queryExists(String date, String title) {

        StringBuffer query = new StringBuffer();

        query.append(" select count(*) ");
        query.append(" from schedule ");
        query.append(" where schedule_date = '" + date + "' ");
        query.append(" and schedule_title = '" + title + "' ");

        Log.d("DaoImpl-queryExists", "query=" + query.toString());

        Cursor c = getReadableDatabase().rawQuery(query.toString(), null);

        if (c.moveToNext()) {
            return (c.getInt(0) > 0);
        } else {
            return false;
        }

    }

    public Cursor queryExistsSchedule(String month) {

        StringBuffer query = new StringBuffer();

        String sDate[] = Common.tokenFn(month + "-01", "-");
        String lStart = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), 1));
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        c.set(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), 1);
        c.add(Calendar.DAY_OF_MONTH, -1);
        int lastDay = c.get(Calendar.DAY_OF_MONTH);
        String lEnd = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), lastDay));
        boolean isChange = (lStart.substring(5).compareTo(lEnd.substring(5)) > 0);

        // 양력일정
        query.append("SELECT  ");
        query.append("    DISTINCT CAST(STRFTIME('%d', " + Schedule.SCHEDULE_DATE + ", 'LOCALTIME') AS INTEGER) DAY  ");
        query.append("FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append("WHERE 1 = 1 ");
        query.append(" AND " + Schedule.SCHEDULE_DATE + " LIKE '" + month + "%' ");
        query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
        query.append(" AND " + Schedule.ANNIVERSARY + " <> 'Y' ");

        // 양력기념일
        query.append(" UNION ALL  ");
        query.append("SELECT  ");
        query.append("    DISTINCT CAST(STRFTIME('%d', " + Schedule.SCHEDULE_DATE + ", 'LOCALTIME') AS INTEGER) DAY  ");
        query.append("FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append("WHERE 1 = 1 ");
        query.append(" AND substr(" + Schedule.SCHEDULE_DATE + ",6,2) = '" + month.substring(5) + "' ");
        query.append(" AND " + Schedule.ANNIVERSARY + " = 'Y' ");
        query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
        query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + month + "-01" + "', 'LOCALTIME')||'-12-31' ");

        query.append(" ORDER BY 1 ");

        Log.d("DaoImpl-queryExistsSchedule", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryExistsSchedule2(String month) {

        StringBuffer query = new StringBuffer();

        String sDate[] = Common.tokenFn(month + "-01", "-");
        String lStart = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), 1));
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        c.set(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), 1);
        c.add(Calendar.DAY_OF_MONTH, -1);
        int lastDay = c.get(Calendar.DAY_OF_MONTH);
        String lEnd = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), lastDay));
        boolean isChange = (lStart.substring(5).compareTo(lEnd.substring(5)) > 0);

        // 음력일정
        query.append("SELECT  ");
        //query.append("    DISTINCT CAST(STRFTIME('%d', " + Schedule.SCHEDULE_LDATE + ", 'LOCALTIME') AS INTEGER) DAY  ");
        query.append("  STRFTIME('%Y', '" + month + "-01" + "', 'LOCALTIME')||'-'|| substr(" + Schedule.SCHEDULE_LDATE + ", 6, 5) DAY  ");
        query.append("FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append("WHERE 1 = 1 ");
        query.append(" AND " + Schedule.ANNIVERSARY + " <> 'Y' ");
        query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
        if (isChange) {
            query.append(" AND (" + Schedule.SCHEDULE_LDATE + " between '" + lStart.substring(5) + "' and '12-31'");
            query.append(" OR " + Schedule.SCHEDULE_LDATE + " between '01-01' and '" + lEnd.substring(5) + "')");
        } else {
            query.append(" AND ( " + Schedule.SCHEDULE_LDATE + " between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "')");
        }
        // 음력기념일
        query.append(" UNION ALL  ");
        query.append("SELECT  ");
        //query.append("    DISTINCT CAST(STRFTIME('%d', " + Schedule.SCHEDULE_LDATE + ", 'LOCALTIME') AS INTEGER) DAY  ");
        query.append("  STRFTIME('%Y', '" + month + "-01" + "', 'LOCALTIME')||'-'|| substr(" + Schedule.SCHEDULE_LDATE + ", 6, 5) DAY  ");
        query.append("FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append("WHERE 1 = 1 ");
        query.append(" AND " + Schedule.ANNIVERSARY + " = 'Y' ");
        query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
        query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + month + "-01" + "', 'LOCALTIME')||'-12-31' ");

        if (isChange) {
            query.append(" AND (substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '" + lStart.substring(5) + "' and '12-31'");
            query.append(" OR substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '01-01' and '" + lEnd.substring(5) + "')");
        } else {
            query.append(" AND ( substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "')");
        }

        query.append(" ORDER BY 1 ");

        Log.d("DaoImpl-queryExistsSchedule", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryExistsDday(String month) {

        StringBuffer query = new StringBuffer();

        query.append("SELECT  ");
        query.append("    DISTINCT CAST(STRFTIME('%d',  DATE(" + Schedule.SCHEDULE_DATE + " ," + Schedule.DDAY_ALARMSIGN + "|| " + Schedule.DDAY_ALARMDAY + " ||' DAY', 'LOCALTIME')) AS INTEGER) DAY ");
        query.append("FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append("WHERE " + Schedule.DDAY_ALARMYN + " = 1 ");
        query.append("    AND STRFTIME('%Y-%m',  DATE(" + Schedule.SCHEDULE_DATE + " ," + Schedule.DDAY_ALARMSIGN + "|| " + Schedule.DDAY_ALARMDAY + " ||' DAY', 'LOCALTIME')) LIKE ?  ");
        query.append("    AND " + Schedule.DDAY_DISPLAYYN + " IN (0, 1, 2)  ");
        query.append("ORDER BY 1 ");

        String selectionArgs[] = new String[] { month + "%" };

        //Log.d("DaoImpl-queryExistsDday", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryDDay() {

        StringBuffer query = new StringBuffer();

        query.append("SELECT ");
        query.append(" " + Schedule.SCHEDULE_TITLE + " ");
        //query.append("," + Schedule.SCHEDULE_DATE + " ");
        query.append(",DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME') " + Schedule.SCHEDULE_DATE + " ");
        query.append(",cast(JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) dday  ");
        //query.append(",ROUND(JULIANDAY('NOW', 'LOCALTIME') - JULIANDAY(" + Schedule.SCHEDULE_DATE + ", 'LOCALTIME')) DDAY");
        //query.append("," + Schedule.SCHEDULE_LDATE + " ");
        //query.append("," + Schedule.LUNARYN + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.DDAY_DISPLAYYN + " = 1 ");

        String selectionArgs[] = null;

        Log.d("DaoImpl-queryDDay", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryWidget() {

        StringBuffer query = new StringBuffer();

        query.append("SELECT ");
        query.append(" " + Schedule.SCHEDULE_TITLE + " ");
        //query.append("," + Schedule.SCHEDULE_DATE + " ");
        query.append(",DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME') " + Schedule.SCHEDULE_DATE + " ");
        query.append(",cast(JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) dday  ");
        //query.append(",ROUND(JULIANDAY('NOW', 'LOCALTIME') - JULIANDAY(" + Schedule.SCHEDULE_DATE + ", 'LOCALTIME')) DDAY");
        //query.append("," + Schedule.SCHEDULE_LDATE + " ");
        //query.append("," + Schedule.LUNARYN + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        //query.append(" WHERE " + Schedule.DDAY_DISPLAYYN + " = 1 ");
        query.append(" LIMIT 1 ");

        String selectionArgs[] = null;

        Log.d("DaoImpl-queryDDay", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryDDay(Long id) {

        StringBuffer query = new StringBuffer();

        query.append("SELECT ");
        query.append(" " + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_DATE + " ");
        query.append(",cast(JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) dday  ");
        //query.append(",ROUND(JULIANDAY('NOW', 'LOCALTIME') - JULIANDAY(" + Schedule.SCHEDULE_DATE + ", 'LOCALTIME')) DDAY");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.DDAY_DISPLAYYN + " = 1 ");
        query.append(" AND " + Schedule._ID + " = " + id);

        String selectionArgs[] = null;

        //Log.d("DaoImpl-queryDDay", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryAlarm(Calendar c, String lDay) {

        String date = Common.fmtDate(c);
        String time = c.get(Calendar.HOUR_OF_DAY) > 9 ? "" + c.get(Calendar.HOUR_OF_DAY) : "0" + c.get(Calendar.HOUR_OF_DAY);

        String sday = Common.fmtDate(c).substring(5);
        String lday = lDay.substring(4, 6) + "-" + lDay.substring(6);
        int ilday = Integer.parseInt(lDay.substring(6).trim());

        StringBuffer query = new StringBuffer();

        query.append("SELECT ");
        query.append(" " + Schedule._ID + " ");
        query.append("," + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_CONTENTS + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.SCHEDULE_REPEAT + "=1"); // 한번 ㅇㅇ년ㅇㅇ월ㅇㅇ일ㅇ시 ㅇ분
        query.append(" AND " + Schedule.ALARM_DATE + " = '" + date + "'");
        query.append(" AND " + Schedule.ALARM_TIME + " >= '" + time + ":0'");
        query.append(" AND " + Schedule.ALARM_TIME + " <= '" + time + ":59'");
        query.append(" AND " + Schedule.SCHEDULE_CHECK + " != '" + date + "'");

        query.append(" UNION ALL ");

        query.append("SELECT ");
        query.append(" " + Schedule._ID + " ");
        query.append("," + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_CONTENTS + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.SCHEDULE_REPEAT + "=2"); // 매일 ㅇ시 ㅇ분
        query.append(" AND " + Schedule.ALARM_TIME + " >= '" + time + ":0'");
        query.append(" AND " + Schedule.ALARM_TIME + " <= '" + time + ":59'");
        query.append(" AND " + Schedule.SCHEDULE_CHECK + " != '" + date + "'");

        query.append(" UNION ALL ");

        query.append("SELECT ");
        query.append(" " + Schedule._ID + " ");
        query.append("," + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_CONTENTS + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.SCHEDULE_REPEAT + "=3"); // 매주 ㅇ요일 ㅇ시 ㅇ분
        query.append(" AND " + Schedule.ALARM_DAYS + " = " + c.get(Calendar.DAY_OF_WEEK));
        query.append(" AND " + Schedule.ALARM_TIME + " >= '" + time + ":0'");
        query.append(" AND " + Schedule.ALARM_TIME + " <= '" + time + ":59'");
        query.append(" AND " + Schedule.SCHEDULE_CHECK + " != '" + date + "'");

        query.append(" UNION ALL ");

        query.append("SELECT ");
        query.append(" " + Schedule._ID + " ");
        query.append("," + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_CONTENTS + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.SCHEDULE_REPEAT + "=4"); // 매월 ㅇ일 ㅇ시 ㅇ분
        query.append(" AND ((" + Schedule.ALARM_LUNASOLAR + " = 0 AND " + Schedule.ALARM_DAY + " = " + c.get(Calendar.DAY_OF_MONTH));
        query.append(") OR (" + Schedule.ALARM_LUNASOLAR + " = 1 AND " + Schedule.ALARM_DAY + " = " + ilday + "))");
        query.append(" AND " + Schedule.ALARM_TIME + " >= '" + time + ":0'");
        query.append(" AND " + Schedule.ALARM_TIME + " <= '" + time + ":59'");
        query.append(" AND " + Schedule.SCHEDULE_CHECK + " != '" + date + "'");

        query.append(" UNION ALL ");

        query.append("SELECT ");
        query.append(" " + Schedule._ID + " ");
        query.append("," + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_CONTENTS + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.SCHEDULE_REPEAT + "=5"); // 매년 ㅇ일 ㅇ시 ㅇ분
        query.append(" AND ((" + Schedule.ALARM_LUNASOLAR + " = 0 AND " + Schedule.ALARM_DATE + " = '" + sday + "'");
        query.append(") OR (" + Schedule.ALARM_LUNASOLAR + " = 1 AND " + Schedule.ALARM_DATE + " = '" + lday + "'))");
        query.append(" AND " + Schedule.ALARM_TIME + " >= '" + time + ":0'");
        query.append(" AND " + Schedule.ALARM_TIME + " <= '" + time + ":59'");
        query.append(" AND " + Schedule.SCHEDULE_CHECK + " != '" + date + "'");

        query.append(" UNION ALL ");

        query.append("SELECT ");
        query.append(" " + Schedule._ID + " ");
        query.append("," + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_CONTENTS + " ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.SCHEDULE_REPEAT + " = 6 ");
        query.append(" and schedule_date > strftime('%Y-%m-%d', 'now', 'localtime') ");
        query.append(" and cast (julianday('now', 'localtime') -  julianday(schedule_date, 'localtime') as integer) % alarm_day  = 0 ");

        String selectionArgs[] = null;

        Log.i("DaoImpl-queryAlarm", "query=" + query.toString());

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

    }

    public Cursor query(Long id) {

        StringBuilder query;
        query = new StringBuilder();

        query.append("SELECT " + Schedule._ID);
        query.append("    ," + Schedule.SCHEDULE_DATE);
        query.append("    ," + Schedule.SCHEDULE_TITLE);
        query.append("    ,case when schedule_repeat = 9 then " + Schedule.ALARM_DATE);
        query.append("    else " + Schedule.SCHEDULE_CONTENTS + " end " + Schedule.SCHEDULE_CONTENTS);
        query.append("    ," + Schedule.SCHEDULE_REPEAT);
        query.append("    ," + Schedule.SCHEDULE_CHECK);
        query.append("    ," + Schedule.ALARM_LUNASOLAR);
        query.append("    ," + Schedule.ALARM_DATE);
        query.append("    ," + Schedule.ALARM_TIME);
        query.append("    ," + Schedule.ALARM_DAYS);
        query.append("    ," + Schedule.ALARM_DAY);
        query.append("    ," + Schedule.DDAY_ALARMYN);
        query.append("    ," + Schedule.DDAY_ALARMDAY);
        query.append("    ," + Schedule.DDAY_ALARMSIGN);
        query.append("    ," + Schedule.DDAY_DISPLAYYN);
        query.append("    ," + Schedule.GID);
        query.append("    ," + Schedule.ANNIVERSARY);
        query.append("    ," + Schedule.LUNARYN);
        query.append("    ," + Schedule.SCHEDULE_LDATE);
        query.append("    ," + Schedule.ALARM_DETAILINFO);
        query.append("    ," + Schedule.DDAY_DETAILINFO);
        query.append("    ," + Schedule.SCHEDULE_TYPE);
        query.append("    ," + Schedule.BIBLE_BOOK);
        query.append("    ," + Schedule.BIBLE_CHAPTER);
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME);
        query.append(" WHERE 1 = 1 ");
        query.append(" AND " + Schedule._ID + " = " + id.toString());

        Log.d("DaoImpl-query", query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryGCalendar(Long id) {

        StringBuilder query;
        query = new StringBuilder();

        query.append("SELECT " + Schedule._ID);
        query.append("    ," + Schedule.SCHEDULE_DATE);
        query.append("    ," + Schedule.SCHEDULE_TITLE);
        query.append("    ,case when schedule_repeat = 9 then " + Schedule.ALARM_DATE);
        query.append("    else " + Schedule.SCHEDULE_CONTENTS + " end " + Schedule.SCHEDULE_CONTENTS);
        query.append("    ," + Schedule.SCHEDULE_REPEAT);
        query.append("    ," + Schedule.SCHEDULE_CHECK);
        query.append("    ," + Schedule.ALARM_LUNASOLAR);
        query.append("    ," + Schedule.ALARM_DATE);
        query.append("    ," + Schedule.ALARM_TIME);
        query.append("    ," + Schedule.ALARM_DAYS);
        query.append("    ," + Schedule.ALARM_DAY);
        query.append("    ," + Schedule.DDAY_ALARMYN);
        query.append("    ," + Schedule.DDAY_ALARMDAY);
        query.append("    ," + Schedule.DDAY_ALARMSIGN);
        query.append("    ," + Schedule.DDAY_DISPLAYYN);
        query.append("    ,strftime('%Y'," + Schedule.SCHEDULE_DATE + ",'localtime') year ");
        query.append("    ,case when schedule_repeat = 9 ");
        query.append("    then strftime('%m','1900-'||" + Schedule.ALARM_DATE + ",'localtime')  ");
        query.append("    else strftime('%m'," + Schedule.SCHEDULE_DATE + ",'localtime') end month ");
        query.append("    ,case when schedule_repeat = 9 ");
        query.append("    then strftime('%d','1900-'||" + Schedule.ALARM_DATE + ",'localtime')  ");
        query.append("    else strftime('%d'," + Schedule.SCHEDULE_DATE + ",'localtime') end day ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME);
        query.append(" WHERE 1 = 1 ");
        query.append(" AND " + Schedule._ID + " = " + id.toString());

        Log.d("DaoImpl-query", query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryGroup(String range, String date) {

        StringBuilder query;
        query = new StringBuilder();
        String baseDate = date;

        if ("".equals(baseDate)) {
            Calendar c = Calendar.getInstance();
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            baseDate = Common.fmtDate(c);
        } else if (baseDate.length() < 10)
            baseDate += "-01";

        query.append("SELECT " + Schedule._ID);
        query.append("    ,CASE WHEN " + Schedule.SCHEDULE_REPEAT + " = 9 THEN '" + mContext.getResources().getString(R.string.anniversary_label) + "'");
        query.append("    when  " + Schedule.SCHEDULE_REPEAT + " in ('F','P') THEN " + " ' B-Plan ' ");
        query.append("    when  " + Schedule.SCHEDULE_REPEAT + " < 9 and dday_alarmyn = 1 THEN " + Schedule.SCHEDULE_DATE + "||'\n'||strftime('%Y-%m-%d', DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'localtime') ");
        query.append("    ELSE " + Schedule.SCHEDULE_DATE + " END " + Schedule.SCHEDULE_DATE);
        query.append("    ,CASE WHEN " + Schedule.SCHEDULE_REPEAT + " IN ('F','P','9') THEN " + Schedule.SCHEDULE_TITLE + "||'('||" + Schedule.ALARM_DATE + "||')'");
        query.append("    when  " + Schedule.SCHEDULE_REPEAT + " < 9 and dday_alarmyn = 1 THEN ");
        query.append("   substr(schedule_title, 1, 15) ||'('|| ");
        query.append("case when cast(JULIANDAY('" + baseDate + "', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) > 0  ");
        query.append("then 'D + ' || cast(JULIANDAY('" + baseDate + "', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer)  ");
        query.append(" when cast(JULIANDAY('" + baseDate + "', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) = 0  ");
        query.append("then 'D day' else 'D ' ||  cast(JULIANDAY('" + baseDate + "', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) end ");
        query.append("    ||')' ");
        query.append("    ELSE " + Schedule.SCHEDULE_TITLE + " END " + Schedule.SCHEDULE_TITLE);
        query.append("    , " + Schedule.SCHEDULE_REPEAT);
        query.append("    , bible_book, bible_chapter ");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME);
        query.append(" WHERE 1 = 1 ");

        if (!"".equals(date)) {
            String sDate[] = Common.tokenFn(date, "-");

            String lDay = "";
            if (sDate.length > 2) {
                lDay = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), Integer.parseInt(sDate[2])));
            } else {
                lDay = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), 1));
            }

            if ("TODAY".equals(range)) {

                String lStart = lDay;
                String lEnd = lDay;

                //양력일정
                query.append(" AND ( " + Schedule.SCHEDULE_DATE + " = '" + date + "' ");
                query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
                query.append(" AND " + Schedule.ANNIVERSARY + " <> 'Y' )");

                //양력기념일
                query.append(" or ( " + Schedule.SCHEDULE_DATE + " = '" + date + "' ");
                query.append(" AND " + Schedule.ANNIVERSARY + " = 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
                query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + date.substring(0, 7) + "-01" + "', 'LOCALTIME')||'-12-31') ");

                //음력일정
                query.append(" or ( " + Schedule.ANNIVERSARY + " <> 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
                query.append(" AND ( " + Schedule.SCHEDULE_LDATE + " between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "'))");

                // 음력기념일
                query.append(" or( " + Schedule.ANNIVERSARY + " = 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
                query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + date.substring(0, 7) + "-01" + "', 'LOCALTIME')||'-12-31' ");
                query.append(" AND ( substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "'))");

                // dday
                query.append(" or ( dday_alarmyn = 1  ");
                query.append("and dday_displayyn = 2  ");
                query.append("or (dday_alarmyn = 1  ");
                query.append("and dday_displayyn in (0, 1)  ");
                query.append("and strftime('%Y-%m-%d', DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'localtime') = '" + date + "'  ) )");

                //성경읽기플랜
                /*
                if (Prefs.getBplan(mContext) && (Prefs.getBplanFamily(mContext) || Prefs.getBplanPersonal(mContext))) {
                    query.append("  or( schedule_repeat in ('F','P') ");
                    query.append("and schedule_date = '1900-01-01' ");
                    query.append("and alarm_date like '" + date.substring(5, 10) + "%' ");
                    query.append("and alarm_time = '00:00' )");
                }
                */

                if (Prefs.getAnniversary(this.mContext)) {

                    // 시스템 기념일
                    query.append("or ( schedule_repeat = 9 ");
                    query.append("and schedule_date = '1900-01-01' ");
                    query.append("and (   (alarm_lunasolar = 0 and alarm_date like '" + date.substring(5, 10) + "%') ");
                    //query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + lStart + "' and '" + lEnd + "') ) ");
                    query.append("     or (alarm_lunasolar = 1 and alarm_date = '" + lDay + "') ) ");
                    query.append("and alarm_time = '00:00' )");

                } else {
                    // 시스템 기념일
                    query.append("and schedule_repeat < 8 ");
                    query.append("and schedule_date > '1900-01-01' ");
                }

            } else if ("WEEK".equals(range)) {

                Calendar c = Calendar.getInstance();
                c.setFirstDayOfWeek(Calendar.SUNDAY);
                c.set(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]) - 1, Integer.parseInt(sDate[2]));
                c.add(Calendar.DAY_OF_MONTH, (c.get(Calendar.DAY_OF_WEEK) - 1) * -1);
                String Start = Common.fmtDate(c);
                c.add(Calendar.DAY_OF_MONTH, 6);
                String End = Common.fmtDate(c);

                c = Calendar.getInstance();
                c.setFirstDayOfWeek(Calendar.SUNDAY);
                c.set(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]) - 1, Integer.parseInt(sDate[2]));
                c.add(Calendar.DAY_OF_MONTH, ((c.get(Calendar.DAY_OF_WEEK) - 1) * -1));

                String lStart = Common.fmtDate(lunar2solar.s2l(c));
                c.add(Calendar.DAY_OF_MONTH, 6);
                String lEnd = Common.fmtDate(lunar2solar.s2l(c));
                boolean isChange = (lStart.substring(5).compareTo(lEnd.substring(5)) > 0);

                //양력일정
                query.append(" AND ( " + Schedule.SCHEDULE_DATE + " between '" + Start + "' and '" + End + "'");
                query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
                query.append(" AND " + Schedule.ANNIVERSARY + " <> 'Y' )");

                //양력기념일
                query.append(" or ( substr(" + Schedule.SCHEDULE_DATE + ",6,5) between '" + Start.substring(5, 10) + "' and '" + End.substring(5, 10) + "'");
                query.append(" AND " + Schedule.ANNIVERSARY + " = 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
                query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + date.substring(0, 7) + "-01" + "', 'LOCALTIME')||'-12-31') ");

                //음력일정
                query.append(" or ( " + Schedule.ANNIVERSARY + " <> 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
                if (isChange) {
                    query.append(" AND (" + Schedule.SCHEDULE_LDATE + " between '" + lStart.substring(5) + "' and '12-31'");
                    query.append(" OR " + Schedule.SCHEDULE_LDATE + " between '01-01' and '" + lEnd.substring(5) + "'))");
                } else {
                    query.append(" AND ( " + Schedule.SCHEDULE_LDATE + " between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "'))");
                }

                // 음력기념일
                query.append(" or( " + Schedule.ANNIVERSARY + " = 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
                query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + date.substring(0, 7) + "-01" + "', 'LOCALTIME')||'-12-31' ");

                if (isChange) {
                    query.append(" AND (substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '" + lStart.substring(5) + "' and '12-31'");
                    query.append(" OR substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '01-01' and '" + lEnd.substring(5) + "'))");
                } else {
                    query.append(" AND ( substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "'))");
                }

                // dday
                query.append(" or ( dday_alarmyn = 1  ");
                query.append("and dday_displayyn = 2  ");
                query.append("or (dday_alarmyn = 1  ");
                query.append("and dday_displayyn in (0, 1)  ");
                query.append("and strftime('%Y-%m-%d', DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'localtime') between '" + Start + "' and '" + End + "' ) )");

                //성경읽기플랜
                /*
                if (Prefs.getBplan(mContext) && (Prefs.getBplanFamily(mContext) || Prefs.getBplanPersonal(mContext))) {
                    query.append("  or( schedule_repeat in ('F','P') ");
                    query.append("and schedule_date = '1900-01-01' ");
                    query.append("and alarm_date between '" + Start.substring(5, 10) + "' and '" + End.substring(5, 10) + "' ");
                    query.append("and alarm_time = '00:00' )");
                }
                */

                if (Prefs.getAnniversary(this.mContext)) {

                    // 시스템 기념일
                    query.append("or ( schedule_repeat = 9 ");
                    query.append("and schedule_date = '1900-01-01' ");
                    query.append("and (alarm_lunasolar = 0 and alarm_date between '" + Start.substring(5, 10) + "' and '" + End.substring(5, 10) + "') ");
                    if (isChange) {
                        query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + lStart + "' and '" + "12-31')  ");
                        query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + "01-01' and '" + lEnd + "')  ");
                    } else {
                        query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + lStart + "' and '" + lEnd + "')  ");
                    }
                    query.append("and alarm_time = '00:00' )");

                } else {
                    // 시스템 기념일
                    query.append("and schedule_repeat < 8 ");
                    query.append("and schedule_date > '1900-01-01' ");
                }

            } else if ("MONTH".equals(range)) {
                String lStart = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), 1));
                Calendar c = Calendar.getInstance();
                c.setFirstDayOfWeek(Calendar.SUNDAY);
                c.set(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]) - 1, 1);
                c.add(Calendar.DAY_OF_MONTH, -1);
                int lastDay = c.get(Calendar.DAY_OF_MONTH);
                String lEnd = Common.fmtDate(lunar2solar.s2l(Integer.parseInt(sDate[0]), Integer.parseInt(sDate[1]), lastDay));

                boolean isChange = (lStart.substring(5).compareTo(lEnd.substring(5)) > 0);

                //양력일정
                query.append(" AND ( " + Schedule.SCHEDULE_DATE + " LIKE '" + date.substring(0, 7) + "%' ");
                query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
                query.append(" AND " + Schedule.ANNIVERSARY + " <> 'Y' )");

                //양력기념일
                query.append(" or ( substr(" + Schedule.SCHEDULE_DATE + ",6,2) = '" + date.substring(5, 7) + "' ");
                query.append(" AND " + Schedule.ANNIVERSARY + " = 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " <> 'Y' ");
                query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + date.substring(0, 7) + "-01" + "', 'LOCALTIME')||'-12-31') ");

                //음력일정
                query.append(" or ( " + Schedule.ANNIVERSARY + " <> 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
                if (isChange) {
                    query.append(" AND (" + Schedule.SCHEDULE_LDATE + " between '" + lStart.substring(5) + "' and '12-31'");
                    query.append(" OR " + Schedule.SCHEDULE_LDATE + " between '01-01' and '" + lEnd.substring(5) + "'))");
                } else {
                    query.append(" AND ( " + Schedule.SCHEDULE_LDATE + " between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "'))");
                }

                // 음력기념일
                query.append(" or ( " + Schedule.ANNIVERSARY + " = 'Y' ");
                query.append(" AND " + Schedule.LUNARYN + " = 'Y' ");
                query.append(" AND " + Schedule.SCHEDULE_DATE + " <= STRFTIME('%Y', '" + date.substring(0, 7) + "-01" + "', 'LOCALTIME')||'-12-31' ");

                if (isChange) {
                    query.append(" AND (substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '" + lStart.substring(5) + "' and '12-31'");
                    query.append(" OR substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '01-01' and '" + lEnd.substring(5) + "' ) ) ");
                } else {
                    query.append(" AND ( substr(" + Schedule.SCHEDULE_LDATE + ",6,5) between '" + lStart.substring(5) + "' and '" + lEnd.substring(5) + "'))");
                }

                // dday
                query.append(" or ( dday_alarmyn = 1  ");
                query.append("and dday_displayyn = 2  ");
                query.append("or (dday_alarmyn = 1  ");
                query.append("and dday_displayyn in (0, 1)  ");
                query.append("and strftime('%Y-%m-%d', DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'localtime') like '" + date.substring(0, 7) + "%'  ) )");

                //성경읽기플랜
                /*
                if (Prefs.getBplan(mContext) && (Prefs.getBplanFamily(mContext) || Prefs.getBplanPersonal(mContext))) {
                    query.append("  or( schedule_repeat in ('F','P') ");
                    query.append("and schedule_date = '1900-01-01' ");
                    query.append("and  alarm_date like '" + date.substring(5, 7) + "%' ");
                    query.append("and alarm_time = '00:00' )");
                }
                */

                if (Prefs.getAnniversary(this.mContext)) {
                    // 시스템 기념일
                    query.append("or ( schedule_repeat in ('F','P','9') ");
                    query.append("and schedule_date = '1900-01-01' ");
                    query.append("and    (alarm_lunasolar = 0 and alarm_date like '" + date.substring(5, 7) + "%') ");
                    if (isChange) {
                        query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + lStart + "' and '" + "12-31')  ");
                        query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + "01-01' and '" + lEnd + "') ");
                    } else {
                        query.append("     or (alarm_lunasolar = 1 and alarm_date between '" + lStart + "' and '" + lEnd + "')  ");
                    }
                    query.append("and alarm_time = '00:00' )");
                } else {
                    // 시스템 기념일
                    query.append("and schedule_repeat < 8 ");
                    query.append("and schedule_date > '1900-01-01' ");
                }
            } else {
                if (!Prefs.getAnniversary(this.mContext)) {
                    // 시스템 기념일
                    query.append("and schedule_repeat < 8 ");
                    query.append("and schedule_date > '1900-01-01' ");
                }
            }
        } else {
            if (!Prefs.getAnniversary(this.mContext)) {
                // 시스템 기념일
                query.append("and schedule_repeat < 8 ");
                query.append("and schedule_date > '1900-01-01' ");
            }
        }

        //query.append(" ORDER BY " + Schedule._ID + " DESC");
        query.append(" ORDER BY SUBSTR(" + Schedule.SCHEDULE_DATE + ", -5) ASC");

        Log.d("DaoImpl-queryGroup", query.toString());

        return getReadableDatabase().rawQuery(query.toString(), null);

    }

    public Cursor queryChild(Long id) {

        String selectionArgs[] = new String[] { id.toString() };

        StringBuilder query;

        query = new StringBuilder();

        query.append("SELECT " + Schedule._ID);
        query.append("    ,case when schedule_repeat = 9 then " + Schedule.ALARM_DATE);
        query.append("    else " + Schedule.SCHEDULE_CONTENTS + " end " + Schedule.SCHEDULE_CONTENTS);
        query.append("    ,case schedule_repeat ");
        query.append("    when 1 then  alarm_date || ' ' || alarm_time ");
        query.append("    when 2 then '" + mContext.getResources().getString(R.string.every_day_label) + "' || alarm_time ");
        query.append("    when 3 then '" + mContext.getResources().getString(R.string.every_week_label) + "' || d.dayname || ' '|| alarm_time ");
        query.append("    when 4 then '" + mContext.getResources().getString(R.string.every_month_label) + "' || ( ");
        query.append("        case when alarm_lunasolar = 0 then '" + mContext.getResources().getString(R.string.gregorian) + "' else '" + mContext.getResources().getString(R.string.lunar) + "' end)|| ' '|| alarm_day || '" + mContext.getResources().getString(R.string.day_label) + "' ");
        query.append("    when 5 then '" + mContext.getResources().getString(R.string.every_year_label) + "' || ( ");
        query.append("        case when alarm_lunasolar = 0 then '" + mContext.getResources().getString(R.string.gregorian) + "' else '" + mContext.getResources().getString(R.string.lunar) + "' end)|| ' '|| alarm_date || ' ' || alarm_time ");
        query.append("    else '" + mContext.getResources().getString(R.string.alarm_none) + "' end alarm_detailinfo ");
        query.append("    ,case when cast(JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) < 0   ");
        query.append("          then 'D ' || cast(JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) || 'day'  ");
        query.append("          when cast (JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) = 0 then 'D day'  ");
        query.append("          else 'D +' || cast(JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) || 'day' end dday_detailinfo  ");
        query.append("    , " + Schedule.SCHEDULE_REPEAT);
        query.append("    , bible_book, bible_chapter ");
        query.append(" FROM " + Schedule.SCHEDULE_DAYS_JOIN_TABLE);
        query.append(" WHERE _id = ? ");

        //Log.d("DaoImpl-queryChild", query.toString());

        return getReadableDatabase().rawQuery(query.toString(), selectionArgs);

        //return db.query(Schedule.SCHEDULE_DAYS_JOIN_TABLE, columns, selection, selectionArgs, groupBy, having, orderBy);

    }

    public void close() {
        if (db != null) {
            db.close();
        }
        super.close();
    }

    public void onDestroy() {
        close();
        super.onDestroy();

    }

}
