package de.lukeslog.alarmclock.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import de.lukeslog.alarmclock.actions.ActionManager;
import de.lukeslog.alarmclock.actions.AmbientAction;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.actions.ActionActivity;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 15.04.14.
 */
public class NewAmbientAction extends Activity
{
    public static String TAG = AlarmClockConstants.TAG;
    Context ctx;
    ActionListAdapter adapter;
    AmbientAlarm alarm;
    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.action_list);
        String alarmID = getIntent().getStringExtra("ambientAlarmID");
        alarm = AmbientAlarmManager.getAlarmById(alarmID);

        ctx = this;

        fillListOfAmbientActions();
    }

    private void fillListOfAmbientActions()
    {
        ListView listOfActionTypes = (ListView) findViewById(R.id.actionListView);
        adapter = new ActionListAdapter(ctx);
        listOfActionTypes.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listOfActionTypes.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                createNewActionView(position);
            }

        });
    }

    private void createNewActionView(int clickPosition)
    {
        String actionTypeName = adapter.getActionTypeNameOnItemClicked(clickPosition);
        //TODO create a new ambient action of this type with default values and add it to the database and the registrar
        Log.d(TAG, "actionTypeName = " + actionTypeName);
        AmbientAction ambientAction = ActionManager.createNewDefaultAction(actionTypeName);
        Log.d(TAG, "ambientAction!=null =>"+(ambientAction!=null));
        Intent startActionConfigurationActivity = new Intent(ctx, ActionActivity.class);
        startActionConfigurationActivity.putExtra("ambientAlarmID", alarm.getAlarmID());
        startActionConfigurationActivity.putExtra("ambientActionID", ambientAction.getActionID());

        alarm.registerAction("0", ambientAction);
        AmbientAlarmManager.updateDataBaseEntry(alarm);

        startActivity(startActionConfigurationActivity);

        NewAmbientAction.this.finish();
    }
}