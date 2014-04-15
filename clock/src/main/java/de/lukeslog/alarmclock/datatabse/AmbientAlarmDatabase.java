package de.lukeslog.alarmclock.datatabse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;

/**
 * Created by lukas on 31.03.14.
 */
public class AmbientAlarmDatabase
{
    private static SQLiteDatabase database;

    public static void createDataBase(Context context)
    {
        DatabaseHelper openHelper = new DatabaseHelper(context);

    }

    public static ArrayList<AmbientAlarm> getAllEntrys()
    {
        return null;
    }

}
