package de.lukeslog.alarmclock.ambientService.mail;

import android.content.SharedPreferences;
import android.util.Log;

import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 31.03.14.
 */
public class Mail
{
    public static String TAG = AlarmClockConstants.TAG;

    public static void sendMail(String address, String subject, String text)
    {
        SharedPreferences settings = ClockWorkService.settings;
        String gmailaccString= settings.getString("gmailacc", "");
        String gmailpswString= settings.getString("gmailpsw", "");
        Log.i(TAG, "gmailacc=" + gmailaccString);
        Log.i(TAG, "newmail");
        final BackgroundMail m = new BackgroundMail(gmailaccString, gmailpswString);
        Log.i(TAG, "setTo");
        String t[] = new String[1];
        t[0]= address;
        m.setTo(t);
        Log.i(TAG, "Set From");
        m.setFrom(gmailaccString);
        Log.i(TAG, "setSubject");
        String header=subject;
        Log.i(TAG, "Sending with header="+header);
        m.setSubject(header);
        Log.i(TAG, "setBody");
        String body=text+"\n \n Sincearly, \n your ambient alarm clock."; //TODO: localization
        Log.i(TAG, "body \n"+body);
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
