/*
 * Copyright (C) 2008 The Android Open Source Project
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

package org.nilriri.LunaCalendar.widget;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.dao.ScheduleDaoImpl;
import org.nilriri.LunaCalendar.dao.Constants.Schedule;
import org.nilriri.LunaCalendar.tools.Prefs;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * The configuration screen for the WidgetProvider widget sample.
 */
public class WidgetConfigure extends Activity {
    static final String TAG = "WidgetConfigure";

    private static final String PREF_KIND_KEY = "kind_";
    private static final String PREF_COLOR_KEY = "color_";
    private static final String PREF_PK_KEY = "pk_";
    private static final String PREF_RECEIVER_KEY = "receiver";

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Spinner spin_widgetkind;
    private Spinner spin_widgetcolor;
    private Button btn_ok;
    private Button btn_cancel;
    private ListView mListView;
    private CheckBox chk_reveiver;

    public ScheduleDaoImpl dao = null;

    public WidgetConfigure() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        dao = new ScheduleDaoImpl(this, null, Prefs.getSDCardUse(this));

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.appwidget_configure);

        // Find the EditText 

        spin_widgetkind = (Spinner) findViewById(R.id.widgetkind);
        spin_widgetcolor = (Spinner) findViewById(R.id.widgetcolor);
        chk_reveiver = (CheckBox) findViewById(R.id.check_receiver);
        btn_ok = (Button) findViewById(R.id.save_button);
        btn_cancel = (Button) findViewById(R.id.cancel_button);
        chk_reveiver.setChecked(getReceiver(getBaseContext()));

        ArrayAdapter<CharSequence> adp = ArrayAdapter.createFromResource(this, R.array.entries_widgetkind, android.R.layout.simple_spinner_item);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_widgetkind.setAdapter(adp);
        spin_widgetkind.setOnItemSelectedListener(new widgetKindSelectedListener());
        spin_widgetkind.setSelection(getWidgetKind(getBaseContext()));

        ArrayAdapter<CharSequence> adp_color = ArrayAdapter.createFromResource(this, R.array.entries_list_preference, android.R.layout.simple_spinner_item);
        adp_color.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin_widgetcolor.setAdapter(adp_color);
        spin_widgetcolor.setSelection(getWidgetColor(getBaseContext(), AppWidgetManager.INVALID_APPWIDGET_ID));

        // Bind the action for the save button.
        btn_ok.setOnClickListener(mOnClickListener);
        btn_cancel.setOnClickListener(mOnClickListener);
        chk_reveiver.setOnClickListener(mOnClickListener);

        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        mListView = (ListView) findViewById(R.id.ContentsListView);
    }

    public void loadData(int kind) {
        Cursor cursor = dao.queryWidget(kind);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, //
                android.R.layout.simple_list_item_single_choice, cursor, //
                new String[] { Schedule.SCHEDULE_TITLE },//
                new int[] { android.R.id.text1 });

        mListView.setAdapter(adapter);

        if (cursor.getCount() > 0) {
            mListView.setItemChecked(0, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dao != null) {
            dao.close();
        }
    }

    public class widgetKindSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (pos >= 0) {
                setWidgetKind(getBaseContext(), pos);
                loadData(pos);
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing. 
        }
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            // final Context context = WidgetConfigure.this;

            switch (v.getId()) {

                case R.id.save_button: {

                    // When the button is clicked, save the string in our prefs and return that they
                    // clicked OK.
                    // int selectWidgetKind = spin_widgetkind.getSelectedItemPosition();
                    //  setWidgetKind(context, mAppWidgetId, selectWidgetKind);

                    int selectWidgetSize = spin_widgetcolor.getSelectedItemPosition();
                    setWidgetColor(getBaseContext(), mAppWidgetId, selectWidgetSize);

                    if (4 == spin_widgetkind.getSelectedItemPosition()) {
                        setDataPk(getBaseContext(), mAppWidgetId, new Long(-1));
                    } else {
                        setDataPk(getBaseContext(), mAppWidgetId, mListView.getItemIdAtPosition(mListView.getCheckedItemPosition()));

                    }

                    // Push widget update to surface with newly set prefix
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
                    WidgetProvider.updateAppWidget(getBaseContext(), appWidgetManager, mAppWidgetId);

                    //IntentFilter filter = new IntentFilter();
                    //filter.addAction(Intent.ACTION_TIME_TICK);
                    //filter.addCategory(Intent.CATEGORY_DEFAULT);
                    //registerReceiver(new WidgetProvider(), filter);

                    setReceiver(getBaseContext(), chk_reveiver.isChecked());

                    // Make sure we pass back the original appWidgetId
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    setResult(RESULT_OK, resultValue);
                    finish();
                }
                case R.id.cancel_button: {
                    setResult(RESULT_CANCELED, new Intent());
                    finish();
                }
            }
        }
    };

    // Write the prefix to the SharedPreferences object for this widget
    static void setWidgetKind(Context context, int widgetkind) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_KIND_KEY + AppWidgetManager.INVALID_APPWIDGET_ID, widgetkind);
        prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static int getWidgetKind(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_KIND_KEY + AppWidgetManager.INVALID_APPWIDGET_ID, 0);
    }

    static void setWidgetColor(Context context, int appWidgetId, int color) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(PREF_COLOR_KEY + AppWidgetManager.INVALID_APPWIDGET_ID, color);
        prefs.putInt(PREF_COLOR_KEY + appWidgetId, color);
        prefs.commit();

    }

    static int getWidgetColor(Context context, int appWidgetId) {
        int defaultColor = 1;
        try {
            defaultColor = Integer.parseInt(Prefs.getWidgetColor(context));
        } catch (Exception e) {

        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_COLOR_KEY + appWidgetId, defaultColor);
    }

    static void setReceiver(Context context, boolean isEnable) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(PREF_RECEIVER_KEY, isEnable);
        prefs.commit();
    }

    public static boolean getReceiver(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_RECEIVER_KEY, false);
    }

    static void setDataPk(Context context, int appWidgetId, Long pk) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(PREF_PK_KEY + appWidgetId, pk);
        prefs.commit();
    }

    static Long getDataPk(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Long pk = prefs.getLong(PREF_PK_KEY + appWidgetId, new Long(0));
        return pk;
    }

    static void removePrefData(Context context, int appWidgetId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains(PREF_PK_KEY + appWidgetId)) {
            prefs.edit().remove(PREF_PK_KEY + appWidgetId).commit();
        }
    }

}
