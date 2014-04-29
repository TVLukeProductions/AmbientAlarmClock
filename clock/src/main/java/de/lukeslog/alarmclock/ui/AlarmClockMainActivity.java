package de.lukeslog.alarmclock.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.session.AccessTokenPair;

import java.util.ArrayList;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.ambientService.dropbox.DropBox;
import de.lukeslog.alarmclock.ambientService.lastfm.Scrobbler;
import de.lukeslog.alarmclock.startup.ServiceStarter;
import de.lukeslog.alarmclock.support.AlarmState;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 31.03.14.
 *
 *
 */
public class AlarmClockMainActivity extends Activity
{
    public static String TAG = AlarmClockConstants.TAG;

    ArrayList<AmbientAlarm> ambientalarms;
    Context ctx;
    AlarmListAdapter adapter;

    private UIUpdater updater;


    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ambient_alarm_clock_main_activity);

        ctx = this;


        ServiceStarter.start(this);

        fillListOfAmbientAlarms();

        configureNewAlarmButton();

        startUIUpdater();

    }


    @Override
    protected void onPause()
    {
        updater.onPause();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        adapter.notifyDataSetChanged();
        updater.onResume();
        authenticateDropBox();
        redrawMenu();
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        resetMenuItems(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_dropbox:
                setDropBoxConnection();
                redrawMenu();
                return true;
            case R.id.action_lastfm:
                setScrobbler();
                redrawMenu();
                break;
            case R.id.action_settings:
                startSettingsActivity();
                break;
        }
        return true;
    }

    private void startSettingsActivity()
    {
        final Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void configureNewAlarmButton()
    {
        ImageView addAlarm = (ImageView) findViewById(R.id.addnewalarm);
        addAlarm.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View arg0)
            {
                openNewAlarmActivity();
            }

        });

        TextView addnewalarmtext = (TextView) findViewById(R.id.addnewalarmtext);
        addnewalarmtext.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                openNewAlarmActivity();
            }
        });
    }

    private void resetMenuItems(Menu menu)
    {
        for(int i=0; i<menu.size(); i++)
        {
            MenuItem item = menu.getItem(i);
            if (item.getItemId()==R.id.action_dropbox)
            {
                if(DropBox.connectedToDropBox(this))
                {
                    Drawable myIcon = getResources().getDrawable(R.drawable.dropbox);
                    item.setIcon(myIcon);
                }
                else
                {
                    Drawable myIcon = getResources().getDrawable(R.drawable.dropbox2);
                    item.setIcon(myIcon);
                }
            }
            if(item.getItemId() == R.id.action_lastfm)
            {
                if(Scrobbler.isConnected() && Scrobbler.isActive())
                {
                    Drawable myIcon = getResources().getDrawable(R.drawable.lastfmlogono);
                    item.setIcon(myIcon);
                }
                else
                {
                    Drawable myIcon = getResources().getDrawable(R.drawable.lastfmlogoyes);
                    item.setIcon(myIcon);
                }
            }
        }
    }

    private void redrawMenu()
    {
        invalidateOptionsMenu();
    }

    private void setScrobbler()
    {
        if(Scrobbler.isActive())
        {
            Scrobbler.useScrobler(false);
        }
        else
        {
            if(Scrobbler.hasLogInDataProvided())
            {
                Scrobbler.useScrobler(true);
            }
            else
            {
                Toast.makeText(this, "Please provide log-in data in the settings", Toast.LENGTH_LONG).show();//TODO: language
            }

        }
    }

    private void setDropBoxConnection()
    {
        if(DropBox.connectedToDropBox(this))
        {
            DropBox.disconnectFromDrobox(this);
        }
        else
        {
            DropBox.connectToDropBox(this);
        }
    }

    private void startUIUpdater()
    {
        updater= new UIUpdater();
        updater.run();
    }


    private void openNewAlarmActivity()
    {
        Intent i = new Intent(ctx, AmbientAlarmConfigurationActivity.class);
        i.putExtra("ambientAlarmID", "");
        ctx.startActivity(i);
    }

    private void fillListOfAmbientAlarms()
    {

        ambientalarms = AmbientAlarmManager.getListOfAmbientAlarms();
        ListView listViewWithAlarms = (ListView) findViewById(R.id.listOfAlarms);
        adapter = new AlarmListAdapter(ctx, ambientalarms);
        listViewWithAlarms.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listViewWithAlarms.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                Log.d(TAG, "longclock");
                if (!ambientalarms.get(position).iscurrentlylocked())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setMessage(R.string.deletealarm)
                            .setTitle(R.string.deletealarm);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            Log.d(TAG, "delete " + position);
                            AmbientAlarmManager.deleteAmbientAlarm(position);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    //builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                    //{
                    //    public void onClick(DialogInterface dialog, int id)
                    //    {
                    // User cancelled the dialog
                    //    }
                    //});
                    Log.d(TAG, "was geht?");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
                return false;
            }
        });

        listViewWithAlarms.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posi, long id)
            {
                Log.d(TAG, "click");
                if(!ambientalarms.get(posi).iscurrentlylocked())
                {
                    Intent i = new Intent(ctx, AmbientAlarmConfigurationActivity.class);
                    i.putExtra("ambientAlarmID", ambientalarms.get(posi).getAlarmID());
                    ctx.startActivity(i);
                }
            }

        });
    }


    private void authenticateDropBox()
    {
        try
        {
            if (DropBox.mDBApi.getSession().authenticationSuccessful())
            {
                try
                {
                    DropBox.mDBApi.getSession().finishAuthentication();
                    AccessTokenPair tokens = DropBox.mDBApi.getSession().getAccessTokenPair();
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

    private class  UIUpdater implements Runnable
    {
        private Handler handler = new Handler();
        public static final int delay= 1000;

        @Override
        public void run()
        {
            //Log.d(TAG, "run");
            adapter.notifyDataSetChanged();
            for(AmbientAlarm alarm : ambientalarms)
            {
                if(alarm.getStatus()== AlarmState.ALARM)
                {
                    alarm.awakeButtonPressed();
                }
            }
            handler.removeCallbacks(this); // remove the old callback
            handler.postDelayed(this, delay); // register a new one
        }

        public void onPause()
        {
            Log.d(TAG, "Activity update on Pause ");
            handler.removeCallbacks(this); // stop the map from updating
        }

        public void onResume()
        {
            handler.removeCallbacks(this); // remove the old callback
            handler.postDelayed(this, delay); // register a new one
        }

    }
}
