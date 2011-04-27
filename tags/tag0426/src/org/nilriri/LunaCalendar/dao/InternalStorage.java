package org.nilriri.LunaCalendar.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class InternalStorage extends SQLiteOpenHelper implements StorageSelector {

    private Context mContext;

    //  private SQLiteDatabase db;

    public InternalStorage(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;

        //    this.db = getWritableDatabase();
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        new DaoCreator().onCreate(this.mContext, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        new DaoCreator().onUpgrade(this.mContext, db, oldVersion, newVersion);

    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
        //  return db;
    }

    @Override
    public SQLiteDatabase getWritableDatabase()

    {
        return super.getWritableDatabase();

    }

    @Override
    public void close() {

        super.close();
    }

    public void onDestroy() {
        // TODO Auto-generated method stub

        super.close();

    }

}
