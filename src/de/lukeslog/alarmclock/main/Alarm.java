package de.lukeslog.alarmclock.main;

import de.lukeslog.alarmclock.main.ClockService.LocalBinder;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;

@SuppressLint({ "Wakelock", "SetJavaScriptEnabled" })
public class Alarm extends Activity
{
    ClockService mService;
    boolean mBound = false;
    
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;
    
    static boolean pureradio = false;
    WakeLock wakeLock;
    
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm);
		try
		{
			KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
			final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
	
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ACQUIRE_CAUSES_WAKEUP
			        | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
			wakeLock.acquire();
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);	
			kl.disableKeyguard();
		}
		catch(Exception e)
		{
			Log.e(TAG, "The Problem seems to be to turn on or unlock the screen");
		}
		try
		{
			WindowManager.LayoutParams params = getWindow().getAttributes();
			params.screenBrightness = 1.0F;
			getWindow().setAttributes(params);
		
		}
		catch(Exception e)
		{
			
		}
		try
		{
			getApplicationContext().startService(new Intent(this, ClockService.class));
	        Intent intent = new Intent(this, ClockService.class);
	        
	        getApplicationContext().bindService(intent, mConnection,Context.BIND_AUTO_CREATE);
		}
		catch(Exception e)
		{
			Log.e(TAG, "there is a service being creted and bound.");
		}
	        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	        String websiteaddress = settings.getString("websiteaddress", "");
	        boolean showSnooze = settings.getBoolean("showsnooze", true);
		try
		{
	        WebView webView = (WebView) findViewById(R.id.webView1);
	        webView.getSettings().setJavaScriptEnabled(true);
	        if(websiteaddress.equals(""))
	        {
	        	webView.loadUrl("http://www.tagesschau.de");
	        }
	        else
	        {
	        	webView.loadUrl(websiteaddress);
	        }
		}
		catch(Exception e)
		{
			Log.e(TAG, "the problem is displaying the website");
			Log.e(TAG, e.getMessage());
		}
		try
		{
        final Button button = (Button) findViewById(R.id.button1);
        if(!showSnooze)
        {
        	button.setVisibility(View.GONE);
        }
        else
        {
		        button.setOnClickListener(new View.OnClickListener() 
		        {
		            public void onClick(View v)
		            {
		            	mService.snooze();
		            	
		    			WindowManager.LayoutParams params = getWindow().getAttributes();
		    			params.screenBrightness = 0.1F;
		    			getWindow().setAttributes(params);
		    			
		            	Alarm.this.finish();
		            }
		        });
	        }
	        
	        final Button button2 = (Button) findViewById(R.id.button2);
	        button2.setOnClickListener(new View.OnClickListener() 
	        {
	            public void onClick(View v)
	            {
	            	mService.awake();
	            	mService.radioOff();
	            	pureradio=false;
	            	Alarm.this.finish();
	            }
	        });
	        
	        final Button button3 = (Button) findViewById(R.id.button3);
	        button3.setOnClickListener(new View.OnClickListener()
	        {
	            public void onClick(View v)
	            {
	            	Log.d(TAG, "BUTTON");
	            	Log.d("clock", "pureradio="+pureradio);
	            	if(!pureradio)
	            	{
	            		Log.d(TAG, "pureradio="+pureradio);
	            		pureradio=true;
	            		Log.d(TAG, "pureradio="+pureradio);
	            		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	            		boolean radio = settings.getBoolean("radio", true);
	            		Log.d(TAG, "radio="+radio);
	            		if(!radio)
	            		{
	            			Log.d(TAG, "turn on now.");
	            			mService.turnOnRadio();
	            		}
	            		mService.awake();
	            		button3.setText("Turn Radio off");
	            		
	            	}
	            	else
	            	{
	            		mService.radioOff();
	            		pureradio=false;
	                	Alarm.this.finish();
	            		//button3.setText("Turn Radio off");
	            	}
	            }
	        });
		}
		catch(Exception e)
		{
			
		}
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
    
  
  public void onPause()
  {
	  super.onPause();
	  try
	  {
		  onDestroy();
	  }
	  catch(Exception e)
	  {
		  
	  }
	  
  }
  
  public void  onDestroy()
  {
	  if(wakeLock!=null)
	  {
		  wakeLock.release();
	  }
	  super.onDestroy();
  }
}
