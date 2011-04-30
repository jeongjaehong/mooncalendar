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

import java.io.IOException;
import java.util.Calendar;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.DAOUtil;
import org.nilriri.LunaCalendar.dao.ScheduleBean;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.dao.Constants.Schedule;
import org.nilriri.LunaCalendar.gcal.EventEntry;
import org.nilriri.LunaCalendar.gcal.GoogleUtil;
import org.nilriri.LunaCalendar.tools.Common;
import org.nilriri.LunaCalendar.tools.OldEvent;
import org.nilriri.LunaCalendar.tools.Prefs;
import org.nilriri.LunaCalendar.tools.Rotate3dAnimation;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;

/**
 * Demonstrates expandable lists using a custom {@link ExpandableListAdapter}
 * from {@link BaseExpandableListAdapter}.
 */
public class ScheduleList extends ExpandableListActivity implements OnTouchListener {

    // Menu item ids    
    public static final int MENU_ITEM_EDITCHEDULE = Menu.FIRST;
    public static final int MENU_ITEM_DELSCHEDULE = Menu.FIRST + 1;
    public static final int MENU_ITEM_SCHEDULELIST = Menu.FIRST + 2;
    public static final int MENU_ITEM_WEEKSCHEDULE = Menu.FIRST + 3;
    public static final int MENU_ITEM_MONTHSCHEDULE = Menu.FIRST + 4;
    public static final int MENU_ITEM_ALLSCHEDULE = Menu.FIRST + 5;
    public static final int MENU_ITEM_GCALADDEVENT = Menu.FIRST + 6;
    public static final int MENU_ITEM_ADDCHEDULE = Menu.FIRST + 7;
    public static final int MENU_ITEM_GCALIMPORT = Menu.FIRST + 8;
    // public static final int MENU_ITEM_BIBLEVIEW = Menu.FIRST + 9;

    private int mGroupIdColumnIndex;
    private Calendar mCalendar;
    private OldEvent mOldEvent;
    private String mSearchRange;
    private ViewGroup mContainer;

    private ExpandableListAdapter mAdapter;
    private ScheduleDaoImpl dao = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));
        mContainer = this.getExpandableListView();//findViewById(R.id.container);

        mContainer.setOnTouchListener(this);

        Intent intent = getIntent();
        Bundle data = intent.getExtras();

        mOldEvent = new OldEvent(-1, -1);
        mCalendar = (Calendar) data.get("workday");
        mSearchRange = intent.getStringExtra("ScheduleRange");
        mSearchRange = mSearchRange == null ? "" : mSearchRange;

        if (mCalendar == null)
            mCalendar = Calendar.getInstance();

        ScheduleLoading();

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

            //Toast.makeText(getBaseContext(), "_ID : " + l, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();

            intent.setClass(getBaseContext(), ScheduleViewer.class);
            intent.putExtra("id", id);
            //final Calendar c = Calendar.getInstance();
            //c.set(mYear, mMonth, mDay);
            //intent.putExtra("org.nilriri.LunaCalendar.today", c);

            startActivity(intent);

            return false;
        }
    }

    /**
     * @throws IllegalArgumentException
     */
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
        } else {
            this.setTitle(getResources().getString(R.string.schedule_alllist_label));
        }

        groupCursor = dao.queryGroup(mSearchRange, date);

        // Cache the ID column index
        mGroupIdColumnIndex = groupCursor.getColumnIndexOrThrow(Schedule._ID);

        // Set up our adapter
        //mAdapter = new MyExpandableListAdapter(groupCursor, this, android.R.layout.simple_expandable_list_item_1, android.R.layout.simple_expandable_list_item_1, new String[] { ScheduleBean.SCHEDULE_DATE, ScheduleBean.SCHEDULE_TITLE }, new int[] { android.R.id.text1  }, new String[] { ScheduleBean.ALARM_DETAILINFO,  }, new int[] { android.R.id.text2 });
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
        if (dao != null) {
            dao.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        ScheduleLoading();
    }

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
        //Toast.makeText(this, "_ID : " + cursor.getLong(0), Toast.LENGTH_SHORT).show();

        //menu.setHeaderTitle(getResources().getString(R.string.app_korname));
        menu.setHeaderTitle(cursor.getString(2));

        String anniversary = getResources().getString(R.string.anniversary_label);
        Cursor c = (Cursor) mAdapter.getGroup(groupPos);
        if (!anniversary.equals(c.getString(1)) && !"B-Plan".equals(c.getString(1))) {
            menu.add(0, MENU_ITEM_EDITCHEDULE, 0, R.string.schedule_modify_action);
            menu.add(0, MENU_ITEM_DELSCHEDULE, 0, R.string.schedule_delete_label);
        }
        //Toast.makeText(getBaseContext(), "c.getString(3)=" + c.getString(3), Toast.LENGTH_LONG).show();
        if ("F".equals(c.getString(3)) || "P".equals(c.getString(3))) {
            // menu.add(0, MENU_ITEM_BIBLEVIEW, 0, "성경읽기");
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

        if (!"".equals(Prefs.getSyncCalName(this))) {
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

            //Toast.makeText(this, "_ID : " + cursor.getLong(0), Toast.LENGTH_SHORT).show();

            id = cursor.getLong(0);

        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
            // Toast.makeText(this, title + ": Group " + groupPos + " clicked", Toast.LENGTH_SHORT).show();

            cursor = (Cursor) mAdapter.getGroup(groupPos);

            //Toast.makeText(this, "_ID : "+cursor.getLong(0) , Toast.LENGTH_SHORT).show();
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
                dao.delete(id);
                
                ScheduleLoading();
                return true;
            }
            case MENU_ITEM_GCALADDEVENT: {

                //CalendarFeedDemo.main();

                //AclFeedDemo.main();

                //EventFeedDemo.addEvents(this, dao.queryGCalendar(id));

                new AddEvent().execute(id);

                return true;

            }
            case MENU_ITEM_GCALIMPORT: {

                //EventFeedDemo.LoadEvents(this, this.mCalendar, "");
                //TODO:

                ScheduleLoading();

                return true;

            }

        }

        return false;
    }

    private class AddEvent extends AsyncTask<Long, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(ScheduleList.this, "", "Add event...", true);

            Log.e(Common.TAG, "****** onPreExecute ********");
        }

        @Override
        protected Void doInBackground(Long... params) {
            Log.e(Common.TAG, "****** doInBackground ********");

            GoogleUtil gu = new GoogleUtil(Prefs.getAuthToken(getBaseContext()));
            ScheduleBean scheduleBean = DAOUtil.Cursor2Bean(dao.query(params[0]));

            try {
                EventEntry event = gu.addEvent(Prefs.getSyncCalendar(getBaseContext()), scheduleBean);

                // 등록후 결과를 스케쥴 정보에 반영한다.
                dao.insert(event);

            } catch (IOException e) {
                cancel(true);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();

            Log.e(Common.TAG, "****** onPostExecute ********");
        }

    }

    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. 
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     *
     */
    public class MyExpandableListAdapter extends SimpleCursorTreeAdapter {

        public MyExpandableListAdapter(Cursor cursor, Context context, int groupLayout, int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom, int[] childrenTo) {
            super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom, childrenTo);
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            // Given the group, we return a cursor for all the children within that group 

            Long id = groupCursor.getLong(mGroupIdColumnIndex);

            Cursor childCursor = dao.queryChild(id);

            // The returned Cursor MUST be managed by us, so we use Activity's helper
            // functionality to manage it for us.
            return childCursor;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);

            TextView textView = new TextView(ScheduleList.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }

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
                //lunaCalendarView.setVisibility(View.GONE);
                //lunaCalendarView.setVisibility(View.VISIBLE);
                //lunaCalendarView.requestFocus();

                rotation = new Rotate3dAnimation(180, 360, centerX, centerY, 310.0f, false);
            } else {
                //lunaCalendarView.setVisibility(View.GONE);
                //lunaCalendarView.setVisibility(View.VISIBLE);
                //lunaCalendarView.requestFocus();

                rotation = new Rotate3dAnimation(180, 0, centerX, centerY, 310.0f, false);
            }

            rotation.setDuration(500);
            rotation.setFillAfter(true);
            rotation.setInterpolator(new DecelerateInterpolator());

            mContainer.startAnimation(rotation);
        }
    }

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

                // EventFeedDemo.LoadEvents(this, this.mCalendar, "");
                //TODO:

                ScheduleLoading();

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

        /*
                if (Prefs.getGCalendarSync(this)) {
                    if (menu.findItem(MENU_ITEM_GCALIMPORT) == null) {
                        MenuItem item3 = menu.add(0, MENU_ITEM_GCALIMPORT, 0, R.string.schedule_gcalimport_menu);
                        item3.setIcon(android.R.drawable.ic_menu_rotate);
                    }
                }
        */
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

}
