package de.lukeslog.alarmclock.actions;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 29.04.14.
 */
public class WebsiteActionConfigurationFragment extends Fragment
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    AmbientAlarm alarm;
    WebsiteAction action;

    /** Called when the activity is first created. */
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View fragment = inflater.inflate(R.layout.action_website_configuration_activity, container, false);

        //well... this is kinda evil.
        ActionActivity parent = (ActionActivity) getActivity();
        action = (WebsiteAction) parent.getAction();
        alarm = parent.getAlarm();

        String url =action.getURL();

        final EditText websiteURLTextEdit = (EditText) fragment.findViewById(R.id.urltext);
        websiteURLTextEdit.setText(url);
        websiteURLTextEdit.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                action.setURL(websiteURLTextEdit.getEditableText().toString());
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
