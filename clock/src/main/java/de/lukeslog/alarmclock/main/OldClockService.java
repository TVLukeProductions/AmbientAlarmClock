package de.lukeslog.alarmclock.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;

import de.lukeslog.alarmclock.service.dropbox.DropBox;
import de.lukeslog.alarmclock.service.lastfm.Scrobbler;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class OldClockService extends Service implements Runnable
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    public static boolean RADIO = true;
    public static int SNOOZETIME = 300;
    public static String RADIOSTATION="";
    public static boolean FADE_IN=false;
    public static String SONG_NAME="";
    public static int timesincewakeup =1000;
    private Thread runner;
    
    private static final int LIVINGROOM = 1;
    private static final int BATHROOM=2;
    
    private static final int HEAT_LOW=1;
    private static final int HEAT_MEDIUM=2;
    private static final int HEAT_HIGH=3;
    private static final int HEAT_VERRY_HIGH=4;

    UUID uuid;

    int snoozetime=-1;
    boolean playmusic=true;

    
    public static String ezcontrolIP ="";
    
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;

    public static SharedPreferences settings;
    
    private static String dropboxfoldername="";

    
    public class LocalBinder extends Binder 
    {
    	OldClockService getService()
    	{
            // Return this instance of LocalService so clients can call public methods
            return OldClockService.this;
        }
    }
    
    private final IBinder mBinder = new LocalBinder();
    
    
	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) 
    {
		//Start foreground is another way to make sure the app does not stop...
		return START_STICKY;
    }
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		Log.d(TAG, "OldClockService onCreate()");
        settings = getSharedPreferences(PREFS_NAME, 0);

        setUUIDs();

        connectToEZControl();


		
		runner = new Thread(this);
		runner.start();

	}

    @Override
    public void onDestroy()
    {
        Log.i(TAG, "onDestroy!");

        stopForeground(true);
        super.onDestroy();
    }

    private void connectToDropBox()
    {
        DropBox.getDropboxAPI();

        DropBox.ListAllFolders();//TODO: Having this method here violates the "Do what the method name says"-Rule
    }

    private void setUUIDs()
    {
        String uuids = settings.getString("UUID", "");
        if(uuids.equals(""))
        {
            uuid = UUID.randomUUID();
            Editor edit = settings.edit();
            edit.putString("UUID", uuid.toString());
            edit.commit();
        }
        else
        {
            uuid = UUID.fromString(uuids);
        }
    }

    private void connectToEZControl()
    {
        ezcontrolIP = settings.getString("ezcontrolIP", "");
        if(ezcontrolIP.equals(""))
        {
            ezcontrolIP="192.168.1.242"; //Default IP for ezControl Servers in a Home Network
        }
    }
	

	public boolean alarmSet() 
	{
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    boolean active = settings.getBoolean("active", false);
	    return active;
	}
	
	private void wake(boolean firstalert)
	{
		Log.i(TAG, "wake");
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean radio = settings.getBoolean("radio", true);
		//fade in the lights

		//start the activity



		
	}
	
	public void awake() 
	{
		try
		{
			timesincewakeup=0;
    		heatControl(LIVINGROOM, HEAT_LOW);
    		coffeMachine(false);
		}
		catch(Exception e)
		{
			
		}
	}
	
	public void snooze()
	{
		snoozetime=SNOOZETIME;

	}
	
	
	@SuppressWarnings("static-access")
	@Override
	public void run() 
	{
		boolean oktorun=true;
		boolean newalert=true;
		long runningcounter=0;
		boolean coffeealarmsend=false;
		
		while(oktorun)
		{
			runningcounter++;
		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			//Log.d("clock", "run1");
			try
	    	{
	    		  Thread.currentThread().sleep(1000);
	    	}
	    	catch(Exception ie)
	    	{

	    	}
			//Check if the name of the Dropboxfolder for the music has changed
			String dropfolderstring = settings.getString("dropboxfolder", "");
			if(!dropboxfoldername.equals("") && !dropboxfoldername.equals(dropfolderstring))
			{
				Log.d(TAG, "folder change");
				//DropBox.syncFiles(settings);
				
			}
			//Log.d("clock", "run2");
		    timesincewakeup++;
		    boolean active = settings.getBoolean("active", true);
		    boolean fadein = settings.getBoolean("fadein", false);
		    boolean reminder = settings.getBoolean("reminder", false);
		    int remindersubtract = settings.getInt("remindersubtract", 8);
		    String remindertext = settings.getString("remindertext", "");
		    FADE_IN = fadein;
		    //Log.i("clock", "? "+timesincewakeup);
		    if(snoozetime>0)
		    {
		    	snoozetime--;
		    	Log.d(TAG, "snoozetime============="+snoozetime);
		    }
		    if(active)
		    {
		    	//Log.d(TAG, "active");
		    	if(getMinute()%30==0 )
		    	{
		    		Date d = new Date();
		    		if(d.getSeconds()==0)
		    		{
		    			//connectToBluetooth();
		    		}
		    	}
		    	boolean turnoncoffee=false;
		    	if(getAlarmMinute()>5)
		    	{
		    		if((getAlarmHour()==getHour() && (getAlarmMinute())==(getMinute()+5)))
			    	{
		    			Log.d(TAG, "TURN THAT MACHINE ON 1");
		    			turnoncoffee=true;
			    	}
		    	}
		    	else
		    	{
		    		if((getAlarmHour()==(getHour()+1) && (getAlarmMinute()+55)==getMinute()))
			    	{
		    			Log.d(TAG, "TURN THAT MACHINE ON");
		    			turnoncoffee=true;
			    	}
		    	}
		    	if(turnoncoffee && ! coffeealarmsend)
		    	{
		    		coffeMachine(true);
		    		coffeealarmsend=true;
		    	}
		    	if((getAlarmHour()==(getHour()+2) && getAlarmMinute()==getMinute()))
		    	{
		    		//Turn on the heat
		    		heatControl(LIVINGROOM, HEAT_VERRY_HIGH);
		    		heatControl(BATHROOM, HEAT_VERRY_HIGH);
		    	}
		    	if((getAlarmHour()==getHour() && getAlarmMinute()==getMinute() && newalert) || snoozetime==0)
		    	{
		    		Log.d(TAG, "ALARM");
		    		wake(snoozetime==-1);
		    		snoozetime=-1;
		    		//Log.i("clock", "alarm time");
		    		newalert=false;
		    	}
		    	if((!(getAlarmHour()==getHour()) || !(getAlarmMinute()==getMinute())) && !newalert)
		    	{
		    		newalert=true;
		    		coffeealarmsend=false;
		    	}
		    }
		    //Log.i("clock", "reminder is"+reminder);
		    if(reminder)
		    {
		    	int rtime = getAlarmHour()-remindersubtract;
		    	if(rtime<0)
		    	{
		    		rtime=24+rtime;
		    		//Log.i("clock", "rtime="+rtime);
		    	}
		    	//Log.i("clock", "rtime="+rtime);
		    	//Log.i("clock", "getHour()"+getHour());
		    	//Log.i("clock", "alarm,Minute()="+getAlarmMinute());
		    	
		    	if(getMinute()==getAlarmMinute() && rtime==getHour() && reminder)
		    	{
		    		//TURN OFF COFFE MACHINE
		    		coffeMachine(false);
		    		coffeealarmsend=false;
		    		Date d = new Date();
		    		if(d.getSeconds()<5)
		    		{
				    	/*int icon = R.drawable.alerticon;
		    	    	final NotificationManager mNotMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		    			final Notification notfication = new Notification(
								icon, "Reminder",
		    					System.currentTimeMillis());
		    			// on can puit the countdown here!
		    			Intent settingsIntent = new Intent(this, AlarmClockActivity.class);
		    			final PendingIntent pIntent = PendingIntent.getActivity(this, 0,
		    					settingsIntent, 0);
		    			notfication.setLatestEventInfo(getApplicationContext(),
		    					"Reminder:", remindertext, pIntent);
		    			notfication.flags |= Notification.FLAG_AUTO_CANCEL;

		    			notfication.defaults |= Notification.DEFAULT_VIBRATE;
		    		    notfication.vibrate = new long[]{500,500,500,500};
	    		    	notfication.defaults |= Notification.DEFAULT_SOUND;
		    			mNotMan.notify(555, notfication);
		    			if(d.getSeconds()>0 && d.getSeconds()<=1)
		    			{
		    				sendMail("reminder, go to bed", remindertext);
		    			}*/
		    		}
		    	}
		    }
		}
	}
	
	public int getAlarmHour()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int hour = settings.getInt("hour", 0);
		//Log.i("clock", "alarm hour "+hour);
		return hour;
	}
	
	public int getAlarmMinute()
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int hour = settings.getInt("minute", 0);
		//Log.i("clock", "alarm minute "+hour);
		return hour;
	}
	
	public int getHour()
	{
		Date d = new Date();
		//Log.i("clock", "actualhour "+d.getHours());
		return d.getHours();
	}
	
	public int getMinute()
	{
		Date d = new Date();
		return d.getMinutes();
	}
	
	public int getSecond()
	{
		Date d = new Date();
		return d.getSeconds();
	}
	
	private void connectToBluetooth()
	{
		Log.d(TAG, "Bluetooth check");
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		List mArrayAdapter = new ArrayList();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			Log.d(TAG, "device does not support bluetooth");
		}
		else
		{
			Log.d(TAG, "device supports bluetooth");
			if (mBluetoothAdapter.isEnabled()) 
			{
				Log.d(TAG, "Bluetooth enabled");
				mBluetoothAdapter.startDiscovery();
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				// If there are paired devices
				if (pairedDevices.size() > 0) 
				{
					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			    	String deviceID = settings.getString("BluetoothDeviceAddress", "00:00");
				    // Loop through paired devices
				    for (BluetoothDevice device : pairedDevices) 
				    {
				    	if(deviceID.equals(device.getAddress()))
			        	{
				    		mBluetoothAdapter.cancelDiscovery();
				    		BluetoothSocket tmp = null;
				    		try
			        		{
			        			Log.d(TAG, "try to connect");
								tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//TODO: This is wrong
					    		mSocket=tmp;
			        		} 
			        		catch (IOException connectException) 
			        		{
				                // Unable to connect; close the socket and get out
				                try 
				                {
					    			Log.e(TAG, "will close socket.");
				                    mSocket.close();
				                } 
				                catch (IOException closeException) 
				                { 
				                	
				                }
			        		}
				    		catch(Exception e)
				    		{
				    			Log.e(TAG, "-------"+e);
				    		}
				    		try
				    		{
				    			mSocket.connect();
				    		}
				    		catch(Exception e)
				    		{
				    			
				    		}
			        	}
				    }
				}
			}
		}
	}
	
	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
	{
	    public void onReceive(Context context, Intent intent) 
	    {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) 
	        {
	        	Log.d(TAG, "ACTION_FOUND");
	        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	        	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	        	String deviceID = settings.getString("BluetoothDeviceAddress", "00:00");
	        	if(deviceID.equals(device.getAddress()))
	        	{
	        		mBluetoothAdapter.cancelDiscovery();
	        		try
	        		{
	        			Log.d(TAG, "try to connect");
						mSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));//TODO: This is wrong
	        			mSocket.connect();
	        		} 
	        		catch (Exception exception) 
	        		{
		                // Unable to connect; close the socket and get out
	        			Log.e(TAG, "will close socket.");
		                try 
		                {
		                    mSocket.close();
		                } 
		                catch (Exception exception2) 
		                { 
		                	
		                }
	        		}
	        	}
	        }
	        else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) 
	        {
	        	// Add the name and address to an array adapter to show in a ListView
	        	BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		    	Log.d(TAG, ""+device.getBluetoothClass().getMajorDeviceClass());
		    	if(BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER==device.getBluetoothClass().getMajorDeviceClass() 
		    			|| device.getBluetoothClass().getMajorDeviceClass()==BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED
		    			|| device.getBluetoothClass().getMajorDeviceClass()==BluetoothClass.Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER
		    			|| device.getBluetoothClass().getMajorDeviceClass()==BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR
		    			|| device.getBluetoothClass().getMajorDeviceClass()==BluetoothClass.Device.AUDIO_VIDEO_HIFI_AUDIO
		    			|| device.getBluetoothClass().getMajorDeviceClass()==BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO)
		    		{
			    		Log.d(TAG, "ITS A SPEAKER");
			    		Log.d(TAG, ""+device.getAddress());
			    		
			    		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			    		Editor edit = settings.edit();
			    		edit.putString("BluetoothDeviceAddress", device.getAddress());
			    		edit.commit();
		    		}

	        }
	        else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
	        {
	        	Log.e(TAG, "Disocnnected!");
	        }
	    }
	};

	

	
	



	public void radioOff() 
	{
		heatControl(LIVINGROOM,HEAT_LOW);
		try
		{

		}
		catch(Exception e)
		{
			
		}
	}
	
	private void coffeMachine(final boolean x)
	{
		//TODO this method assumes that the machine has the number 3. It should not... but this will all be better, when I have a good server for the ezcontrol
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		int number = settings.getInt("CoffeMachine", 1);
			if(number>0)
			{
			//Turn on my Coffe machine
			//http://192.168.1.242/control?cmd=set_state_actuator&number=3&function=1&page=control.html	
			new Thread(new Runnable()
			{
				public void run()
				{
					int function=1;
					if(!x)
					{
						function=2;
					}
					try
					{
				        URL oracle = new URL("http://"+ezcontrolIP+"/control?cmd=set_state_actuator&number=3&function="+function+"&page=control.html");
				        URLConnection yc = oracle.openConnection();
				        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
						Log.d(TAG, "command->coffee");
					}
					catch(Exception e)
					{
						Log.e(TAG, "there was an error while setting the coffe machine");
					}
				}
		 	}).start();
		}
	}
	
	private void heatControl(final int device, final int function)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					URL oracle = new URL("http://"+ezcontrolIP+"/control?cmd=set_state_actuator&number="+device+"&function="+function+"&page=control.html");
				    URLConnection yc = oracle.openConnection();
				    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
					Log.d(TAG, "command->heat");
				}
				catch(Exception e)
				{
					Log.e(TAG, "there was an error when setting the heat");
				}
			}
		}).start();
	}
	
	private void sendMail(String subject, String text)
	{
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String gmailaccString= settings.getString("gmailacc", "");
	    String gmailpswString= settings.getString("gmailpsw", "");
	    Log.i(TAG, "gmailacc="+gmailaccString);
	    Log.i(TAG, "newmail");
		final de.lukeslog.mail.BackgroundMail m = new de.lukeslog.mail.BackgroundMail(gmailaccString, gmailpswString);
		Log.i(TAG, "setTo");
		String t[] = new String[1];
		t[0]= gmailaccString;
		m.setTo(t);
		Log.i(TAG, "Set From");
		m.setFrom(gmailaccString);
		Log.i(TAG, "setSubject");
		String header=subject;
		Log.i(TAG, "Sending with herder="+header);
		m.setSubject(header);
		Log.i(TAG, "setBody");
		String body=text+"\n \n Sincearly, \n your alarm clock.";
		Log.i(TAG, "body"+body);
		m.setBody(body);
		Thread tt = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					Log.i(TAG, "send?");
					m.send();
					
				} 
				catch (Exception e) 
				{
					Log.i(TAG, "cc"+e);
					e.printStackTrace();
				}	
			}
			
		});
		tt.start();
	}


}


