package org.nilriri.LunaCalendar.schedule;

import java.util.Calendar;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.RefreshManager;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.dao.Constants.Schedule;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.OldEvent;
import org.nilriri.LunaCalendar.tools.Prefs;
import org.nilriri.LunaCalendar.tools.Rotate3dAnimation;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;

public class ScheduleList extends ExpandableListActivity implements OnTouchListener, RefreshManager {

    public static final int MENU_ITEM_EDITCHEDULE = Menu.FIRST;
    public static final int MENU_ITEM_DELSCHEDULE = Menu.FIRST + 1;
    public static final int MENU_ITEM_SCHEDULELIST = Menu.FIRST + 2;
    public static final int MENU_ITEM_WEEKSCHEDULE = Menu.FIRST + 3;
    public static final int MENU_ITEM_MONTHSCHEDULE = Menu.FIRST + 4;
    public static final int MENU_ITEM_ALLSCHEDULE = Menu.FIRST + 5;
    public static final int MENU_ITEM_GCALADDEVENT = Menu.FIRST + 6;
    public static final int MENU_ITEM_ADDCHEDULE = Menu.FIRST + 7;
    public static final int MENU_ITEM_GCALIMPORT = Menu.FIRST + 8;

    private int mGroupIdColumnIndex;
    private Calendar mCalendar;
    private OldEvent mOldEvent;
    private String mSearchRange;
    private ViewGroup mContainer;
    private boolean isSearch;
    private int mOperator;
    private String mKeyword1;
    private String mKeyword2;

    private ExpandableListAdapter mAdapter;
    private ScheduleDaoImpl dao = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContainer = this.getExpandableListView();

        mContainer.setOnTouchListener(this);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();

        mOldEvent = new OldEvent(-1, -1);
        mCalendar = (Calendar) data.get("workday");
        mSearchRange = intent.getStringExtra("ScheduleRange");
        mSearchRange = mSearchRange == null ? "" : mSearchRange;
        isSearch = intent.getBooleanExtra("isSearch", false);

        mOperator = intent.getIntExtra("operator", 0);
        mKeyword1 = intent.getStringExtra("keyword1");
        mKeyword2 = intent.getStringExtra("keyword2");

        if (mCalendar == null)
            mCalendar = Calendar.getInstance();

        registerForContextMenu(getExpandableListView());

        this.getExpandableListView().setFastScrollEnabled(true);

        this.getExpandableListView().setOnChildClickListener(new myOnChildClickListener());
    }

    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOldEvent.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:

                // 전체 일정목록일때는 이전, 이후자료 검색기능이 없다.
                if ("TODAY".equals(mSearchRange) || "WEEK".equals(mSearchRange) || "MONTH".equals(mSearchRange)) {
                    if (event.getX() - mOldEvent.getX() > 50) {//Right
                        if (Prefs.getAnimation(ScheduleList.this)) {
                            applyRotation(-1, 0, 180);
                        }
                        ChangeScheduleList(-1);
                    } else if (event.getX() - mOldEvent.getX() < -50) {
                        if (Prefs.getAnimation(ScheduleList.this)) {
                            applyRotation(1, 360, 180);
                        }
                        ChangeScheduleList(1);
                    }
                }
                mOldEvent.set(event.getX(), event.getY());
                break;
            default:
                return false;
        }
        return false;
    }

    private void ChangeScheduleList(int arg) {

        if ("TODAY".equals(mSearchRange)) {
            mCalendar.add(Calendar.DAY_OF_MONTH, arg);
        } else if ("WEEK".equals(mSearchRange)) {
            mCalendar.add(Calendar.WEEK_OF_YEAR, arg);
        } else if ("MONTH".equals(mSearchRange)) {
            mCalendar.add(Calendar.MONTH, arg);
        }

        ScheduleLoading();
    }

    public class myOnChildClickListener implements OnChildClickListener {

        public boolean onChildClick(ExpandableListView expandablelistview, View view, int i, int j, long id) {
            Intent intent = new Intent();
            intent.setClass(getBaseContext(), ScheduleViewer.class);
            intent.putExtra("id", id);
            startActivity(intent);
            return false;
        }
    }

    private void ScheduleLoading() throws IllegalArgumentException {

        Cursor groupCursor = null;

        String date = "";

        if ("TODAY".equals(mSearchRange)) {
            date = Common.fmtDate(mCalendar);
            this.setTitle(getResources().getString(R.string.schedule_todaylist_label) + " (" + date + ")");

        } else if ("WEEK".equals(mSearchRange)) {
            date = Common.fmtDate(mCalendar);

            Calendar c = Calendar.getInstance();
            c.setFirstDayOfWeek(Calendar.SUNDAY);
            c.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));

            c.add(Calendar.DAY_OF_MONTH, ((c.get(Calendar.DAY_OF_WEEK) - 1) * -1));

            String lStart = Common.fmtDate(c);
            c.add(Calendar.DAY_OF_MONTH, 6);
            String lEnd = Common.fmtDate(c);

            this.setTitle(getResources().getString(R.string.schedule_weeklist_label) + " (" + lStart + "~" + lEnd + ")");

        } else if ("MONTH".equals(mSearchRange)) {
            date = Common.fmtDate(mCalendar).substring(0, 7);
            this.setTitle(getResources().getString(R.string.schedule_monthlist_label) + " (" + date + ")");
        } else if (isSearch) {
            this.setTitle("검색결과");
        } else {
            this.setTitle(getResources().getString(R.string.schedule_alllist_label));
        }

        groupCursor = dao.queryGroup(mSearchRange, date, isSearch, mOperator, mKeyword1, mKeyword2);

        mGroupIdColumnIndex = groupCursor.getColumnIndexOrThrow(Schedule._ID);

        mAdapter = new MyExpandableListAdapter(groupCursor, this, R.layout.schedule_groupitem, R.layout.schedule_childitem, new String[] { Schedule.SCHEDULE_DATE, Schedule.SCHEDULE_TITLE }, new int[] { R.id.schedule_date, R.id.schedule_title }, new String[] { Schedule.SCHEDULE_CONTENTS, Schedule.ALARM_DETAILINFO, Schedule.DDAY_DETAILINFO, }, new int[] { R.id.schedule_contents, R.id.alarm_detailinfo, R.id.dday_detailinfo });
        setListAdapter(mAdapter);

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

        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));

        ScheduleLoading();
    }

    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

        public MyExpandableListAdapter(Cursor cursor, Context context, int groupLayout, int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom, int[] childrenTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom, childrenTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            Long id = groupCursor.getLong(mGroupIdColumnIndex);
            Cursor childCursor = dao.queryChild(id);
            return childCursor;
        }

        public TextView getGenericView() {
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
            TextView textView = new TextView(ScheduleList.this);
            textView.setLayoutParams(lp);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
    }
    
    /*
     * Change Animation
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
     * Context Menu       
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;

        Cursor cursor = null;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPos = 0;
        int childPos = 0;
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
            cursor = (Cursor) mAdapter.getChild(groupPos, childPos);
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            cursor = (Cursor) mAdapter.getGroup(groupPos);
        }
        menu.setHeaderTitle(cursor.getString(2));

        String anniversary = getResources().getString(R.string.anniversary_label);

        Cursor c = (Cursor) mAdapter.getGroup(groupPos);
        if (!anniversary.equals(c.getString(1)) && !"B-Plan".equals(c.getString(1))) {
            menu.add(0, MENU_ITEM_EDITCHEDULE, 0, R.string.schedule_modify_action);
            menu.add(0, MENU_ITEM_DELSCHEDULE, 0, R.string.schedule_delete_label);
        }

        if ("F".equals(c.getString(3)) || "P".equals(c.getString(3))) {
            try {

                Intent intent = new Intent();

                intent.setAction("org.nilriri.webbibles.VIEW");
                intent.setType("vnd.org.nilriri/web-bible");

                intent.putExtra("VERSION", 0);
                intent.putExtra("VERSION2", 0);
                intent.putExtra("BOOK", c.getInt(4));
                intent.putExtra("CHAPTER", c.getInt(5));
                intent.putExtra("VERSE", 0);

                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "온라인성경 앱일 설치되어있지 않거나 최신버젼이 아닙니다.", Toast.LENGTH_LONG).show();

            }

        }

        if ("auto".equals(Prefs.getSyncMethod(this)) // 동기화 방법 
                && !"".equals(Prefs.getSyncCalendar(this))) {
            menu.add(0, MENU_ITEM_GCALADDEVENT, 0, R.string.schedule_gcaladdevent_label);
            menu.add(0, MENU_ITEM_GCALIMPORT, 0, R.string.schedule_gcalimport_label);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
        Cursor cursor = null;

        Long id = new Long(-1);

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
            cursor = (Cursor) mAdapter.getChild(groupPos, childPos);
            id = cursor.getLong(0);
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            cursor = (Cursor) mAdapter.getGroup(groupPos);
            id = cursor.getLong(0);
        }

        switch (item.getItemId()) {

            case MENU_ITEM_EDITCHEDULE: {
                String anniversary = getResources().getString(R.string.anniversary_label);

                if (anniversary.equals(cursor.getString(1))) {
                    Intent intent = new Intent();
                    intent.setClass(this, ScheduleViewer.class);

                    intent.putExtra("id", id);

                    startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(this, ScheduleEditor.class);

                    intent.putExtra("SID", id);
                    intent.putExtra("STODAY", mCalendar);

                    startActivity(intent);
                }
                return true;
            }
            case MENU_ITEM_DELSCHEDULE: {
                dao.syncDelete(id, this);
                return true;
            }
            case MENU_ITEM_GCALADDEVENT: {
                dao.syncInsert(id, this);
                return true;
            }

            case MENU_ITEM_GCALIMPORT: {
                dao.syncImport(this);
                return true;
            }
        }
        return false;
    }

    /*
     * Option Menu
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        MenuItem item0 = menu.add(0, MENU_ITEM_ADDCHEDULE, 0, R.string.schedule_add_label);
        item0.setIcon(android.R.drawable.ic_menu_add);

        if ("TODAY".equals(mSearchRange)) {
            MenuItem item1 = menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
            item1.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item2 = menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);
            item2.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item3 = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
            item3.setIcon(android.R.drawable.ic_menu_agenda);
        } else if ("WEEK".equals(mSearchRange)) {
            MenuItem item1 = menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
            item1.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item2 = menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);
            item2.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item3 = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
            item3.setIcon(android.R.drawable.ic_menu_agenda);
        } else if ("MONTH".equals(mSearchRange)) {
            MenuItem item1 = menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
            item1.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item2 = menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
            item2.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item3 = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
            item3.setIcon(android.R.drawable.ic_menu_agenda);
        } else {
            MenuItem item1 = menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
            item1.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item2 = menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
            item2.setIcon(android.R.drawable.ic_menu_agenda);

            MenuItem item3 = menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);
            item3.setIcon(android.R.drawable.ic_menu_agenda);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case MENU_ITEM_ADDCHEDULE: {

                Intent intent = new Intent();
                intent.setClass(this, ScheduleEditor.class);

                intent.putExtra("SID", new Long(0));
                intent.putExtra("STODAY", mCalendar);
                startActivity(intent);

                return true;
            }
            case MENU_ITEM_SCHEDULELIST: {

                mSearchRange = "TODAY";

                this.ScheduleLoading();

                return true;
            }
            case MENU_ITEM_WEEKSCHEDULE: {

                mSearchRange = "WEEK";

                this.ScheduleLoading();

                return true;
            }
            case MENU_ITEM_MONTHSCHEDULE: {

                mSearchRange = "MONTH";

                this.ScheduleLoading();

                return true;
            }

            case MENU_ITEM_ALLSCHEDULE: {
                mSearchRange = "ALL";

                this.ScheduleLoading();

                return true;
            }

            case MENU_ITEM_GCALIMPORT: {

                dao.syncImport(this);

                return true;

            }
            case R.id.settings: {
                startActivity(new Intent(this, Prefs.class));
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.removeItem(MENU_ITEM_GCALIMPORT);
        menu.removeItem(R.id.settings);

        if ("TODAY".equals(mSearchRange)) {
            menu.removeItem(MENU_ITEM_SCHEDULELIST);
            if (menu.findItem(MENU_ITEM_MONTHSCHEDULE) == null) {
                MenuItem item1 = menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);
                item1.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_WEEKSCHEDULE) == null) {
                MenuItem item2 = menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
                item2.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_ALLSCHEDULE) == null) {
                MenuItem item3 = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
                item3.setIcon(android.R.drawable.ic_menu_agenda);
            }
        } else if ("WEEK".equals(mSearchRange)) {
            menu.removeItem(MENU_ITEM_WEEKSCHEDULE);
            if (menu.findItem(MENU_ITEM_SCHEDULELIST) == null) {
                MenuItem item1 = menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
                item1.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_MONTHSCHEDULE) == null) {
                MenuItem item2 = menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);
                item2.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_ALLSCHEDULE) == null) {
                MenuItem item3 = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
                item3.setIcon(android.R.drawable.ic_menu_agenda);
            }
        } else if ("MONTH".equals(mSearchRange)) {
            menu.removeItem(MENU_ITEM_MONTHSCHEDULE);

            if (menu.findItem(MENU_ITEM_SCHEDULELIST) == null) {
                MenuItem item1 = menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
                item1.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_WEEKSCHEDULE) == null) {
                MenuItem item2 = menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
                item2.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_ALLSCHEDULE) == null) {
                MenuItem item3 = menu.add(0, MENU_ITEM_ALLSCHEDULE, 0, R.string.schedule_alllist_label);
                item3.setIcon(android.R.drawable.ic_menu_agenda);
            }
        } else {
            menu.removeItem(MENU_ITEM_ALLSCHEDULE);

            if (menu.findItem(MENU_ITEM_SCHEDULELIST) == null) {
                MenuItem item1 = menu.add(0, MENU_ITEM_SCHEDULELIST, 0, R.string.schedule_todaylist_label);
                item1.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_WEEKSCHEDULE) == null) {
                MenuItem item2 = menu.add(0, MENU_ITEM_WEEKSCHEDULE, 0, R.string.schedule_weeklist_label);
                item2.setIcon(android.R.drawable.ic_menu_agenda);
            }
            if (menu.findItem(MENU_ITEM_MONTHSCHEDULE) == null) {
                MenuItem item3 = menu.add(0, MENU_ITEM_MONTHSCHEDULE, 0, R.string.schedule_monthlist_label);
                item3.setIcon(android.R.drawable.ic_menu_agenda);
            }
        }

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    public void refresh() {
        this.ScheduleLoading();
    }

}
