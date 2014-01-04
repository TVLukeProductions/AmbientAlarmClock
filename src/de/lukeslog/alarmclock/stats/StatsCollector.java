package de.lukeslog.alarmclock.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

import de.lukeslog.alarmclock.support.AlarmClockConstants;

public class StatsCollector 
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    private static File file;
	
	public static void snooze()
	{
		file = new File(Environment.getExternalStorageDirectory().getPath()+"/AmbientAlarmClock/logfile.txt");
		file.mkdirs(); 
		// if file doesnt exists, then create it
		if (!file.exists()) 
		{
			try 
			{
				file.createNewFile();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void awake(int minutes)
	{
		Log.d(TAG, "awake");
		file = new File(Environment.getExternalStorageDirectory().getPath()+"/AmbientAlarmClock/logfile.txt");
		file.mkdirs(); 
		// if file doesnt exists, then create it
		if (!file.exists()) 
		{
			try 
			{
				file.createNewFile();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileOutputStream outputStream = null;
		try
		{
			String content=" \n";
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			//outputStream = new FileOutputStream(file);
		}
		catch(Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
		finally 
		{
		    if (outputStream != null) 
		    {
		        try 
		        {
		            //outputStream.close();
		        } 
		        catch (Exception ef) 
		        {
		        	
		        }
		    }
		}
	}
}
