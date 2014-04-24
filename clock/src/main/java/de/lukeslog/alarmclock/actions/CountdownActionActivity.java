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
 * Created by lukas on 06.04.14.
 */
public class CountdownActionActivity extends ActionActivity
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    Context ctx;
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.countdown_action_activity);

        ctx = this;

        String actionID = getIntent().getStringExtra("ambientActionID");
        final CountdownAction action = (CountdownAction) ActionManager.getActionByID(actionID);

        int seconds =action.getCountDownDuration();

        final EditText countdownTextEdit = (EditText) findViewById(R.id.countdownlengthtext);
        countdownTextEdit.setText("" + (seconds / 60));

        Button button = (Button) findViewById(R.id.countdownsave);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int s = Integer.parseInt(countdownTextEdit.getEditableText().toString());
                action.setDurationInSeconds(s * 60);
            }
        });
        TextView actionName = (TextView) findViewById(R.id.actionname);
        actionName.setText(action.getActionName());
    }
}
