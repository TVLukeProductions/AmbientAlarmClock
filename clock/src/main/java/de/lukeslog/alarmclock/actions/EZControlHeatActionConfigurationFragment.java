package de.lukeslog.alarmclock.actions;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 29.04.14.
 */
public class EZControlHeatActionConfigurationFragment extends Fragment
{
    public static String TAG = AlarmClockConstants.TAG;

    EZControlHeatAction action;
    AmbientAlarm alarm;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View fragment = inflater.inflate(R.layout.ezcontrolheater_action_configuration_activity, container, false);

        ActionActivity parent = (ActionActivity) getActivity();
        action = (EZControlHeatAction) parent.getAction();
        alarm = parent.getAlarm();

        setIPTextView(fragment);

        setHeaterNumberView(fragment);

        setHeaterLevelView(fragment);

        return fragment;
    }

    private void setHeaterLevelView(View fragment)
    {
        final Spinner heaterlevelspinner = (Spinner) fragment.findViewById(R.id.heaterlevel);
        List<String> list2 = new ArrayList<String>();
        list2.add("1");
        list2.add("2");
        list2.add("3");
        list2.add("4");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list2);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        heaterlevelspinner.setAdapter(dataAdapter2);
        heaterlevelspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == 0)
                {
                    action.setLevel("1");
                } else if (position == 1)
                {
                    action.setLevel("2");
                } else if (position == 2)
                {
                    action.setLevel("3");
                } else
                {
                    action.setLevel("4");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        if(action.getLevel().equals("1"))
        {
            heaterlevelspinner.setSelection(0);
        }
        else if(action.getLevel().equals("2"))
        {
            heaterlevelspinner.setSelection(1);
        }
        else if(action.getLevel().equals("3"))
        {
            heaterlevelspinner.setSelection(2);
        }
        else
        {
            heaterlevelspinner.setSelection(3);
        }

    }

    private void setIPTextView(View fragment)
    {
        EditText ezcontrolip = (EditText) fragment.findViewById(R.id.ezcontrolip2);
        ezcontrolip.setText(action.getEzControlIP());
        ezcontrolip.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                action.setEzControlIP(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });
    }

    public void setHeaterNumberView(View fragment)
    {
        EditText heaternumber = (EditText) fragment.findViewById(R.id.heaternumber);
        heaternumber.setText(action.getHeaternumber());
        heaternumber.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                action.setHeaternumber(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });
    }
}
