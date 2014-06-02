package de.lukeslog.alarmclock.ambientService.mail;

import android.content.SharedPreferences;

import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;

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
        Logger.i(TAG, "gmailacc=" + gmailaccString);
        Logger.i(TAG, "newmail");
        final BackgroundMail m = new BackgroundMail(gmailaccString, gmailpswString);
        Logger.i(TAG, "setTo");
        String t[] = new String[1];
        t[0]= address;
        m.setTo(t);
        Logger.i(TAG, "Set From");
        m.setFrom(gmailaccString);
        Logger.i(TAG, "setSubject");
        String header=subject;
        Logger.i(TAG, "Sending with header="+header);
        m.setSubject(header);
        Logger.i(TAG, "setBody");
        String body=text+"\n \n Sincearly, \n your ambient alarm clock."; //TODO: localization
        Logger.i(TAG, "body \n"+body);
        m.setBody(body);
        Thread tt = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Logger.i(TAG, "send?");
                    m.send();

                }
                catch (Exception e)
                {
                    Logger.i(TAG, "cc"+e);
                    e.printStackTrace();
                }
            }

        });
        tt.start();
    }
}
