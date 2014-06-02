package de.lukeslog.alarmclock.support;

import android.os.Environment;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by lukas on 28.05.14.
 */
public class Logger
{
    public static boolean testing = false;
    public static boolean logfile = false;

    public static void i(String TAG, String m)
    {
        if(testing)
        {
            Log.i(TAG, m);
        }
        if(logfile)
        {
            store(TAG, m);
        }
    }

    public static void d(String TAG, String m)
    {
        if(testing)
        {
            Log.d(TAG, m);
        }
        if(logfile)
        {
            store(TAG, m);
        }
    }

    public static void e(String TAG, String m)
    {
        if(testing)
        {
            Log.e(TAG, m);
        }
        if(logfile)
        {
            store(TAG, m);
        }
    }

    public static void v(String TAG, String m)
    {
        if(testing)
        {
            Log.v(TAG, m);
        }
        if(logfile)
        {
            store(TAG, m);
        }
    }

    public static void w(String TAG, String m)
    {
        if(testing)
        {
            Log.w(TAG, m);
        }
        if(logfile)
        {
            store(TAG, m);
        }
    }

    public static void wtf(String TAG, String m)
    {
        if(testing)
        {
            Log.wtf(TAG, m);
        }
        if(logfile)
        {
            store(TAG, m);
        }
    }

    private static boolean store(String TAG, String m)
    {
        DateTime d = new DateTime();
        String log="aaclog.txt";
        File Root = Environment.getExternalStorageDirectory();
        if(Root.canWrite())
        {
            File logFile = new File(Root, log);
            if (!logFile.exists())
            {
                try
                {
                    logFile.createNewFile();
                }
                catch (IOException e)
                {

                    e.printStackTrace();
                }
            }
            try
            {
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                //BufferedWriter for performance, true to set append to file flag
                String logString = d.getDayOfMonth()+"-"+d.getMonthOfYear()+"-"+d.getYear()+" "+d.getHourOfDay()+":"+d.getMinuteOfHour()+":"+d.getSecondOfMinute()+"  "+TAG+" "+m+"\n";
                buf.append(logString);
                buf.close();
            }
            catch (IOException e)
            {

                e.printStackTrace();
            }
        }
        return true;
    }
}
