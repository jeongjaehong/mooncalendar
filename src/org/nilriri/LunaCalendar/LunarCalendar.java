package org.nilriri.LunaCalendar;

import java.util.Calendar;

import org.nilriri.LunaCalendar.alarm.AlarmService_Service;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.schedule.ScheduleEditor;
import org.nilriri.LunaCalendar.schedule.ScheduleList;
import org.nilriri.LunaCalendar.schedule.ScheduleViewer;
import org.nilriri.LunaCalendar.tools.About;
import org.nilriri.LunaCalendar.tools.DataManager;
import org.nilriri.LunaCalendar.tools.EventFeedDemo;
import org.nilriri.LunaCalendar.tools.OldEvent;
import org.nilriri.LunaCalendar.tools.Prefs;
import org.nilriri.LunaCalendar.tools.Rotate3dAnimation;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
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

public class LunarCalendar extends Activity {

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

    // date and time
    public int mYear;
    public int mMonth;
    public int mDay;
    public ListView mListView;
    private int mAddMonthOffset = 0;

    public ScheduleDaoImpl dao = null;

    private LunarCalendarView lunarCalendarView;
    private ViewGroup mContainer;
    private PendingIntent mAlarmSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));

        mAlarmSender = PendingIntent.getService(LunarCalendar.this, 0, new Intent(LunarCalendar.this, AlarmService_Service.class), 0);

        oldEvent = new OldEvent(-1, -1);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

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
                        //Log.d(TAG, "oldEvent():" + oldEvent.toString());
                        //Log.d(TAG, "upEvent():" + event.toString());

                        if (getExpandRect(lunarCalendarView.mPrevMonthR, 20).contains((int) event.getX(), (int) event.getY())) {
                            AddMonth(-1);
                        } /*else if (getExpandRect(lunarCalendarView.mPrevYearR, 20).contains((int) event.getX(), (int) event.getY())) {
                            // Toast.makeText(getBaseContext(), lunarCalendarView.mPrevYearR.toString() + ":" + event.getX() + "," + (int) event.getY(), Toast.LENGTH_LONG).show();
                            AddMonth(-12);
                          }*/else if (getExpandRect(lunarCalendarView.mNextMonthR, 20).contains((int) event.getX(), (int) event.getY())) {
                            AddMonth(1);
                        } /*else if (getExpandRect(lunarCalendarView.mNextYearR, 20).contains((int) event.getX(), (int) event.getY())) {
                            AddMonth(12);
                          } */else if (lunarCalendarView.titleRect.contains((int) event.getX(), (int) event.getY())) {
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
                                //applyRotation(-1, 0, 180);
                                //AddMonth(-12);
                                mAddMonthOffset = -12;
                            } else if (event.getY() - oldEvent.getY() < -50) {
                                //applyRotation(1, 360, 180);
                                //AddMonth(12);
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

        // Inform the list we provide context menus for items
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(new ScheduleOnItemClickListener());

    }

    private Rect getExpandRect(Rect rect, int offset) {
        Rect target = new Rect();
        target.set(rect.left - offset, rect.top - (offset + 20), rect.right + offset, rect.bottom + (offset + 20));
        return target;
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
                    Toast.makeText(getBaseContext(), "온라인성경 앱일 설치되어있지 않거나 최신버젼이 아닙니다.", Toast.LENGTH_LONG).show();

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
        //Log.d("XXXXXXXXXXXX", "AddMonth=" + mMonth + "." + mDay);

        final Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        c.set(mYear, mMonth, 1);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_MONTH, -1);

        //Log.d("AddMonth", "mDay=" + mDay + ",max=" + c.get(Calendar.DAY_OF_MONTH));

        if (mDay < 1 || mDay > c.get(Calendar.DAY_OF_MONTH)) {
            mDay = 1;
            lunarCalendarView.setSelX(c.get(Calendar.DAY_OF_WEEK) - 1);
            lunarCalendarView.setSelY(2);
            //updateDisplay();
        }

        c.set(mYear, mMonth, mDay);
        c.add(Calendar.MONTH, offset);

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        //updateDisplay();

        lunarCalendarView.loadSchduleExistsInfo();

        // 달이 바뀔때는 화면전체를 다시 그린다.
        lunarCalendarView.invalidate();

    }

    @Override
    protected void onResume() {
        super.onResume();
        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));
        // Music.play(this, android.R.raw.);

        /*
        String uri = "geo:"+ -122.084095 + "," + 37.422006;   
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));   
          
        // You can also choose to place a point like so:   
        String uris = "geo:"+ 37.422006 + "," + -122.084095 + "?q=my+street+address";   
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uris)));           
        */

        /*
        
        Intent intent = new Intent();   
        intent.setAction(Intent.ACTION_PICK);   
        Uri startDir = Uri.fromFile(new File("/sdcard"));   
        // Files and directories   
        intent.setDataAndType(startDir, "vnd.android.cursor.dir/lysesoft.andexplorer.file");   
        // Optional filtering on file extension.   
        intent.putExtra("browser_filter_extension_whitelist", "*.png,*.txt,*.mp3");   
        // Title   
        intent.putExtra("explorer_title", "Select a file");   
        // Optional colors   
        intent.putExtra("browser_title_background_color", "440000AA");   
        intent.putExtra("browser_title_foreground_color", "FFFFFFFF");   
        intent.putExtra("browser_list_background_color", "66000000");   
        // Optional font scale   
        intent.putExtra("browser_list_fontscale", "120%");   
        // Optional 0=simple list, 1 = list with filename and size, 2 = list with filename, size and date.   
        intent.putExtra("browser_list_layout", "2");   
        startActivityForResult(intent, 0);
        */

        if (Prefs.getAlarmCheck(this)) {
            long firstTime = SystemClock.elapsedRealtime();

            // ScheduleBean the alarm!
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1000 * 60 * 5, mAlarmSender);

            // Tell the user about what we did.
            // Toast.makeText(this, "알람이 설정되었습니다.", Toast.LENGTH_LONG).show();           

        } else {
            // And cancel the alarm.
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            am.cancel(mAlarmSender);

            // Tell the user about what we did.
            // Toast.makeText(this, "알람이 해재되었습니다.", Toast.LENGTH_LONG).show();
        }
        //화면으로 복귀할때 새로 등록되거나 삭제된 일정정보를 화면에 갱신한다.

        lunarCalendarView.loadSchduleExistsInfo();

        updateDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void updateDisplay() {

        lunarCalendarView.setSelection(lunarCalendarView.getSelX(), lunarCalendarView.getSelY());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        MenuItem item1 = menu.add(0, MENU_ITEM_ADDSCHEDULE, 0, R.string.schedule_add_label);
        item1.setIcon(android.R.drawable.ic_menu_add);

        MenuItem item2 = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
        item2.setIcon(android.R.drawable.ic_menu_agenda);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        //Intent intent = new Intent(null, getIntent().getData());
        //intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        //menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, Keypad.class), null, intent, 0, null);

        if (Prefs.getGCalendarSync(this)) {
            MenuItem item3 = menu.add(0, MENU_ITEM_GCALIMPORT, 0, R.string.schedule_gcalimport_menu);
            item3.setIcon(android.R.drawable.ic_menu_rotate);
        }

        MenuItem item5 = menu.add(0, MENU_ITEM_BACKUP, 0, R.string.backup_label);
        item5.setIcon(android.R.drawable.ic_menu_save);

        MenuItem item6 = menu.add(0, MENU_ITEM_RESTORE, 0, R.string.restore_label);
        item6.setIcon(android.R.drawable.ic_menu_upload);

        MenuItem item4 = menu.add(0, MENU_ITEM_ABOUT, 0, R.string.about_label);
        item4.setIcon(android.R.drawable.ic_menu_help);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

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

                Calendar c = Calendar.getInstance();
                c.setFirstDayOfWeek(Calendar.SUNDAY);

                c.set(this.mYear, this.mMonth, this.mDay);
                EventFeedDemo.LoadEvents(this, c);

                AddMonth(0);

                return true;

            }
            case MENU_ITEM_BACKUP: {

                DataManager.StartBackup(LunarCalendar.this);

                return true;

            }
            case MENU_ITEM_RESTORE: {

                DataManager.StartRestore(LunarCalendar.this);

                this.AddMonth(0);
                this.updateDisplay();

                return true;

            }
            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                return true;
            }
            case MENU_ITEM_ABOUT: {
                startActivity(new Intent(this, About.class));
                //startActivity(new Intent(this, ContactSelector.class));
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final boolean haveItems = true;//this.lunaCalendarView.getBaseline() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {
            // 
            // ... is followed by whatever other actions are available...

            // Give a shortcut to the edit action.

        } else {
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {

        // Setup the menu header        
        menu.setHeaderTitle(getResources().getString(R.string.app_name));

        menu.add(0, MENU_ITEM_ADDSCHEDULE, 0, R.string.add_schedule);
        menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
        menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
        menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);

        if (view.equals(this.mListView)) {
            //Toast.makeText(getBaseContext(),"Selected id is " + this.mListView.getItemIdAtPosition(position) , Toast.LENGTH_LONG).show();

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
                // For some reason the requested item isn't available, do nothing
                menu.add(0, MENU_ITEM_DELSCHEDULE, 0, R.string.schedule_delete_label);
            }

        }

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

                dao.delete(info.id);

                this.AddMonth(0); // refresh;
                return true;
            }
        }

        return false;
    }

    private void applyRotation(int position, float start, float end) {
        // Find the center of the container
        final float centerX = mContainer.getWidth() / 2.0f;
        final float centerY = mContainer.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
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
                //lunarCalendarView.setVisibility(View.GONE);
                //lunarCalendarView.setVisibility(View.VISIBLE);

                //lunarCalendarView.requestFocus();

                rotation = new Rotate3dAnimation(180, 360, centerX, centerY, 310.0f, false);
            } else {
                //lunarCalendarView.setVisibility(View.GONE);
                //lunarCalendarView.setVisibility(View.VISIBLE);

                //lunarCalendarView.requestFocus();

                rotation = new Rotate3dAnimation(180, 0, centerX, centerY, 310.0f, false);
            }

            rotation.setDuration(500);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            mContainer.startAnimation(rotation);
        }
    }

}
