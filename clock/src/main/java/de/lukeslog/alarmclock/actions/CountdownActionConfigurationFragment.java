package de.lukeslog.alarmclock.actions;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 06.04.14.
 */
public class CountdownActionConfigurationFragment extends Fragment
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    AmbientAlarm alarm;

    /** Called when the activity is first created. */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View fragment = inflater.inflate(R.layout.countdown_action_activity, container, false);

        //well... this is kinda evil.
        ActionActivity parent = (ActionActivity) getActivity();
        final CountdownAction action = (CountdownAction) parent.getAction();
        alarm = parent.getAlarm();

        int seconds =action.getCountDownDuration();

        final EditText countdownTextEdit = (EditText) fragment.findViewById(R.id.countdownlengthtext);
        countdownTextEdit.setText("" + (seconds / 60));
        countdownTextEdit.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                action.setDurationInSeconds(Integer.parseInt(countdownTextEdit.getEditableText().toString()));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        return fragment;
    }
}
