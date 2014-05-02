package de.lukeslog.alarmclock.actions;

import android.content.Intent;
import android.support.v7.media.MediaRouter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.ChromeCast.ChromeCastService;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;

/**
 * Created by lukas on 01.05.14.
 */
public class ChromecastAction extends AmbientAction
{
    public ChromecastAction(String actionName)
    {
        super(actionName);
    }

    public ChromecastAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
    }

    @Override
    public void action(boolean isFirstAlert)
    {
        Intent statdevice = new Intent();
        statdevice.setAction(ChromeCastService.ACTION_DISPLAY_DATA);
        statdevice.putExtra("AmbientActionID", getActionID());
        statdevice.putExtra("ChromeCastDeviceName", "StevieTheTV");
        ClockWorkService.getClockworkContext().sendBroadcast(statdevice);
    }

    @Override
    public void snooze()
    {

    }

    @Override
    public void awake()
    {
        Intent statdevice = new Intent();
        statdevice.setAction(ChromeCastService.ACTION_STOP_DISPLAYING);
        statdevice.putExtra("AmbientActionID", getActionID());
        statdevice.putExtra("ChromeCastDeviceName", "StevieTheTV");
        ClockWorkService.getClockworkContext().sendBroadcast(statdevice);
    }

    @Override
    public void tick(DateTime currentTime)
    {

    }

    @Override
    public int getPriority()
    {
        return AmbientAction.ACTION_UI_PRIORITY_NONE;
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
    protected ActionConfigBundle setConfigurationData()
    {
        ActionConfigBundle configBundle = new ActionConfigBundle();
        return configBundle;
    }

    @Override
    public Class getConfigActivity()
    {
        return ChromecastActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {

    }
}
