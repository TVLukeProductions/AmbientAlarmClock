package de.lukeslog.alarmclock.main;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import org.joda.time.DateTime;

import java.util.Calendar;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 29.03.14.
 */
public class ClockWorkService extends IntentService
{

    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    public static SharedPreferences settings;

    private static int currentSecond=-1;
    private static Context context=null;

    public ClockWorkService()
    {
        super("ClockWorkService");
    }

    public static Context getClockworkContext()
    {
        if(context!=null)
        {
            return context;
        }
        else
        {
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        //Log.d(TAG, "ClockWorkService onStartCommand()");
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate()
    {
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

    @Override
    protected void onHandleIntent(Intent intent)
    {
        tick();
    }

    /**
     * Tick is the method that ticks along with each new call of the ClockWorkService
     */
    private void tick()
    {
        if(newSecondHasStarted())
        {
            //Log.d(TAG, "tick");
            DateTime currentTime = new DateTime();
            AmbientAlarmManager.notifyActiveAlerts(currentTime);
        }
        scheduleNextTick();
    }

    /**
     *
     * @return returns true if a new second has started since the last time this method has been called
     */
    private boolean newSecondHasStarted()
    {
        DateTime currentTime = new DateTime();
        if(currentSecond!=currentTime.getSecondOfMinute())
        {
            currentSecond=currentTime.getSecondOfMinute();
            return true;
        }
        return false;
    }

    /**
     * Schedules the next time this class is called. This is like the clockwork of this entire thing.
     *
     */
    private void scheduleNextTick()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, 100); //since timing is not all that reliable on most machines we call this more than once per second.
        Intent intent = new Intent(this, ClockWorkService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, AlarmClockConstants.TICK, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
    }
}
