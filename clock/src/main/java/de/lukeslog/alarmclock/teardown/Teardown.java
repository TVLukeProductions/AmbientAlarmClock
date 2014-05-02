package de.lukeslog.alarmclock.teardown;

import android.content.Context;
import android.content.Intent;

import de.lukeslog.alarmclock.ChromeCast.ChromeCastService;
import de.lukeslog.alarmclock.MediaPlayer.MediaPlayerService;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.startup.NotificationService;

/**
 * Created by lukas on 29.04.14.
 */
public class Teardown
{
    public static void stopAll(Context ctx)
    {

        //STOP MUSIC PLAYER
        Intent stopmusic = new Intent();
        stopmusic.setAction(MediaPlayerService.ACTION_TURN_MEDIASERVICE_PERMNENTLY_OFF);
        ClockWorkService.getClockworkContext().sendBroadcast(stopmusic);
        //STOP NOTIFICATION
        NotificationService.stop();
        ChromeCastService.stop();
        //STOP CLOCKWORK
        ClockWorkService.stopService();
    }
}
