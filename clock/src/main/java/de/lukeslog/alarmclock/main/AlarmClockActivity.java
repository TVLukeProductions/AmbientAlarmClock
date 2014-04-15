package de.lukeslog.alarmclock.main;

import de.lukeslog.alarmclock.actions.SendMailAction;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.dropbox.DropBox;
import de.lukeslog.alarmclock.startup.NotificationService;
import de.lukeslog.alarmclock.startup.ServiceStarter;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.Day;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import org.joda.time.DateTime;

public class AlarmClockActivity extends Activity 
{
    boolean mBound = false;
    //private Handler mHandler = new Handler();
    long mStartTime;
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;
    
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
   
	    final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    int hour = settings.getInt("hour", 0);
	    int minute = settings.getInt("minute", 0);
	    boolean active = settings.getBoolean("active", true);

	    DropBox.ListAllFolders();
	    
    final CheckBox cb1 = (CheckBox)findViewById(R.id.checkBox1);
	    cb1.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
		{

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1)
			{
				Editor edit = settings.edit();
				edit.putBoolean("active", cb1.isChecked());
				edit.commit();
				
			}
	
		});


	    cb1.setChecked(active);

        //final Button buttonsave = (Button) findViewById(R.id.savebutton);
	    TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker1);
	    timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        if(isCloseToWakeUp())
        {
        	timePicker.setVisibility(View.INVISIBLE);
        }
        else
        {
        	timePicker.setVisibility(View.VISIBLE);
        }
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() 
        {

            public void onTimeChanged(TimePicker view, int hourOfDay, int minutex) 
            {
            	Log.i(TAG, "wakeup time now"+hourOfDay+":"+minutex);
            	Editor edit = settings.edit();
          	    
              	//minutes
              	edit.putInt("minute", minutex);
              	edit.putInt("hour", hourOfDay);
              	//active
          	    CheckBox cb1 = (CheckBox)findViewById(R.id.checkBox1);
          	    edit.putBoolean("active", cb1.isChecked());
              	//commit
              	edit.commit();
            }
        });
        timePicker.setOnKeyListener(new TimePicker.OnKeyListener()
        {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) 
			{
				TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker1);
				Integer hourOfDay = timePicker.getCurrentHour();
				Integer minutex = timePicker.getCurrentMinute();
				Log.i(TAG, "wakeup time now"+hourOfDay+":"+minutex);
				Editor edit = settings.edit();
          	    
              	//minutes
              	edit.putInt("minute", minutex);
              	edit.putInt("hour", hourOfDay);
              	//active
          	    CheckBox cb1 = (CheckBox)findViewById(R.id.checkBox1);
          	    edit.putBoolean("active", cb1.isChecked());
              	//commit
              	edit.commit();
              	return true;
			}
        	
        });
        
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
	@Override
	protected void onPause() 
	{
		AlarmClockActivity.this.finish();
		super.onPause();
	}
    
    private boolean isCloseToWakeUp()
    {
    	Log.i(TAG, ""+ OldClockService.timesincewakeup);
    	if(AlarmClockConstants.TESTING)
    	{
    		return OldClockService.timesincewakeup<1;
    	}
    	else
    	{
    		return OldClockService.timesincewakeup<900;
    	}
    }
	
}