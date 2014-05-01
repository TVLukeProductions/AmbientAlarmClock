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
import android.widget.Button;
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
public class EZControlPlugActionConfigurationFragment extends Fragment
{
    public static String TAG = AlarmClockConstants.TAG;

    EZControlPlugAction action;
    AmbientAlarm alarm;

    /**
     * Called when the activity is first created.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View fragment = inflater.inflate(R.layout.eezontrolplug_action_configuration_activity, container, false);

        ActionActivity parent = (ActionActivity) getActivity();
        action = (EZControlPlugAction) parent.getAction();
        alarm = parent.getAlarm();

        setIPTextView(fragment);

        setPlugNumberTextView(fragment);

        setonoffspinner(fragment);

        setTestButton(fragment);

        return fragment;
    }

    private void setPlugNumberTextView(View fragment)
    {
        TextView plugnumber = (TextView) fragment.findViewById(R.id.plugnumber);
        plugnumber.setText(action.getPlugnumber());
        plugnumber.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                action.setPlugnumber(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });
    }

    private void setIPTextView(View fragment)
    {
        EditText ezcontrolip = (EditText) fragment.findViewById(R.id.ezcontrolip);
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

    private void setonoffspinner(View fragment)
    {
        final Spinner onoff = (Spinner) fragment.findViewById(R.id.onoff);
        List<String> list2 = new ArrayList<String>();
        list2.add("1");
        list2.add("2");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list2);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        onoff.setAdapter(dataAdapter2);
        onoff.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (position == 0)
                {
                    action.setTurnon(true);
                } else
                {
                    action.setTurnon(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        if(action.isTurnon())
        {
            onoff.setSelection(0);
        }
        else
        {
            onoff.setSelection(1);
        }
    }

    public void setTestButton(View fragment)
    {
        Button testbutton = (Button) fragment.findViewById(R.id.plugtestbutton);
        testbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                action.action(true);
            }
        });

    }
}
