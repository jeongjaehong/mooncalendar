package org.nilriri.LunaCalendar.dao;

import org.nilriri.LunaCalendar.tools.Common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public abstract class AbstractDao {

    StorageSelector daoIf;

    SQLiteDatabase db;

    public AbstractDao(Context context, CursorFactory factory, boolean sdcarduse) {

        if (sdcarduse && Common.isSdPresent()) {
            Log.d("onCreate", "TRUE");

            daoIf = new ExternalStorage(context, factory);
        } else {
            Log.d("onCreate", "FALSE");

            daoIf = new InternalStorage(context, Constants.DATABASE_NAME, factory, Constants.DATABASE_VERSION);
        }

    }

    public Context getContext() {
        return daoIf.getContext();
    }

    public void CloseDatabase() {
        if (db == null || !db.isOpen()) {
            db.close();
        }
    }

    private void OpenDatabase() {
        db = daoIf.getWritableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        if (db == null || !db.isOpen()) {
            this.OpenDatabase();
        }
        return db;
        //return daoIf.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        //return daoIf.getReadableDatabase();
        if (db == null || !db.isOpen()) {
            this.OpenDatabase();
        }
        return db;
    }

}
