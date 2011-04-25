package org.nilriri.LunaCalendar.tools;

import org.nilriri.LunaCalendar.R;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

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

    private static final String OPT_GCALENDARSYNC = "GCalendarSync";
    private static final boolean OPT_GCALENDARSYNC_DEF = false;

    private static final String OPT_GMAILUSERID = "username";
    private static final String OPT_GMAILUSERID_DEF = "userid@gmail.com";

    private static final String OPT_GMAILPASSWORD = "password";
    private static final String OPT_GMAILPASSWORD_DEF = "xxx";

    private static final String OPT_SDCARDUSE = "sdcarduse";
    private static final boolean OPT_SDCARDUSE_DEF = false;

    private static final String OPT_WIDGETCOLOR = "widgetcolor";
    private static final String OPT_WIDGETCOLOR_DEF = "1";

    private static final String OPT_BPLAN = "bplan";
    private static final boolean OPT_BPLAN_DEF = false;

    private static final String OPT_BPLANFAMILY = "bplanfamily";
    private static final boolean OPT_BPLANFAMILY_DEF = false;

    private static final String OPT_BPLANPERSONAL = "bplanpersonal";
    private static final boolean OPT_BPLANPERSONAL_DEF = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        findPreference("GoogleAccountInfo").setEnabled(((CheckBoxPreference) findPreference("GCalendarSync")).isChecked());
        findPreference("GCalendarSync").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                CheckBoxPreference cpf = (CheckBoxPreference) preference;
                findPreference("GoogleAccountInfo").setEnabled(cpf.isChecked());
                return false;
            }
        });

        /*
                findPreference("bplaninfo").setEnabled(((CheckBoxPreference) findPreference("bplan")).isChecked());
                findPreference("bplan").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        CheckBoxPreference cpf = (CheckBoxPreference) preference;
                        findPreference("bplaninfo").setEnabled(cpf.isChecked());
                        return false;
                    }
                });
        */
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

    public static boolean getGCalendarSync(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_GCALENDARSYNC, OPT_GCALENDARSYNC_DEF);
    }

    public static String getGMailUserID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_GMAILUSERID, OPT_GMAILUSERID_DEF);
    }

    public static String getGMailPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_GMAILPASSWORD, OPT_GMAILPASSWORD_DEF);
    }

    public static boolean getSDCardUse(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_SDCARDUSE, OPT_SDCARDUSE_DEF);
    }

    public static int getWidgetColor(Context context) {
        String color = PreferenceManager.getDefaultSharedPreferences(context).getString(OPT_WIDGETCOLOR, OPT_WIDGETCOLOR_DEF);

        return Integer.parseInt(color);

    }

    public static boolean getBplan(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_BPLAN, OPT_BPLAN_DEF);
    }

    public static boolean getBplanFamily(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_BPLANFAMILY, OPT_BPLANFAMILY_DEF);
    }

    public static boolean getBplanPersonal(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(OPT_BPLANPERSONAL, OPT_BPLANPERSONAL_DEF);
    }

}
