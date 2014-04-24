package de.lukeslog.alarmclock.actions;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import de.lukeslog.alarmclock.R;

/**
 * Created by lukas on 03.04.14.
 */
public class ActionActivity extends Activity
{
    Context ctx;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_configuration_base_activity);

        ctx = this;
    }
}
