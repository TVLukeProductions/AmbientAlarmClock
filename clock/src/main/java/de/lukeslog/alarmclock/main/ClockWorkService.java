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
import android.preference.PreferenceManager;

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
public class ClockWorkService extends Service implements Runnable
{

    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    public static SharedPreferences settings;

    private static int currentSecond=-1;
    private static DateTime lasttime;
    private static Context context=null;
    private static boolean running = true;

    private Thread runner;

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

        runner = new Thread(this);
        runner.start();
    }

    @Override
    public void onCreate()
    {
        running=true;
        super.onCreate();
        context=this;
        //Log.d(TAG, "ClockWorkService onCreate()");
        settings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void run()
    {
        boolean running=true;
        while(running)
        {
            DateTime currentTime = new DateTime();
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
                    //since lasttime is a global object which may change during execution on the other thread
                    final DateTime time = new DateTime(lasttime);

                    Handler mainHandler = new Handler(context.getMainLooper());

                     // This is your code
                    mainHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            tick(time);
                        }
                    });
                }
                lasttime=currentTime;
            }
        }
        try
        {
            Thread.currentThread().sleep(500);
        }
        catch(Exception ie)
        {

        }
    }

    private void tick(DateTime time)
    {
        Logger.i(TAG + "_time", ". "+time.getHourOfDay()+":"+time.getMinuteOfHour()+":"+time.getSecondOfMinute()+":"+time.getMillisOfSecond());
        AmbientAlarmManager.notifyActiveAlerts(time);
        ActionManager.notifyOfCurrentTime(time);
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
