package de.lukeslog.alarmclock.ambientalarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.lukeslog.alarmclock.datatabse.AmbientAlarmDatabase;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 31.03.14.
 *
 * Manages the set of alarms generated by the user, also manages access to the database.
 */
public class AmbientAlarmManager
{

    private static ArrayList<AmbientAlarm> registeredAlarms;

    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    /**
     *
     * This method informs all currently active alarms of the current time, this time may then
     * be transformed into actions by these alarms
     *
     * @param currentTime to inform all alerts of an identical time the current Time is provided by
     *                    the calling method
     */
    public static void notifyActiveAlerts(DateTime currentTime)
    {
        if(registeredAlarms!=null)
        {
            Log.d(TAG, "--tick--" + registeredAlarms.size());
            for (AmbientAlarm alarm : registeredAlarms)
            {
                if (alarm.isActive())
                {
                    alarm.notifyOfCurrentTime(currentTime);
                }
            }
        }
        else
        {
            updateListFromDataBase();
        }
    }

    /**
     * add a new ambient alarm to the registry
     *
     * @param ambientAlarm
     */
    public static void addNewAmbientAlarm(AmbientAlarm ambientAlarm)
    {
        Log.d(TAG, "  addNewAmbientAlarm(AmbientAlarm)");
        if(registeredAlarms!=null)
        {
            registeredAlarms.add(ambientAlarm);
        }
        else
        {
            updateListFromDataBase();
            registeredAlarms.add(ambientAlarm);
        }
        AmbientAlarmDatabase.updateAmbientAlarm(ambientAlarm);
    }

    public static void updateDataBaseEntry(AmbientAlarm ambientAlarm)
    {
        Log.d(TAG, "  updateDatabaseEntry(AmbientAlarm)");
        AmbientAlarmDatabase.updateAmbientAlarm(ambientAlarm);
    }

    public static ArrayList<AmbientAlarm> getListOfAmbientAlarms()
    {
        Log.d(TAG, "  getListOfAmbientAlarms()");
        return registeredAlarms;
    }

    public static void updateListFromDataBase()
    {
        Log.d(TAG, "  update Alarm List from Database");
        //Log.d(TAG, "  Listsize="+registeredAlarms.size());
        registeredAlarms = AmbientAlarmDatabase.getAllAlarmsFromDatabase();
        Log.d(TAG, "  Listsize="+registeredAlarms.size());
    }

    public static void startAlarmActivity(AmbientAlarm ambientAlarm)
    {
        Log.d(TAG, "  startAlarmActivity!");
        Context ctx = ClockWorkService.getClockworkContext();
        //TODO: change to alarmID
        int alarmNumber = getAlarmNumber(ambientAlarm);
        if(alarmNumber>-1 && ctx != null)
        {
            Intent intent = new Intent(ctx, ambientAlarm.getAlarmActivity());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("ambientAlarmID", alarmNumber);
            ctx.startActivity(intent);
        }
    }

    public static int getAlarmNumber(AmbientAlarm ambientAlarm)
    {
        Log.d(TAG, "  getAlarmNumber---");
        if(registeredAlarms!=null)
        {
            for (int i = 0; i < registeredAlarms.size(); i++)
            {
                if (registeredAlarms.get(i).equals(ambientAlarm))
                {
                    return i;
                }
            }

            return -1;
        }
        updateListFromDataBase();
        return getAlarmNumber(ambientAlarm);
    }

    public static void deleteAmbientAlarm(int position)
    {
        Log.d(TAG, "  delete Ambient Alarm!");
        if(registeredAlarms!=null)
        {
            AmbientAlarmDatabase.removeAmbientAlarm(registeredAlarms.get(position));
            registeredAlarms.remove(position);
        }
    }

    public static AmbientAlarm getAlarmById(String alarmID)
    {
        if(registeredAlarms!=null)
        {
            Log.d(TAG, "  getAlarmByID " + alarmID);
            Log.d(TAG, "  size of AlarmList " + registeredAlarms.size());
            for (AmbientAlarm alarm : registeredAlarms)
            {
                Log.d(TAG, "    -->" + alarm.getAlarmID());
                if (alarm.getAlarmID().equals(alarmID))
                {
                    return alarm;
                }
            }
            return null;
        }
        else
        {
            updateListFromDataBase();
            return getAlarmById(alarmID);
        }
    }
}
