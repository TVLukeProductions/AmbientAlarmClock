package de.lukeslog.alarmclock.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.joda.time.DateTime;

import java.util.HashMap;

import de.lukeslog.alarmclock.alarmactivity.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 31.03.14.
 */
public abstract class AmbientAction
{
    protected String actionName;
    protected String actionID;

    public static final String COUNTDOWN_ACTION = CountdownAction.class.toString();
    public static final String SENDMAIL_ACTION = SendMailAction.class.toString();

    public static String TAG = AlarmClockConstants.TAG;

    AmbientAction(String actionName)
    {
        this.actionName=actionName;
        DateTime now = new DateTime();
        actionID = "action"+actionName+""+now.getMillis();
        ActionManager.addNewAction(this);
    }

    public AmbientAction(ActionConfigBundle configBundle)
    {
        this.actionName = configBundle.getString("actionName");
    }

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public abstract void action();

    public abstract void snooze();

    public abstract void awake();

    public abstract void defineSettingsView(LinearLayout configurationActivity);

    public ActionConfigBundle getConfigurationData()
    {
        ActionConfigBundle configBundle = setConfigurationData();
        configBundle.putString("actionName", actionName);
        return configBundle;
    }

    protected abstract ActionConfigBundle setConfigurationData();

    public void openConfigurationActivity(Context ctx)
    {
        Intent openconfigactivity = new Intent(ctx, this.getConfigActivity());
        openconfigactivity.putExtra("ambientActionID", actionID);
        ctx.startActivity(openconfigactivity);
    }

    public abstract Class getConfigActivity();

    public abstract void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity);

    public String getActionID()
    {
        return actionID;
    }
}
