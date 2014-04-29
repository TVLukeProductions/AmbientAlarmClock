package de.lukeslog.alarmclock.ambientService.lastfm;

import android.content.SharedPreferences;
import android.util.Log;

import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleResult;

/**
 * Created by lukas on 31.03.14.
 */
public class Scrobbler
{
    public static String TAG = AlarmClockConstants.TAG;

    public static void scrobble(final String artist, final String song)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                SharedPreferences settings = ClockWorkService.settings;
                String lastfmusername = settings.getString("lastfmusername", "");
                String lastfmpassword = settings.getString("lastfmpassword", "");
                boolean scrobbletolastfm = settings.getBoolean("scrobble", false);
                if(!lastfmusername.equals("") && scrobbletolastfm)
                {
                    Session session=null;
                    try
                    {
                        Caller.getInstance().setCache(null);
                        session = Authenticator.getMobileSession(lastfmusername, lastfmpassword, LastFMConstants.key, LastFMConstants.secret);
                    }
                    catch(Exception e)
                    {
                        Log.e(TAG, e.getMessage());
                    }
                    if(session!=null)
                    {
                        int now = (int) (System.currentTimeMillis() / 1000);
                        ScrobbleResult result = Track.updateNowPlaying(artist, song, session);
                        result = Track.scrobble(artist, song, now, session);
                    }
                }
            }
        }).start();
    }

    public static boolean isConnected()
    {
        SharedPreferences settings = ClockWorkService.settings;
        if(settings!=null)
        {
            String lastfmusername = settings.getString("lastfmusername", "");
            String lastfmpassword = settings.getString("lastfmpassword", "");
            if (!lastfmusername.equals("") && !lastfmpassword.equals(""))
            {
                return true;
            }
        }
        return false;
    }

    public static void useScrobler(boolean b)
    {
        SharedPreferences settings = ClockWorkService.settings;

        SharedPreferences.Editor edit = settings.edit();
        edit.putBoolean("scrobble", b);
        edit.commit();
    }

    public static boolean isActive()
    {
        SharedPreferences settings = ClockWorkService.settings;
        boolean scrobbletolastfm = settings.getBoolean("scrobble", false);
        String lastfmusername = settings.getString("lastfmusername", "");
        String lastfmpassword = settings.getString("lastfmpassword", "");
        if(!lastfmusername.equals("") && !lastfmpassword.equals("") && scrobbletolastfm)
        {
            return scrobbletolastfm;
        }
        return false;
    }

    public static boolean hasLogInDataProvided()
    {
        SharedPreferences settings = ClockWorkService.settings;
        String lastfmusername = settings.getString("lastfmusername", "");
        String lastfmpassword = settings.getString("lastfmpassword", "");
        if(!lastfmusername.equals("") && !lastfmpassword.equals(""))
        {
            return true;
        }
        return false;
    }
}
