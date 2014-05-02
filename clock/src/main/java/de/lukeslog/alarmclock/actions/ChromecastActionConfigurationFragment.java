package de.lukeslog.alarmclock.actions;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.lukeslog.alarmclock.ChromeCast.ChromeCastService;
import de.lukeslog.alarmclock.MediaPlayer.MediaPlayerService;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 02.05.14.
 */
public class ChromecastActionConfigurationFragment extends Fragment
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    ChromecastAction action;
    AmbientAlarm alarm;

    /** Called when the activity is first created. */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View fragment = inflater.inflate(R.layout.action_chromecast_configuration_actifivity, container, false);

        //well... this is kinda evil.
        final ActionActivity parent = (ActionActivity) this.getActivity();
        action = (ChromecastAction) parent.getAction();
        alarm = parent.getAlarm();

        Button testbutton = (Button) fragment.findViewById(R.id.cctest);
        testbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent finddevices = new Intent();
                finddevices.setAction(ChromeCastService.ACTION_FIND_DEVICES);
                finddevices.putExtra("AmbientActionID", action.getActionID());
                parent.getApplicationContext().sendBroadcast(finddevices);
            }
        });

        return fragment;
    }
}
