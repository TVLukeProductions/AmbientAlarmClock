package de.lukeslog.alarmclock.main;

import de.lukeslog.alarmclock.support.AlarmClockConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartUp  extends BroadcastReceiver
{
	public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;
	
    @Override
	public void onReceive(Context context, Intent intent) 
    {
    	try
    	{
    	 Intent startIntent = new Intent(context, ClockService.class);
    	 context.startService(startIntent);
    	}
    	catch(Exception e)
    	{
    		Log.e(TAG, "Error on Autostart");
    	}
	}
}
