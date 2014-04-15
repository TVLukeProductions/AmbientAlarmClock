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
    public static String TAG = AlarmClockConstants.TAG;

    private static HashMap<String, AmbientAction> ambientActions = new HashMap<String, AmbientAction>();

    AmbientAction(String actionName)
    {
        this.actionName=actionName;
        DateTime now = new DateTime();
        actionID = "action_"+actionName+"_"+now.getMillis();
        ambientActions.put(actionID, this);
    }

    protected AmbientAction(Bundle configBundle)
    {
        this.actionName = configBundle.getString("actionName");
    }

    public abstract AmbientAction createFromBundle(Bundle configBundle);

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

    public Bundle getConfigurationData()
    {
        Bundle configBundle = setConfigurationData();
        configBundle.putString("actionName", actionName);
        return configBundle;
    }

    public static AmbientAction getActionByID(String actionID)
    {
        return ambientActions.get(actionID);
    }

    protected abstract Bundle setConfigurationData();

    public void openConfigurationActivity(Context ctx)
    {
        Intent openconfigactivity = new Intent(ctx, this.getConfigActivity());
        openconfigactivity.putExtra("ambientActionID", actionID);
        ctx.startActivity(openconfigactivity);
    }

    public abstract Class getConfigActivity();

    public abstract void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity);
}
