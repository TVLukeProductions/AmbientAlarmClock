package de.lukeslog.alarmclock.datatabse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by lukas on 03.04.14.
 *
 * Creates and deletes databases
 */
public class DatabaseHelper extends SQLiteOpenHelper
{

    DatabaseHelper(Context context)
    {
        super(context, DatabaseConstants.DATABASE_NAME, null,  DatabaseConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //Log.i(TAG, "onCreate for EntryDB");
        //db.execSQL(SnapNowConstants.TABLE_ENTRY_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Log.i(TAG, "onUpgrade for EntryDB");
        //db.execSQL("DROP TABLE IF EXISTS " + SnapNowConstants.TABLE_ENTRY);
        //onCreate(db);
    }
}
