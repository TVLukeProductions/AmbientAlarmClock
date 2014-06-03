package de.lukeslog.alarmclock.ui;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientService.dropbox.DropBox;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

@SuppressLint("CutPasteId")
public class SettingsActivity extends Activity implements OnItemSelectedListener
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

	Activity ctx;
	 
	 private static final int REQUEST_LINK_TO_DBX = 547;
	 
	 /** Called when the activity is first created. */
		public void onCreate(Bundle savedInstanceState) 
		{
		    super.onCreate(savedInstanceState);

            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new NewSettingsFragment())
                    .commit();

		    //setContentView(R.layout.settings);
		    //ctx=this;
		    //final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

		    //String lastfmusername = settings.getString("lastfmusername", "");
		    //String lastfmpassword = settings.getString("lastfmpassword", "");




		    //getAccountInfo();
			//String gmailaccString= settings.getString("gmailacc", "");
		    //String gmailpswString= settings.getString("gmailpsw", "");
		    
		    //DropBox.ListAllFolders();

		    
		    

		    //EditText lastfmusernamefield = (EditText ) findViewById(R.id.lastfm);
		    //lastfmusernamefield.setText(lastfmusername);
		    //lastfmusernamefield.addTextChangedListener(new TextWatcher()
		    //{

		    //      public void afterTextChanged(Editable s)
		    //      {
		    //    	  saveall();
		    //      }

		    //      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		    //      public void onTextChanged(CharSequence s, int start, int before, int count) {}
		    //});

		    /*
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

            //THIS IS HOW ONE KNOWS IF DROPBOX IS CONNECTED!
            DropBox.mDBApi.getSession().authenticationSuccessful();

		    */

		}

		 

	
		@Override
		protected void onPause() 
		{
			super.onPause();
		}
		
		protected void onResume() 
		{
    		 super.onResume();
		}

	    private void saveall()
	    {
	    	//initialize editor
	    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        	Editor edit = settings.edit();

        	//get all UI elements
        	EditText lastfmusernamefield = (EditText) findViewById(R.id.lastfm);
        	EditText lastfmpasswordfield = (EditText) findViewById(R.id.lastfmpassword);

		    
		    EditText  gmailacc = (EditText ) findViewById(R.id.gmailacc);
		    EditText  gmailpsw = (EditText ) findViewById(R.id.gmailpsw);


		    edit.putString("lastfmusername", lastfmusernamefield.getEditableText().toString());
		    edit.putString("lastfmpassword", lastfmpasswordfield.getEditableText().toString());
		    edit.putString("gmailacc", gmailacc.getEditableText().toString());
		    edit.putString("gmailpsw", gmailpsw.getEditableText().toString());

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
			    	Logger.d(TAG, account.name);
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
	    	Logger.d("clock", "activity result");
	        if (requestCode == REQUEST_LINK_TO_DBX) 
	        {
	            if (resultCode == Activity.RESULT_OK) 
	            {
	                //doDropboxTest();
	            } 
	            else 
	            {
	                Logger.d("clock", "Link to Dropbox failed or was cancelled.");
	            }
	        } else 
	        {
	            super.onActivityResult(requestCode, resultCode, data);
	        }
	    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
	{
		Logger.d("clock", "selected");
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		Logger.d("clock", "nothing selected");
	}
}
