package de.lukeslog.alarmclock.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dropbox.client2.session.AccessTokenPair;

import de.jaetzold.philips.hue.HueBridge;
import de.jaetzold.philips.hue.HueLightBulb;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.dropbox.DropBox;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.ui.AlarmClockMainActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

@SuppressLint("CutPasteId")
public class SettingsActivity extends Activity implements OnItemSelectedListener
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;
    
	ProgressBar connectbar;
	Button connectbutton;
	TextView text;
	Activity ctx;
	
    ArrayList<String> localFolderList;
	 
	 private static final int REQUEST_LINK_TO_DBX = 547;
	 
	 /** Called when the activity is first created. */
		public void onCreate(Bundle savedInstanceState) 
		{
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.settings);
		    ctx=this;
		    final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    boolean radio = settings.getBoolean("radio", true);
		    //boolean system = settings.getBoolean("system", true);
		    boolean fadein = settings.getBoolean("fadein", true);
		    boolean uselocalchecked = settings.getBoolean("uselocal", true);
		    boolean usedropboxchecked = settings.getBoolean("usedropbox", false);
		    Log.d(TAG, "use local"+uselocalchecked);
		    Log.d(TAG, "use dp"+usedropboxchecked);
		    boolean scrobbletolastfm = settings.getBoolean("scrobble", false);
		    boolean reminder = settings.getBoolean("reminder", false);
		    boolean sendemail = settings.getBoolean("sendemail", false);
		    boolean showcountdown=settings.getBoolean("showcountdown", false);

		    
		    String lastfmusername = settings.getString("lastfmusername", "");
		    String lastfmpassword = settings.getString("lastfmpassword", "");
		    String websiteaddress = settings.getString("websiteaddress", "");
		    final String dropfolderstring = settings.getString("dropboxfolder", "");
		    String localfolderstring = settings.getString("localfolder", "WakeUpSongs");
		    int remindersubtract = settings.getInt("remindersubtract", 8);
		    String remindertext = settings.getString("remindertext", "");
		    
		    int countdownseconds = settings.getInt("countDownSeconds", 3000);
		    int snoozetime = settings.getInt("snoozetime", 5);
		    boolean showSnooze = settings.getBoolean("showsnooze", true);
		    int station = settings.getInt("radiostation", 0);
		    getAccountInfo();
			String gmailaccString= settings.getString("gmailacc", "");
		    String gmailpswString= settings.getString("gmailpsw", "");
		    
		    DropBox.ListAllFolders();
		    
		    
		    final CheckBox cb2 = (CheckBox)findViewById(R.id.reminder_generate);
		    cb2.setOnCheckedChangeListener(new OnCheckedChangeListener()
		    {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1)
				{
					Log.i(TAG, "checked1");
					Editor edit = settings.edit();
					edit.putBoolean("reminder", cb2.isChecked());
					edit.commit();
					
				}
		    	
		    });
		    final CheckBox cb3 = (CheckBox)findViewById(R.id.reminder_sendemail);
		    cb3.setOnCheckedChangeListener(new OnCheckedChangeListener()
		    {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1)
				{
					Log.i(TAG, "checked2");
					Editor edit = settings.edit();
					edit.putBoolean("sendemail", cb3.isChecked());
					edit.commit();
					
				}
		    	
		    });
		    cb2.setChecked(reminder);
		    cb3.setChecked(sendemail);
		    CheckBox radioCheckBox = (CheckBox) findViewById(R.id.checkBox_radio);
		    radioCheckBox.setChecked(radio);
		    radioCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
				{
					saveall();
				}
		    });
		    CheckBox fadeInCheckBox = (CheckBox) findViewById(R.id.fadeInCheckBox);
		    fadeInCheckBox.setChecked(fadein);
		    fadeInCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
				{
					saveall();
				}
		    });
		    CheckBox showSnoozeCheckBox = (CheckBox) findViewById(R.id.showsnooze);
		    showSnoozeCheckBox.setChecked(showSnooze);
		    showSnoozeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
				{
					saveall();
				}
		    });
		    final Spinner dpfolderlist = (Spinner) findViewById(R.id.spinnerdpf); 
		    final Spinner localfolderlist = (Spinner) findViewById(R.id.spinnerlocalf); 
		    
		    
		    final EditText dropboxfolder = (EditText) findViewById(R.id.dropboxfolder);
		    dropboxfolder.setText(dropfolderstring);
		    dropboxfolder.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		    });
		    EditText localfolder = (EditText) findViewById(R.id.localfolder);
		    localfolder.setText(localfolderstring);
		    localfolder.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		    });
		    
		    Log.d(TAG, "2 use local"+uselocalchecked);
		    Log.d(TAG, "2 use dp"+usedropboxchecked);
		    final CheckBox use_local = (CheckBox) findViewById(R.id.use_local);

		    Log.d(TAG, "3 use local"+uselocalchecked);
		    Log.d(TAG, "3 use dp"+usedropboxchecked);
		    final CheckBox use_dropbox = (CheckBox) findViewById(R.id.use_dropbox);

		    Log.d(TAG, "4 use local"+uselocalchecked);
		    Log.d(TAG, "4 use dp"+usedropboxchecked);
		    
		    //FILLING THE SPINER FOR LOCAL FOLDERS
		    localFolderList = new ArrayList<String>();
		    //get the folders
		    File filesystem = Environment.getExternalStorageDirectory();
			createFolderList(filesystem, 0);
			//clean folderlist
			for(int i=0; i<localFolderList.size(); i++)
			{
				
			}
			final List<String> localfolderspinerArray = new ArrayList<String>();
			int lsf = settings.getInt("selectedLocalFolder", 0);
			for(int i=0; i<localFolderList.size(); i++)
			{
				localfolderspinerArray.add(localFolderList.get(i));
				if(localFolderList.get(i).equals(localfolderstring))
				{
					lsf=i;
				}
			}
			localfolderlist.setOnItemSelectedListener(new OnItemSelectedListener() 
		    {
		    	@Override
	            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
		    	{
		    		Log.d(TAG, "selected");
		    		final EditText localfolder = (EditText) findViewById(R.id.localfolder);
		    		localfolder.setText(localfolderspinerArray.get(arg2));
		    		//use_local.setChecked(true);
	                saveall();
	            }
	
	            @Override
	            public void onNothingSelected(AdapterView<?> arg0) 
	            {
	               
		    		Log.d(TAG, "not selected");
	            }
		    });
		    ArrayAdapter<String> localadapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_item, localfolderspinerArray);
		    localadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    localadapter.notifyDataSetChanged();
		    localfolderlist.setAdapter(localadapter);
		    localfolderlist.setClickable(true);
		    localfolderlist.setSelected(true);
		    if(localFolderList.size()>=lsf)
		    {
		    	localfolderlist.setSelection(lsf);
		    }
		    localadapter.notifyDataSetChanged();
		    Log.d(TAG, "5 use ocal"+uselocalchecked);
		    Log.d(TAG, "5 use dp"+usedropboxchecked);
		    
		    //---------------------------------------
		    //FROM HERE ON WE FILL THE DROPBOX SPINER
		    ArrayList<String> folderlist = DropBox.folders;
		    Log.d(TAG, "folderlistsize="+folderlist.size());
		    final List<String> spinnerArray = new ArrayList<String>();
		    int sf = settings.getInt("selectedfolder", 0);
		    
		    for(int i=0; i<folderlist.size(); i++)
		    {
			    spinnerArray.add(folderlist.get(i));	
			    if(folderlist.get(i).equals(dropfolderstring))
			    {
			    	sf=i;
			    }
		    }
		    dpfolderlist.setOnItemSelectedListener(new OnItemSelectedListener() 
			    {
		    	@Override
	            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
		    	{
		    		Log.d(TAG, "selected");
		    		final EditText dropboxfolder = (EditText) findViewById(R.id.dropboxfolder);
		    		dropboxfolder.setText(spinnerArray.get(arg2));
		    		//use_dropbox.setChecked(true);
	                saveall();
	                DropBox.syncFiles(settings);
	            }
	
	            @Override
	            public void onNothingSelected(AdapterView<?> arg0) 
	            {
	               
		    		Log.d(TAG, "not selected");
	            }
		    });

		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SettingsActivity.this, android.R.layout.simple_spinner_item, spinnerArray);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    adapter.notifyDataSetChanged();
		    dpfolderlist.setAdapter(adapter);
		    dpfolderlist.setClickable(true);
		    dpfolderlist.setSelected(true);
		    if(DropBox.folders.size()>=sf)
		    {
		    	dpfolderlist.setSelection(sf);
		    }
		    adapter.notifyDataSetChanged();
		    
		    Log.d(TAG, "6 use local"+uselocalchecked);
		    Log.d(TAG, "6 use dp"+usedropboxchecked);

		    //make sure these are exclusive
		    use_local.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
				{
					if(arg1)
					{
						use_dropbox.setChecked(false);
						saveall();
					}
				}
		    });
		    use_dropbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
				{
					if(arg1)
					{
						
						final ArrayList<String> spinnerArray = DropBox.folders;
						if(spinnerArray.size()>0)
						{
							final EditText dropboxfolder = (EditText) findViewById(R.id.dropboxfolder);
							dropboxfolder.setText(spinnerArray.get(dpfolderlist.getSelectedItemPosition()));	
							use_local.setChecked(false);
						 }
						 else
						 {
							 use_dropbox.setChecked(false);
							 use_local.setChecked(true);
						 }
						saveall();
					}
				}
		    });
		    Log.d(TAG, "7 use local"+uselocalchecked);
		    Log.d(TAG, "7 use dp"+usedropboxchecked);
		    use_local.setChecked(uselocalchecked);
		    use_dropbox.setChecked(usedropboxchecked);
		    
		    final EditText editText500 = (EditText) findViewById(R.id.editText500);
		    editText500.setText(""+remindersubtract);
		    editText500.addTextChangedListener(new TextWatcher()
		    {
		        public void afterTextChanged(Editable s)
		        {
					Log.i(TAG, "change reminder subtractX");
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
					Log.i(TAG, "change reminder subtractX");
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
		    
		    connectbar = (ProgressBar) findViewById(R.id.progressBar1);
		    connectbutton = (Button) findViewById(R.id.findhue);
		    text = (TextView) findViewById(R.id.textView4);
		    
		    CheckBox lastfmcheckbox = (CheckBox) findViewById(R.id.checkBox_lastfm);
		    lastfmcheckbox.setChecked(scrobbletolastfm);
		    lastfmcheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
				{
					if(arg1)
					{
						
						saveall();
					}
				}
		    });
		    
		    EditText lastfmusernamefield = (EditText ) findViewById(R.id.lastfm);
		    lastfmusernamefield.setText(lastfmusername);
		    lastfmusernamefield.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		    });
		    
		    
		    
		    EditText lastfmpasswordfield = (EditText ) findViewById(R.id.lastfmpassword);
		    lastfmpasswordfield.setText(lastfmpassword);
		    lastfmpasswordfield.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		    });
		    
		    EditText  gmailacc = (EditText ) findViewById(R.id.gmailacc);
		    gmailacc.setText(gmailaccString);
		    gmailacc.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		       });
		    
		    EditText  gmailpsw = (EditText ) findViewById(R.id.gmailpsw);
		    gmailpsw.setText(gmailpswString);
		    gmailpsw.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		       });
		    
		    //RadioGroup radioradiogroup = (RadioGroup) findViewById(R.id.radioGroup1);
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
		    
		    EditText websiteaddressfield = (EditText) findViewById(R.id.websiteaddress);
		    if(websiteaddress.equals(""))
		    {
		    	websiteaddressfield.setText("http://www.tagesschau.de");
		    }
		    else
		    {
		    	websiteaddressfield.setText(websiteaddress);
		    }
		    websiteaddressfield.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		    });    
		    CheckBox countdown = (CheckBox) findViewById(R.id.showcountdown);
		    countdown.setChecked(showcountdown);
		    countdown.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) 
				{
					if(arg1)
					{
						
						saveall();
					}
				}
		    });
		    EditText countdownSec = (EditText) findViewById(R.id.countdowntime);
		    countdownSec.setText(""+countdownseconds);
		    countdownSec.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		       });
		    
		    EditText snoozetimeText = (EditText) findViewById(R.id.snoozetime);
		    snoozetimeText.setText(""+snoozetime);
		    snoozetimeText.addTextChangedListener(new TextWatcher() 
		    {

		          public void afterTextChanged(Editable s) 
		          {
		        	  saveall();
		          }

		          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		          public void onTextChanged(CharSequence s, int start, int before, int count) {}
		       });
		    
		    
		    Button settingButton = (Button) findViewById(R.id.settingsave);
		    settingButton.setOnClickListener(new View.OnClickListener() 
	        {

				@Override
				public void onClick(View arg0) 
				{
					saveall();
				}
		    	
	        });
		    
		    Button dropboxconnect = (Button) findViewById(R.id.dropboxconnect);
		    
		    try
		    {
                if(DropBox.mDBApi.getSession().authenticationSuccessful())
                {
                    dropboxconnect.setVisibility(View.GONE);
                }
		    }
		    catch(Exception e)
		    {
		    	Log.e(TAG, "WHY THE FUCK DOES THIS EVEN!");
		    }
		    
		    dropboxconnect.setOnClickListener(new View.OnClickListener() 
	        {

				@Override
				public void onClick(View v) 
				{
					Log.d(TAG, "DROPBOX CONNECT CLICK!");
					 DropBox.mDBApi.getSession().startAuthentication(SettingsActivity.this);
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
				 		@SuppressWarnings("unchecked")
						public void run()
				 		{
				 			List<HueBridge> bridges = HueBridge.discover();
						    for(HueBridge bridge : bridges) 
						    {
						    	Log.d(TAG, "Found " + bridge);
						        // You may need a better scheme to store your username that to just hardcode it.
						        // suggestion: Save a mapping from HueBridge.getUDN() to HueBridge.getUsername() somewhere.
						        bridge.setUsername(OldClockService.BRIDGEUSERNAME);
						        if(!bridge.authenticate(false)) 
						        {
						        	Log.d(TAG, "Press the button on your Hue bridge in the next 30 seconds to grant access.");
						            if(bridge.authenticate(true)) 
						            {
						            	Log.d(TAG, "Access granted. username: " + bridge.getUsername());
						    			Collection<HueLightBulb> lights = (Collection<HueLightBulb>) bridge.getLights();
						    			Log.d(TAG, "Available LightBulbs: "+lights.size());
						    			for (HueLightBulb bulb : lights) 
						    			{
						    				Log.d(TAG, bulb.toString());
						    				OldClockService.identifiy(bulb);
						    			}
						    			System.out.println("");
						            } 
						            else 
						            {
						            	Log.d(TAG, "Authentication failed.");
						            }
						        } 
						        else 
						        {
						        	Log.d(TAG, "Already granted access. username: " + bridge.getUsername());
					    			Collection<HueLightBulb> lights = (Collection<HueLightBulb>) bridge.getLights();
					    			Log.d(TAG, "Available LightBulbs: "+lights.size());
					    			for (HueLightBulb bulb : lights) {
					    				Log.d(TAG, bulb.toString());
					    				OldClockService.identifiy(bulb);
					    			}
					    			Log.d(TAG, "");
						        }
						    }
				 		}
				 	}).start();	    
					new Countdown().execute();
				}
		    	
	        });
		}
		
		private void createFolderList(File f, int depth)
		{
			//Log.d(TAG, "checkfolderlist");
			if(f.isDirectory() && (depth<3 || localFolderList.size()<100))
			{
				File[] filelist = f.listFiles();
				localFolderList.add(f.getAbsolutePath());
				for(int j=0; j<filelist.length; j++)
				{
					if(filelist[j].isDirectory())
					{
						createFolderList(filelist[j], depth+1);
					}
				}
			}
		}
		 
		/**
		  * @author lukas
		  *
		  */
		private class Countdown extends AsyncTask<Integer, Integer, Long> 
		{
			protected Long doInBackground(Integer... urls) 
			{
			    	Log.d(TAG, "doInBackground");
			    	for(int i=0; i<30; i++)
			    	{
			    		try 
						{
			    			Log.d(TAG, "sleep");
							Thread.sleep(1000);
						} 
						catch (InterruptedException e) 
						{

								e.printStackTrace();
						}
			    		Log.d(TAG, "pp");
						publishProgress(i);
				    }
					return 0l;
				  }

				  protected void onProgressUpdate(Integer... progress) 
				  {
				    	 if(progress[0]==0)
				    	 {
					    	 Log.d(TAG, "set visble");
				 	    	 connectbar.setVisibility(View.VISIBLE);
				    		 Log.d(TAG, "p=0 start disc and auth");
				    	 }
				    	 text.setText("Press the button on your Hue bridge in the next 30 seconds to grant access.");
				    	 connectbutton.setClickable(false);
				    	  Log.d(TAG, "int p");
				    	  double px = progress[0];
				    	 int p = (int) (px/(0.3));
				    	 if(progress[0]==29)
				    	 {
				    		 p=100;
				    	 }
				    	  Log.d(TAG, "p="+p);
				    	 connectbar.setProgress(p);
				    	  Log.d(TAG, "done");
				  	}

				     protected void onPostExecute(Long result) 
				     {
				    	 Log.d(TAG, "on post execute");
				    	 connectbar.setVisibility(View.GONE);		    	
				    	 connectbutton.setClickable(true);
				    	 text.setText("");
				    	 Log.d(TAG, "on Post Execute 2");
				     }
				 }
	
		@Override
		protected void onPause() 
		{
			super.onPause();
		}
		
		protected void onResume() 
		{
		    super.onResume();
		 try
         {
		    if (DropBox.mDBApi.getSession().authenticationSuccessful())
		    {
		        try 
		        {
		            // MANDATORY call to complete auth.
		            // Sets the access token on the session
		        	DropBox.mDBApi.getSession().finishAuthentication();
		 
		            AccessTokenPair tokens = DropBox.mDBApi.getSession().getAccessTokenPair();
		 
		            // Provide your own storeKeys to persist the access token pair
		            // A typical way to store tokens is using SharedPreferences
		            DropBox.storeKeys(tokens.key, tokens.secret, this);
		        } 
		        catch (IllegalStateException e) 
		        {
		            Log.i(TAG, "Error authenticating", e);
		        }
		    }
         }
         catch(Exception e)
         {
             Log.d(TAG, "probably a null pointer exception from the dropbox...");
         }
		}

	    private void saveall()
	    {
	    	//initialize editor
	    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        	Editor edit = settings.edit();

        	//get all UI elements
        	EditText lastfmusernamefield = (EditText) findViewById(R.id.lastfm);
        	EditText lastfmpasswordfield = (EditText) findViewById(R.id.lastfmpassword);
        	
		    CheckBox radioCheckBox = (CheckBox) findViewById(R.id.checkBox_radio);
		    CheckBox fadeInCheckBox = (CheckBox) findViewById(R.id.fadeInCheckBox);
		    CheckBox showSnoozeCheckBox = (CheckBox) findViewById(R.id.showsnooze);
		    CheckBox lastfmcheckbox = (CheckBox) findViewById(R.id.checkBox_lastfm);
		    CheckBox countdown = (CheckBox) findViewById(R.id.showcountdown);
		    		    
		    EditText dropboxfolder = (EditText) findViewById(R.id.dropboxfolder);
		    EditText localfolder = (EditText) findViewById(R.id.localfolder);
		    
		    EditText  gmailacc = (EditText ) findViewById(R.id.gmailacc);
		    EditText  gmailpsw = (EditText ) findViewById(R.id.gmailpsw);
		    
		    CheckBox use_local = (CheckBox) findViewById(R.id.use_local);
		    CheckBox use_dropbox = (CheckBox) findViewById(R.id.use_dropbox);
		    
		    RadioGroup radioradiogroup = (RadioGroup) findViewById(R.id.radioGroup1);
		    EditText websiteaddressfield = (EditText) findViewById(R.id.websiteaddress);
		    EditText snoozetime = (EditText) findViewById(R.id.snoozetime);
		    EditText countdownSec = (EditText) findViewById(R.id.countdowntime);
		    
		    final Spinner dpfolderlist = (Spinner) findViewById(R.id.spinnerdpf);
		    final Spinner localfolderlist = (Spinner) findViewById(R.id.spinnerlocalf); 
		    
		    //get content
		    try
		    {
		    Editable st = snoozetime.getEditableText();
		    String sts = st.toString();
		    int time = Integer.parseInt(sts);
		     	 
		    //store content
		    edit.putInt("snoozetime", time);
		    }
		    catch(Exception e)
		    {
		    	
		    }
		    try
		    {
		    	edit.putInt("countDownSeconds", Integer.parseInt(countdownSec.getEditableText().toString()));
		    }
		    catch(Exception e)
		    {
		    	//some dope might write something other than numbers in here
		    }
		    edit.putString("lastfmusername", lastfmusernamefield.getEditableText().toString());
		    edit.putString("lastfmpassword", lastfmpasswordfield.getEditableText().toString());
		    edit.putString("websiteaddress", websiteaddressfield.getEditableText().toString());
		    edit.putInt("radiostation", radioradiogroup.getCheckedRadioButtonId());
		    edit.putString("gmailacc", gmailacc.getEditableText().toString());
		    edit.putString("gmailpsw", gmailpsw.getEditableText().toString());
		    edit.putString("dropboxfolder", dropboxfolder.getEditableText().toString());
		    edit.putString("localfolder", localfolder.getEditableText().toString());

		    
		    edit.putBoolean("uselocal", use_local.isChecked());
		    edit.putBoolean("usedropbox", use_dropbox.isChecked());
		    edit.putBoolean("radio", radioCheckBox.isChecked());
		    edit.putBoolean("showsnooze",  showSnoozeCheckBox.isChecked());
		    edit.putBoolean("fadein", fadeInCheckBox.isChecked());
		    edit.putBoolean("scrobble", lastfmcheckbox.isChecked());
		    edit.putBoolean("showcountdown", countdown.isChecked());
		    
		    edit.putInt("selectedfolder", dpfolderlist.getSelectedItemPosition());
		    edit.putInt("selectedLocalFolder", localfolderlist.getSelectedItemPosition());
		    
		    RadioButton rb0 = (RadioButton) findViewById(R.id.radio0);
		    RadioButton rb1 = (RadioButton) findViewById(R.id.radio1);
		    RadioButton rb2 = (RadioButton) findViewById(R.id.radio2);
		    RadioButton rb3 = (RadioButton) findViewById(R.id.radio3);
		    if(rb0.isChecked())
		    {
		    	edit.putInt("radiostation", 0);
		    }
		    if(rb1.isChecked())
		    {
		    	edit.putInt("radiostation", 1);
		    }
		    if(rb2.isChecked())
		    {
		    	edit.putInt("radiostation", 2);
		    }
		    if(rb3.isChecked())
		    {
		    	edit.putInt("radiostation", 3);
		    }
		    
		    
        	edit.commit();
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
			    	Log.d(TAG, account.name);
			        String gmail = account.name;
		        	Editor edit = settings.edit();
		        	edit.putString("gmailacc", gmail);
		        	edit.commit();
			        break;
			    }
			}
		}
	    
	   @Override
	    public void onActivityResult(int requestCode, int resultCode, Intent data) 
	    {
	    	Log.d("clock", "activity result");
	        if (requestCode == REQUEST_LINK_TO_DBX) 
	        {
	            if (resultCode == Activity.RESULT_OK) 
	            {
	                //doDropboxTest();
	            } 
	            else 
	            {
	                Log.d("clock", "Link to Dropbox failed or was cancelled.");
	            }
	        } else 
	        {
	            super.onActivityResult(requestCode, resultCode, data);
	        }
	    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
	{
		Log.d("clock", "selected");
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		Log.d("clock", "nothing selected");
	}
}
