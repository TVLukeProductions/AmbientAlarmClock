package de.lukeslog.alarmclock.main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.lukeslog.alarmclock.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class SoundSettings extends ListActivity
{
	 public static final String PREFS_NAME = "TwentyEightClock";
	 /** Called when the activity is first created. */
		public void onCreate(Bundle savedInstanceState) 
		{
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.soundsettings);
		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    boolean ringtone = settings.getBoolean("ringtone", true);
		    CheckBox ownSongCheckBox = (CheckBox) findViewById(R.id.ownSongCheckBox);
		    ownSongCheckBox.setChecked(!ringtone);
	        try
	        {
	        	String [] days = {"bla", "blub"};
	        	ArrayList list = getSongList();
	            setListAdapter(new SimpleAdapter(this, list, R.layout.rowlayout, new String[] {"text1", "text2"}, new int[]{R.id.rowline1, R.id.rowline2}));
	        }
	        catch(Exception e)
	        {
	        	String[] listItems = {"No entries yet", };
	        	setListAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems));
	        }
		    
	        final ListView lv = getListView();
	        lv.setTextFilterEnabled(true);

	        lv.setOnItemClickListener(new OnItemClickListener() 
	        {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) 
				{
					HashMap item = (HashMap) lv.getItemAtPosition(arg2);
					String songname = (String) item.get("text1");
					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
					Editor edit = settings.edit();
					edit.putString("possibleSong", songname);
					edit.commit();
					CheckBox ownSongCheckBox = (CheckBox) findViewById(R.id.ownSongCheckBox);
					ownSongCheckBox.setChecked(true);
					Log.i("clock", ""+songname);
					
				}	
	        });

	        	
		    Button settingButton = (Button) findViewById(R.id.settingsave);
		    settingButton.setOnClickListener(new View.OnClickListener() 
	        {

				@Override
				public void onClick(View arg0) 
				{
					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
					String posible= settings.getString("possibleSong", "");
					Editor edit = settings.edit();
					edit.putString("song", posible);
					CheckBox ownSongCheckBox = (CheckBox) findViewById(R.id.ownSongCheckBox);
					edit.putBoolean("ringtone", !ownSongCheckBox.isChecked());
					edit.commit();
	            	SoundSettings.this.finish();       	
				}
	        });
		}
		
		@Override
		protected void onPause() 
		{
			startActivity(new Intent(this, AlarmClockActivity.class));
			SoundSettings.this.finish();
			super.onPause();
		}
		
	private ArrayList getSongList() 
	{
		ArrayList result = new ArrayList();
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) 
		{
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} 
		else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} 
		else 
		{
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		if( mExternalStorageAvailable)
		{
			File filesystem = Environment.getExternalStorageDirectory();
			String path = filesystem.getAbsolutePath();
			File[] filelist = filesystem.listFiles();
			for(int i=0; i<filelist.length; i++)
			{
				if(filelist[i].getName().equals("WakeUpSongs"))
				{
					File[] filelist2 = filelist[i].listFiles();
					for(int j=0; j<filelist2.length; j++)
					{
						HashMap hm = new HashMap();
						hm.put("text1", filelist2[j].getName());
						hm.put("text2", "Artist");
						result.add(hm);
					}
				}
			}
		}
		return result;
	}
}
