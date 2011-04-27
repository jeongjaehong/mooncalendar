package org.nilriri.LunaCalendar.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.nilriri.LunaCalendar.widget.AppWidgetProvider1x1;
import org.nilriri.LunaCalendar.widget.AppWidgetProvider2x2;
import org.nilriri.LunaCalendar.widget.WidgetService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.telephony.TelephonyManager;
import android.util.FloatMath;
import android.util.Log;

import com.google.api.client.util.DateTime;

public class Common extends Activity {
    public final static String TAG = "LunarCalendar";

    /** SDK 2.2 ("FroYo") version build number. */
    public static final int FROYO = 8;
    public static final String AUTH_TOKEN_TYPE = "cl";

    public static final int SIZE_1x1 = 11;
    public static final int SIZE_2x2 = 22;
    public static final int SIZE_4x4 = 44;

    public static final String ACTION_ALARM_START = "org.nilriri.LunarCalendar.ALARM_START";
    public static final String ACTION_ALARM_STOP = "org.nilriri.LunarCalendar.ALARM_STOP";
    public static final String ACTION_REFRESH = "org.nilriri.LunarCalendar.REFRESH";
    public static final String ACTION_REFRESH_START = "org.nilriri.LunarCalendar.REFRESH_START";
    public static final String ACTION_REFRESH_FINISH = "org.nilriri.LunarCalendar.REFRESH_FINISH";
    public static final String ACTION_UPDATE = "org.nilriri.LunarCalendar.UPDATE";

    public static void sendServiceAlarmStart(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(Common.ACTION_ALARM_START);

        context.startService(intent);
    }

    public static void sendServiceAlarmStop(Context context) {
        Intent intent = new Intent(context, WidgetService.class);
        intent.setAction(Common.ACTION_ALARM_STOP);

        context.stopService(intent);
    }

    public static void sendRefreshFinish(Context context) {
        Intent finish = new Intent(Common.ACTION_REFRESH_FINISH);
        context.sendBroadcast(finish);

        Intent finishWidget1 = new Intent(context, AppWidgetProvider1x1.class);
        finishWidget1.setAction(Common.ACTION_REFRESH_FINISH);
        context.sendBroadcast(finishWidget1);

        Intent finishWidget2 = new Intent(context, AppWidgetProvider2x2.class);
        finishWidget2.setAction(Common.ACTION_REFRESH_FINISH);
        context.sendBroadcast(finishWidget2);

        Intent finishService = new Intent(context, WidgetService.class);
        finishService.setAction(Common.ACTION_REFRESH_FINISH);
        context.startService(finishService);
    }

    public static void sendWidgetUpdate(Context context) {
        Intent i1 = new Intent(context, AppWidgetProvider1x1.class);
        i1.setAction(Common.ACTION_UPDATE);
        context.sendBroadcast(i1);

        Intent i2 = new Intent(context, AppWidgetProvider2x2.class);
        i2.setAction(Common.ACTION_UPDATE);
        context.sendBroadcast(i2);
    }

    public static String fmtDate(int year, int month, int day) {
        String returnValue = "";
        returnValue = (new StringBuilder()).append(year).append("-").append(month > 9 ? month : "0" + month).append("-").append(day > 9 ? day : "0" + day).toString();
        return returnValue;
    }

    public static DateTime toDateTime(String date) {
        Calendar c = Calendar.getInstance();

        String value = date.replace("-", "");

        int year = Integer.parseInt(value.substring(0, 4));
        int month = Integer.parseInt(value.substring(4, 6)) - 1;
        int day = Integer.parseInt(value.substring(6));

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);

        return new DateTime(c.getTime());
    }

    public static String fmtDate(String date) {
        String returnValue = "";
        returnValue = (new StringBuilder()).append(date.substring(0, 4)).append("-").append(date.substring(4, 6)).append("-").append(date.substring(6)).toString();
        return returnValue;
    }

    public static String fmtDate(Calendar c) {
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        return fmtDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    public static String fmtDateTime(Calendar c) {
        return (c.get(Calendar.YEAR) + "" + (c.get(Calendar.MONTH) + 1) + "" + c.get(Calendar.DAY_OF_MONTH) + "" + c.getTimeInMillis() + "");
    }

    public static String fmtTime(int hour, int minute) {
        String returnValue = "";
        returnValue = (new StringBuilder()).append(hour > 9 ? hour : "0" + hour).append(":").append(minute > 9 ? minute : "0" + minute).toString();
        return returnValue;
    }

    public static String fmtTime(Calendar c) {
        c.setFirstDayOfWeek(Calendar.SUNDAY);
        return fmtTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public void showMessage() {
        final ProgressDialog dialog = ProgressDialog.show(this, "Title", "Message", true);
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dialog.dismiss();
            }
        };
        Thread checkUpdate = new Thread() {
            @Override
            public void run() {
                //   
                // YOUR LONG CALCULATION (OR OTHER) GOES HERE   
                //   
                handler.sendEmptyMessage(0);
            }
        };
        checkUpdate.start();
    }

    public void callVibration() {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // 1. Vibrate for 1000 milliseconds   
        long milliseconds = 1000;
        v.vibrate(milliseconds);

        // 2. Vibrate in a Pattern with 500ms on, 500ms off for 5 times   
        long[] pattern = { 500, 300 };
        v.vibrate(pattern, 5);
    }

    /*

    private static final int DOWNLOAD_FILES_REQUEST = 1;   
    
    Intent intent = new Intent();   
    intent.setAction(Intent.ACTION_PICK);   
    // FTP URL (Starts with ftp://, sftp:// or ftps:// followed by hostname and port).   
    Uri ftpUri = Uri.parse("ftp://yourftpserver.com");   
    intent.setDataAndType(ftpUri, "vnd.android.cursor.dir/lysesoft.andftp.uri");   
    // FTP credentials (optional)   
    intent.putExtra("ftp_username", "anonymous");   
    intent.putExtra("ftp_password", "something@somewhere.com");   
    //intent.putExtra("ftp_keyfile", "/sdcard/dsakey.txt");   
    //intent.putExtra("ftp_keypass", "optionalkeypassword");   
    // FTP settings (optional)   
    intent.putExtra("ftp_pasv", "true");   
    //intent.putExtra("ftp_resume", "true");   
    //intent.putExtra("ftp_encoding", "UTF8");   
    // Download   
    intent.putExtra("command_type", "download");   
    // Activity title   
    intent.putExtra("progress_title", "Downloading files ...");   
    // Remote files to download.   
    intent.putExtra("remote_file1", "/remotefolder/subfolder/file1.zip");   
    intent.putExtra("remote_file2", "/remotefolder/subfolder/file2.zip");   
    // Target local folder where files will be downloaded.   
    intent.putExtra("local_folder", "/sdcard/localfolder");            
    startActivityForResult(intent, DOWNLOAD_FILES_REQUEST);  

     
     */

    private double[] getGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null)
                break;
        }

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

    public boolean locactionServiceAvaiable() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        if (providers.size() > 0)
            return true;
        else
            return false;
    }

    //uses-permission android:name="android.permission.READ_PHONE_STATE"
    private String getMyPhoneNumber() {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    private String getMy10DigitPhoneNumber() {
        String s = getMyPhoneNumber();
        return s.substring(2);
    }

    private double gps2m(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180 / 3.14169);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        float t1 = FloatMath.cos(a1) * FloatMath.cos(a2) * FloatMath.cos(b1) * FloatMath.cos(b2);
        float t2 = FloatMath.cos(a1) * FloatMath.sin(a2) * FloatMath.cos(b1) * FloatMath.sin(b2);
        float t3 = FloatMath.sin(a1) * FloatMath.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    public static boolean isSdPresent() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public void setRingtone() {
        //sample file     
        String filepath = "/sdcard/play2.mp3";
        File ringtoneFile = new File(filepath);

        ContentValues content = new ContentValues();
        content.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
        content.put(MediaStore.MediaColumns.TITLE, "chinnu");
        content.put(MediaStore.MediaColumns.SIZE, 215454);
        content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        content.put(AudioColumns.ARTIST, "Madonna");
        content.put(AudioColumns.DURATION, 230);
        content.put(AudioColumns.IS_RINGTONE, true);
        content.put(AudioColumns.IS_NOTIFICATION, false);
        content.put(AudioColumns.IS_ALARM, false);
        content.put(AudioColumns.IS_MUSIC, false);

        //Insert it into the database   
        Log.i("TAG", "the absolute path of the file is :" + ringtoneFile.getAbsolutePath());
        Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());
        Uri newUri = this.getBaseContext().getContentResolver().insert(uri, content);
        Uri ringtoneUri = newUri;
        Log.i("TAG", "the ringtone uri is :" + ringtoneUri);
        RingtoneManager.setActualDefaultRingtoneUri(this.getBaseContext(), RingtoneManager.TYPE_RINGTONE, newUri);
    }

    public static String[] tokenFn(String str, String token) {
        StringTokenizer st = null;
        String toStr[] = null;
        int tokenCount = 0;
        int index = 0;
        int len = 0;
        try {
            len = str.length();
            for (int i = 0; i < len; i++)
                if ((index = str.indexOf((new StringBuilder(String.valueOf(token))).append(token).toString())) != -1)
                    str = (new StringBuilder(String.valueOf(str.substring(0, index)))).append(token).append(" ").append(token).append(str.substring(index + 2, str.length())).toString();

            st = new StringTokenizer(str, token);
            tokenCount = st.countTokens();
            toStr = new String[tokenCount];
            for (int i = 0; i < tokenCount; i++)
                toStr[i] = st.nextToken();

        } catch (Exception e) {
            toStr = null;
        }
        return toStr;
    }

    /*
    private boolean haveInternet(){   
        NetworkInfo info=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE).getActiveNetworkInfo();   
        if(info==null || !info.isConnected()){   
            return false;   
        }   
        if(info.isRoaming()){   
            //here is the roaming option you can change it if you want to disable internet while roaming, just return false   
            return true;   
        }   
        return true;   
    }  
    */

    /*
     
      String filepath ="/sdcard/play2.mp3";   
    File ringtoneFile = new File(filepath);   
      
    WhereClause2 content = new WhereClause2();   
    content.put(MediaStore.MediaColumns.DATA,      ringtoneFile.getAbsolutePath());   
    content.put(MediaStore.MediaColumns.TITLE, "chinnu");   
    content.put(MediaStore.MediaColumns.SIZE, 215454);   
    content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");   
    content.put(MediaStore.Audio.Media.ARTIST, "Madonna");   
    content.put(MediaStore.Audio.Media.DURATION, 230);   
    content.put(MediaStore.Audio.Media.IS_RINGTONE, true);   
    content.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);   
    content.put(MediaStore.Audio.Media.IS_ALARM, false);   
    content.put(MediaStore.Audio.Media.IS_MUSIC, false);   
      
      
    //Insert it into the database   
    //Log.i(TAG, "the absolute path of the file is :"+ringtoneFile.getAbsolutePath());   
    Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtoneFile.getAbsolutePath());   
    Uri newUri = context.getContentResolver().insert(uri, content);   
          ringtoneUri = newUri;   
          //Log.i(TAG,"the ringtone uri is :"+ringtoneUri);   
    RingtoneManager.setActualDefaultRingtoneUri(context,RingtoneManager.TYPE_RINGTONE,newUri);     
      
      
     */
    public void postData() {
        // Create a new HttpClient and Post Header   
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://www.yoursite.com/script.php");

        try {
            // Add your data   
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", "12345"));
            nameValuePairs.add(new BasicNameValuePair("stringdata", "AndDev is Cool!"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request   
            HttpResponse response = httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block   
        } catch (IOException e) {
            // TODO Auto-generated catch block   
        }
    }

}
