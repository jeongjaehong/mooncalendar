package org.nilriri.LunaCalendar.tools;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.nilriri.LunaCalendar.R;
import org.nilriri.LunaCalendar.gcal.CalendarEntry;
import org.nilriri.LunaCalendar.gcal.GoogleUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;

public class Prefs extends PreferenceActivity {
    // Option names and default values
    private static final String OPT_LUNAICON = "lunaicon";
    private static final boolean OPT_LUNAICON_DEF = true;

    private static final String OPT_LUNADAYS = "lunadays";
    private static final boolean OPT_LUNADAYS_DEF = true;

    private static final String OPT_ALARMCHECK = "alarmcheck";
    private static final boolean OPT_ALARMCHECK_DEF = true;

    private static final String OPT_ANIMATION = "useanimation";
    private static final boolean OPT_ANIMATION_DEF = false;

    private static final String OPT_ANNIVERSARY = "anniversary";
    private static final boolean OPT_ANNIVERSARY_DEF = false;

    private static final String OPT_SDCARDUSE = "sdcarduse";
    private static final boolean OPT_SDCARDUSE_DEF = false;

    private static final String OPT_WIDGETCOLOR = "widgetcolor";
    private static final String OPT_WIDGETCOLOR_DEF = "1";

    private static final String OPT_ACCOUNTS = "accounts";
    private static final String OPT_ACCOUNTS_DEF = "";

    private static final String OPT_CALENDARS = "calendars";
    private static final String OPT_CALENDARS_DEF = "";

    private static final String OPT_AUTHTOKEN = "authtoken";
    private static final String OPT_CALLIST = "callist";

    private static final String OPT_SYNCMETHOD = "syncmethod";
    private static final String OPT_SYNCMETHOD_DEF = "auto";

    private ListPreference accounts;
    private ListPreference calendars;

    protected AccountManager manager;

    private static final int DIALOG_ACCOUNTS = 0;
    private static final int REQUEST_AUTHENTICATE = 0;

    public static ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        accounts = (ListPreference) findPreference("accounts");
        CharSequence entries[];

        manager = AccountManager.get(this);
        Account[] acc = manager.getAccountsByType("com.google");

        final int size = acc.length;
        entries = new String[size];
        for (int i = 0; i < size; i++) {
            entries[i] = acc[i].name;
        }
        accounts.setEntries(entries);
        accounts.setEntryValues(entries);
        accounts.setSummary(getAccountName(getBaseContext()));

        //pd = new ProgressDialog(this);
        //pd.setTitle("Move!");
        //pd.setMessage("Connecting to Google server...");

        //accounts.getDialog();

        accounts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object obj) {
                ListPreference listPref = (ListPreference) preference;

                if (!obj.toString().equals(getSyncCalName(getBaseContext()))) {

                    // 변경된 계정정보를 셋팅.
                    setAccountName(getBaseContext(), obj.toString());
                    /*
                                        Log.e(Common.TAG, "****** changing ********");

                                        // 기존 선택된 계정의 캘린더 목록초기화
                                        setCalendars(getBaseContext(), new String[] { "" });

                                        // 기존선택값 삭제.
                                        setSyncCalName(getBaseContext(), "");

                                        calendars.setEnabled(false);
                                        calendars.setSummary("");

                                        // 변경된 계정정보를 셋팅.
                                        setAccountName(getBaseContext(), obj.toString());

                                        // 계정이 변경되면 캘린더 목록을 다시 조회하여 셋팅한다.                      
                                        //loadCalendarList(manager, getAccounts(getBaseContext()));
                    */
                    loadCalendarList();

                }

                listPref.setSummary((String) obj.toString());
                return true;
            }
        });

        // 동기화 대상 캘린더 목록을 설정한다.
        calendars = (ListPreference) findPreference("calendars");
        CharSequence entryValues[] = Common.tokenFn(getCalendars(getBaseContext()), ",");
        if (entryValues.length > 0) {
            calendars.setEntries(entryValues);
            calendars.setEntryValues(entryValues);
        } else {
            calendars.setEnabled(false);
        }
        calendars.setSummary(getSyncCalName(getBaseContext()));
        calendars.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object obj) {
                ListPreference listPref = (ListPreference) preference;

                // 캘린더를 변경하고 자동으로 일정(이벤트)을 동기화 하려면 여기서 작업한다.
                // TODO:

                // 선택된 캘린더 명을 표시한다.
                listPref.setSummary((String) obj.toString());
                return true;
            }
        });

        findPreference("sdcarduse").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cpf = (CheckBoxPreference) preference;
                if (cpf.isChecked()) {
                    if (!Common.isSdPresent()) {

                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean(OPT_SDCARDUSE, false).commit();

                        Toast.makeText(getBaseContext(), getBaseContext().getResources().getString(R.string.sdcarduse_notinstall), Toast.LENGTH_LONG).show();
                    } else {

                        Toast.makeText(getBaseContext(), "스케쥴 정보 복사중...", Toast.LENGTH_LONG).show();
                        // backup
                        DataManager.StartCopy(Prefs.this, cpf.isChecked());
                    }
                } else {
                    // restore
                    DataManager.StartCopy(Prefs.this, cpf.isChecked());
                }
                return false;
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ACCOUNTS:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a Google account");
                final AccountManager manager = AccountManager.get(this);
                final Account[] accounts = manager.getAccountsByType("com.google");

                final int size = accounts.length;
                String[] names = new String[size];
                for (int i = 0; i < size; i++) {
                    names[i] = accounts[i].name;
                }
                builder.setItems(names, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //loadCalendarList(manager, accounts[which]);
                    }
                });
                return builder.create();

        }
        return null;
    }

    public void gotAccount(boolean tokenExpired) {
        String accountName = getAccountName(getBaseContext());
        if (accountName != null) {
            AccountManager manager = AccountManager.get(getBaseContext());
            Account[] accounts = manager.getAccountsByType("com.google");
            int size = accounts.length;
            for (int i = 0; i < size; i++) {
                Account account = accounts[i];
                if (accountName.equals(account.name)) {
                    if (tokenExpired) {
                        manager.invalidateAuthToken("com.google", Prefs.getAuthToken(getBaseContext()));
                    }
                    loadCalendarList();
                    return;
                }
            }
        }
    }

    void loadCalendarList() {

        new BackgroundTask().execute();
    }

    /*
    void loadCalendarList(final AccountManager manager, final Account account) {


    }
    */

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(Prefs.this, "", "Connecting to google...", true);

            Log.e(Common.TAG, "****** onPreExecute ********");

            // 기존 선택된 계정의 캘린더 목록초기화
            setCalendars(getBaseContext(), new String[] { "" });

            // 기존선택값 삭제.
            setSyncCalName(getBaseContext(), "");

            calendars.setEnabled(false);
            calendars.setSummary("");

        }

        @Override
        protected Void doInBackground(Void... params) {

            //new Thread() {

            //@Override
            //public void run() {
            try {

                final Bundle bundle = manager.getAuthToken(getAccounts(getBaseContext()), Common.AUTH_TOKEN_TYPE, true, null, null).getResult();
                //runOnUiThread(new Runnable() {

                //                            public void run() {
                //                              try {

                if (bundle.containsKey(AccountManager.KEY_INTENT)) {
                    Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                    int flags = intent.getFlags();
                    flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
                    intent.setFlags(flags);
                    startActivityForResult(intent, REQUEST_AUTHENTICATE);
                } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    setAuthToken(getBaseContext(), authToken);

                    GoogleUtil gu = new GoogleUtil(getAuthToken(getBaseContext()));

                    // gu.GoogleLogin(authToken);

                    List<CalendarEntry> cals = gu.getCalendarList();
                    int numCalendars = cals.size();
                    CharSequence entryValues[] = new CharSequence[numCalendars];
                    for (int i = 0; i < numCalendars; i++) {
                        entryValues[i] = cals.get(i).title;
                    }

                    // 캘린더 목록을 저장해 둔다.
                    if (entryValues.length > 0) {
                        setCalendars(getBaseContext(), entryValues);
                        setSyncCalName(getBaseContext(), entryValues[0].toString());
                    } else {
                        setCalendars(getBaseContext(), new CharSequence[0]);
                        setSyncCalName(getBaseContext(), "");

                    }

                }

                //Toast.makeText(getBaseContext(), "", Toast.LENGTH_LONG).show();
                /*                                    
                                                } catch (Exception e) {
                                                    calendars.setEnabled(false);
                                                    calendars.setSummary(e.getMessage());
                                                    handleException(e);
                                                }
                */
                //}
                //});
            } catch (Exception e) {
                dialog.dismiss();
                cancel(true);
                handleException(e);
                e.printStackTrace();
            }
            //   }
            // }.start();            

            Log.e(Common.TAG, "****** doInBackground ********");

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();

            Log.e(Common.TAG, "****** onPostExecute ********");

            // 동기화 대상 캘린더 목록을 설정한다.
            CharSequence entryValues[] = Common.tokenFn(getCalendars(getBaseContext()), ",");
            if (entryValues.length > 0) {
                calendars.setEntries(entryValues);
                calendars.setEntryValues(entryValues);
                calendars.setEnabled(true);
                calendars.setSummary(entryValues[0]);
                calendars.setValue(entryValues[0].toString());
            } else {
                calendars.setEnabled(false);
                calendars.setSummary("");
                calendars.setValue("");
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_AUTHENTICATE:
                if (resultCode == RESULT_OK) {
                    gotAccount(false);
                }/* else {
                    showDialog(DIALOG_ACCOUNTS);
                 }*/
                break;
        }
    }

    void handleException(Exception e) {

        if (e instanceof HttpResponseException) {
            HttpResponse response = ((HttpResponseException) e).response;
            int statusCode = response.statusCode;

            Log.e(Common.TAG, "handleException statusCode=" + statusCode);
            Log.e(Common.TAG, e.getMessage(), e);

            try {
                response.ignore();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (statusCode == 401 || statusCode == 403) {
                gotAccount(true);
                return;
            }

            try {

                Log.e(Common.TAG, response.parseAsString());
            } catch (IOException parseException) {
                parseException.printStackTrace();
            }
        } else if (e instanceof UnknownHostException) {
            Log.e(Common.TAG, e.getMessage(), e);

        } else {
            e.printStackTrace();

        }

    }

    public static boolean getLunaIcon(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_LUNAICON, OPT_LUNAICON_DEF);
    }

    public static boolean getLunaDays(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_LUNADAYS, OPT_LUNADAYS_DEF);
    }

    public static boolean getAlarmCheck(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_ALARMCHECK, OPT_ALARMCHECK_DEF);
    }

    public static boolean getAnimation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_ANIMATION, OPT_ANIMATION_DEF);
    }

    public static boolean getAnniversary(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_ANNIVERSARY, OPT_ANNIVERSARY_DEF);
    }

    public static void setAccountName(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OPT_ACCOUNTS, value).commit();
    }

    public static String getAccountName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_ACCOUNTS, OPT_ACCOUNTS_DEF);
    }

    public static String getAuthToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_AUTHTOKEN, null);
    }

    public static void setAuthToken(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OPT_AUTHTOKEN, value).commit();
    }

    public static void setSyncCalName(Context context, String value) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OPT_CALENDARS, value).commit();
    }

    public static String getSyncCalName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_CALENDARS, OPT_CALENDARS_DEF);
    }

    public static String getSyncMethod(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_SYNCMETHOD, OPT_SYNCMETHOD_DEF);
    }

    public static CalendarEntry getSyncCalendar(Context context) throws IOException {
        String calName = getSyncCalName(context);

        GoogleUtil gu = new GoogleUtil(getAuthToken(context));

        //gu.GoogleLogin(getAuthToken(context));

        List<CalendarEntry> cals = gu.getCalendarList();
        for (int i = 0; i < cals.size(); i++) {
            if (calName.equals(cals.get(i).title))
                return cals.get(i);
        }
        return null;

    }

    public static void setCalendars(Context context, CharSequence[] values) {
        String value = "";

        for (int i = 0; i < values.length; i++) {
            if (i == 0) {
                value = values[i].toString();
            } else {

                value += "," + values[i].toString();
            }

        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(OPT_CALLIST, value).commit();
    }

    public static String getCalendars(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_CALLIST, "");
    }

    public static Account getAccounts(Context context) {
        String accName = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_ACCOUNTS, OPT_ACCOUNTS_DEF);

        AccountManager manager = AccountManager.get(context);
        Account[] acc = manager.getAccountsByType("com.google");

        for (int i = 0; i < acc.length; i++) {
            if (accName.equals(acc[i].name))
                return acc[i];
        }

        return null;

    }

    public static boolean getSDCardUse(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_SDCARDUSE, OPT_SDCARDUSE_DEF);
    }

    public static int getWidgetColor(Context context) {
        String color = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_WIDGETCOLOR, OPT_WIDGETCOLOR_DEF);

        return Integer.parseInt(color);

    }

}
