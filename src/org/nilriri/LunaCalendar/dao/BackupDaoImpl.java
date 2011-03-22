package org.nilriri.LunaCalendar.dao;

import java.util.Calendar;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.Constants.Schedule;
import org.nilriri.LunaCalendar.tools.Common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class BackupDaoImpl extends AbstractDao {

    private Context mContext;

    private static String mColumns[] = new String[] { //
    Schedule._ID, //0
            Schedule.SCHEDULE_DATE, //1 
            Schedule.SCHEDULE_TITLE, //2
            Schedule.SCHEDULE_CONTENTS, //3 
            Schedule.SCHEDULE_REPEAT, //4
            Schedule.SCHEDULE_CHECK, //5
            Schedule.ALARM_LUNASOLAR, //6
            Schedule.ALARM_DATE, //7
            Schedule.ALARM_TIME, //8
            Schedule.ALARM_DAYS, //9
            Schedule.ALARM_DAY, //10
            Schedule.DDAY_ALARMYN, //11
            Schedule.DDAY_ALARMDAY, //12
            Schedule.DDAY_ALARMSIGN, //13
            Schedule.DDAY_DISPLAYYN, //14
    };

    public BackupDaoImpl(Context context, CursorFactory factory, boolean sdcarduse) {
        super(context, factory, sdcarduse);

        mContext = context;
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
        val.put(Schedule.SCHEDULE_DATE, scheduleBean.getDate());
        val.put(Schedule.SCHEDULE_TITLE, scheduleBean.getTitle());
        val.put(Schedule.SCHEDULE_CONTENTS, scheduleBean.getContents());
        val.put(Schedule.SCHEDULE_REPEAT, scheduleBean.getRepeat());

        val.put(Schedule.ALARM_LUNASOLAR, scheduleBean.getLunaSolar());

        switch (scheduleBean.getRepeat()) {
            case 5:
                // 매년주기 알람인경우 년도를 빼고 월과 일만 저장한다.
                String alarmdate = scheduleBean.getAlarmDate();
                if (!"".equals(alarmdate))
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

        //Log.d("DaoImpl-insert", "val=" + val.toString());

        getWritableDatabase().insert(Schedule.SCHEDULE_TABLE_NAME, null, val);
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

        SQLiteDatabase db = getReadableDatabase();

        return db.query(Schedule.SCHEDULE_TABLE_NAME, mColumns, selection, selectionArgs, groupBy, having, orderBy);
        */

        StringBuffer query = new StringBuffer();

        query.append("SELECT   ");
        query.append("    _id ");
        query.append("    ,'Schedule' schedule_type ");
        query.append("    ,schedule_title ");
        query.append("FROM  schedule ");
        query.append("WHERE schedule_date = ? ");
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

        query.append("union all  ");
        query.append("select _id, 'anniversary' schedule_type, schedule_title ");
        query.append("from schedule ");
        query.append("where schedule_repeat = 9 ");
        query.append("and schedule_date = '1900-01-01' ");
        query.append("and (   (alarm_lunasolar = 0 and alarm_date = '" + sday + "') ");
        query.append("     or (alarm_lunasolar = 1 and alarm_date = '" + lday + "') ) ");
        query.append("and alarm_time = '00:00' ");

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
        query.append("and dday_alarmyn = 1  ");
        query.append("and dday_displayyn = 2  ");
        query.append("or (dday_alarmyn = 1  ");
        query.append("and dday_displayyn in (0, 1)  ");
        query.append("and strftime('%Y-%m-%d', DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'localtime') = ? ) ");

        query.append("ORDER BY 1 ");

        String selectionArgs[] = new String[] { date, date, date, date, date, date, };

        //Log.d("DaoImpl-query", "date=" + date);

        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryExistsAnniversary(String month, String lfromday, String ltoday) {

        SQLiteDatabase db = getReadableDatabase();

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

        return db.rawQuery(query.toString(), null);

    }

    public Cursor queryExistsSchedule(String month) {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append("SELECT  ");
        query.append("    DISTINCT CAST(STRFTIME('%d', " + Schedule.SCHEDULE_DATE + ", 'LOCALTIME') AS INTEGER) DAY  ");
        query.append("FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append("WHERE " + Schedule.SCHEDULE_DATE + " LIKE ? ");
        query.append("ORDER BY 1 ");

        String selectionArgs[] = new String[] { month + "%" };

        //Log.d("DaoImpl-queryExistsSchedule", "query=" + query.toString());

        return db.rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryExistsDday(String month) {

        SQLiteDatabase db = getReadableDatabase();

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

        return db.rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryDDay() {

        SQLiteDatabase db = getReadableDatabase();

        StringBuffer query = new StringBuffer();

        query.append("SELECT ");
        query.append(" " + Schedule.SCHEDULE_TITLE + " ");
        query.append("," + Schedule.SCHEDULE_DATE + " ");
        query.append(",cast(JULIANDAY('now', 'LOCALTIME') - JULIANDAY(DATE(schedule_date, dday_alarmsign || dday_alarmday ||' DAY', 'LOCALTIME'), 'LOCALTIME') as integer) dday  ");
        //query.append(",ROUND(JULIANDAY('NOW', 'LOCALTIME') - JULIANDAY(" + Schedule.SCHEDULE_DATE + ", 'LOCALTIME')) DDAY");
        query.append(" FROM " + Schedule.SCHEDULE_TABLE_NAME + " ");
        query.append(" WHERE " + Schedule.DDAY_DISPLAYYN + " = 1 ");

        String selectionArgs[] = null;

        //Log.d("DaoImpl-queryDDay", "query=" + query.toString());

        return db.rawQuery(query.toString(), selectionArgs);

    }

    public Cursor queryDDay(Long id) {

        SQLiteDatabase db = getReadableDatabase();

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

        return db.rawQuery(query.toString(), selectionArgs);

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

        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(query.toString(), selectionArgs);

    }

    public Cursor query(Long id) {
        String selection = Schedule._ID + " = ? ";
        String selectionArgs[] = new String[] { id.toString() };
        String groupBy = null;
        String having = null;
        String orderBy = null;

        SQLiteDatabase db = getReadableDatabase();

        //Log.d("DaoImpl-query", id + "=" + id);

        return db.query(Schedule.SCHEDULE_TABLE_NAME, mColumns, selection, selectionArgs, groupBy, having, orderBy);

    }

    public Cursor queryGroup(String range, String date) {
        String columns[] = new String[] { Schedule._ID, Schedule.SCHEDULE_DATE, Schedule.SCHEDULE_TITLE, };

        String selection = null;
        String selectionArgs[] = null;
        if ("TODAY".equals(range)) {
            selection = Schedule.SCHEDULE_DATE + " = ? ";
            selectionArgs = new String[] { date };
        } else if ("MONTH".equals(range)) {
            selection = Schedule.SCHEDULE_DATE + " LIKE ? ";
            selectionArgs = new String[] { date + "%" };
        }
        String groupBy = null;
        String having = null;
        String orderBy = Schedule._ID + " DESC";

        SQLiteDatabase db = getReadableDatabase();

        //Log.d("DaoImpl-queryGroup", range + "," + date);

        return db.query(Schedule.SCHEDULE_TABLE_NAME, columns, selection, selectionArgs, groupBy, having, orderBy);

    }

    public Cursor queryChild(Long id) {

        String selectionArgs[] = new String[] { id.toString() };

        SQLiteDatabase db = getReadableDatabase();

        StringBuilder query;

        query = new StringBuilder();

        query.append("SELECT " + Schedule._ID);
        query.append("    ," + Schedule.SCHEDULE_CONTENTS);
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
        query.append(" FROM " + Schedule.SCHEDULE_DAYS_JOIN_TABLE);
        query.append(" WHERE _id = ? ");

        //Log.d("DaoImpl-queryChild", query.toString());

        return db.rawQuery(query.toString(), selectionArgs);

        //return db.query(Schedule.SCHEDULE_DAYS_JOIN_TABLE, columns, selection, selectionArgs, groupBy, having, orderBy);

    }

    public void update(ScheduleBean scheduleBean) {

        String[] args = new String[] { scheduleBean.getId() + "" };
        ContentValues val = new ContentValues();

        val.put(Schedule._ID, scheduleBean.getId());
        val.put(Schedule.SCHEDULE_DATE, scheduleBean.getDate());
        val.put(Schedule.SCHEDULE_TITLE, scheduleBean.getTitle());
        val.put(Schedule.SCHEDULE_CONTENTS, scheduleBean.getContents());
        val.put(Schedule.SCHEDULE_REPEAT, scheduleBean.getRepeat());
        val.put(Schedule.SCHEDULE_CHECK, scheduleBean.getCheck());

        val.put(Schedule.ALARM_LUNASOLAR, scheduleBean.getLunaSolar());

        switch (scheduleBean.getRepeat()) {
            case 5:
                // 매년주기 알람인경우 년도를 빼고 월과 일만 저장한다.
                String alarmdate = scheduleBean.getAlarmDate();
                if (!"".equals(alarmdate))
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

        //Log.d("DaoImpl-update", val.toString());

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        if (1 == scheduleBean.getDday_displayyn()) {
            // 기존에 상단에 표시하도록 되어있던 D-day정보는 목록표시로 변경한다.
            db.execSQL("update schedule set dday_displayyn = 0 where dday_displayyn = 1");
        }

        db.update(Schedule.SCHEDULE_TABLE_NAME, val, Schedule._ID + "=?", args);

        db.setTransactionSuccessful();
        db.endTransaction();
    }
}
