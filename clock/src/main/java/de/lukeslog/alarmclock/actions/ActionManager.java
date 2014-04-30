package de.lukeslog.alarmclock.actions;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.ArrayList;

import de.lukeslog.alarmclock.datatabse.AmbientAlarmDatabase;

/**
 * Created by lukas on 15.04.14.
 */
public class ActionManager
{

    private static ArrayList<AmbientAction> actionList = new ArrayList<AmbientAction>();

    public static void updateActionList()
    {
        actionList = AmbientAlarmDatabase.getAllActionsFromDatabase();
    }

    public static AmbientAction createActionFromConfigBundle(ActionConfigBundle configBundle, String className)
    {
        //This method is lame but I don't want to do reflection and building a plugin infrastructre is
        // just a lot of work so the types are hardcoded... I'm sorry about that.
        if(className.equals(AmbientAction.COUNTDOWN_ACTION))
        {
            CountdownAction ca = new CountdownAction(configBundle);
            return ca;
        }
        if(className.equals(AmbientAction.SENDMAIL_ACTION))
        {
            SendMailAction sma = new SendMailAction(configBundle);
            return sma;
        }
        if(className.equals(AmbientAction.PHILIPSHUE_ACTION))
        {
            PhilipsHueAction pha = new PhilipsHueAction(configBundle);
            return pha;
        }
        if(className.equals(AmbientAction.MUSIC_ACTION))
        {
            MusicAction ma = new MusicAction(configBundle);
            return ma;
        }
        if(className.equals(AmbientAction.EZCONTROLPLUG_ACTION))
        {
            EZControlPlugAction ezpa = new EZControlPlugAction(configBundle);
            return ezpa;
        }
        if(className.equals(AmbientAction.EZCONTROLHEAT_ACTION))
        {
            EZControlHeatAction ezha = new EZControlHeatAction(configBundle);
            return ezha;
        }
        if(className.equals(AmbientAction.WEBSITE_ACTION))
        {
            WebsiteAction wa = new WebsiteAction(configBundle);
            return wa;
        }
        return null;
    }

    public static void notifyOfCurrentTime(DateTime currentTime)
    {
        for(AmbientAction action: actionList)
        {
            action.tick(currentTime);
        }
    }

    public static void addNewAction(AmbientAction ambientAction)
    {
        actionList.add(ambientAction);
    }

    /**
     *
     * @return
     */
    public static ArrayList<HashMap<String, String>> getActionTypes()
    {
        ArrayList<HashMap<String, String>> actionInformation = new ArrayList<HashMap<String, String>>();
       //CountDownAction
        HashMap<String, String> descriptionCountdownAction = new HashMap<String, String>();
        fillDescription(descriptionCountdownAction, AmbientAction.COUNTDOWN_ACTION, "Countdown Action", "countdown_action_icon", "Showing you a countdown when the alarm rings.");

        HashMap<String, String> descriptionSendMailAction = new HashMap<String, String>();
        fillDescription(descriptionSendMailAction, AmbientAction.SENDMAIL_ACTION, "Send Mail Action", "send_mail_action_icon", "sending an email at a specified time");

        HashMap<String, String> descriptionPhilipsHueAction = new HashMap<String, String>();
        fillDescription(descriptionPhilipsHueAction, AmbientAction.PHILIPSHUE_ACTION, "Philips Hue Action", "light_action_icon", "turning on the light when you wake up");

        HashMap<String, String> descriptionMusicAction = new HashMap<String, String>();
        fillDescription(descriptionMusicAction, AmbientAction.MUSIC_ACTION, "Music Action", "music_action_icon", "Playing Music on Wakeup.");

        HashMap <String, String> descriptionEZControlPlugAction = new HashMap<String, String>();
        fillDescription(descriptionEZControlPlugAction, AmbientAction.EZCONTROLPLUG_ACTION, "ezControl Plug Action", "action_icon", "Switching a plug using the ezControl Home Automation System.");

        HashMap<String, String> descriptionEZControlHeatAction = new HashMap<String, String>();
        fillDescription(descriptionEZControlHeatAction, AmbientAction.EZCONTROLHEAT_ACTION, "ezControl Radiator Action", "action_icon", "Controling The Radiator");

        HashMap<String, String> descriptionWebsiteAction = new HashMap<String, String>();
        fillDescription(descriptionWebsiteAction, AmbientAction.WEBSITE_ACTION, "Website Action", "action_icon", "Show a website on the alarm-screen.");


        actionInformation.add(descriptionCountdownAction);
        actionInformation.add(descriptionSendMailAction);
        actionInformation.add(descriptionPhilipsHueAction);
        actionInformation.add(descriptionMusicAction);
        actionInformation.add(descriptionEZControlPlugAction);
        actionInformation.add(descriptionWebsiteAction);
        actionInformation.add(descriptionEZControlHeatAction);

        return actionInformation;
    }

    private static void fillDescription(HashMap<String, String> map, String type, String name, String icon, String desc)
    {
        map.put("actionType", type);
        map.put("actionName", name);
        map.put("actionIcon", icon);
        map.put("description", desc);
    }

    public static AmbientAction getActionByID(String actionID)
    {
        for(AmbientAction action : actionList)
        {
            if(action.getActionID().equals(actionID))
            {
                return action;
            }
        }
        return null;
    }

    public static ArrayList<AmbientAction> getActionList()
    {
        return actionList;
    }

    public static AmbientAction createNewDefaultAction(String actionTypeName)
    {
        if(actionTypeName.equals(AmbientAction.COUNTDOWN_ACTION))
        {
            CountdownAction cda = new CountdownAction("New Count Down Action", 3600);
            return cda;
        }
        if(actionTypeName.equals(AmbientAction.SENDMAIL_ACTION))
        {
            SendMailAction sma = new SendMailAction("New Send Mail Action", "", "Subject", "Content");
            return sma;
        }
        if(actionTypeName.equals(AmbientAction.PHILIPSHUE_ACTION))
        {
            PhilipsHueAction pha = new PhilipsHueAction("New Philips Hue Action");
            return pha;
        }
        if(actionTypeName.equals(AmbientAction.MUSIC_ACTION))
        {
            MusicAction ma = new MusicAction("New Music Action");
            return ma;
        }
        if(actionTypeName.equals(AmbientAction.EZCONTROLPLUG_ACTION))
        {
            EZControlPlugAction ezpa = new EZControlPlugAction("new ezControl Plug Action");
            return ezpa;
        }
        if(actionTypeName.equals((AmbientAction.EZCONTROLHEAT_ACTION)))
        {
            EZControlHeatAction ezha = new EZControlHeatAction("New ezControl Radiator Action");
            return ezha;
        }
        if(actionTypeName.equals(AmbientAction.WEBSITE_ACTION))
        {
            WebsiteAction wa = new WebsiteAction("New Website Action", "http://www.tagesschau.de");
            return wa;
        }
        return null;
    }
}
