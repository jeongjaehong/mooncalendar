package org.nilriri.LunaCalendar.dao;

import android.provider.BaseColumns;

/**
 * Convenience definitions for NotePadProvider
 */
public final class Constants {

   
    public static final String DATABASE_NAME = "lunacalendar.sqlite";
    public static final String EXTERNAL_DB_NAME = "/sdcard/lunacalendar.sqlite";
    public static final int DATABASE_VERSION = 31;
    public static final int EXTERNAL_DB_VERSION = 31;

    public static final String DEFAULT_SORT_ORDER = "_id DESC";

    // This class cannot be instantiated
    private Constants() {
    }

    /**
     * Schedule table
     */
    public static final class Schedule implements BaseColumns {
        // This class cannot be instantiated
        private Schedule() {
        }

        public static final String SCHEDULE_TABLE_NAME = "schedule";
        public static final String SCHEDULE_DAYS_JOIN_TABLE = "schedule s left join days d on s.alarm_days = d.days ";

        public static final String _ID = "_id";
        public static final String SCHEDULE_DATE = "schedule_date";
        public static final String SCHEDULE_TITLE = "schedule_title";
        public static final String SCHEDULE_CONTENTS = "schedule_contents";
        public static final String SCHEDULE_REPEAT = "schedule_repeat";
        public static final String SCHEDULE_CHECK = "schedule_check";
        public static final String ALARM_LUNASOLAR = "alarm_lunasolar";
        public static final String ALARM_DATE = "alarm_date";
        public static final String ALARM_TIME = "alarm_time";
        public static final String ALARM_DAYS = "alarm_days";
        public static final String ALARM_DAY = "alarm_day";
        public static final String DDAY_ALARMYN = "dday_alarmyn";
        public static final String DDAY_ALARMDAY = "dday_alarmday";
        public static final String DDAY_ALARMSIGN = "dday_alarmsign";
        public static final String DDAY_DISPLAYYN = "dday_displayyn";
        public static final String GID = "gid";
        public static final String ANNIVERSARY = "anniversary";
        public static final String LUNARYN = "lunaryn";
        public static final String SCHEDULE_LDATE = "schedule_ldate";
        public static final String ALARM_DETAILINFO = "alarm_detailinfo";
        public static final String DDAY_DETAILINFO = "dday_detailinfo";
        public static final String SCHEDULE_TYPE = "schedule_type";
        public static final String BIBLE_BOOK = "bible_book";
        public static final String BIBLE_CHAPTER = "bible_chapter";

        public static final int COL_ID = 0;
        public static final int COL_SCHEDULE_DATE = 1;
        public static final int COL_SCHEDULE_TITLE = 2;
        public static final int COL_SCHEDULE_CONTENTS = 3;
        public static final int COL_SCHEDULE_REPEAT = 4;
        public static final int COL_SCHEDULE_CHECK = 5;
        public static final int COL_ALARM_LUNASOLAR = 6;
        public static final int COL_ALARM_DATE = 7;
        public static final int COL_ALARM_TIME = 8;
        public static final int COL_ALARM_DAYS = 9;
        public static final int COL_ALARM_DAY = 10;
        public static final int COL_DDAY_ALARMYN = 11;
        public static final int COL_DDAY_ALARMDAY = 12;
        public static final int COL_DDAY_ALARMSIGN = 13;
        public static final int COL_DDAY_DISPLAYYN = 14;
        public static final int COL_GID = 15;
        public static final int COL_ANNIVERSARY = 16;
        public static final int COL_LUNARYN = 17;
        public static final int COL_SCHEDULE_LDATE = 18;
        public static final int COL_ALARM_DETAILINFO = 19;
        public static final int COL_DDAY_DETAILINFO = 20;
        public static final int COL_SCHEDULE_TYPE = 21;
        public static final int COL_BIBLE_BOOK = 22;
        public static final int COL_BIBLE_CHAPTER = 23;

    }

}
