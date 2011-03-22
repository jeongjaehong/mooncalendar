package org.nilriri.LunaCalendar.tools;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SeekBar;

public class SeekBarPreference extends DialogPreference {
    private final String TAG = "SeekBarPreference";
    private final String SENSITIVITY_LEVEL_PREF = "sensitivity";
    private final String VOLUME_LEVEL_PREF = "volume";

    private Context context;
    private SeekBar sensitivityLevel = null;
    private LinearLayout layout = null;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        persistInt(10);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        layout.setMinimumWidth(400);
        layout.setPadding(20, 20, 20, 20);
        sensitivityLevel = new SeekBar(context);
        if (this.getKey().equalsIgnoreCase(SENSITIVITY_LEVEL_PREF)) {
            sensitivityLevel.setMax(100);
        } else {
            sensitivityLevel.setMax(10);
        }
        sensitivityLevel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sensitivityLevel.setProgress(getPersistedInt(10));
        layout.addView(sensitivityLevel);
        builder.setView(layout);
        //super.onPrepareDialogBuilder(builder);    
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            persistInt(sensitivityLevel.getProgress());
        }
        super.onDialogClosed(positiveResult);
    }
}

/*
 //////////////////////////////////////////////////////////////////////////////////////////
 String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";   
  
String[] projection = {   
    MediaStore.Audio.Media._ID,   
    MediaStore.Audio.Media.ARTIST,   
    MediaStore.Audio.Media.TITLE,   
    MediaStore.Audio.Media.DATA,   
    MediaStore.Audio.Media.DISPLAY_NAME,   
    MediaStore.Audio.Media.DURATION   
};   
  
cursor = this.managedQuery(   
    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,   
    projection,   
    selection,   
    null,   
    null);   
  
private List<String> songs = new ArrayList<String>();   
while(cursor.moveToNext()){   
    songs.add(cursor.getString(0) + "||" + cursor.getString(1) + "||" +   cursor.getString(2) + "||" +   cursor.getString(3) + "||" +  cursor.getString(4) + "||" +  cursor.getString(5));   
}  

//////////////////////////////////////////////////////////////////////////////////

Uri playUri = Uri.parse("file:///sdcard/music/an.mp3");   
Intent intent = new Intent(Intent.ACTION_VIEW, playUri);    
startActivity(intent);  


///////////////////////////////////////////////////////////////////////////////
private float scaleVolume(float volume) {   
    //make a logarithmic scale based on value from seekbar   
    return (float)Math.log10(volume);   
}  

  
///////////////////////////////////////////////////////////////////////////////////////
 * Intent intent = new Intent();   
intent.setAction(android.content.Intent.ACTION_VIEW);   
File file = new File("/sdcard/test.mp3");   
intent.setDataAndType(Uri.fromFile(file), "audio/*");   
startActivity(intent
 
 
   */
