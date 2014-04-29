package de.lukeslog.alarmclock.actions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 31.03.14.
 */
public abstract class AmbientAction
{
    protected String actionName;
    protected String actionID;
    protected boolean joinSnoozing=false;

    public static final String COUNTDOWN_ACTION = CountdownAction.class.toString();
    public static final String SENDMAIL_ACTION = SendMailAction.class.toString();
    public static final String PHILIPSHUE_ACTION = PhilipsHueAction.class.toString();
    public static final String MUSIC_ACTION = MusicAction.class.toString();
    public static final String EZCONTROLPLUG_ACTION = EZControlPlugAction.class.toString();
    public static final String EZCONTROLHEAT_ACTION = EZControlHeatAction.class.toString();
    public static final String WEBSITE_ACTION = WebsiteAction.class.toString();


    public static final int ACTION_UI_PRIORITY_HIGH = 1;
    public static final int ACTION_UI_PRIORITY_MEDIUM=2;
    public static final int ACTION_UI_PRIORITY_LOW=3;
    public static final int ACTION_UI_PRIORITY_NONE=0;

    public static String TAG = AlarmClockConstants.TAG;

    public static int priority=ACTION_UI_PRIORITY_NONE;

    AmbientAction(String actionName)
    {
        this.actionName=actionName;
        DateTime now = new DateTime();
        actionID = "action"+actionName+""+now.getMillis();
        ActionManager.addNewAction(this);
    }

    public AmbientAction(ActionConfigBundle configBundle)
    {
        Log.d(TAG, "Ambient Action from config Bunlde... ");
        this.actionName = configBundle.getString("actionName");
        if(actionName!=null)
        {
            Log.d(TAG, actionName);
        }
        else
        {
            actionName="New Action.";
            Log.d(TAG, actionName);
        }
        this.actionID = configBundle.getString("actionID");
        String js = configBundle.getString("joinSnoozing");
        if(js!=null)
        {
            this.joinSnoozing = js.equals("1");
        }
        else
        {
            this.joinSnoozing=false;
        }
        if(actionID!=null)
        {
            Log.d(TAG, actionID);
        }
        else
        {
            DateTime now = new DateTime();
            actionID = "action"+actionName+""+now.getMillis();
        }
    }


    public boolean isJoinSnoozing()
    {
        return joinSnoozing;
    }

    public void setJoinSnoozing(boolean joinSnoozing)
    {
        this.joinSnoozing = joinSnoozing;
    }

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public abstract void action(boolean isFirstAlert);

    public abstract void snooze();

    public abstract void awake();

    public abstract void tick(DateTime currentTime);

    public abstract int getPriority();

    public abstract void defineSettingsView(LinearLayout configurationActivity, AmbientAlarm alarm);

    public ActionConfigBundle getConfigurationData()
    {
        ActionConfigBundle configBundle = setConfigurationData();
        configBundle.putString("actionName", actionName);
        configBundle.putString("actionID", actionID);
        if(joinSnoozing)
        {
            configBundle.putString("joinSnoozing", "1");
        }
        else
        {
            configBundle.putString("joinSnoozing", "0");
        }
        return configBundle;
    }

    protected abstract ActionConfigBundle setConfigurationData();

    protected void openConfigurationActivity(Context ctx, AmbientAlarm alarm)
    {
        Intent openconfigactivity = new Intent(ctx, ActionActivity.class);
        openconfigactivity.putExtra("ambientActionID", actionID);
        openconfigactivity.putExtra("ambientAlarmID", alarm.getAlarmID());
        ctx.startActivity(openconfigactivity);
    }

    protected void deleteActionDialog(Context ctx, final AmbientAlarm alarm)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setMessage(R.string.deleteaction)
                .setTitle(R.string.deleteaction);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                Log.d(TAG, "delete "+AmbientAction.this.getActionID());
                alarm.unregisterAction(AmbientAction.this);
                AmbientAlarmManager.updateDataBaseEntry(alarm);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected LinearLayout createLayout(final LinearLayout configView, final AmbientAlarm alarm)
    {
        LinearLayout mainLayout = new LinearLayout(configView.getContext());
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setMinimumHeight(40);
        mainLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Log.d(TAG, "click");
                openConfigurationActivity(configView.getContext(), alarm);
            }
        });
        mainLayout.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                //Log.d(TAG, "lonklick");
                deleteActionDialog(configView.getContext(), alarm);
                return true;
            }
        });
        return mainLayout;
    }

    public abstract Class getConfigActivity();

    public abstract void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity);

    public String getActionID()
    {
        return actionID;
    }
}
