package de.lukeslog.alarmclock.startup;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import de.lukeslog.alarmclock.main.NotificationManagement;

/**
 * Created by lukas on 31.03.14.
 */
public class NotificationService extends Service
{
    private static NotificationService ctxs;


    private static void startPermanentNotification()
    {
        NotificationManagement.setAlarmClockIcon(ctxs);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        ctxs=this;
        startPermanentNotification();
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

    }

    @Override
    public void onDestroy()
    {
        stopPermanentNotification();
        super.onDestroy();
    }

    private void stopPermanentNotification()
    {
        NotificationManagement.stopAlarmClockIcon(ctxs);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public static void stop()
    {
        if(ctxs!=null)
        {
            ctxs.stopEverything();
        }
    }

    private void stopEverything()
    {
        stopPermanentNotification();
        stopSelf();
    }

    public static NotificationService getNotificationContext()
    {
        if(ctxs!=null)
        {
            return ctxs;
        }
        return null;
    }
}
