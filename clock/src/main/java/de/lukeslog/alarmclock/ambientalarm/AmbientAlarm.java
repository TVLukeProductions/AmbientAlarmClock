package de.lukeslog.alarmclock.ambientalarm;

import android.widget.LinearLayout;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import de.lukeslog.alarmclock.actions.AmbientAction;
import de.lukeslog.alarmclock.main.Timable;
import de.lukeslog.alarmclock.support.Logger;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;
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
public class AmbientAlarm implements Timable
{
    private static String TAG = AlarmClockConstants.TAG;

    private boolean active=false;
    private boolean snoozing = false;
    private boolean lock = false;
    private int snoozeTimeInMinutes = 5;
    private int snoozeButtonPressedCounter=0;
    private DateTime alarmTime = new DateTime();
    private DateTime snoozealert;
    private DateTime lastAlarmTime = new DateTime();
    private boolean[] weekdays = new boolean[7];
    private String alarmID="";

    private int alarmState = AlarmState.WAITING;
    private HashMap<String, ArrayList<AmbientAction>> registeredActions = new HashMap<String, ArrayList<AmbientAction>>();
    private HashMap<String, AmbientAction> prioritylist = new HashMap<String, AmbientAction>();

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
        Logger.d(TAG, "ALARM ID=" + alarmID);
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

    public boolean islocked()
    {
        return lock;
    }

    public void setLocked(boolean l)
    {
        lock=l;
    }
    // activate or deactivate snoozing
    public void setSnoozing(boolean snoozing)
    {
        this.snoozing = snoozing;
    }

    //get the set snooze time
    public int getSnoozeTimeInMinutes()
    {
        return snoozeTimeInMinutes;
    }

    //set the snooze Time
    public void setSnoozeTimeInMinutes(int snoozeTimeInMinutes)
    {
        this.snoozeTimeInMinutes = snoozeTimeInMinutes;
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
            Logger.d(TAG, "alarm is in the past");
            Logger.d(TAG, this.alarmTime.toString());
        }
        else
        {
            Logger.d(TAG, "alarm is in the future... the future charlie");
            this.alarmTime = this.alarmTime.minusDays(1);
        }
        setToNextAlarmTime();
        this.lastAlarmTime = this.alarmTime;
    }

    public int minutesSinceAlertTime(DateTime currentTime)
    {
        if(Minutes.minutesBetween(lastAlarmTime, currentTime).getMinutes()<0)
        {
            return -1;
        }
        return (Minutes.minutesBetween(lastAlarmTime, currentTime).getMinutes());
    }

    public int minutesToAlertTime(DateTime currentTime)
    {

        return (Minutes.minutesBetween(currentTime, alarmTime).getMinutes());
    }

    public int secondsToAlertTime(DateTime currentTime)
    {
        return (Seconds.secondsBetween(currentTime, alarmTime).getSeconds());
    }

    private int minutesToSnoozeAlertTime(DateTime currentTime)
    {
        if(snoozealert!=null)
        {
            //Log.d(TAG, "secondstonooze->"+Seconds.secondsBetween(currentTime, snoozealert).getSeconds());
            int x = Minutes.minutesBetween(snoozealert, currentTime).getMinutes();
            return x;
        }
        else
        {
            return -1; //TODO: should throw error
        }
    }

    private void setSnoozeAlertTime()
    {
        DateTime now = new DateTime();
        Logger.d(TAG, "alarmtime + "+(minutesSinceAlertTime(now)+snoozeTimeInMinutes));
        int adding = minutesSinceAlertTime(now)+snoozeTimeInMinutes;
        snoozealert = lastAlarmTime.plusMinutes(adding);
        Logger.d(TAG, "New SnoozeAlert: "+snoozealert.getHourOfDay()+":"+snoozealert.getMinuteOfHour());
    }

    public void registerAction(String relativeTime, AmbientAction action)
    {
        Logger.d(TAG, "register action... "+action.getActionName()+" "+relativeTime);
        unregisterAction(action); //delete from its old timing if it exists...
        if(registeredActions.containsKey(relativeTime))
        {
            Logger.d(TAG, "old relative time...");
            ArrayList<AmbientAction> actions = registeredActions.get(relativeTime);
            actions.add(action);
            registeredActions.put(relativeTime, actions);
        }
        else
        {
            Logger.d(TAG, "new relative time");
            ArrayList<AmbientAction> actions = new ArrayList<AmbientAction>();
            actions.add(action);
            registeredActions.put(relativeTime, actions);
        }
    }
    //activate or deactivate the alarm on a day
    public void setAlarmStateForDay(int day, boolean state) throws ArrayIndexOutOfBoundsException
    {
        Logger.d(TAG, "update AlarmforDay "+day);
        weekdays[day]=state;
        setAlarmTime(alarmTime);
    }

    public void snoozeButtonPressed()
    {
        Logger.d(TAG, "snoozeButton");
        alarmState=AlarmState.SNOOZING;
        Set<String> keys = registeredActions.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
            for (AmbientAction action : actions)
            {
                action.snooze();
            }
        }
        incrementSnoozeButtonCounter();
        setSnoozeAlertTime();
    }

    public void notifyOfCurrentTime(DateTime currentTime)
    {
        Logger.i(TAG, "Alarm "+alarmID+" notified of the time.");
        Logger.i(TAG, "State of  "+alarmID+" = "+alarmState);
        Logger.d(TAG, "Minutes since last Alert: "+ minutesSinceAlertTime(currentTime));
        Logger.d(TAG, "Minutes to next Alert: "+ minutesToAlertTime(currentTime));
        performActionIfActionIsRequired(currentTime);
        if(minutesToAlertTime(currentTime)<=0)
        {
            setToNextAlarmTime();
        }
        //Log.d(TAG, "" + alarmTime.getDayOfWeek());
        //Log.d(TAG, alarmTime.toString());
    }

    public HashMap<String, ArrayList<AmbientAction>> getRegisteredActions()
    {
        return registeredActions;
    }

    private void performActionIfActionIsRequired(DateTime currentTime)
    {
        Logger.d(TAG, "performActionIfActionIsRequired");
        //an action has been registered to be performed x minutes before alert
        Logger.d(TAG, "do we have an action for: "+"-"+ minutesToAlertTime(currentTime));
        if(registeredActions.containsKey("-"+ minutesToAlertTime(currentTime)))
        {
            Logger.d(TAG, "jap1");
            performActions("-" + minutesToAlertTime(currentTime));
        }
        //an action has been registered to be performed x minutes after
        if(registeredActions.containsKey("+"+ minutesSinceAlertTime(currentTime)))
        {
            Logger.d(TAG, "jap2");
            performActions("+"+ minutesSinceAlertTime(currentTime));
        }
        //action registered on alert
        if(minutesToAlertTime(currentTime)==0 || minutesToSnoozeAlertTime(currentTime)==0)
        {
            Logger.d(TAG, "ALAAAAAARRM");
            if(registeredActions.containsKey("0"))
            {
                try
                {
                    performActions("0");
                }
                catch(Exception e)
                {
                    Logger.e(TAG, "problem when calling perform actions"+e.getLocalizedMessage());
                }
            }
            alert();
        }
    }

    private void performActions(String s)
    {
        Logger.d(TAG, "performactions..."+s);
        ArrayList<AmbientAction> actions = registeredActions.get(s);
        if(s.equals("0") && alarmState!=AlarmState.ALARM || !s.equals("0"))
        {
            //Log.d(TAG, ""+actions.size());
            for(AmbientAction action : actions)
            {
                Logger.d(TAG, "...");
                if(isFirstAlert())
                {
                    Logger.d(TAG, "is first action...");
                    action.action(true);
                }
                else
                {
                    Logger.d(TAG, "is later action");
                    action.action(false);
                }
            }
        }

    }

    private boolean isFirstAlert()
    {
        return snoozeButtonPressedCounter==0;
    }

    public void awakeButtonPressed()
    {
        alarmState = AlarmState.WAITING;
        Logger.d(TAG, "awakeButton");
        Set<String> keys = registeredActions.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
            for (AmbientAction action : actions)
            {
                try
                {
                    action.awake();
                }
                catch(Exception e )
                {
                    Logger.e(TAG, "problem while pressing awake...");
                }
            }
        }
        resetSnoozeButtonCounter();
        snoozealert=null;
    }

    /**
     * This s the method to be called if it time for an alert
     */
    private void alert()
    {
        if(alarmState == AlarmState.WAITING || alarmState == AlarmState.SNOOZING )
        {
            Logger.d(TAG, "set AlarmState to ALARM!");
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
        Logger.d(TAG, "realert()");
        AmbientAlarmManager.startAlarmActivity(this);
    }

    private void firstAlert()
    {
        Logger.d(TAG, "first Alert");
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
            Logger.d(TAG, "check this date"+alarmTime.dayOfWeek().getAsShortText());
            int day = alarmTime.getDayOfWeek()-1;
            if(weekdays[day])
            {
                Logger.d(TAG, "yap");
                break;
            }
            Logger.d(TAG, "nope...");
        }

    }

    public void fillInActionView(LinearLayout scrollView)
    {
        Logger.d(TAG, "registered Actions Child Count = "+scrollView.getChildCount());
        scrollView.removeAllViews();
        for(int i=0; i<4; i++)
        {
            Set<String> keys = registeredActions.keySet();
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext())
            {
                String actiontime = iterator.next();
                ArrayList<AmbientAction> actions = registeredActions.get(actiontime);

                for (AmbientAction action : actions)
                {
                    Logger.d(TAG, action.getActionName());
                    if (action.getPriority() == i)
                    {
                        action.defineSettingsView(scrollView, this);
                    }
                }
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
        for(int i=1; i<4; i++)
        {
            Set<String> keys = registeredActions.keySet();
            Iterator<String> iterator = keys.iterator();
            while (iterator.hasNext())
            {
                String actiontime = iterator.next();
                ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
                for (AmbientAction action : actions)
                {
                    if (action.getPriority() == i)
                    {
                        Logger.d(TAG, "updateUI from Ambient Alarm to AlarmActivity...");
                        action.updateUI(this, alarmActivity);
                    }
                }
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

    public void unregisterAction(AmbientAction actionToDelete)
    {
        Set<String> keys = registeredActions.keySet();
        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
            for(int i=actions.size()-1; i>=0; i--)
            {
                AmbientAction action = actions.get(i);
                //Log.d(TAG, action.getActionID());
                if(action.getActionID().equals(actionToDelete.getActionID()))
                {
                    Logger.d(TAG, "  --> remove action.");
                    actions.remove(i);
                    break;
                }
            }
        }
    }

    public boolean isCurrentlyLocked()
    {
        DateTime now = new DateTime();
        if(islocked())
        {
            if(minutesSinceAlertTime(now)>-1 && minutesSinceAlertTime(now)<AlarmClockConstants.LOCKTIME)
            {
                return true;
            }
            if(minutesToAlertTime(now)>-1 && minutesToAlertTime(now)<AlarmClockConstants.LOCKTIME)
            {
                return true;
            }
        }
        else
        {
            return false;
        }
        return false;
    }

    public int numberOfRegisteredActions()
    {
        Set<String> keys = registeredActions.keySet();
        Iterator<String> iterator = keys.iterator();
        int number=0;
        while(iterator.hasNext())
        {
            String actiontime = iterator.next();
            ArrayList<AmbientAction> actions = registeredActions.get(actiontime);
            for(int i=0; i<actions.size(); i++)
            {
               number++;
            }
        }
        return number;
    }
}
