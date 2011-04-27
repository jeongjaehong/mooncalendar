package org.nilriri.LunaCalendar.dao;

import org.nilriri.LunaCalendar.dao.Constants.Schedule;

import android.database.Cursor;

public class DAOUtil {

    public static ScheduleBean Cursor2Bean(Cursor cursor) {
        ScheduleBean scheduleBean = new ScheduleBean();

        if (cursor.moveToNext()) {

            scheduleBean.setId(cursor.getInt(Schedule.COL_ID));
            scheduleBean.setDate(cursor.getString(Schedule.COL_SCHEDULE_DATE));

            scheduleBean.setLDate(cursor.getString(Schedule.COL_SCHEDULE_LDATE));

            scheduleBean.setLunarYN("Y".equals(cursor.getString(Schedule.COL_LUNARYN)));
            scheduleBean.setAnniversary("Y".equals(cursor.getString(Schedule.COL_ANNIVERSARY)));

            scheduleBean.setTitle(cursor.getString(Schedule.COL_SCHEDULE_TITLE));
            scheduleBean.setContents(cursor.getString(Schedule.COL_SCHEDULE_CONTENTS));
            scheduleBean.setRepeat(cursor.getInt(Schedule.COL_SCHEDULE_REPEAT));

            scheduleBean.setScheduleCheck(cursor.getString(Schedule.COL_SCHEDULE_CHECK));
            scheduleBean.setLunaSolar(cursor.getInt(Schedule.COL_ALARM_LUNASOLAR));
            scheduleBean.setAlarmDate(cursor.getString(Schedule.COL_ALARM_DATE));
            scheduleBean.setAlarmTime(cursor.getString(Schedule.COL_ALARM_TIME));
            scheduleBean.setAlarmDays(cursor.getInt(Schedule.COL_ALARM_DAYS));
            scheduleBean.setAlarmDay(cursor.getInt(Schedule.COL_ALARM_DAY));

            scheduleBean.setDday_alarmyn(cursor.getInt(Schedule.COL_DDAY_ALARMYN));
            scheduleBean.setDday_alarmday(cursor.getInt(Schedule.COL_DDAY_ALARMDAY));
            scheduleBean.setDday_alarmsign(cursor.getString(Schedule.COL_DDAY_ALARMSIGN));
            scheduleBean.setDday_displayyn(cursor.getInt(Schedule.COL_DDAY_DISPLAYYN));
        }

        return scheduleBean;

    }

}
