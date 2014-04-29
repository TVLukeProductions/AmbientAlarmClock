package de.lukeslog.alarmclock.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.lukeslog.alarmclock.ambientService.dropbox.DropBox;
import de.lukeslog.alarmclock.ambientService.mail.BackgroundMail;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

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
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class OldClockService
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

    
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;

    public static SharedPreferences settings;


	
	

	


	

	
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

	

	
	






}


