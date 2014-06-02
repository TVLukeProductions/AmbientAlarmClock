package de.lukeslog.alarmclock.main;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;

import de.lukeslog.alarmclock.actions.ActionManager;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.startup.NotificationService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;

/**
 * Created by lukas on 29.03.14.
 */
public class ClockWorkService extends Service
{

    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    public static SharedPreferences settings;

    private static int currentSecond=-1;
    private static DateTime lasttime;
    private static Context context=null;
    private static boolean running = true;

    private static Updater updater;

    private static ArrayList<Timable> notifications = new ArrayList<Timable>();

    public static Context getClockworkContext()
    {
        running=true;
        if(context!=null)
        {
            return context;
        }
        else
        {
            return null;
        }
    }

    public static void registerForNotofication(Timable x)
    {
        notifications.add(x);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        //Log.d(TAG, "ClockWorkService onStartCommand()");

        startUpdater();

        return START_NOT_STICKY;
    }

    private void startUpdater()
    {
        updater= new Updater();
        updater.run();
    }

    @Override
    public void onCreate()
    {
        running=true;
        super.onCreate();
        context=this;
        //Log.d(TAG, "ClockWorkService onCreate()");
        settings = getSharedPreferences(PREFS_NAME, 0);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    private class  Updater implements Runnable
    {
        private Handler handler = new Handler();
        public static final int delay = 1000;

        @Override
        public void run()
        {
            DateTime currentTime = new DateTime();
            if(running)
            {
                if(lasttime==null)
                {
                    lasttime=currentTime;
                }
                if(newSecondHasStarted())
                {
                    //sometimes the handler can skip one or two seconds, this would mean missing the
                    //alarm if we only called with the current time so we have a while loop calling for all times
                    //since the last time
                    currentTime = currentTime.withMillisOfSecond(0);
                    lasttime = lasttime.withMillisOfSecond(0);
                    while(lasttime.isBefore(currentTime))
                    {
                        lasttime=lasttime.plusSeconds(1);
                        Logger.i(TAG + "_time", ". "+lasttime.getHourOfDay()+":"+lasttime.getMinuteOfHour()+":"+lasttime.getSecondOfMinute()+":"+lasttime.getMillisOfSecond());
                        AmbientAlarmManager.notifyActiveAlerts(lasttime);
                        ActionManager.notifyOfCurrentTime(lasttime);
                    }
                    lasttime=currentTime;
                }
            }
            handler.removeCallbacks(this); // remove the old callback
            handler.postDelayed(this, delay); // register a new one
        }

        public void onPause()
        {
            Logger.d(TAG, "Clock Work Service update on Pause ");
            handler.removeCallbacks(this); // stop the map from updating
        }

        public void onResume()
        {
            handler.removeCallbacks(this); // remove the old callback
            handler.postDelayed(this, delay); // register a new one
        }
    }

    /**
     *
     * @return returns true if a new second has started since the last time this method has been called
     */
    private boolean newSecondHasStarted()
    {
        DateTime currentTime = new DateTime();
        if(currentTime.getSecondOfMinute()-currentSecond>1)
        {
            Logger.e(TAG, "WE JUST SKIPPED A SECCOND! WE JUST SKIPPED A SECOND! THIS IS WORST THING. EVER.");
        }

        if(currentSecond!=currentTime.getSecondOfMinute())
        {
            currentSecond=currentTime.getSecondOfMinute();
            return true;
        }
        return false;
    }

    public static void stopService()
    {
        updater.onPause();
        running=false;
    }

    public static Context getContext()
    {
        if(context!=null)
        {
            return context;
        }
        throw new NullPointerException();
    }
}
