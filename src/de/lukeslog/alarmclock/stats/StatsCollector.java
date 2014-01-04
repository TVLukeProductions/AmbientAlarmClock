package de.lukeslog.alarmclock.stats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.lukeslog.alarmclock.support.AlarmClockConstants;

public class StatsCollector 
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
	private static File file;
	
	public static void snooze()
	{
		file = new File("logfile.txt");
		 
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
		FileOutputStream outputStream = null;
		try
		{
			outputStream = new FileOutputStream(file, true);
		}
		catch(Exception e)
		{
			
		}
	}
}
