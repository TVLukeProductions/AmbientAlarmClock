package de.lukeslog.alarmclock.startup;

import de.lukeslog.alarmclock.support.AlarmClockConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

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
        Log.d(TAG, "STARTUPSTARTUPSTATUP");
        Log.d(TAG, "-----------------------------------------------");
        Log.d(TAG, "-----------------------------------------------");
        Log.d(TAG, "-----------------------------------------------");
        Log.d(TAG, "-----------------------------------------------");
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
