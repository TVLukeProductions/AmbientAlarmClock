package de.lukeslog.alarmclock;

import java.util.Collection;
import java.util.List;

import de.jaetzold.philips.hue.ColorHelper;
import de.jaetzold.philips.hue.HueBridge;
import de.jaetzold.philips.hue.HueLightBulb;
import de.lukeslog.alarmclock.R;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class Settings extends Activity 
{
	 public static final String PREFS_NAME = "TwentyEightClock";
	 ProgressBar connectbar;
	 Button connectbutton;
	 TextView text;
	 
	 /** Called when the activity is first created. */
		public void onCreate(Bundle savedInstanceState) 
		{
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.settings);
		    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    boolean radio = settings.getBoolean("radio", true);
		    //boolean system = settings.getBoolean("system", true);
		    boolean fadein = settings.getBoolean("fadein", true);
		    String lastfmusername = settings.getString("lastfmusername", "");
		    String lastfmpassword = settings.getString("lastfmpassword", "");
		    String websiteaddress = settings.getString("websiteaddress", "");
		    int snoozetime = settings.getInt("snoozetime", 5);
		    int station = settings.getInt("radiostation", 0);
		    getAccountInfo();
			String gmailaccString= settings.getString("gmailacc", "");
		    String gmailpswString= settings.getString("gmailpsw", "");
		    
		    //CheckBox radioCheckBox = (CheckBox) findViewById(R.id.radioCheckBox);
		    CheckBox fadeInCheckBox = (CheckBox) findViewById(R.id.fadeInCheckBox);
		    connectbar = (ProgressBar) findViewById(R.id.progressBar1);
		    connectbutton = (Button) findViewById(R.id.findhue);
		    text = (TextView) findViewById(R.id.textView4);
		    //radioCheckBox.setChecked(radio);
		    fadeInCheckBox.setChecked(fadein);
		    
		    final TextView lastfmusernamefield = (TextView) findViewById(R.id.lastfm);
		    lastfmusernamefield.setText(lastfmusername);
		    final TextView lastfmpasswordfield = (TextView) findViewById(R.id.lastfmpassword);
		    lastfmpasswordfield.setText(lastfmpassword);
		    
		    final TextView gmailacc = (TextView) findViewById(R.id.gmailacc);
		    final TextView gmailpsw = (TextView) findViewById(R.id.gmailpsw);
		    
		    final RadioGroup radioradiogroup = (RadioGroup) findViewById(R.id.radioGroup1);
		    if(station==0)
		    {
		    	RadioButton rb0 = (RadioButton) findViewById(R.id.radio0);
		    	rb0.setChecked(true);
		    }
		    if(station==1)
		    {
		    	RadioButton rb1 = (RadioButton) findViewById(R.id.radio1);
		    	rb1.setChecked(true);
		    }
		    if(station==2)
		    {
		    	RadioButton rb2 = (RadioButton) findViewById(R.id.radio2);
		    	rb2.setChecked(true);
		    }
		    if(station==3)
		    {
		    	RadioButton rb3 = (RadioButton) findViewById(R.id.radio3);
		    	rb3.setChecked(true);
		    }
		    
		    final TextView websiteaddressfield = (TextView) findViewById(R.id.websiteaddress);
		    if(websiteaddress.equals(""))
		    {
		    	websiteaddressfield.setText("http://www.tagesschau.de");
		    }
		    else
		    {
		    	websiteaddressfield.setText(websiteaddress);
		    }
		    

		    EditText snoozetimeText = (EditText) findViewById(R.id.snoozetime);
		    snoozetimeText.setText(""+snoozetime);
		    
		    Button settingButton = (Button) findViewById(R.id.settingsave);
		    settingButton.setOnClickListener(new View.OnClickListener() 
	        {

				@Override
				public void onClick(View arg0) 
				{
					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	            	Editor edit = settings.edit();

	    		    //CheckBox radioCheckBox = (CheckBox) findViewById(R.id.radioCheckBox);
	    		    CheckBox fadeInCheckBox = (CheckBox) findViewById(R.id.fadeInCheckBox);
	    		    
	    		    EditText snoozetime = (EditText) findViewById(R.id.snoozetime);
	    		    Editable st = snoozetime.getEditableText();
	    		    String sts = st.toString();
	    		    int time = Integer.parseInt(sts);
	    		        		    
	    		    edit.putInt("snoozetime", time);
	    		    edit.putString("lastfmusername", lastfmusernamefield.getEditableText().toString());
	    		    edit.putString("lastfmpassword", lastfmpasswordfield.getEditableText().toString());
	    		    edit.putString("websiteaddress", websiteaddressfield.getEditableText().toString());
	    		    edit.putInt("radiostation", radioradiogroup.getCheckedRadioButtonId());
	    		    edit.putString("gmailacc", gmailacc.getEditableText().toString());
	    		    edit.putString("gmailpsw", gmailpsw.getEditableText().toString());
	    		    
	    		    //edit.putBoolean("radio", radioCheckBox.isChecked());

	    		    edit.putBoolean("fadein", fadeInCheckBox.isChecked());
	            	edit.commit();
	            	Settings.this.finish();
				}
		    	
	        });
		    
		    Button findhue = (Button) findViewById(R.id.findhue);
		    findhue.setOnClickListener(new View.OnClickListener() 
	        {

				@Override
				public void onClick(View arg0) 
				{
					new Thread(new Runnable()
				 	{
				 		public void run()
				 		{
				 			List<HueBridge> bridges = HueBridge.discover();
						    for(HueBridge bridge : bridges) 
						    {
						    	Log.d("HUE", "Found " + bridge);
						        // You may need a better scheme to store your username that to just hardcode it.
						        // suggestion: Save a mapping from HueBridge.getUDN() to HueBridge.getUsername() somewhere.
						        bridge.setUsername(ClockService.BRIDGEUSERNAME);
						        if(!bridge.authenticate(false)) 
						        {
						        	Log.d("HUE", "Press the button on your Hue bridge in the next 30 seconds to grant access.");
						            if(bridge.authenticate(true)) 
						            {
						            	Log.d("HUE", "Access granted. username: " + bridge.getUsername());
						    			Collection<HueLightBulb> lights = (Collection<HueLightBulb>) bridge.getLights();
						    			Log.d("HUE", "Available LightBulbs: "+lights.size());
						    			for (HueLightBulb bulb : lights) 
						    			{
						    				Log.d("HUE", bulb.toString());
						    				ClockService.identifiy(bulb);
						    			}
						    			System.out.println("");
						            } 
						            else 
						            {
						            	Log.d("HUE", "Authentication failed.");
						            }
						        } 
						        else 
						        {
						        	Log.d("HUE", "Already granted access. username: " + bridge.getUsername());
					    			Collection<HueLightBulb> lights = (Collection<HueLightBulb>) bridge.getLights();
					    			Log.d("HUE", "Available LightBulbs: "+lights.size());
					    			for (HueLightBulb bulb : lights) {
					    				Log.d("HUE", bulb.toString());
					    				ClockService.identifiy(bulb);
					    			}
					    			System.out.println("");
						        }
						    }
				 		}
				 	}).start();	    
					new Countdown().execute();
				}
		    	
	        });
		}
		
		
		 
		/**
		  * @author lukas
		  *
		  */
		private class Countdown extends AsyncTask<Integer, Integer, Long> 
		{
			protected Long doInBackground(Integer... urls) 
			{
			    	Log.d("HUE", "doInBackground");
			    	for(int i=0; i<30; i++)
			    	{
			    		try 
						{
			    			Log.d("HUE", "sleep");
							Thread.sleep(1000);
						} 
						catch (InterruptedException e) 
						{

								e.printStackTrace();
						}
			    		Log.d("HUE", "pp");
						publishProgress(i);
				    }
					return 0l;
				  }

				  protected void onProgressUpdate(Integer... progress) 
				  {
				    	 if(progress[0]==0)
				    	 {
					    	 Log.d("HUE", "set visble");
				 	    	 connectbar.setVisibility(View.VISIBLE);
				    		 Log.d("HUE", "p=0 start disc and auth");
				    	 }
				    	 text.setText("Press the button on your Hue bridge in the next 30 seconds to grant access.");
				    	 connectbutton.setClickable(false);
				    	  Log.d("HUE", "int p");
				    	  double px = progress[0];
				    	 int p = (int) (px/(0.3));
				    	 if(progress[0]==29)
				    	 {
				    		 p=100;
				    	 }
				    	  Log.d("HUE", "p="+p);
				    	 connectbar.setProgress(p);
				    	  Log.d("HUE", "done");
				  	}

				     protected void onPostExecute(Long result) 
				     {
				    	 Log.d("HUE", "on post execute");
				    	 connectbar.setVisibility(View.GONE);		    	
				    	 connectbutton.setClickable(true);
				    	 text.setText("");
				    	 Log.d("Hue", "on Post Execute 2");
				     }
				 }
	
		@Override
		protected void onPause() 
		{
			startActivity(new Intent(this, AlarmClockActivity.class));
			Settings.this.finish();
			super.onPause();
		}
		
	    private void saveall()
	    {
	    	
	    }
	    
	    private void getAccountInfo()
		{
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
			Account[] list = manager.getAccounts();
			for(Account account: list)
			{
			    if(account.type.equalsIgnoreCase("com.google"))
			    {
			    	Log.d("clock", account.name);
			        String gmail = account.name;
		        	Editor edit = settings.edit();
		        	edit.putString("gmailacc", gmail);
		        	edit.commit();
			        break;
			    }
			}
		}
}
