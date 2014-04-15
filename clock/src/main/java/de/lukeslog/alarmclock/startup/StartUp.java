package de.lukeslog.alarmclock.startup;

import de.lukeslog.alarmclock.actions.SendMailAction;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.dropbox.DropBox;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Day;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTime;

/**
 * This Receiver is registered to be started on boot to start the alarm clock when the systems is
 * started
 */
public class StartUp  extends BroadcastReceiver
{
	public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    @Override
	public void onReceive(Context context, Intent intent) 
    {

        try
        {
           ServiceStarter.start(context);
        }
        catch(Exception e)
        {
            Log.e(TAG, "Error on Startup.");
        }
	}
}
