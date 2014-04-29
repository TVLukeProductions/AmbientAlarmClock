package de.lukeslog.alarmclock.actions;

import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientService.mail.Mail;
import de.lukeslog.alarmclock.R;

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
    public void action(boolean isFirstAction)
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
    public void tick(DateTime now)
    {

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
        icon.setImageResource(R.drawable.send_mail_action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);
        return icon;
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
        return SendMailActionFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        //nothing to do on this one
    }


}
