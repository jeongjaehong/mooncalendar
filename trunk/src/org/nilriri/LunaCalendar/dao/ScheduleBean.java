package org.nilriri.LunaCalendar.dao;

import java.util.Calendar;
import java.util.StringTokenizer;

import org.nilriri.LunaCalendar.tools.Common;

import android.text.Editable;

public class ScheduleBean {

    private int _id;
    private String schedule_date;
    private String schedule_ldate;
    private String lunaryn;
    private String anniversary;

    private String schedule_title;
    private String schedule_contents;
    private int schedule_repeat;
    private String schedule_check;
    private int alram_lunasolar;
    private String alarm_date;
    private String alarm_time;
    private int alarm_days;
    private int alarm_day;

    private int dday_alarmyn; // 알림여부
    private int dday_alarmday; // +- 일수
    private String dday_alarmsign; // +- 이전 or 이후
    private int dday_displayyn; // 표시여부
    private String gid; // google calendar icaluid 

    public String getTitle() {
        return this.schedule_title == null ? "" : this.schedule_title;
    }

    public void setTitle(String title) {
        this.schedule_title = title;
    }

    public void setTitle(Editable title) {
        this.schedule_title = title.toString();
    }

    public int getId() {
        return this._id;
    }

    public void setId(int id) {
        this._id = id;
    }

    public String getContents() {
        return this.schedule_contents == null ? "" : this.schedule_contents;
    }

    public int getRepeat() {

        return this.schedule_repeat;
    }

    public String getDate() {
        return this.schedule_date == null ? "" : this.schedule_date;
    }

    public String getLDate() {
        return this.schedule_ldate == null ? "" : this.schedule_ldate;
    }

    public boolean getLunarYN() {

        return "Y".equals(this.lunaryn);
    }

    public boolean getAnniversary() {

        return "Y".equals(this.anniversary);
    }

    public String getDisplayDate() {
        String ret = Common.fmtDate(this.getYear(), this.getMonth(), this.getDay());
        return ret;
    }

    public int getYear() {
       
        StringTokenizer token = new StringTokenizer(this.schedule_date, "-");
        int rssult = Integer.parseInt(token.nextToken());
        
        return rssult;

    }

    public int getMonth() {
        StringTokenizer token = new StringTokenizer(this.schedule_date, "-");

        token.nextToken();

        int rssult = Integer.parseInt(token.nextToken());
        return rssult;
    }

    public int getDay() {
        StringTokenizer token = new StringTokenizer(this.schedule_date, "-");

        token.nextToken();
        token.nextToken();

        int rssult = Integer.parseInt(token.nextToken());
        return rssult;
    }

    public void setDate(Editable date) {
        this.schedule_date = date.toString();

    }

    public void setDate(Calendar c) {

        String date = Common.fmtDate(c);

        this.schedule_date = date;

    }

    public void setLDate(String date) {

        this.schedule_ldate = date;
    }

    public void setLunarYN(boolean lunaryn) {

        this.lunaryn = (lunaryn == true ? "Y" : "N");
    }

    public void setAnniversary(boolean anniversary) {

        this.anniversary = (anniversary == true ? "Y" : "N");
    }

    public void setDate(String date) {
        this.schedule_date = date;

    }

    public void setContents(Editable contents) {
        this.schedule_contents = contents.toString();

    }

    public void setContents(String contents) {
        this.schedule_contents = contents;

    }

    public void setRepeat(int repeat) {
        this.schedule_repeat = repeat;

    }

    public void setScheduleCheck(String schedule_check) {
        this.schedule_check = schedule_check;

    }

    public String getCheck() {
        return this.schedule_check == null ? "" : this.schedule_check;
    }

    public int getLunaSolar() {
        return this.alram_lunasolar;
    }

    public String getAlarmDate() {

        return this.alarm_date == null ? "" : this.alarm_date;
    }

    public int getAlarmYear() {
        String src = this.alarm_date == null ? "" : this.alarm_date;

        StringTokenizer token = new StringTokenizer(src, "-");

        switch (src.length()) {
            case 10: {
                return Integer.parseInt(token.nextToken());
            }
            default:
                return initToday(Calendar.YEAR);
        }
    }

    public int getAlarmMonth() {
        String src = this.alarm_date == null ? "" : this.alarm_date;

        StringTokenizer token = new StringTokenizer(src, "-");

        switch (src.length()) {
            case 10: {
                token.nextToken();
                return Integer.parseInt(token.nextToken());
            }
            case 5: {
                return Integer.parseInt(token.nextToken());
            }
            default:

                return initToday(Calendar.MONTH) + 1;
        }
    }

    public int getAlarmDayofMonth() {
        String src = this.alarm_date == null ? "" : this.alarm_date;

        StringTokenizer token = new StringTokenizer(src, "-");

        switch (src.length()) {
            case 10: {
                token.nextToken();
                token.nextToken();
                return Integer.parseInt(token.nextToken());

            }
            case 5: {
                token.nextToken();
                return Integer.parseInt(token.nextToken());
            }
            default:

                return Integer.parseInt(fmtToday(Calendar.DAY_OF_MONTH));
        }

    }

    public String fmtToday(int field) {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);

        int ret = c.get(field);

        //if (field == Calendar.MONTH)            ret += 1;
        if (field == Calendar.HOUR && ret == 0)
            ret += 12;

        return ret >= 10 ? ret + "" : "0" + ret;
    }

    public int initToday(int field) {
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);

        int ret = c.get(field);

        //if (field == Calendar.MONTH)            ret += 1;

        return ret;

    }

    public String getDisplayAlarmDate() {
        String ret = "";
        if (getRepeat() == 5) { // 매년 주기면 월과 일자만.

            ret = getAlarmMonth() > 9 ? getAlarmMonth() + "" : "0" + getAlarmMonth();
            ret += "-" + (getAlarmDayofMonth() > 9 ? getAlarmDayofMonth() : "0" + getAlarmDayofMonth());
            if ("".equals(ret))
                ret = fmtToday(Calendar.MONTH) + "-" + fmtToday(Calendar.DAY_OF_MONTH);
        } else {
            ret = getAlarmYear() + "-";
            ret += getAlarmMonth() > 9 ? getAlarmMonth() : "0" + getAlarmMonth();
            ret += "-" + (getAlarmDayofMonth() > 9 ? getAlarmDayofMonth() : "0" + getAlarmDayofMonth());
            if ("".equals(ret)) {
                ret = fmtToday(Calendar.YEAR) + "-" + fmtToday(Calendar.MONTH) + "-" + fmtToday(Calendar.DAY_OF_MONTH);
            }
        }

        return ret;

    }

    public String getAlarmTime() {
        return this.alarm_time == null ? "" : this.alarm_time;
    }

    public String getDisplayAlarmTime() {
        String ret = this.getAlarmAmPm() + ":";
        int h = this.getAlarmHour() == 0 ? 12 : this.getAlarmHour();
        ret += h >= 10 ? h + ":" : "0" + h + ":";
        ret += this.getAlarmMinute() >= 10 ? this.getAlarmMinute() : "0" + this.getAlarmMinute();

        return ret;
    }

    public int getAlarmDays() {
        return this.alarm_days == 0 ? this.initToday(Calendar.DAY_OF_WEEK) : this.alarm_days;
    }

    public int getAlarmDay() {
        return (this.alarm_day + "") == null ? initToday(Calendar.DAY_OF_MONTH) : this.alarm_day;
    }

    public String getDisplayAlarmDay() {
        String ret = this.alarm_day >= 10 ? this.alarm_day + "" : "0" + this.alarm_day;
        return ret == null ? fmtToday(Calendar.DAY_OF_MONTH) : ret;
    }

    public void setLunaSolar(int arg) {
        this.alram_lunasolar = arg;
    }

    public void setAlarmDate(String arg) {
        if (arg == null || "".equals(arg)) {
            final Calendar c = Calendar.getInstance();
            if (getRepeat() == 5) { // 매년 주기면 월과 일자만.
                arg = Common.fmtDate(c).substring(0, 7);
            } else {
                arg = Common.fmtDate(c);
            }
        }
        this.alarm_date = arg;
    }

    public void setAlarmTime(String arg) {

        if (arg == null || "".equals(arg)) {
            final Calendar c = Calendar.getInstance();
            Common.fmtTime(c);
        }
        this.alarm_time = arg;
    }

    public void setAlarmDays(int arg) {
        if (arg == 0) {
            final Calendar c = Calendar.getInstance();
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            arg = c.get(Calendar.DAY_OF_WEEK);
        }
        this.alarm_days = arg;
    }

    public void setAlarmDay(int arg) {
        if (arg == 0) {
            final Calendar c = Calendar.getInstance();
            arg = c.get(Calendar.DAY_OF_MONTH);
        }
        this.alarm_day = arg;
    }

    public String getAlarmAmPm() {
        String src = this.alarm_time == null ? "" : this.alarm_time;

        StringTokenizer token = new StringTokenizer(src, ":");

        if (token.hasMoreTokens())
            return Integer.parseInt(token.nextToken()) >= 12 ? "PM" : "AM";
        else
            return initToday(Calendar.AM_PM) == 0 ? "AM" : "PM";

    }

    public int getAlarmHour24() {
        String src = this.alarm_time == null ? "" : this.alarm_time;

        StringTokenizer token = new StringTokenizer(src, ":");

        if (token.countTokens() == 2) {
            return Integer.parseInt(token.nextToken());
        } else {
            return initToday(Calendar.HOUR_OF_DAY);
        }

    }

    public int getAlarmHour() {
        String src = this.alarm_time == null ? "" : this.alarm_time;

        StringTokenizer token = new StringTokenizer(src, ":");

        if (token.countTokens() == 2) {

            int ret = Integer.parseInt(token.nextToken());

            return ret > 12 ? ret - 12 : ret;
        } else {
            return initToday(Calendar.HOUR);
        }

    }

    public int getAlarmMinute() {
        String src = this.alarm_time == null ? "" : this.alarm_time;

        StringTokenizer token = new StringTokenizer(src, ":");
        if (token.countTokens() == 2) {
            token.nextToken(); //hour

            return Integer.parseInt(token.nextToken());
        } else {
            return initToday(Calendar.MINUTE);
        }

    }

    /**
     * @param dday_alarmyn the dday_alarmyn to set
     */
    public void setDday_alarmyn(int dday_alarmyn) {
        this.dday_alarmyn = dday_alarmyn;
    }

    /**
     * @return the dday_alarmyn
     */
    public int getDday_alarmyn() {
        return dday_alarmyn;
    }

    /**
     * @param dday_alarmday the dday_alarmday to set
     */
    public void setDday_alarmday(int dday_alarmday) {
        this.dday_alarmday = dday_alarmday;
    }

    public void setDday_alarmday(String dday_alarmday) {
        if ("".equals(dday_alarmday))
            dday_alarmday = "0";
        this.dday_alarmday = Integer.parseInt(dday_alarmday.trim());
    }

    /**
     * @return the dday_alarmday
     */
    public int getDday_alarmday() {
        return dday_alarmday;
    }

    public String getDisplayDday_alarmday() {
        return dday_alarmday + "";
    }

    /**
     * @param dday_alarmsign the dday_alarmsign to set
     */
    public void setDday_alarmsign(int dday_alarmsign) {
        this.dday_alarmsign = dday_alarmsign == 0 ? "-" : "+";
    }

    public void setDday_alarmsign(String dday_alarmsign) {
        this.dday_alarmsign = dday_alarmsign;
    }

    /**
     * @return the dday_alarmsign
     */
    public String getDday_alarmsign() {
        return dday_alarmsign;
    }

    public int getDisplayDday_alarmsign() {
        return "-".equals(dday_alarmsign) ? 0 : 1;
    }

    /**
     * @param dday_displayyn the dday_displayyn to set
     */
    public void setDday_displayyn(int dday_displayyn) {
        this.dday_displayyn = dday_displayyn;
    }

    /**
     * @return the dday_displayyn
     */
    public int getDday_displayyn() {
        return dday_displayyn;
    }

    public void setGID(String arg) {
        // TODO Auto-generated method stub

        this.gid = (arg == null ? "" : arg);

    }

    public String getGID() {
        // TODO Auto-generated method stub

        return this.gid == null ? "" : this.gid;

    }

}
