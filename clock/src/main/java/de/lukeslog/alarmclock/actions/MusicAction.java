package de.lukeslog.alarmclock.actions;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.MediaPlayer.MediaPlayerService;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.ambientService.dropbox.DropBox;

/**
 * Created by lukas on 24.04.14.
 */
public class MusicAction extends AmbientAction
{
    private String localFolder = "WakeUpSongs";
    private boolean useLocal=true;
    private boolean useDropbox=false;
    private String dropboxFolder="";
    private boolean fadein = false;
    private String radiourl="DLF";
    private static boolean switchedToRadio =false;

    public static int priority = AmbientAction.ACTION_UI_PRIORITY_HIGH;

    public MusicAction(String actionName)
    {
        super(actionName);
        switchedToRadio =false;
    }

    public MusicAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
        try
        {
            localFolder = configBundle.getString("localFolder");
            dropboxFolder = configBundle.getString("dropBoxFolder");
            useLocal = configBundle.getString("uselocal").equals("1");
            useDropbox = configBundle.getString("usedropbox").equals("1");
            fadein = configBundle.getString("fadein").equals("1");
            radiourl = configBundle.getString("radiourl");
            switchedToRadio =false;
        }
        catch(Exception e)
        {

        }
    }

    @Override
    public void action(boolean isFirstAlert)
    {
        switchedToRadio =false;
        playmusic();
    }


    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        ActionConfigBundle configBundle = new ActionConfigBundle();
        configBundle.putString("localFolder", localFolder);
        configBundle.putString("dropBoxFolder", dropboxFolder);
        String uselocalS="0";
        if(useLocal)
        {
            uselocalS="1";
        }
        configBundle.putString("uselocal", uselocalS);
        String usedropboxS="0";
        if(useDropbox)
        {
            usedropboxS="1";
        }
        configBundle.putString("usedropbox", usedropboxS);
        String fadeS = "0";
        if(fadein)
        {
            fadeS="1";
        }
        configBundle.putString("fadein", fadeS);
        configBundle.putString("radiourl", radiourl);
        return configBundle;
    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void snooze()
    {
        if(isJoinSnoozing())
        {
            stopMusic();
        }
    }

    @Override
    public void awake()
    {
        stopMusic();
    }

    @Override
    public void tick(DateTime now)
    {
        if(now.getSecondOfMinute()==0 && now.getMinuteOfHour()%10==0)
        {
            ConnectivityManager connManager = (ConnectivityManager) ClockWorkService.getClockworkContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected())
            {
                DropBox.syncFiles(dropboxFolder);
            }
        }
    }

    @Override
    public void defineSettingsView(LinearLayout configView, AmbientAlarm alarm)
    {
        LinearLayout mainLayout = createLayout(configView, alarm);


        TextView name = createNameTextView(configView);

        ImageView icon = createActionIcon(configView);

        mainLayout.addView(icon);
        mainLayout.addView(name);
        configView.addView(mainLayout);
    }

    private TextView createNameTextView(LinearLayout configView)
    {
        TextView name = new TextView(configView.getContext());
        name.setText(getActionName());
        return name;
    }

    private ImageView createActionIcon(LinearLayout configView)
    {
        ImageView icon = new ImageView(configView.getContext());
        icon.setImageResource(R.drawable.music_action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);
        return icon;
    }

    @Override
    public Class getConfigActivity()
    {
        return MusicActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        //TODO: if there is no switch to radio button, make one
        LinearLayout content = (LinearLayout) alarmActivity.findViewById(R.id.content);
        View v;
        v = content.findViewById(89543);
        if(v!=null)
        {

        }
        else
        {
            final Button b = new Button(alarmActivity);
            b.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent switchtoRadio = new Intent();
                    switchtoRadio.setAction(MediaPlayerService.ACTION_SWITCH_TO_RADIO);
                    switchtoRadio.putExtra("AmbientActionID", getActionID());
                    ClockWorkService.getClockworkContext().sendBroadcast(switchtoRadio);
                    b.setVisibility(View.GONE);
                    switchedToRadio = true;
                }
            });
            b.setText("Switch To Radio");
            b.setId(89543);
            if(switchedToRadio)
            {
                b.setVisibility(View.GONE);
            }
            content.addView(b);
        }
    }

    private void playmusic()
    {
        Log.d(TAG, "play! says the music Action");
        Intent startmusic = new Intent();
        startmusic.setAction(MediaPlayerService.ACTION_START_MUSIC);
        startmusic.putExtra("AmbientActionID", getActionID());
        ClockWorkService.getClockworkContext().sendBroadcast(startmusic);
    }

    private void stopMusic()
    {
        Intent stopmusic = new Intent();
        stopmusic.setAction(MediaPlayerService.ACTION_STOP_MUSIC);
        stopmusic.putExtra("AmbientActionID", getActionID());
        ClockWorkService.getClockworkContext().sendBroadcast(stopmusic);
        switchedToRadio =false;
    }

    public boolean isUseLocal()
    {
        return useLocal;
    }

    public boolean isUseDropbox()
    {
        return useDropbox;
    }

    public String getDropboxFolder()
    {
        return dropboxFolder;
    }

    public String getLocalFolder()
    {
        return localFolder;
    }

    public void setUselocal(boolean uselocal)
    {
        this.useLocal = uselocal;
    }

    public void setUseDropbox(boolean useDropbox)
    {
        this.useDropbox = useDropbox;
    }

    public void setDropBoxFolder(String dropBoxFolder)
    {
        this.dropboxFolder = dropBoxFolder;
    }

    public boolean isFadein()
    {
        return fadein;
    }

    public void setFadein(boolean fadein)
    {
        this.fadein = fadein;
    }

    public void setLocalFolder(String localFolder)
    {
        this.localFolder = localFolder;
    }

    public String getRadiourl()
    {
        return radiourl;
    }

    public void setUseLocal(boolean useLocal)
    {
        this.useLocal = useLocal;
    }

    public void setRadioStation(String radioStation)
    {
        this.radiourl = radioStation;
    }

    public String getRadioURL()
    {
        return radiourl;
    }
}
