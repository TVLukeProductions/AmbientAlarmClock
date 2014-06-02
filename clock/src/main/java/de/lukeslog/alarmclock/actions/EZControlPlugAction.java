package de.lukeslog.alarmclock.actions;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.Logger;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;

/**
 * Created by lukas on 29.04.14.
 */
public class EZControlPlugAction extends AmbientAction
{
    String ezControlIP="192.168.1.242";
    String plugnumber="1";
    boolean turnon=true;

    public EZControlPlugAction(String actionname)
    {
        super(actionname);
    }

    public EZControlPlugAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
        ezControlIP=configBundle.getString("ezControlIP");
        plugnumber=configBundle.getString("plugnumber");
        turnon=configBundle.getString("turnon").equals("1");

    }

    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        ActionConfigBundle bundle = new ActionConfigBundle();
        bundle.putString("ezControlIP", ezControlIP);
        bundle.putString("plugnumber", plugnumber);
        String t = "1";
        if(!turnon)
        {
            t="0";
        }
        bundle.putString("turnon", t);
        return bundle;
    }

    @Override
    public void action(boolean isFirstAlert)
    {
        switchtheplug(turnon);
    }

    private void switchtheplug(final boolean on)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                int function=1;
                if(!on)
                {
                    function=2;
                }
                try
                {
                    URL oracle = new URL("http://"+ezControlIP+"/control?cmd=set_state_actuator&number="+plugnumber+"&function="+function+"&page=control.html");
                    URLConnection yc = oracle.openConnection();
                    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                    Logger.d(TAG, "command->coffee");
                }
                catch(Exception e)
                {
                    Logger.e(TAG, "there was an error while setting the coffe machine");
                }
            }
        }).start();
    }

    @Override
    public void snooze()
    {
        if(isJoinSnoozing())
        {
            switchtheplug(!turnon);
        }
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
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void defineSettingsView(LinearLayout configView, AmbientAlarm alarm)
    {
        LinearLayout mainLayout = createLayout(configView, alarm);


        TextView name = createNameTextView(configView);

        ImageView icon = createActionIcon(configView);

        mainLayout.addView(icon);
        mainLayout.addView(name);
        configView.addView(mainLayout);
    }

    private TextView createNameTextView(LinearLayout configView)
    {
        TextView name = new TextView(configView.getContext());
        name.setText(getActionName());
        return name;
    }

    private ImageView createActionIcon(LinearLayout configView)
    {
        ImageView icon = new ImageView(configView.getContext());
        icon.setImageResource(R.drawable.action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);
        return icon;
    }

    @Override
    public Class getConfigActivity()
    {
        return EZControlPlugActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        //do nothing
    }

    public String getEzControlIP()
    {
        return ezControlIP;
    }

    public void setEzControlIP(String ezControlIP)
    {
        this.ezControlIP = ezControlIP;
    }

    public String getPlugnumber()
    {
        return plugnumber;
    }

    public void setPlugnumber(String plugnumber)
    {
        this.plugnumber = plugnumber;
    }

    public boolean isTurnon()
    {
        return turnon;
    }

    public void setTurnon(boolean turnon)
    {
        this.turnon = turnon;
    }
}
