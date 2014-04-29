package de.lukeslog.alarmclock.actions;

import android.widget.LinearLayout;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;

/**
 * Created by lukas on 29.04.14.
 */
public class EZControlHeatAction extends AmbientAction
{
    String ezControlIP="192.168.1.242";
    String heaternumber="1";
    String level = "1";

    public EZControlHeatAction(String actionName)
    {
        super(actionName);
    }

    public EZControlHeatAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
    }

    @Override
    public void action(boolean isFirstAlert)
    {

    }

    @Override
    public void snooze()
    {

    }

    @Override
    public void awake()
    {

    }

    @Override
    public void tick(DateTime currentTime)
    {

    }

    @Override
    public void defineSettingsView(LinearLayout configurationActivity, AmbientAlarm alarm)
    {

    }

    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        return null;
    }

    @Override
    public Class getConfigActivity()
    {
        return EZControlHeatActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {

    }
}
