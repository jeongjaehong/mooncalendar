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

package org.nilriri.LunaCalendar.schedule;

import java.util.Calendar;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.ScheduleBean;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.dao.Constants.Schedule;
import org.nilriri.LunaCalendar.tools.Prefs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ScheduleViewer extends Activity implements OnClickListener {

    static final int TIME_DIALOG_ID = 0;
    static final int DATE_DIALOG_ID = 1;
    private ScheduleDaoImpl dao = null;

    ScheduleBean scheduleBean = new ScheduleBean();

    private TextView mSchedule_conents;
    private TextView mSchedule_repeat;
    private TextView mSpecialday_displayyn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));
        setContentView(R.layout.schedule_viewer);

        mSchedule_conents = (TextView) findViewById(R.id.schedule_contents);
        mSchedule_repeat = (TextView) findViewById(R.id.schedule_repeat);
        mSpecialday_displayyn = (TextView) findViewById(R.id.specialday_displayyn);

        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_editalarm).setOnClickListener(this);

    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_ok:
                this.finish();
                break;
            case R.id.btn_editalarm:
                Long id = getIntent().getLongExtra("id", -1);

                final Calendar cal = Calendar.getInstance();
                cal.setFirstDayOfWeek(Calendar.SUNDAY);

                Intent intent = new Intent();
                intent.setClass(getBaseContext(), ScheduleEditor.class);
                intent.putExtra("SID", new Long(id));
                intent.putExtra("STODAY", cal);
                startActivity(intent);

                this.finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Cursor cursor = dao.query(getIntent().getLongExtra("id", 0));

        int anniversary = -1;

        if (cursor.moveToNext()) {
            if ("F".equals(cursor.getString(Schedule.COL_SCHEDULE_REPEAT)) || "P".equals(cursor.getString(Schedule.COL_SCHEDULE_REPEAT))) {

                try {

                    Intent intent = new Intent();
                    intent.setAction("org.nilriri.webbibles.VIEW");
                    intent.setType("vnd.org.nilriri/web-bible");

                    intent.putExtra("VERSION", 0);
                    intent.putExtra("VERSION2", 0);
                    intent.putExtra("BOOK", cursor.getInt(Schedule.COL_BIBLE_BOOK));
                    intent.putExtra("CHAPTER", cursor.getInt(Schedule.COL_BIBLE_CHAPTER));
                    intent.putExtra("VERSE", 0);

                    startActivity(intent);

                    findViewById(R.id.btn_editalarm).setVisibility(View.GONE);

                    //onClick(findViewById(R.id.btn_ok));

                    this.finish();
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "온라인성경 앱일 설치되어있지 않거나 최신버젼이 아닙니다.", Toast.LENGTH_LONG).show();

                }

            }

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
            anniversary = cursor.getInt(Schedule.COL_ALARM_DAY);

            scheduleBean.setDday_alarmyn(cursor.getInt(Schedule.COL_DDAY_ALARMYN));
            scheduleBean.setDday_alarmday(cursor.getInt(Schedule.COL_DDAY_ALARMDAY));
            scheduleBean.setDday_alarmsign(cursor.getString(Schedule.COL_DDAY_ALARMSIGN));
            scheduleBean.setDday_displayyn(cursor.getInt(Schedule.COL_DDAY_DISPLAYYN));

        } else {

            this.finish();
        }
        cursor.close();

        String title = scheduleBean.getTitle();
        String lunarlabel = this.getResources().getString(R.string.check_lunar_label);
        if (scheduleBean.getRepeat() != 9) {
            if (scheduleBean.getLunarYN()) {
                title += " (" + lunarlabel + ":" + scheduleBean.getLDate() + ")";
            } else {
                title += " (" + scheduleBean.getDisplayDate() + ")";
            }
        }
        this.setTitle(title);

        mSchedule_conents.setText(scheduleBean.getContents());

        String repeat[] = getResources().getStringArray(R.array.schedule_repeat);
        String days[] = getResources().getStringArray(R.array.repeat_days);
        String lunarsolar[] = getResources().getStringArray(R.array.repeat_lunasolar);

        String repeatnm = "";
        if (repeat.length > scheduleBean.getRepeat()) {
            repeatnm = repeat[scheduleBean.getRepeat()] + " \n";
        }

        switch (scheduleBean.getRepeat()) {
            case 1:
                repeatnm += scheduleBean.getDisplayAlarmTime();
                break;
            case 2:
                repeatnm += scheduleBean.getDisplayAlarmDate() + ", ";
                repeatnm += scheduleBean.getDisplayAlarmTime();
                break;
            case 3:
                repeatnm += days[scheduleBean.getAlarmDays()] + ", ";
                repeatnm += scheduleBean.getDisplayAlarmTime();
                break;
            case 4:
                repeatnm += lunarsolar[scheduleBean.getLunaSolar()] + ", ";
                repeatnm += scheduleBean.getDisplayAlarmDay() + getResources().getString(R.string.schedule_dayname_viewer) + ", ";
                repeatnm += scheduleBean.getDisplayAlarmTime();
                break;
            case 5:
                repeatnm += lunarsolar[scheduleBean.getLunaSolar()] + ", ";
                repeatnm += scheduleBean.getDisplayAlarmDate() + getResources().getString(R.string.schedule_dayname_viewer) + ", ";
                repeatnm += scheduleBean.getDisplayAlarmTime();
                break;
            case 6:
                repeatnm += lunarsolar[scheduleBean.getLunaSolar()] + ", ";
                repeatnm += scheduleBean.getDisplayAlarmDay() + getResources().getString(R.string.schedule_dayname2_viewer) + ", ";
                repeatnm += scheduleBean.getDisplayAlarmTime();
                break;
            case 9:
                repeatnm = getResources().getString(R.string.schedule_anniversary_label);
                if (anniversary == 0) {
                    repeatnm += ", " + getResources().getString(R.string.schedule_holiday_label);
                }

                findViewById(R.id.btn_editalarm).setVisibility(View.GONE);
                break;
        }

        mSchedule_repeat.setText(repeatnm);

        String ddaykind[] = getResources().getStringArray(R.array.specialday_displayyn);
        String ddayalarmyn[] = getResources().getStringArray(R.array.specialday_alarmyn);
        String ddaysign[] = getResources().getStringArray(R.array.specialday_sign);

        String ddayinfo = "";

        switch (scheduleBean.getDday_alarmyn()) {
            case 0:
                ddayinfo += ddayalarmyn[scheduleBean.getDday_alarmyn()];
                break;
            case 1:
                // d-day를
                ddayinfo += getResources().getString(R.string.dday_info_label) + " ";

                // ㅇㅇㅇㅇ-ㅇㅇ-ㅇㅇ부터
                ddayinfo += scheduleBean.getDisplayDate() + getResources().getString(R.string.dday_fromto_label) + " ";

                // ㅇ일 후로, 일 전으로 설정
                ddayinfo += scheduleBean.getDisplayDday_alarmday();
                ddayinfo += ddaysign[scheduleBean.getDisplayDday_alarmsign()];

                String mDday_msg = "";
                Cursor cursor1 = dao.queryDDay(getIntent().getLongExtra("id", 0));
                if (cursor1.moveToNext()) {
                    int D_Day = cursor1.getInt(2);

                    if (D_Day == 0) {
                        mDday_msg += " (D day)";
                    } else if (D_Day > 0) {
                        mDday_msg += " (D + " + D_Day + "일)";
                    } else {
                        mDday_msg += " (D - " + Math.abs(D_Day - 1) + "일)";
                    }
                    mDday_msg = mDday_msg == null ? "" : mDday_msg;
                } else {
                    mDday_msg = "";
                }
                cursor1.close();

                ddayinfo += mDday_msg + "\n";
                ddayinfo += "\n" + getResources().getString(R.string.specialday_displayposigion) + " : ";
                ddayinfo += ddaykind[scheduleBean.getDday_displayyn()];

                break;
        }

        mSpecialday_displayyn.setText(ddayinfo);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
