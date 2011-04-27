package org.nilriri.LunaCalendar.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class ExternalStorage extends DaoCreator implements StorageSelector {
    private Context mContext;
    private CursorFactory mFactory;
    private SQLiteDatabase db;

    public ExternalStorage(Context context, CursorFactory factory) {
        Log.d("onCreate", "EXTERNAL_DB_NAME=" + Constants.EXTERNAL_DB_NAME);

        db = SQLiteDatabase.openOrCreateDatabase(Constants.EXTERNAL_DB_NAME, factory);

        Log.d("ExternalStorage", "db.getVersion()=" + db.getVersion());

        if (Constants.EXTERNAL_DB_VERSION != db.getVersion()) {

            switch (db.getVersion()) {
                case 0:
                    onCreate(context, db);
                    break;
                default:

                    onUpgrade(context, db, db.getVersion(), Constants.EXTERNAL_DB_VERSION);
                    break;
            }

            db.setVersion(Constants.EXTERNAL_DB_VERSION);
        }
        //db.setTransactionSuccessful();
        //db.endTransaction();
        //db.close();
        
        //getWritableDatabase();

        mContext = context;

    }

    public Context getContext() {
        // TODO Auto-generated method stub
        return mContext;
    }

    public SQLiteDatabase getReadableDatabase() {
        // TODO Auto-generated method stub
        this.db = SQLiteDatabase.openDatabase(Constants.EXTERNAL_DB_NAME, mFactory, SQLiteDatabase.OPEN_READONLY);
        return db;

    }

    public SQLiteDatabase getWritableDatabase() {
        // TODO Auto-generated method stub
        this.db = SQLiteDatabase.openDatabase(Constants.EXTERNAL_DB_NAME, mFactory, SQLiteDatabase.OPEN_READWRITE);

        return db;
    }

    public void close() {
        if (db != null || db.isOpen()){
            db.close();
        }
    }

    public void onDestroy() {
 
  
        if (db != null) {
            db.close();
        }
     
    }

}
