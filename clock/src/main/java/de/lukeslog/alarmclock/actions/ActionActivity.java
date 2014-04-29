package de.lukeslog.alarmclock.actions;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.UISupport;
import de.lukeslog.alarmclock.R;

/**
 * Created by lukas on 03.04.14.
 */
public class ActionActivity extends Activity
{
    public static String TAG = AlarmClockConstants.TAG;
    Context ctx;
    AmbientAlarm alarm;
    AmbientAction action;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_configuration_base_activity);

        ctx = this;

        String alarmID = getIntent().getStringExtra("ambientAlarmID");
        alarm = AmbientAlarmManager.getAlarmById(alarmID);

        String actionID = getIntent().getStringExtra("ambientActionID");
        action = ActionManager.getActionByID(actionID);

        Log.d(TAG, "ACTIONACTIVITE_>_>_>_>_>_>_ "+action.getActionName());
        setAlarmHeader();

        setJoinSnoozingCheckBox();

        setActionName();

        setTiming();

        insertActionConfigrationFRagment();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        ActionActivity.this.finish();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        AmbientAlarmManager.updateDataBaseEntry(alarm);
    }

    private void setJoinSnoozingCheckBox()
    {
        final CheckBox joinSnoozing = (CheckBox) findViewById(R.id.joinsnooze);
        joinSnoozing.setChecked(action.isJoinSnoozing());
        joinSnoozing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                action.setJoinSnoozing(isChecked);
            }
        });
    }

    private void setActionName()
    {
        final TextView actionname = (TextView) findViewById(R.id.actionname);
        actionname.setText(action.getActionName());
        actionname.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                action.setActionName(actionname.getEditableText().toString());
            }
        });
    }

    public AmbientAction getAction()
    {
        return action;
    }

    private void insertActionConfigrationFRagment()
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        try
        {
            Fragment configFragment = (Fragment) action.getConfigActivity().newInstance();
            fragmentTransaction.add(R.id.configspace, configFragment);
            fragmentTransaction.commit();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void setTiming()
    {

        String timing = getTimingValue();
        Log.d(TAG, "TIMING VALUE-------------->"+timing);
        timing = timing.replace("-", "");
        timing = timing.replace("+", "");
        timing = timing.replace(" ", "");
        final TextView timingText = (TextView) findViewById(R.id.timingtext);

        timingText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                reregisteraction();
            }
        });
        int t = Integer.parseInt(timing);

        final Spinner timeunits = (Spinner) findViewById(R.id.timeunits);
        List<String> list2 = new ArrayList<String>();
        list2.add("seconds");
        list2.add("minutes");
        list2.add("hours");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list2);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeunits.setAdapter(dataAdapter2);
        timeunits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                reregisteraction();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        if(t>60 && t%60==0)
        {
            t=t/60;
            timeunits.setSelection(1);
        }
        if(t>60 && t%60==0)
        {
            t=t/60;
            timeunits.setSelection(2);
        }


        Spinner beforeafter = (Spinner) findViewById(R.id.beforeafter);
        List<String> list = new ArrayList<String>();
        list.add("before");
        list.add("after");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beforeafter.setAdapter(dataAdapter);
        beforeafter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                reregisteraction();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        if(getTimingValue().startsWith("+"))
        {
            beforeafter.setSelection(1);
        }

        timing=""+t;
        timingText.setText(timing);
    }

    private void reregisteraction()
    {
        Log.d(TAG, ">>>>> reregisteraction");
        Spinner beforeafter = (Spinner) findViewById(R.id.beforeafter);
        Spinner timeunits = (Spinner) findViewById(R.id.timeunits);
        TextView timingText = (TextView) findViewById(R.id.timingtext);
        try
        {
            Log.d(TAG, ">>>>> TRY to get Timing");
            int timing = Integer.parseInt(timingText.getEditableText().toString());
            Log.d(TAG, "timing="+timing);
            if (timeunits.getSelectedItemPosition() == 1)
            {
                timing = timing*60;
            }
            if(timeunits.getSelectedItemPosition() == 2)
            {
                timing=timing*60*60;
            }
            Log.d(TAG, "reclaculated timing="+timing);
            String s = ""+timing;
            if(timing!=0)
            {
                if (beforeafter.getSelectedItemPosition() == 0)
                {
                    s = "-" + s;
                } else
                {
                    s = "+" + s;
                }
            }
            Log.d(TAG, "s = "+s);
            alarm.registerAction(s, action);
        }
        catch (Exception e)
        {

        }
    }

    private void setAlarmHeader()
    {

        String alarmTime = UISupport.getTimeAsString(alarm.getAlarmTime());

        TextView alarmInformation = (TextView) findViewById(R.id.alarminformation);
        alarmInformation.setText(alarmTime);

        ImageView alarmicon = (ImageView) findViewById(R.id.alarmicon);
        if(alarm.isActive())
        {
            if(alarm.isSnoozing())
            {
                alarmicon.setImageResource(R.drawable.alarmicon_a_s);
                if(alarm.isCurrentlyLocked())
                {
                    alarmicon.setImageResource(R.drawable.alarmicon_a_s_l);
                }
            }
            else
            {
                alarmicon.setImageResource(R.drawable.alarmicon_a);
                if(alarm.isCurrentlyLocked())
                {
                    alarmicon.setImageResource(R.drawable.alarmicon_a_l);
                }
            }
        }
        else
        {
            alarmicon.setImageResource(R.drawable.alarmicon_b);
        }

    }

    private String getTimingValue()
    {
        return calculateTimingValue();
    }

    private String calculateTimingValue()
    {
        HashMap<String, ArrayList<AmbientAction>> registeredActions = alarm.getRegisteredActions();
        Set<String> keys = registeredActions.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
            for (AmbientAction action : actions)
            {
                if(action.getActionID().equals(this.action.getActionID()))
                {
                    //Log.d(TAG, "found the action at: "+actiontime);
                    return actiontime;
                }
            }
        }
        return "0";
    }

    public AmbientAlarm getAlarm()
    {
        return alarm;
    }
}
