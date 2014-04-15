package de.lukeslog.alarmclock.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.ui.AlarmClockMainActivity;

/**
 * Created by lukas on 31.03.14.
 */
public class NotificationManagement
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    public static void setAlarmClockIcon(Context ctx)
    {
        Log.d(TAG, "setAlarmClockIcon");
        int icon = R.drawable.launchericon;
        Notification note=new Notification(icon, "Clock running", System.currentTimeMillis());//TODO: Text as variable
        Intent i=new Intent(ctx, AlarmClockMainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi=PendingIntent.getActivity(ctx, 0,
                i, 0);

        note.setLatestEventInfo(ctx, "AlarmActivity Clock", //TODO: Text as variable
                "...running", //TODO: Text as variable
                pi);
        //note.flags|=Notification.FLAG_AUTO_CANCEL;

        NotificationManager mNotificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(1337, note);
        //ctx.startForeground(1337, note);
    }
}
