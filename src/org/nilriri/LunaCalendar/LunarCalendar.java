package org.nilriri.LunaCalendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.nilriri.LunaCalendar.dao.ScheduleBean;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.gcal.EventEntry;
import org.nilriri.LunaCalendar.gcal.GoogleUtil;
import org.nilriri.LunaCalendar.schedule.ScheduleEditor;
import org.nilriri.LunaCalendar.schedule.ScheduleList;
import org.nilriri.LunaCalendar.schedule.ScheduleViewer;
import org.nilriri.LunaCalendar.tools.About;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.DataManager;
import org.nilriri.LunaCalendar.tools.Lunar2Solar;
import org.nilriri.LunaCalendar.tools.OldEvent;
import org.nilriri.LunaCalendar.tools.Prefs;
import org.nilriri.LunaCalendar.tools.Rotate3dAnimation;
import org.nilriri.LunaCalendar.tools.SearchData;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LunarCalendar extends Activity implements RefreshManager {

    static final int DATE_DIALOG_ID = 1;

    private OldEvent oldEvent;

    // Menu item ids    
    public static final int MENU_ITEM_SCHEDULELIST = Menu.FIRST;
    public static final int MENU_ITEM_ADDSCHEDULE = Menu.FIRST + 1;
    public static final int MENU_ITEM_ALLSCHEDULE = Menu.FIRST + 2;
    public static final int MENU_ITEM_WEEKSCHEDULE = Menu.FIRST + 3;
    public static final int MENU_ITEM_MONTHSCHEDULE = Menu.FIRST + 4;
    public static final int MENU_ITEM_GCALADDEVENT = Menu.FIRST + 5;
    public static final int MENU_ITEM_GCALIMPORT = Menu.FIRST + 6;
    public static final int MENU_ITEM_ABOUT = Menu.FIRST + 7;
    public static final int MENU_ITEM_DELSCHEDULE = Menu.FIRST + 8;
    public static final int MENU_ITEM_BACKUP = Menu.FIRST + 9;
    public static final int MENU_ITEM_RESTORE = Menu.FIRST + 10;
    public static final int MENU_ITEM_MAKECAL = Menu.FIRST + 11;
    public static final int MENU_ITEM_ONLINECAL = Menu.FIRST + 12;
    public static final int MENU_ITEM_SEARCH = Menu.FIRST + 13;

    // date and time
    public int mYear;
    public int mMonth;
    public int mDay;
    public ListView mListView;
    private int mAddMonthOffset = 0;
    public List<EventEntry> todayEvents = new ArrayList<EventEntry>();

    public ScheduleDaoImpl dao = null;

    private LunarCalendarView lunarCalendarView;
    private ViewGroup mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Common.checkAlarmService(LunarCalendar.this);

        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));
        oldEvent = new OldEvent(-1, -1);

        Intent intent = getIntent();

        final Calendar c = Calendar.getInstance();
        if (intent.hasExtra("DataPk")) {

            Bundle data = intent.getExtras();
            Long dataPK = (Long) data.get("DataPk");
            Log.d(Common.TAG, "dataPK=" + dataPK);
            ScheduleBean s = new ScheduleBean(dao.query(dataPK));

            if (dataPK > 0) {
                if (s.getLunaryn()) {
                    String sdate = Lunar2Solar.l2s(c.get(Calendar.YEAR) + "", s.getLMonth() + "", s.getLDay() + "");

                    Log.d(Common.TAG, "sdate=" + sdate);

                    mYear = Integer.parseInt(sdate.substring(0, 4));
                    mMonth = Integer.parseInt(sdate.substring(4, 6)) - 1;
                    mDay = Integer.parseInt(sdate.substring(6, 8));

                } else {
                    mYear = c.get(Calendar.YEAR);
                    mMonth = s.getMonth() - 1;
                    mDay = s.getDay();
                }
            } else {
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);
            }
        } else {
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
        }

        setContentView(R.layout.animations_main_screen);

        mContainer = (ViewGroup) findViewById(R.id.container);
        lunarCalendarView = (LunarCalendarView) findViewById(R.id.lunaCalendarView);
        lunarCalendarView.requestFocus();

        lunarCalendarView.mToDay = mYear + "." + mMonth + "." + mDay;

        lunarCalendarView.setOnCreateContextMenuListener(this);
        lunarCalendarView.setFocusableInTouchMode(true);
        lunarCalendarView.setFocusable(true);
        //lunarCalendarView.setLongClickable(true);

        lunarCalendarView.setDrawingCacheEnabled(true);

        lunarCalendarView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        oldEvent.set(event.getX(), event.getY());
                        break;
                    case MotionEvent.ACTION_UP:

                        if (Common.getExpandRect(lunarCalendarView.mPrevMonthR, 20).contains((int) event.getX(), (int) event.getY())) {
                            AddMonth(-1);
                        } else if (Common.getExpandRect(lunarCalendarView.mNextMonthR, 20).contains((int) event.getX(), (int) event.getY())) {
                            AddMonth(1);
                        } else if (lunarCalendarView.titleRect.contains((int) event.getX(), (int) event.getY())) {
                            showDialog(LunarCalendar.DATE_DIALOG_ID);
                        } else {
                            if (event.getX() - oldEvent.getX() > 50) {//Right
                                if (Prefs.getAnimation(LunarCalendar.this)) {
                                    applyRotation(-1, 0, 180);
                                    mAddMonthOffset = -1;
                                } else {
                                    AddMonth(-1);
                                }
                            } else if (event.getX() - oldEvent.getX() < -50) {
                                if (Prefs.getAnimation(LunarCalendar.this)) {
                                    applyRotation(1, 360, 180);
                                    mAddMonthOffset = 1;
                                } else {
                                    AddMonth(1);
                                }
                            } else if (event.getY() - oldEvent.getY() > 50) {
                                mAddMonthOffset = -12;
                            } else if (event.getY() - oldEvent.getY() < -50) {
                                mAddMonthOffset = 12;
                            } else {
                                lunarCalendarView.setSelection((int) (event.getX() / lunarCalendarView.getTileWidth()), (int) (event.getY() / lunarCalendarView.getTileHeight()));
                            }
                        }
                        oldEvent.set(event.getX(), event.getY());
                        break;

                    default:

                        return false;

                }
                return false;
            }

        });

        mListView = (ListView) findViewById(R.id.ContentsListView);

        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new ScheduleOnItemClickListener());

    }

    public class ScheduleOnItemClickListener implements OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

            if (pos < 0)
                return;

            Cursor c = (Cursor) parent.getItemAtPosition(pos);

            if (c != null) {
                Intent intent = new Intent();

                try {

                    if ("B-Plan".equals(c.getString(1))) {
                        intent.setAction("org.nilriri.webbibles.VIEW");
                        intent.setType("vnd.org.nilriri/web-bible");

                        intent.putExtra("VERSION", 0);
                        intent.putExtra("VERSION2", 0);
                        intent.putExtra("BOOK", c.getInt(4));
                        intent.putExtra("CHAPTER", c.getInt(5));
                        intent.putExtra("VERSE", 0);
                    } else {
                        intent.setClass(getBaseContext(), ScheduleViewer.class);
                        intent.putExtra("id", new Long(id));
                    }

                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "온라인성경 앱이 설치되어있지 않거나 최신버젼이 아닙니다.", Toast.LENGTH_LONG).show();

                }
            }

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }

    }

    public void AddMonth(int offset) {
        // 달이 바뀌면서 기존에 선택된 영역이 새로 바뀐달에서는 날짜 영역이 아닌경우 에러가 발생함.
        // TODO: 새로운 달의 날짜범위를 넘어가면 1일이나 마지막 날짜로 변환.
        try {
            final Calendar c = Calendar.getInstance();
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            c.set(mYear, mMonth, 1);
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DAY_OF_MONTH, -1);

            if (mDay < 1 || mDay > c.get(Calendar.DAY_OF_MONTH)) {
                mDay = 1;
                lunarCalendarView.setSelX(c.get(Calendar.DAY_OF_WEEK) - 1);
                lunarCalendarView.setSelY(2);
            }

            c.set(mYear, mMonth, mDay);
            c.add(Calendar.MONTH, offset);
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            lunarCalendarView.loadSchduleExistsInfo();

            // 달이 바뀔때는 화면전체를 다시 그린다.
            lunarCalendarView.invalidate();

            // 날짜가 바뀌면 다시 조회하기 위해서 초기화한다.
            todayEvents.clear();
        } catch (Exception e) {
            Log.e(Common.TAG, e.getMessage(), e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dao != null) {
            dao.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 설정화면에서 sd카드 사용여부를 변경하면 dao를 지정한 위치의 db로 다시 연결한다.
        if (dao.mSdcarduse != Prefs.getSDCardUse(this)) {
            dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));
        }

        /*
                if (Prefs.getAlarmCheck(this)) {// 알람생성
                    long firstTime = SystemClock.elapsedRealtime();

                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1000 * 60 * 5, mAlarmSender);
                } else {// 알람해제
                    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    am.cancel(mAlarmSender);
                }
        */
        //화면으로 복귀할때 새로 등록되거나 삭제된 일정정보를 화면에 갱신한다.
        lunarCalendarView.loadSchduleExistsInfo();

        updateDisplay();
    }

    public void updateDisplay() {
        lunarCalendarView.setSelection(lunarCalendarView.getSelX(), lunarCalendarView.getSelY());
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateDialog(int)
     * 날짜 선택 대화상자 생성및 표시.
     */

    private class ShowOnlineCalendar extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private AlertDialog.Builder builder;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(LunarCalendar.this, "", "구글캘린더에서 일정을 가져오고 있습니다...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String url = Prefs.getOnlineCalendar(LunarCalendar.this);

                Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                c.add(Calendar.DAY_OF_MONTH, -1);

                StringBuilder where = new StringBuilder("?start-min=");
                where.append(Common.fmtDate(c));
                where.append("&start-max=");
                c.add(Calendar.DAY_OF_MONTH, 2);
                where.append(Common.fmtDate(c));
                url += where.toString();

                if (todayEvents.size() <= 0) {
                    GoogleUtil gu = new GoogleUtil(Prefs.getAuthToken(LunarCalendar.this));
                    todayEvents = gu.getEvents(url);
                    if (todayEvents.size() <= 0) {
                        cancel(true);
                    }
                }

                String names[] = new String[todayEvents.size()];
                final String index[] = new String[todayEvents.size()];

                for (int i = 0; i < todayEvents.size(); i++) {
                    names[i] = todayEvents.get(i).getStartDate().substring(5, 10) + " : " + todayEvents.get(i).title;
                    index[i] = todayEvents.get(i).content;
                }

                builder = new AlertDialog.Builder(LunarCalendar.this);
                builder.setTitle("확인할 일정을 선택하십시오.");
                builder.setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        try {

                            if (index[which].indexOf("bindex:") >= 0) {

                                String bindex = index[which].replace("bindex:", "");
                                String data[] = Common.tokenFn(bindex, ",");

                                Intent intent = new Intent();
                                intent.setAction("org.nilriri.webbibles.VIEW");
                                intent.setType("vnd.org.nilriri/web-bible");

                                intent.putExtra("VERSION", 0);
                                intent.putExtra("VERSION2", 0);
                                intent.putExtra("BOOK", Integer.parseInt(data[0]));
                                intent.putExtra("CHAPTER", Integer.parseInt(data[1]));
                                intent.putExtra("VERSE", 0);

                                startActivity(intent);
                            } else {
                                Toast.makeText(LunarCalendar.this, index[which], Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(LunarCalendar.this, "온라인성경 앱이 설치되어있지 않거나 최신버젼이 아닙니다.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();

            if (todayEvents.size() > 0) {
                builder.show();
            } else {
                Toast.makeText(LunarCalendar.this, "구독하는 달력에 등록된 일정이 없습니다.", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);
                break;
        }
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            lunarCalendarView.loadSchduleExistsInfo();
            lunarCalendarView.invalidate();
            updateDisplay();
        }
    };

    public void onSelectTargetCalendar(int choice) {
        
        final int mChoice = choice;

        final String names[] = Prefs.getSyncCalendarName(LunarCalendar.this);
        final String values[] = Prefs.getSyncCalendarValue(LunarCalendar.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(LunarCalendar.this);
        builder.setTitle("배치 작업을 실행할 달력을 선택하십시오.");
        builder.setItems(names, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                try {

                    switch (mChoice) {
                        case 0: // <item>음력일정 일괄생성</item>

                            dao.batchMakeCalendar(LunarCalendar.this, values[which]);

                            break;
                        case 1: // <item>맥체인성경읽기 일정생성(가정)</item>

                            dao.batchBibleCalendar(LunarCalendar.this, values[which], which + "");

                            break;
                        case 2: // <item>맥체인성경읽기 일정생성(개인)</item>
                            dao.batchBibleCalendar(LunarCalendar.this, values[which], which + "");

                            break;
                        case 3: // <item>로컬일정 일괄 생성</item>

                            dao.batchUpload(LunarCalendar.this, values[which]);

                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LunarCalendar.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).show();

    }

    public void onBatchJob() {

        String dataworks[] = getResources().getStringArray(R.array.entries_batchjobs);

        AlertDialog.Builder builder = new AlertDialog.Builder(LunarCalendar.this);
        builder.setTitle("배치 작업을 선택하십시오.");
        builder.setItems(dataworks, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                try {

                    onSelectTargetCalendar(which);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LunarCalendar.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).show();

    }

    public void onDataWork() {

        String dataworks[] = getResources().getStringArray(R.array.entries_dataworks);

        AlertDialog.Builder builder = new AlertDialog.Builder(LunarCalendar.this);
        builder.setTitle("작업을 선택하십시오.");
        builder.setItems(dataworks, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                try {
                    switch (which) {
                        case 0: // 백업

                            DataManager.StartBackup(LunarCalendar.this);

                            break;
                        case 1: // 복원

                            DataManager.StartRestore(LunarCalendar.this);
                            AddMonth(0);
                            updateDisplay();

                            break;
                        case 2: // csv export
                            break;
                        case 3: // csv import
                            break;
                    }

                } catch (Exception e) {
                    Toast.makeText(LunarCalendar.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).show();

    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     * 옵션메뉴 생성.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem itemAdd = menu.add(0, MENU_ITEM_ADDSCHEDULE, 0, R.string.schedule_add_label);
        itemAdd.setIcon(android.R.drawable.ic_menu_add);

        MenuItem itemAllList = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
        itemAllList.setIcon(android.R.drawable.ic_menu_agenda);

        if (!"".equals(Prefs.getSyncCalendar(this))) {
            MenuItem itemImport = menu.add(0, MENU_ITEM_GCALIMPORT, 0, R.string.schedule_gcalimport_menu);
            itemImport.setIcon(android.R.drawable.ic_popup_sync);
        }

        MenuItem itemSearch = menu.add(0, MENU_ITEM_SEARCH, 0, R.string.eventsearch_label);
        itemSearch.setIcon(android.R.drawable.ic_menu_search);

        /*        
                MenuItem itemBackup = menu.add(0, MENU_ITEM_BACKUP, 0, R.string.backup_label);
                itemBackup.setIcon(android.R.drawable.ic_menu_save);

                MenuItem itemRestore = menu.add(0, MENU_ITEM_RESTORE, 0, R.string.restore_label);
                itemRestore.setIcon(android.R.drawable.ic_menu_upload);

                MenuItem itemLunarEvent = menu.add(0, MENU_ITEM_MAKECAL, 0, R.string.makecal_label);
                itemLunarEvent.setIcon(android.R.drawable.ic_menu_my_calendar);
        */

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_ALLSCHEDULE: {
                Intent intent = new Intent();
                intent.setClass(this, ScheduleList.class);
                final Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                intent.putExtra("org.nilriri.gscheduler.workday", c);
                intent.putExtra("ScheduleRange", "ALL");
                startActivity(intent);
                return true;
            }
            case MENU_ITEM_ADDSCHEDULE: {
                Intent intent = new Intent();
                intent.setClass(this, ScheduleEditor.class);

                intent.putExtra("SID", new Long(0));
                final Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                intent.putExtra("STODAY", c);
                startActivity(intent);
                return true;
            }
            case MENU_ITEM_GCALIMPORT: {
                if ("".equals(Prefs.getAuthToken(this)) || Prefs.getAuthToken(this) == null) {
                    Toast.makeText(getBaseContext(), "Google 계정을 설정하십시오.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, Prefs.class));
                } else {
                    dao.syncImport(this);
                }
                return true;
            }
            case MENU_ITEM_SEARCH: { // 자료검색
                Intent intent = new Intent();
                intent.setClass(this, SearchData.class);
                startActivity(intent);
                return true;
            }
            case R.id.datawork: { // 자료관리
                onDataWork();
                return true;
            }
            case R.id.batchjob: { // 배치작업
                onBatchJob();
                return true;
            }

            case R.id.settings: { // 설정메뉴
                startActivity(new Intent(this, Prefs.class));
                return true;
            }
            case R.id.about: { // 프로그램 정보
                startActivity(new Intent(this, About.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * (non-Javadoc)
    * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
    * 팝업메뉴 생성.
    */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(getResources().getString(R.string.app_name));

        menu.add(0, MENU_ITEM_ADDSCHEDULE, 0, R.string.add_schedule);
        menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
        menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
        menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);

        if (view.equals(this.mListView)) {
            AdapterView.AdapterContextMenuInfo info;
            try {
                info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            } catch (ClassCastException e) {
                Log.e("LunarCalendar", "bad menuInfo", e);
                return;
            }

            // 사용자가 등록한 일정일경우 삭제메뉴 표시
            Cursor cursor = (Cursor) this.mListView.getItemAtPosition(info.position);
            if (cursor != null && "Schedule".equals(cursor.getString(1))) {
                menu.add(0, MENU_ITEM_DELSCHEDULE, 0, R.string.schedule_delete_label);
            }
        }
        menu.add(0, MENU_ITEM_ONLINECAL, 0, R.string.onlinecalendar_label);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case MENU_ITEM_SCHEDULELIST: {
                Intent intent = new Intent();
                intent.setClass(this, ScheduleList.class);
                final Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                intent.putExtra("workday", c);
                intent.putExtra("ScheduleRange", "TODAY");

                startActivity(intent);

                return true;
            }
            case MENU_ITEM_WEEKSCHEDULE: {
                Intent intent = new Intent();
                intent.setClass(this, ScheduleList.class);
                final Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                intent.putExtra("workday", c);
                intent.putExtra("ScheduleRange", "WEEK");

                startActivity(intent);

                return true;
            }
            case MENU_ITEM_MONTHSCHEDULE: {
                Intent intent = new Intent();
                intent.setClass(this, ScheduleList.class);
                final Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                intent.putExtra("workday", c);
                intent.putExtra("ScheduleRange", "MONTH");

                startActivity(intent);

                return true;
            }
            case MENU_ITEM_ADDSCHEDULE: {

                Intent intent = new Intent();
                intent.setClass(this, ScheduleEditor.class);
                intent.putExtra("SID", new Long(0));
                final Calendar c = Calendar.getInstance();
                c.set(mYear, mMonth, mDay);
                intent.putExtra("STODAY", c);
                startActivity(intent);
                return true;
            }
            case MENU_ITEM_DELSCHEDULE: {
                AdapterView.AdapterContextMenuInfo info;
                try {
                    info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                } catch (ClassCastException e) {
                    Log.e("LunarCalendar", "bad menuInfo", e);
                    return false;
                }
                dao.syncDelete(info.id, this);
                return true;
            }
            case MENU_ITEM_ONLINECAL: {
                if (!"".equals(Prefs.getOnlineCalendar(LunarCalendar.this))) {
                    new ShowOnlineCalendar().execute();
                } else {
                    Toast.makeText(getBaseContext(), "설정화면에서 온라인 구독 달력을 지정하십시오.", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        }
        return false;
    }

    /*
     * 화면 전화 효과 설정.
     */
    private void applyRotation(int position, float start, float end) {
        final float centerX = mContainer.getWidth() / 2.0f;
        final float centerY = mContainer.getHeight() / 2.0f;

        final Rotate3dAnimation rotation = new Rotate3dAnimation(start, end, centerX, centerY, 310.0f, true);
        rotation.setDuration(500);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(position));

        mContainer.startAnimation(rotation);
    }

    private final class DisplayNextView implements Animation.AnimationListener {
        private final int mPosition;

        private DisplayNextView(int position) {
            mPosition = position;
        }

        public void onAnimationStart(Animation animation) {
            AddMonth(mAddMonthOffset);
        }

        public void onAnimationEnd(Animation animation) {
            mContainer.post(new SwapViews(mPosition));
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private final class SwapViews implements Runnable {
        private final int mPosition;

        public SwapViews(int position) {
            mPosition = position;
        }

        public void run() {
            final float centerX = mContainer.getWidth() / 2.0f;
            final float centerY = mContainer.getHeight() / 2.0f;
            Rotate3dAnimation rotation;

            if (mPosition < 0) {
                rotation = new Rotate3dAnimation(180, 360, centerX, centerY, 310.0f, false);
            } else {
                rotation = new Rotate3dAnimation(180, 0, centerX, centerY, 310.0f, false);
            }
            rotation.setDuration(500);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            mContainer.startAnimation(rotation);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nilriri.LunaCalendar.RefreshManager#refresh()
     * 싱크 작업 종료후 화면 리프레쉬.
     */
    public void refresh() {
        this.AddMonth(0);
    }

}
