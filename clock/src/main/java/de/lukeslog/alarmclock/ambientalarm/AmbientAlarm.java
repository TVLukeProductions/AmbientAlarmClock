package de.lukeslog.alarmclock.ambientalarm;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import de.lukeslog.alarmclock.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.lukeslog.alarmclock.actions.AmbientAction;
import de.lukeslog.alarmclock.alarmactivity.AmbientAlarmActivity;
import de.lukeslog.alarmclock.datatabse.AmbientAlarmDatabase;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.AlarmState;

/**
 * Created by lukas on 31.03.14.
 *
 * An AmbientAlarm is a Class that stores the settings for a user generated alert. It contains the
 * time as well as information about on what days the alert is to be done.
 *
 * Every alert has its own settings, which contains information about the services to be released
 * depending on the alert time.
 */
public class AmbientAlarm
{
    private static String TAG = AlarmClockConstants.TAG;

    private boolean active=false;
    private boolean snoozing = false;
    private int snoozeTimeInSeconds = 500;
    private int snoozeButtonPressedCounter=0;
    private DateTime alarmTime = new DateTime();
    private DateTime lastAlarmTime = new DateTime();
    private boolean[] weekdays = new boolean[7];
    private String alarmID="";

    private int alarmState = AlarmState.WAITING;
    private HashMap<String, ArrayList<AmbientAction>> registeredActions = new HashMap<String, ArrayList<AmbientAction>>();

    //is the alarm active
    public boolean isActive()
    {
        return active;
    }

    public AmbientAlarm()
    {
        DateTime now = new DateTime();
        long milis = now.getMillis();
        this.alarmID = "ambientalarm"+milis;
        Log.d(TAG, "ALARM ID="+alarmID);
    }

    public AmbientAlarm(String alarmID)
    {
        this.alarmID=alarmID;
    }

    public String getAlarmID()
    {
        return this.alarmID;
    }

    //activate the alarm
    public void setActive(boolean active)
    {
        this.active = active;
        setAlarmTime(alarmTime);
    }

    public boolean getActiveForDayOfTheWeek(int weekday)  throws ArrayIndexOutOfBoundsException
    {
        return weekdays[weekday];
    }
    //is snoozing active
    public boolean isSnoozing()
    {
        return snoozing;
    }

    // activate or deactivate snoozing
    public void setSnoozing(boolean snoozing)
    {
        this.snoozing = snoozing;
    }

    //get the set snooze time
    public int getSnoozeTimeInSeconds()
    {
        return snoozeTimeInSeconds;
    }

    //set the snooze Time
    public void setSnoozeTimeInSeconds(int snoozeTimeInSeconds)
    {
        this.snoozeTimeInSeconds = snoozeTimeInSeconds;
    }

    //get the Time of the Alarm
    public DateTime getAlarmTime()
    {
        return alarmTime;
    }

    //set the Alarm to a new time
    public void setAlarmTime(DateTime alarmTime)
    {
        DateTime newalertTime = new DateTime();
        newalertTime = newalertTime.withSecondOfMinute(0);
        newalertTime = newalertTime.withHourOfDay(alarmTime.getHourOfDay());
        newalertTime = newalertTime.withMinuteOfHour(alarmTime.getMinuteOfHour());
        this.alarmTime = newalertTime;
        this.lastAlarmTime=newalertTime;
        DateTime now = new DateTime();
        if(Seconds.secondsBetween(alarmTime, now).getSeconds()>0)
        {
            Log.d(TAG, "alarm is in the past");
            Log.d(TAG, this.alarmTime.toString());
        }
        else
        {
            Log.d(TAG, "alarm is in the future... the future charlie");
            this.alarmTime = this.alarmTime.minusDays(1);
        }
        setToNextAlarmTime();
        this.lastAlarmTime = this.alarmTime;
    }

    public int secondsSinceAlertTime(DateTime currentTime)
    {
        if(Seconds.secondsBetween(lastAlarmTime, currentTime).getSeconds()<0)
        {
            return -1;
        }
        return (Seconds.secondsBetween(lastAlarmTime, currentTime).getSeconds());
    }

    public int secondsToAlertTime(DateTime currentTime)
    {
        return (Seconds.secondsBetween(currentTime, alarmTime).getSeconds());
    }

    public void registerAction(String relativeTime, AmbientAction action)
    {
        Log.d(TAG, "register action...");
        if(registeredActions.containsKey(relativeTime))
        {
            Log.d(TAG, "old relative time...");
            ArrayList<AmbientAction> actions = registeredActions.get(relativeTime);
            actions.add(action);
            registeredActions.put(relativeTime, actions);
        }
        else
        {
            Log.d(TAG, "new relative time");
            ArrayList<AmbientAction> actions = new ArrayList<AmbientAction>();
            actions.add(action);
            registeredActions.put(relativeTime, actions);
        }
    }
    //activate or deactivate the alarm on a day
    public void setAlarmStateForDay(int day, boolean state) throws ArrayIndexOutOfBoundsException
    {
        Log.d(TAG, "update AlarmforDay "+day);
        weekdays[day]=state;
        setAlarmTime(alarmTime);
    }

    public void snoozeButtonPressed()
    {
        Log.d(TAG, "snoozeButton");
        alarmState=AlarmState.SNOOZING;
        //TODO: go through registered actions and deactivate those that deactivate on snooze
        incrementSnoozeButtonCounter();
    }

    public void notifyOfCurrentTime(DateTime currentTime)
    {
        Log.d(TAG, "Seconds since last Alert: "+secondsSinceAlertTime(currentTime));
        Log.d(TAG, "Seconds to next Alert: "+secondsToAlertTime(currentTime));
        performActionIfActionIsRequired(currentTime);
        if(secondsToAlertTime(currentTime)==0)
        {
            setToNextAlarmTime();
        }
        Log.d(TAG, "" + alarmTime.getDayOfWeek());
        Log.d(TAG, alarmTime.toString());
        //TODO: Check if any Service has registered to be started on that distance.
    }

    public HashMap<String, ArrayList<AmbientAction>> getRegisteredActions()
    {
        return registeredActions;
    }

    private void performActionIfActionIsRequired(DateTime currentTime)
    {
        //an action has been registered to be performed x seconds before alert
        if(registeredActions.containsKey("-"+secondsToAlertTime(currentTime)))
        {
            Log.d(TAG, "jap1");
            performActions("-" + secondsToAlertTime(currentTime));
        }
        //an action has been registered to be performed x seconds after
        if(registeredActions.containsKey("+"+secondsSinceAlertTime(currentTime)))
        {
            Log.d(TAG, "jap2");
            performActions("+"+secondsSinceAlertTime(currentTime));
        }
        //action registered on alert
        if(secondsSinceAlertTime(currentTime)==snoozeTimeInSeconds*snoozeButtonPressedCounter)
        {
            alert();
            Log.d(TAG, "ALAAAAAARRM");
            if(registeredActions.containsKey("0"))
            {
                performActions("0");
            }
        }
    }

    private void performActions(String s)
    {
        Log.d(TAG, "performactions..."+s);
        ArrayList<AmbientAction> actions = registeredActions.get(s);
        Log.d(TAG, ""+actions.size());
        for(AmbientAction action : actions)
        {
            Log.d(TAG, "...");
            if(isFirstAlert())
            {
                Log.d(TAG, "is first action...");
                action.action();
            }
            else
            {

            }
        }
    }

    private boolean isFirstAlert()
    {
        return snoozeButtonPressedCounter==0;
    }

    public void awakeButtonPressed()
    {
        Log.d(TAG, "awakeButton");
        alarmState = AlarmState.WAITING;
        resetSnoozeButtonCounter();
        //TODO: go through registered actions and deactivate those that deactivate on awake
    }

    /**
     * This s the method to be called if it time for an alert
     */
    private void alert()
    {
        if(alarmState == AlarmState.WAITING || alarmState == AlarmState.SNOOZING )
        {
            alarmState=AlarmState.ALARM;
            if (isFirstAlert())
            {
                firstAlert();
            }
            else
            {
                reAltert();
            }
        }
    }


    private void reAltert()
    {
        if(alarmState == AlarmState.SNOOZING)
        {
            AmbientAlarmManager.startAlarmActivity(this);
        }
    }

    private void firstAlert()
    {
        AmbientAlarmManager.startAlarmActivity(this);
    }

    private void incrementSnoozeButtonCounter()
    {
        snoozeButtonPressedCounter++;
    }

    private void resetSnoozeButtonCounter()
    {
        snoozeButtonPressedCounter=0;
    }

    private void setToNextAlarmTime()
    {
        lastAlarmTime = alarmTime; //TODO: This should not be in this method
        //now we find the next day (which may be today) when this alert is applicable.
        for(int i=0; i<7; i++)
        {
            alarmTime = alarmTime.plusDays(1);
            Log.d(TAG, "check this date"+alarmTime.dayOfWeek().getAsShortText());
            int day = alarmTime.getDayOfWeek()-1;
            if(weekdays[day])
            {
                Log.d(TAG, "yap");
                break;
            }
            Log.d(TAG, "nope...");
        }

    }

    public void fillInActionView(LinearLayout scrollView)
    {
        Set<String> keys = registeredActions.keySet();
        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
            for(AmbientAction action: actions)
            {
                action.defineSettingsView(scrollView);
            }
        }
   }

    public Class<AmbientAlarmActivity> getAlarmActivity()
    {
        //Currently there is only one But there may be more in the future
        return AmbientAlarmActivity.class;
    }

    public void updateAlarmUI(AmbientAlarmActivity alarmActivity)
    {
        Set<String> keys = registeredActions.keySet();
        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
            for(AmbientAction action: actions)
            {
                action.updateUI(this, alarmActivity);
            }
        }
    }


    public DateTime getLastAlarmTime()
    {
        return lastAlarmTime;
    }

    public int getStatus()
    {
        return alarmState;
    }
}
