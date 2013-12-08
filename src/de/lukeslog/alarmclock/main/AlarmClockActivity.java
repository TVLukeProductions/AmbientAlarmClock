package de.lukeslog.alarmclock.main;

import de.lukeslog.alarmclock.main.ClockService.LocalBinder;
import de.lukeslog.alarmclock.R;

import android.app.Activity;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;

public class AlarmClockActivity extends Activity 
{
    ClockService mService;
    boolean mBound = false;
    //private Handler mHandler = new Handler();
    long mStartTime;
    public static final String PREFS_NAME = "TwentyEightClock";
    
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
   
	    final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    int hour = settings.getInt("hour", 0);
	    int minute = settings.getInt("minute", 0);
	    boolean active = settings.getBoolean("active", true);
	    boolean reminder = settings.getBoolean("reminder", false);
	    boolean sendemail = settings.getBoolean("sendemail", false);
	    int remindersubtract = settings.getInt("remindersubtract", 8);
	    String remindertext = settings.getString("remindertext", "");
	    
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
	    final CheckBox cb2 = (CheckBox)findViewById(R.id.checkBox2);
	    cb2.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
	    {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1)
			{
				Log.i("clock", "checked");
				Editor edit = settings.edit();
				edit.putBoolean("reminder", cb2.isChecked());
				edit.commit();
				
			}
	    	
	    });
	    final CheckBox cb3 = (CheckBox)findViewById(R.id.checkBox3);
	    cb3.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
	    {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1)
			{
				Log.i("clock", "checked");
				Editor edit = settings.edit();
				edit.putBoolean("sendemail", cb3.isChecked());
				edit.commit();
				
			}
	    	
	    });
	    cb1.setChecked(active);
	    cb2.setChecked(reminder);
	    cb3.setChecked(sendemail);
	    
	    final EditText editText500 = (EditText) findViewById(R.id.editText500);
	    editText500.setText(""+remindersubtract);
	    editText500.addTextChangedListener(new TextWatcher()
	    {
	        public void afterTextChanged(Editable s)
	        {
				Log.i("clock", "change reminder subtractX");
            	Editor edit = settings.edit();
            	try
            	{
            		edit.putInt("remindersubtract", Integer.parseInt(editText500.getEditableText().toString()));
            	}
            	catch(Exception e)
            	{
            		
            	}
            	edit.commit();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    });
	   	    
	    final EditText editText501 = (EditText) findViewById(R.id.editText501);
	    editText501.setText(remindertext);
	    editText501.addTextChangedListener(new TextWatcher()
	    {
	        public void afterTextChanged(Editable s)
	        {
				Log.i("clock", "change reminder subtractX");
            	Editor edit = settings.edit();
            	try
            	{
            		edit.putString("remindertext", editText501.getEditableText().toString());
            	}
            	catch(Exception e)
            	{
            		
            	}
            	edit.commit();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 

	    
	    getApplicationContext().startService(new Intent(this, ClockService.class));
        Intent intent = new Intent(this, ClockService.class);
        
        getApplicationContext().bindService(intent, mConnection,Context.BIND_AUTO_CREATE);
                
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
            	Log.i("clock", "wakeup time now"+hourOfDay+":"+minutex);
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
				Log.i("clock", "wakeup time now"+hourOfDay+":"+minutex);
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        switch (item.getItemId()) 
        {
            case R.id.text1:    final Intent intent = new Intent(this, Settings.class);
            					startActivity(intent);
                                break;
        }
        return true;
    }

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() 
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
        	LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) 
        {
        	mService=null;
            mBound = false;
        }
    };
    
    private boolean isCloseToWakeUp()
    {
    	Log.i("clock", ""+ClockService.timesincewakeup);
    	return ClockService.timesincewakeup<900;
    }
	
}