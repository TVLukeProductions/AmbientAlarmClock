package de.lukeslog.alarmclock.actions;

import android.os.Bundle;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 15.04.14.
 */
public class ActionConfigBundle
{
    private Bundle content;
    public static String TAG = AlarmClockConstants.TAG;

    public ActionConfigBundle()
    {
        content=new Bundle();
    }

    public ActionConfigBundle(String semiColonSeparatedData)
    {
        content = new Bundle();
        StringTokenizer tk = new StringTokenizer(semiColonSeparatedData, ";");
        while(tk.hasMoreTokens())
        {
            try
            {
                String key = tk.nextToken();
                String value = tk.nextToken();
                content.putString(key, value);

            }
            catch(Exception e)
            {
                Log.d(TAG, "Exception in ActionConfigBundle Constructor");
            }
        }
    }

    public void setContent(Bundle content)
    {
        this.content = content;
    }

    public Bundle getContent()
    {
        return content;
    }

    @Override
    public String toString()
    {
        return stringRepresentation(content);
    }

    public String getString(String key)
    {
        return content.getString(key);
    }

    public void putString(String key, String value)
    {
        content.putString(key, value);
    }

    private String stringRepresentation(Bundle content)
    {
        String configString ="";
        Bundle configBundle = content;
        Set<String> keyset = configBundle.keySet();
        Iterator<String> iterator = keyset.iterator();
        while(iterator.hasNext())
        {
            String key = iterator.next();
            String value = (String) configBundle.get(key);
            configString=configString+""+key+";"+value+";";
        }
        return configString;
    }
}
