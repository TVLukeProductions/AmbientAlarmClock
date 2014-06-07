package de.lukeslog.alarmclock.actions;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import de.lukeslog.alarmclock.support.Logger;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.UISupport;
import de.lukeslog.alarmclock.R;

/**
 * Created by lukas on 04.04.14.
 */
public class CountdownAction extends AmbientAction
{

    int durationInSeconds=3600;
    boolean offonend=false;
    public static int priority = AmbientAction.ACTION_UI_PRIORITY_MEDIUM;

    public CountdownAction(String actionName, int durationInSeconds)
    {
        super(actionName);
        this.durationInSeconds=durationInSeconds;
    }

    public CountdownAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
        try
        {
            this.durationInSeconds = Integer.parseInt(configBundle.getString("durationInSeconds"));
            this.offonend = configBundle.getString("offonend").equals("1");
        }
        catch(Exception e)
        {

        }
        Logger.d(TAG, "new Countdownaction from config bunlde");
    }


    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        ActionConfigBundle configBundle = new ActionConfigBundle();
        configBundle.putString("durationInSeconds", ""+this.durationInSeconds);
        String ox="0";
        if(offonend)
        {
            ox="1";
        }
        configBundle.putString("offonend", ox);
        return configBundle;
    }

    public void setDurationInSeconds(int seconds)
    {
        this.durationInSeconds=seconds;
    }

    public int getDurationInSeconds()
    {
        return durationInSeconds;
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
    public void tick(DateTime now)
    {

    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void defineSettingsView(final LinearLayout configView, AmbientAlarm alarm)
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
        icon.setImageResource(R.drawable.countdown_action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);
        return icon;
    }

    @Override
    public Class getConfigActivity()
    {
        return CountdownActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        Logger.d(TAG, "update Layout is called");
        LinearLayout content = (LinearLayout) alarmActivity.findViewById(R.id.content);

        DateTime now = new DateTime();

        DateTime alarmTime = ambientAlarm.getLastAlarmTime();
        alarmTime = alarmTime.plusSeconds(this.durationInSeconds);
        //Log.d(TAG, alarmTime.toString());
        int seconds = Seconds.secondsBetween(now, alarmTime).getSeconds();
        //Log.d(TAG, "SECONDS: "+seconds);
        TextView textview;
        if(content.findViewById(17493)!=null)
        {
            textview = (TextView) content.findViewById(17493);
            //textview.setText(ambientAlarm.getTimeAsString());
        }
        else
        {
            //TODO: this is not centered yet.
            textview = new TextView(content.getContext());
            //textview.setText(ambientAlarm.getTimeAsString());
            textview.setTextSize(40);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            textview.setLayoutParams(params);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            textview.setLayoutParams(params);
            textview.setId(17493);
            content.addView(textview);
        }
        textview.setText(UISupport.secondsToCountdownString(seconds));
        if(seconds==0)
        {
            if(offonend)
            {
                ambientAlarm.awakeButtonPressed();
                alarmActivity.awakeButtonPressedRemotely();
            }
        }
    }

    public int getCountDownDuration()
    {
        return durationInSeconds;
    }
}
