package de.lukeslog.alarmclock.ui;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
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
        try
        {
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
            wakeLock.acquire();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            kl.disableKeyguard();
        }
        catch(Exception e)
        {
            Log.e(TAG, "The Problem seems to be to turn on or unlock the screen");
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
