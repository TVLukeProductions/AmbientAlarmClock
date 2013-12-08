package de.lukeslog.alarmclock.main;

import de.lukeslog.alarmclock.main.ClockService.LocalBinder;
import de.lukeslog.alarmclock.R;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class Alarm extends Activity
{
    ClockService mService;
    boolean mBound = false;
    public static final String PREFS_NAME = "TwentyEightClock";
    static boolean pureradio = false;
    WakeLock wakeLock;
    
    /** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm);
		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("MyKeyguardLock");
		kl.disableKeyguard();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
		        | PowerManager.ON_AFTER_RELEASE, "MyWakeLock");
		wakeLock.acquire();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		getApplicationContext().startService(new Intent(this, ClockService.class));
        Intent intent = new Intent(this, ClockService.class);
        
        getApplicationContext().bindService(intent, mConnection,Context.BIND_AUTO_CREATE);
        
        WebView webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String websiteaddress = settings.getString("websiteaddress", "");
        if(websiteaddress.equals(""))
        {
        	webView.loadUrl("http://www.tagesschau.de");
        }
        else
        {
        	webView.loadUrl(websiteaddress);
        }
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
            	mService.snooze();
            	Alarm.this.finish();
            }
        });
        
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
            	mService.awake();
            	mService.radioOff();
            	Alarm.this.finish();
            }
        });
        
        final Button button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	if(!pureradio)
            	{
            		pureradio=true;
            		mService.awake();
            		mService.turnOnRadio();
            		button3.setText("Turn Radio Off");
            	}
            	else
            	{
            		mService.radioOff();
                	Alarm.this.finish();
            	}
            }
        });
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
    
  public void  onDestroy()
  {
	  super.onDestroy();
  }

}
