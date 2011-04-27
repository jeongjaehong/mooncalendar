package org.nilriri.LunaCalendar.dao;

import org.nilriri.LunaCalendar.tools.Common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public abstract class AbstractDao {

    private StorageSelector daoIf = null;

    private SQLiteDatabase db = null;

    public AbstractDao(Context context, CursorFactory factory, boolean sdcarduse) {

        if (sdcarduse && Common.isSdPresent()) {
            Log.d("onCreate", "TRUE");

            daoIf = new ExternalStorage(context, factory);
        } else {
            Log.d("onCreate", "FALSE");

            daoIf = new InternalStorage(context, Constants.DATABASE_NAME, factory, Constants.DATABASE_VERSION);
        }

       // CloseDatabase();
        
        this.db = getWritableDatabase();

    }

    public Context getContext() {
        return daoIf.getContext();
    }

    public void close() {
        if (db != null || db.isOpen()) {
            db.close();
        }
        daoIf.close();
    }

     protected void onDestroy() {
        if (daoIf != null) {
            daoIf.close();
        }
        if (db != null) {
            db.close();
        }
    }

 
     public SQLiteDatabase getWritableDatabase() {
         if (db == null || !db.isOpen()) {
            db = daoIf.getWritableDatabase();
         }
         return db;
       
     }
     public SQLiteDatabase getReadableDatabase() {
         if (db == null || !db.isOpen()) {
            db = daoIf.getReadableDatabase();
         }
         return db;
       
     }

  

}
