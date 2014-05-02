package de.lukeslog.alarmclock.actions;

import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;

/**
 * Created by lukas on 29.04.14.
 */
public class WebsiteAction extends AmbientAction
{
    String websiteurl = "";
    public static int priority=AmbientAction.ACTION_UI_PRIORITY_LOW;

    public WebsiteAction(String actionName, String websiteurl)
    {
        super(actionName);
        this.websiteurl =websiteurl;
    }

    public WebsiteAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
        websiteurl = configBundle.getString("websiteurl");
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
    public void tick(DateTime currentTime)
    {

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
        configBundle.putString("websiteurl", websiteurl);
        return configBundle;
    }

    @Override
    public Class getConfigActivity()
    {
        return WebsiteActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        LinearLayout content = (LinearLayout) alarmActivity.findViewById(R.id.content);
        View v;
        v = content.findViewById(97256);
        if(v!=null)
        {

        }
        else
        {
            final WebView w = new WebView(alarmActivity);
            w.getSettings().setJavaScriptEnabled(true);
            w.loadUrl(websiteurl);
            w.setId(97256);
           content.addView(w);
        }
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    public String getURL()
    {
        return websiteurl;
    }

    public void setURL(String url)
    {
        this.websiteurl=url;
    }
}
