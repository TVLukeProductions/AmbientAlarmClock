package de.lukeslog.alarmclock.actions;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 03.04.14.
 */
public class SendMailActionFragment extends Fragment
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    /** Called when the activity is first created. */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View fragment = inflater.inflate(R.layout.send_mail_action_activity, container, false);

        //well... this is kinda evil.
        ActionActivity parent = (ActionActivity) getActivity();
        final SendMailAction action = (SendMailAction) parent.getAction();

        final EditText sendTo = (EditText) fragment.findViewById(R.id.sendto);
        final EditText subject = (EditText) fragment.findViewById(R.id.subject);
        final EditText content = (EditText) fragment.findViewById(R.id.content);

        sendTo.setText(action.getSendTo());
        subject.setText(action.getSubject());
        content.setText(action.getText());

        Button saveMailButton = (Button) fragment.findViewById(R.id.savemailbutton);
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
        return fragment;
    }

}
