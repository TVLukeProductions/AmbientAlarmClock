package de.lukeslog.alarmclock.ui;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 03.04.14.
 */
public class AmbientAlarmActivity extends Activity
{

    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    PowerManager.WakeLock wakeLock;
    AmbientAlarmActivity alarmActivity;

    AmbientAlarm alarm;

    private UIUpdater updater;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ambient_alarm_base_activity);
        Log.d(TAG, "AmbientAlarmActivity: onCreate");
        String alarmID = getIntent().getStringExtra("ambientAlarmID");
        alarm = AmbientAlarmManager.getAlarmById(alarmID);

        alarmActivity = this;

        turnScreenOnAndBright();

        configureSnoozeButton();
        configureAwakeButton();

        startUIUpdater();

    }

    private void startUIUpdater()
    {
        updater= new UIUpdater();
        updater.run();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        closeActivity();

    }

    @Override
    public void  onDestroy()
    {
        try
        {
            if(wakeLock!=null)
            {
                wakeLock.release();
            }
            if(updater!=null)
            {
                updater.onPause();
            }
        }
        catch(Exception e)
        {

        }
        super.onDestroy();
    }

    private void configureAwakeButton()
    {
        final Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                awakeButtonPressed();
                closeActivity();
            }
        });
    }

    private void closeActivity()
    {
        AmbientAlarmActivity.this.finish();
    }

    private void configureSnoozeButton()
    {
        final Button button = (Button) findViewById(R.id.button1);
        boolean showSnooze=alarm.isSnoozing();
        if (!showSnooze)
        {
            button.setVisibility(View.GONE);
        }
        else
        {

        }
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                snoozeButtonPressed();
                closeActivity();
            }
        });
    }

    private void turnScreenOnAndBright()
    {
        Log.d(TAG, "turnscreenonandbright");
        try
        {
            Log.e(TAG, "1");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            Log.e(TAG, "2");
            Log.e(TAG, "3");

        }
        catch(Exception e)
        {
            Log.e(TAG, "The Problem seems to be to turn on or unlock the screen");
        }
        Log.e(TAG, "a");
        final Window win = getWindow();
        Log.e(TAG, "b");
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        try
        {
            Log.e(TAG, "c");
            // API 8+
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            Log.e(TAG, "d");
            }
            catch (final Throwable whocares)
            {
                // API 7+
                Log.e(TAG, "c2");
                win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                Log.e(TAG, "d2");
            }
        try
        {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            Log.e(TAG, "4");
            wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            Log.e(TAG, "5");
            wakeLock.acquire();
        }
        catch(Exception e)
        {
            Log.d(TAG, "The power manager stuff does not work...");
            Log.e(TAG, e.getLocalizedMessage());
        }
        try
        {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = 1.0F;
            getWindow().setAttributes(params);

        }
        catch(Exception e)
        {

        }
        Log.e(TAG, ""+7);
    }

    private class  UIUpdater implements Runnable
    {
        private Handler handler = new Handler();
        public static final int delay= 500;

        @Override
        public void run()
        {
           Log.d(TAG, "Alarm Activity is running...");
           alarm.updateAlarmUI(alarmActivity);

           handler.removeCallbacks(this); // remove the old callback
           handler.postDelayed(this, delay); // register a new one
        }

        public void onPause()
        {
            Log.d(TAG, "Activity update on Pause ");
            handler.removeCallbacks(this); // stop updating
        }

        public void onResume()
        {
            handler.removeCallbacks(this); // remove the old callback
            handler.postDelayed(this, delay); // register a new one
        }

    }

    public void awakeButtonPressedRemotely()
    {
            AmbientAlarmActivity.this.awakeButtonPressed();
            AmbientAlarmActivity.this.closeActivity();
    }

    private void awakeButtonPressed()
    {
        alarm.awakeButtonPressed();
    }

    private void snoozeButtonPressed()
    {
        alarm.snoozeButtonPressed();
    }
}
