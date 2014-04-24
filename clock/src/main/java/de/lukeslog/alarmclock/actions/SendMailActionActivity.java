package de.lukeslog.alarmclock.actions;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 03.04.14.
 */
public class SendMailActionActivity extends ActionActivity
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    Context ctx;
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_mail_action_activity);

        ctx = this;

        String actionID = getIntent().getStringExtra("ambientActionID");
        final SendMailAction action = (SendMailAction) ActionManager.getActionByID(actionID);

        final EditText sendTo = (EditText) findViewById(R.id.sendto);
        final EditText subject = (EditText) findViewById(R.id.subject);
        final EditText content = (EditText) findViewById(R.id.content);

        sendTo.setText(action.getSendTo());
        subject.setText(action.getSubject());
        content.setText(action.getText());

        TextView actionName = (TextView) findViewById(R.id.actionname);
        actionName.setText(action.getActionName());

        Button saveMailButton = (Button) findViewById(R.id.savemailbutton);
        saveMailButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                action.setSendTo(sendTo.getEditableText().toString());
                action.setSubject(subject.getEditableText().toString());
                action.setText(content.getEditableText().toString());
            }
        });
    }
}
