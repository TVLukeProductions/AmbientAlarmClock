package de.lukeslog.alarmclock.actions;

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
        return null;
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
        descriptionCountdownAction.put("actionType", AmbientAction.COUNTDOWN_ACTION);
        descriptionCountdownAction.put("actionName", "Countdown Action");
        descriptionCountdownAction.put("actionIcon", "countdown_action_icon");
        descriptionCountdownAction.put("description", "ololroflcopter");

        HashMap<String, String> descriptionSendMailAction = new HashMap<String, String>();
        descriptionSendMailAction.put("actionType", AmbientAction.SENDMAIL_ACTION);
        descriptionSendMailAction.put("actionName", "Send Mail Action");
        descriptionSendMailAction.put("actionIcon", "send_mail_action_icon");
        descriptionSendMailAction.put("description", "send a mail.");

        actionInformation.add(descriptionCountdownAction);
        actionInformation.add(descriptionSendMailAction);

        return actionInformation;
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
        return null;
    }
}
