package de.lukeslog.alarmclock.actions;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.alarmactivity.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.mail.Mail;

/**
 * Created by lukas on 31.03.14.
 */
public class SendMailAction extends AmbientAction
{
    String sendTo="";
    String subject="";
    String text="";

    public SendMailAction(String actionName, String sendTo, String subject, String text)
    {
        super(actionName);
        this.sendTo=sendTo;
        this.subject=subject;
        this.text=text;
    }

    public SendMailAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
        this.sendTo = configBundle.getString("sendTo");
        this.subject = configBundle.getString("subject");
        this.text = configBundle.getString("text");

    }

    public String getSendTo()
    {
        return sendTo;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getText()
    {
        return text;
    }

    public void setSendTo(String sendTo)
    {
        this.sendTo = sendTo;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public void action()
    {
        Log.d(TAG, "mail action");
        Mail.sendMail(sendTo, subject, text);
    }

    @Override
    public void snooze()
    {
        //nothing to do here...
    }

    @Override
    public void awake()
    {
        //nothing to do here...
    }

    @Override
    public void defineSettingsView(final LinearLayout configView)
    {
        LinearLayout mainLayout = new LinearLayout(configView.getContext());
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setMinimumHeight(40);
        mainLayout.setBackgroundColor(0xfff00000);
        mainLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Log.d(TAG, "click");
                openConfigurationActivity(configView.getContext());
            }
        });

        ImageView icon = new ImageView(configView.getContext());
        icon.setImageResource(R.drawable.send_mail_action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);

        TextView name = new TextView(configView.getContext());
        name.setText(getActionName());
        TableRow.LayoutParams params2 = new TableRow.LayoutParams(400, TableLayout.LayoutParams.WRAP_CONTENT);
        name.setLayoutParams(params2);

        mainLayout.addView(icon);
        mainLayout.addView(name);
        configView.addView(mainLayout);
    }

    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        ActionConfigBundle configBundle = new ActionConfigBundle();
        configBundle.putString("sendTo", sendTo);
        configBundle.putString("subject", subject);
        configBundle.putString("text", text);
        return configBundle;
    }

    @Override
    public Class getConfigActivity()
    {
        return SendMailActionActivity.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        //nothing to do on this one
    }


}
