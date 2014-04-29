package de.lukeslog.alarmclock.startup;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import de.lukeslog.alarmclock.main.NotificationManagement;

/**
 * Created by lukas on 31.03.14.
 */
public class NotificationService extends Service
{
    private static NotificationService ctx;


    private static void startPermanentNotification()
    {
        NotificationManagement.setAlarmClockIcon(ctx);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        ctx=this;
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
        NotificationManagement.stopAlarmClockIcon(ctx);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public static void stop()
    {
        if(ctx!=null)
        {
            ctx.stopEverything();
        }
    }

    private void stopEverything()
    {
        stopPermanentNotification();
        stopSelf();
    }

    public static NotificationService getContext()
    {
        if(ctx!=null)
        {
            return ctx;
        }
        return null;
    }
}
