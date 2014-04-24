package de.lukeslog.alarmclock.datatabse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.lukeslog.alarmclock.actions.*;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Day;

/**
 * Created by lukas on 31.03.14.
 */
public class AmbientAlarmDatabase
{
    private static SQLiteDatabase database;
    public static String TAG = AlarmClockConstants.TAG;

    public static final String TABLE_ALARM="ambientalarm";
    public static final String TABLE_ACTION="ambientaction";
    public static final String TABLE_ALARMTOACTION = "alarmtoaction";

    public static final String TABLE_ALARM_ID = "_id";
    public static final String TABLE_ALARM_ALARMID = "alarmid";
    public static final String TABLE_ALARM_ACTIVE = "isactive";
    public static final String TABLE_ALARM_SNOOZING = "issnoozing";
    public static final String TABLE_ALARM_SNOOZE_TIME = "snoozetime";
    public static final String TABLE_ALARM_TIME_HOUR = "alarmhour";
    public static final String TABLE_ALARM_TIME_MINUTE = "alarmminute";
    public static final String TABLE_ALARM_DAY_MONDAY = "alarmmonday";
    public static final String TABLE_ALARM_DAY_TUESDAY = "alarmtuesday";
    public static final String TABLE_ALARM_DAY_WEDNESDAY = "alarmwednesday";
    public static final String TABLE_ALARM_DAY_THURSDAY = "alarmthursday";
    public static final String TABLE_ALARM_DAY_FRIDAY = "alarmfriday";
    public static final String TABLE_ALARM_DAY_SATURDAY = "alarmsaturday";
    public static final String TABLE_ALARM_DAY_SUNDAY = "alarmsunday";

    public static final String TABLE_ACTION_ID = "_id";
    public static final String TABLE_ACTION_ACTIONID = "actionid";
    public static final String TABLE_ACTION_TYPE = "actiontype";
    public static final String TABLE_ACTION_NAME = "actionname";
    public static final String TABLE_ACTION_CONFIG_BUNDLE_VALUES = "configbundlevalues";

    public static final String TABLE_ALARMTOACTION_ID = "_id";
    public static final String TABLE_ALARMTOACTION_TIMING = "alarmtoactiontiming";

    public static final String TABLE_ACTION_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ACTION +
                    " ("+TABLE_ACTION_ID + " integer primary key autoincrement, "+
                    ""+TABLE_ACTION_ACTIONID +" text, " +
                    ""+TABLE_ACTION_TYPE +" text, "+
                    ""+TABLE_ACTION_NAME + " text, " +
                    ""+TABLE_ACTION_CONFIG_BUNDLE_VALUES + " text "+
                    ");";

    public static final String TABLE_ALARMTOACTION_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ALARMTOACTION +
                    " ("+TABLE_ALARMTOACTION_ID +" integer primary key autoincrement, "+
                    ""+TABLE_ALARM_ALARMID+" text, "+
                    ""+TABLE_ALARMTOACTION_TIMING+" text, "+
                    ""+TABLE_ACTION_ACTIONID+" text "+
                    ");";

    public static final String TABLE_ALARM_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ALARM +
                    " ("+TABLE_ALARM_ID +" integer primary key autoincrement, " +
                    ""+TABLE_ALARM_ALARMID+" text, " +
                    ""+TABLE_ALARM_ACTIVE+" integer, " +
                    ""+TABLE_ALARM_SNOOZING+" integer, " +
                    ""+TABLE_ALARM_SNOOZE_TIME+" integer, " +
                    ""+TABLE_ALARM_TIME_HOUR+" integer, " +
                    ""+TABLE_ALARM_TIME_MINUTE+" integer, " +
                    ""+TABLE_ALARM_DAY_MONDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_TUESDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_WEDNESDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_THURSDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_FRIDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_SATURDAY+" integer, " +
                    ""+TABLE_ALARM_DAY_SUNDAY+" integer " +
                    ");";

    public static void createDataBase(Context context)
    {
        OpenHelper openHelper = new OpenHelper(context);
        database = openHelper.getWritableDatabase();

    }

    public static void updateAmbientAction(AmbientAction ambientAction)
    {
        Log.d(TAG, "upate Action Database");
        ContentValues cValues = new ContentValues();
        cValues.put(TABLE_ACTION_NAME, ambientAction.getActionName());
        cValues.put(TABLE_ACTION_TYPE, ambientAction.getClass().toString());
        String configString=ambientAction.getConfigurationData().toString();
        cValues.put(TABLE_ACTION_CONFIG_BUNDLE_VALUES, configString);
        if(actionEntryExists(ambientAction))
        {
            Log.d(TAG, "we make an update "+ambientAction.getActionID());
            String[] args={ambientAction.getActionID()};
            database.update(TABLE_ACTION, cValues, TABLE_ACTION_ACTIONID + " = ?", args);
        }
        else
        {
            cValues.put(TABLE_ACTION_ACTIONID, ambientAction.getActionID());
            Log.d(TAG, "we create a new entry");
            database.insert(TABLE_ACTION, null, cValues);
        }
    }

    public static ArrayList<AmbientAction> getAllActionsFromDatabase()
    {
        Log.i(TAG, "get all...");

        Cursor c = queryDatabaseForAllActions();

        Log.i(TAG, "cursorsize: "+c.getCount());


        ArrayList<AmbientAction> ambientActions = createActionListFromCursor(c);

        c.close();

        return ambientActions;
    }

    public static void removeAmbientAction(AmbientAction ambientAction)
    {
        database.delete(TABLE_ACTION, TABLE_ACTION_ACTIONID+" = '"+ambientAction.getActionID()+"'", null);
    }

    private static ArrayList<AmbientAction> createActionListFromCursor(Cursor c)
    {
        ArrayList<AmbientAction> ambientActions = new ArrayList<AmbientAction>();
        while (c.moveToNext())
        {
            AmbientAction ambientAction = createActionFromCursorElement(c);
            ambientActions.add(ambientAction);
        }
        return ambientActions;
    }

    private static AmbientAction createActionFromCursorElement(Cursor c)
    {
        String configString = c.getString(c.getColumnIndex(TABLE_ACTION_CONFIG_BUNDLE_VALUES));
        String className = c.getString(c.getColumnIndex(TABLE_ACTION_TYPE));
        ActionConfigBundle acb = new ActionConfigBundle(configString);

        if(className.equals("CountdownAction"))
        {
            CountdownAction ca = new CountdownAction(acb);
            return ca;
        }
        if(className.equals("SendMailAction"))
        {
            SendMailAction sma = new SendMailAction(acb);
            return sma;
        }


        return null;
    }

    private static Cursor queryDatabaseForAllActions()
    {
         Cursor c= database.query(TABLE_ACTION, new String[] {
                            TABLE_ACTION_ACTIONID,
                            TABLE_ACTION_NAME,
                            TABLE_ACTION_CONFIG_BUNDLE_VALUES},
                    null,
                    null,
                    null,
                    null,
                    null);
        return c;
    }

    private static boolean actionEntryExists(AmbientAction ambientAction)
    {
        String[] args={ambientAction.getActionID()};
        Log.d(TAG, "alarmID="+ambientAction.getActionID());
        Cursor cursor = database.rawQuery("SELECT * FROM "+TABLE_ACTION+" WHERE "+TABLE_ACTION_ACTIONID+" = ? ", args);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        Log.d(TAG, "existst? "+exists);
        return exists;
    }

    public static void updateAmbientAlarm(AmbientAlarm ambientAlarm)
    {
        Log.d(TAG, "update Database");
        ContentValues cValues = new ContentValues();
        cValues.put(TABLE_ALARM_ACTIVE, boolToInt(ambientAlarm.isActive()));
        cValues.put(TABLE_ALARM_SNOOZING, boolToInt(ambientAlarm.isActive()));
        cValues.put(TABLE_ALARM_SNOOZE_TIME, ambientAlarm.getSnoozeTimeInSeconds());
        cValues.put(TABLE_ALARM_TIME_HOUR, ambientAlarm.getAlarmTime().getHourOfDay());
        cValues.put(TABLE_ALARM_TIME_MINUTE, ambientAlarm.getAlarmTime().getMinuteOfHour());
        cValues.put(TABLE_ALARM_DAY_MONDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.MONDAY)));
        cValues.put(TABLE_ALARM_DAY_TUESDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.TUESDAY)));
        cValues.put(TABLE_ALARM_DAY_WEDNESDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.WEDNESDAY)));
        cValues.put(TABLE_ALARM_DAY_THURSDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.THURSDAY)));
        cValues.put(TABLE_ALARM_DAY_FRIDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.FRIDAY)));
        cValues.put(TABLE_ALARM_DAY_SATURDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.SATURDAY)));
        cValues.put(TABLE_ALARM_DAY_SUNDAY, boolToInt(ambientAlarm.getActiveForDayOfTheWeek(Day.SUNDAY)));
        if(alarmEntryExists(ambientAlarm))
        {
            Log.d(TAG, "we make an update "+ambientAlarm.getAlarmID());
            String[] args={ambientAlarm.getAlarmID()};
            database.update(TABLE_ALARM, cValues, TABLE_ALARM_ALARMID + " = ?", args);
        }
        else
        {
            cValues.put(TABLE_ALARM_ALARMID, ambientAlarm.getAlarmID());
            Log.d(TAG, "we create a new entry");
            database.insert(TABLE_ALARM, null, cValues);
        }
    }

    public static ArrayList<AmbientAlarm> getAllAlarmsFromDatabase()
    {
        Log.i(TAG, "get all...");

        Cursor c = queryDatabaseForAllAlarms();

        Log.i(TAG, "cursorsize: "+c.getCount());


        ArrayList<AmbientAlarm> ambientAlarms = createAlarmListFromCursor(c);

        c.close();

        return ambientAlarms;
    }

    public static void removeAmbientAlarm(AmbientAlarm ambientAlarm)
    {
        database.delete(TABLE_ALARM, TABLE_ALARM_ALARMID+" = '"+ambientAlarm.getAlarmID()+"'", null);
        removeActionsRelatedToAlarm(ambientAlarm);
    }

    private void updateActionsForAmbientAlarm(AmbientAlarm alarm)
    {
        removeActionsRelatedToAlarm(alarm);
        HashMap<String, ArrayList<AmbientAction>> actions = alarm.getRegisteredActions();
        Set<String> keyset = actions.keySet();
        for(String s : keyset)
        {
            ArrayList<AmbientAction> ambientactions = actions.get(s);
            for(AmbientAction action : ambientactions)
            {
                addToAlarmToActionDatabase(alarm.getAlarmID(), s, action.getActionID());
            }

        }
    }

    private static void removeActionsRelatedToAlarm(AmbientAlarm alarm)
    {
        database.delete(TABLE_ALARMTOACTION, TABLE_ALARM_ALARMID+" = '"+alarm.getAlarmID()+"'", null);
    }

    private void addToAlarmToActionDatabase(String alarmID, String s, String actionID)
    {
        ContentValues cValues = new ContentValues();
        cValues.put(TABLE_ALARM_ALARMID, alarmID);
        cValues.put(TABLE_ALARMTOACTION_TIMING, s);
        cValues.put(TABLE_ACTION_ACTIONID, actionID);
        database.insert(TABLE_ALARMTOACTION, null, cValues);
    }

    private static boolean alarmEntryExists(AmbientAlarm ambientAlarm)
    {
        String[] args={ambientAlarm.getAlarmID()};
        Log.d(TAG, "alarmID="+ambientAlarm.getAlarmID());
        Cursor cursor = database.rawQuery("SELECT * FROM "+TABLE_ALARM+" WHERE "+TABLE_ALARM_ALARMID+" = ? ", args);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        Log.d(TAG, "existst? "+exists);
        return exists;
    }

    private static ArrayList<AmbientAlarm> createAlarmListFromCursor(Cursor c)
    {
        ArrayList<AmbientAlarm> ambientAlarms = new ArrayList<AmbientAlarm>();
        while (c.moveToNext())
        {
            AmbientAlarm ambientAlarm = createAlarmFromCursorElement(c);
            ambientAlarms.add(ambientAlarm);
        }
        return ambientAlarms;
    }

    private static AmbientAlarm createAlarmFromCursorElement(Cursor c)
    {
        AmbientAlarm ambientAlarm = new AmbientAlarm(c.getString(c.getColumnIndex(TABLE_ALARM_ALARMID)));
        DateTime alarmTime = new DateTime();
        alarmTime = alarmTime.withHourOfDay(c.getInt(c.getColumnIndex(TABLE_ALARM_TIME_HOUR)));
        alarmTime = alarmTime.withMinuteOfHour(c.getInt(c.getColumnIndex(TABLE_ALARM_TIME_MINUTE)));
        ambientAlarm.setAlarmTime(alarmTime);
        ambientAlarm.setActive(intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_ACTIVE))));
        Log.d(TAG, "" + ambientAlarm.isActive());
        ambientAlarm.setSnoozing(intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_SNOOZING))));
        Log.d(TAG, "" + ambientAlarm.isSnoozing());
        ambientAlarm.setSnoozeTimeInSeconds(c.getInt(c.getColumnIndex(TABLE_ALARM_SNOOZE_TIME)));
        Log.d(TAG, "olol");
        ambientAlarm.setAlarmStateForDay(Day.MONDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_MONDAY))));
        ambientAlarm.setAlarmStateForDay(Day.TUESDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_TUESDAY))));
        ambientAlarm.setAlarmStateForDay(Day.WEDNESDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_WEDNESDAY))));
        ambientAlarm.setAlarmStateForDay(Day.THURSDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_THURSDAY))));
        ambientAlarm.setAlarmStateForDay(Day.FRIDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_FRIDAY))));
        ambientAlarm.setAlarmStateForDay(Day.SATURDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_SATURDAY))));
        ambientAlarm.setAlarmStateForDay(Day.SUNDAY, intToBool(c.getInt(c.getColumnIndex(TABLE_ALARM_DAY_SUNDAY))));
        return ambientAlarm;
    }

    private static Cursor queryDatabaseForAllAlarms()
    {
        Cursor c= database.query(TABLE_ALARM, new String[] {
                        TABLE_ALARM_ALARMID,
                        TABLE_ALARM_ACTIVE,
                        TABLE_ALARM_SNOOZING,
                        TABLE_ALARM_SNOOZE_TIME,
                        TABLE_ALARM_TIME_HOUR,
                        TABLE_ALARM_TIME_MINUTE,
                        TABLE_ALARM_DAY_MONDAY,
                        TABLE_ALARM_DAY_TUESDAY,
                        TABLE_ALARM_DAY_WEDNESDAY,
                        TABLE_ALARM_DAY_THURSDAY,
                        TABLE_ALARM_DAY_FRIDAY,
                        TABLE_ALARM_DAY_SATURDAY,
                        TABLE_ALARM_DAY_SUNDAY},
                null,
                null,
                null,
                null,
                null);
        return c;
    }

    private static int boolToInt(boolean b)
    {
        if(b)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private static boolean intToBool(int i)
    {
        if(i==0)
        {
            return false;
        }
        return true;
    }

    public static class OpenHelper extends SQLiteOpenHelper
    {

        OpenHelper(Context context)
        {
            super(context, DatabaseConstants.DATABASE_NAME, null,  DatabaseConstants.DATABASE_VERSION);
            Log.d(TAG, "Open Helper");
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            Log.i(TAG, "onCreate for EntryDB");
            db.execSQL(TABLE_ALARM_CREATE);
            db.execSQL(TABLE_ACTION_CREATE);
            db.execSQL(TABLE_ALARMTOACTION_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.i(TAG, "onUpgrade for EntryDB");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTION);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMTOACTION);
            onCreate(db);
        }
    }
}
