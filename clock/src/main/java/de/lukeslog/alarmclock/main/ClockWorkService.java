package de.lukeslog.alarmclock.main;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.util.ArrayList;
import java.util.Calendar;

import de.lukeslog.alarmclock.actions.ActionManager;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;

/**
 * Created by lukas on 29.03.14.
 */
public class ClockWorkService  extends IntentService
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    public static SharedPreferences settings;

    private static int currentMinute=-1;
    private static DateTime lasttime;
    private static Context context=null;
    private static boolean running = true;

    private static ArrayList<Timable> notifications = new ArrayList<Timable>();

    public ClockWorkService()
    {
        super("ClockWorkService");
    }

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

        return START_NOT_STICKY;
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
    protected void onHandleIntent(Intent intent)
    {
        if(running)
        {
            Logger.d(TAG, ".");
            DateTime currentTime = new DateTime();
            if (lasttime == null)
            {
                lasttime = currentTime;
            }
            if (newSecondHasStarted())
            {
                //sometimes the handler can skip one Minute, this would mean missing the
                //alarm if we only called with the current time so we have a while loop calling for all times
                //since the last time
                currentTime = currentTime.withMillisOfSecond(0);
                currentTime = currentTime.withSecondOfMinute(0);
                lasttime = lasttime.withMillisOfSecond(0);
                lasttime = lasttime.withSecondOfMinute(0);
                while (lasttime.isBefore(currentTime))
                {
                    lasttime = lasttime.plusMinutes(1);
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
                lasttime = currentTime;
            }
            scheduleNext();
        }
    }

    private void scheduleNext()
    {
        Calendar cal = Calendar.getInstance();
        DateTime now = new DateTime();
        DateTime inaminute = new DateTime();
        inaminute = inaminute.plusMinutes(1);
        inaminute = inaminute.withSecondOfMinute(0);
        int secondstoadd = Seconds.secondsBetween(now, inaminute).getSeconds();
        Logger.d(TAG, ""+secondstoadd);
        cal.add(Calendar.SECOND, secondstoadd);
        Intent intent = new Intent(this, ClockWorkService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, AlarmClockConstants.TICK, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
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
        if(currentTime.getMinuteOfHour()-currentMinute>1)
        {
            Logger.e(TAG, "WE JUST SKIPPED A MINUTE! WE JUST SKIPPED A MINUTE! THIS IS WORST THING. EVER.");
        }

        if(currentMinute!=currentTime.getMinuteOfHour())
        {
            currentMinute=currentTime.getMinuteOfHour();
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
