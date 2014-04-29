package de.lukeslog.alarmclock.MediaPlayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;

import java.io.File;
import java.io.IOException;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.actions.ActionManager;
import de.lukeslog.alarmclock.actions.MusicAction;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.ambientService.lastfm.Scrobbler;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Radiostations;

/**
 * Created by lukas on 25.04.14.
 */
public class MediaPlayerService extends Service implements OnPreparedListener, OnCompletionListener, MediaPlayer.OnErrorListener
{
    public static final String ACTION_START_MUSIC = "startmusic";
    public static final String ACTION_STOP_MUSIC = "stopmusic";
    public static final String ACTION_SWITCH_TO_RADIO = "switchtoradio";
    public static final String ACTION_TURN_MEDIA_OFF = "turnitalloff";

    public static String TAG = AlarmClockConstants.TAG;

    MediaPlayer mp;

    private int[] mediaarray = {R.raw.trance};

    public static final String ADDR_DRADIO = "DLF";

    private String localfolderstring;
    private boolean uselocalchecked;
    private boolean usedropboxchecked;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        //Log.d(TAG, "ClockWorkService onStartCommand()");
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mp=null;
        registerIntentFilters();
    }

    private void registerIntentFilters()
    {
        IntentFilter inf = new IntentFilter(ACTION_START_MUSIC);
        IntentFilter inf2 = new IntentFilter(ACTION_STOP_MUSIC);
        IntentFilter inf3 = new IntentFilter(ACTION_SWITCH_TO_RADIO);
        registerReceiver(mReceiver, inf);
        registerReceiver(mReceiver, inf2);
        registerReceiver(mReceiver, inf3);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mp=null;
    }

    public void play(MusicAction action)
    {
        if(mp==null)
        {
            if (action.isFadein())
            {
                fadein();
            }
            else
            {
                AudioManager audio;
                audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, 15, AudioManager.FLAG_VIBRATE);
            }
            localfolderstring = action.getLocalFolder();
            uselocalchecked = action.isUseLocal();
            usedropboxchecked = action.isUseDropbox();
            playmp3();
        }
    }

    private void playmp3()
    {
        boolean mExternalStorageAvailable = false;
        String state = Environment.getExternalStorageState();
        Log.d(TAG, "Go Play 3");
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            // We can read and write the media
            mExternalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            // We can only read the media
            mExternalStorageAvailable = true;
        } else
        {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }
        if (mExternalStorageAvailable)
        {
            Log.i(TAG, Environment.getExternalStorageState());
            File filesystem = Environment.getExternalStorageDirectory();
            if (usedropboxchecked)
            {
                localfolderstring = filesystem.getPath() + "/Music/WakeUpSongs/";
            }
            File file = new File(localfolderstring);
            Log.d(TAG, "wakeupsongsX");
            File[] filelist3 = new File[0];
            Log.d(TAG, "--1");
            try
            {
                filelist3 = file.listFiles();
            } catch (Exception e)
            {
                Log.d(TAG, "Could not get Filelist");
            }
            Log.d(TAG, "--2");
            //count mp3s
            int numberOfMp3 = 0;
            String musicpath = "";
            if (filelist3 != null)
            {
                for (int i = 0; i < filelist3.length; i++)
                {
                    Log.d(TAG, "--2b");
                    if (filelist3[i].getName().endsWith("mp3"))
                    {
                        Log.d(TAG, "Number of MP3s");
                        numberOfMp3++;
                    }
                }
                Log.d(TAG, "FILELIST LENGTH " + numberOfMp3);
                if (numberOfMp3 > 0)
                {
                    int randomsongnumber = (int) (Math.random() * (filelist3.length));
                    musicpath = filelist3[randomsongnumber].getAbsolutePath();
                    File f = new File(musicpath);
                    String artist = "";
                    String song = "";
                    try
                    {
                        MP3File mp3 = new MP3File(f);
                        ID3v1 id3 = mp3.getID3v1Tag();
                        artist = id3.getArtist();
                        Log.d(TAG, "----------->ARTIST:" + artist);
                        song = id3.getSongTitle();
                        Log.d(TAG, "----------->SONG:" + song);
                        Scrobbler.scrobble(artist, song);
                    } catch (IOException e1)
                    {
                        e1.printStackTrace();
                    } catch (TagException e1)
                    {
                        e1.printStackTrace();
                    } catch (Exception ex)
                    {
                        Log.e(TAG, "There has been an exception while extracting ID3 Tag Information from the MP3");
                    }
                }
            }
            try
            {
                mp = new MediaPlayer();
                if (numberOfMp3 == 0)
                {
                    Log.d(TAG, "DEFAULT FILE");
                    mp = MediaPlayer.create(this, mediaarray[0]);
                    //mp.setLooping(true);
                    mp.setVolume(0.99f, 0.99f);
                    mp.setOnCompletionListener(this);
                    mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                    mp.setOnPreparedListener(this);
                } else
                {
                    mp.setDataSource(musicpath);
                    mp.setLooping(false);
                    mp.setVolume(0.99f, 0.99f);
                    Log.d(TAG, "...");
                    mp.setOnCompletionListener(this);
                    mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                    Log.d(TAG, "....");
                    mp.setOnPreparedListener(this);
                    Log.d(TAG, ".....");
                    mp.prepareAsync();
                }


            } catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            } catch (IllegalStateException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        } else
        {
            Log.d(TAG, "not read or writeable...");
        }
    }

    public void stop()
    {
        Log.d(TAG, "stop Media Player Service");
        mp.stop();
        mp.release();
        mp=null;
    }

    private void fadein()
    {
        final AudioManager audio;
        audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        new Thread(new Runnable()
        {
            @SuppressWarnings("static-access")
            public void run()
            {
                for (int x = 0; x < 16; x++)
                {
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, x, AudioManager.FLAG_VIBRATE);
                    try
                    {
                        Thread.currentThread().sleep(12000);
                    } catch (Exception ie)
                    {

                    }
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void turnOnRadio(String station)
    {
        if(mp!=null)
        {
            mp.stop();
            mp.release();
        }
        Log.d("clock", "turnOnRadio");
        try
        {
            Log.d(TAG, "try");
            mp = new MediaPlayer();
            mp.setScreenOnWhilePlaying(true);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Log.d(TAG, "Station---------------------" + station);
            try
            {
                mp.setDataSource(station);
            } catch (Exception e)
            {
                Log.d(TAG, "default radio");
                try
                {
                    mp.setDataSource(Radiostations.stations.get("DLF"));
                }
                catch (Exception ex)
                {
                    Log.d(TAG, "fuck this");
                }
            }
            mp.setVolume(0.99f, 0.99f);
            mp.setOnCompletionListener(this);
            mp.setOnPreparedListener(this);
            mp.prepareAsync();

        } catch (Exception ee)
        {
            Log.e("Error", "No Stream");
        }
    }

    @Override
    public void onPrepared(MediaPlayer mpx)
    {
        Log.d(TAG, "on Prepared!");
        mpx.setOnCompletionListener(this);
        Log.d(TAG, "ok, I'v set the on Completion Listener again...");
        mpx.start();
    }

    @Override
    public void onCompletion(MediaPlayer mpx)
    {
        Log.d(TAG, "on Completetion");
        playmp3();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.d(TAG, "MEDIAPLAYER ON ERROR");
        playmp3();
        return true;
    }

    private void stopeverything()
    {
        if(mp!=null)
        {
            mp.stop();
            mp.release();
            mp=null;
        }
        this.stopSelf();
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ACTION_START_MUSIC))
            {
                Log.d(TAG, "I GOT THE START MUSIC THING!");
                String actionID = intent.getStringExtra("AmbientActionID");
                MusicAction ma = (MusicAction) ActionManager.getActionByID(actionID);
                play(ma);
            }
            if(action.equals(ACTION_STOP_MUSIC))
            {
                Log.d(TAG, "STOooooooooP");
                stop();
            }
            if(action.equals(ACTION_SWITCH_TO_RADIO))
            {
                Log.d(TAG, "switchtoradio....");
                String actionID = intent.getStringExtra("AmbientActionID");
                MusicAction ma = (MusicAction) ActionManager.getActionByID(actionID);
                String station = Radiostations.stations.get(ma.getRadiourl());
                turnOnRadio(station);
            }
            if(action.equals(ACTION_TURN_MEDIA_OFF))
            {
                Log.d(TAG, "turneverythingoff");
                stopeverything();
            }
        }
    };
}
