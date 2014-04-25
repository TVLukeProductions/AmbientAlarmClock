package de.lukeslog.alarmclock.support;

import org.joda.time.DateTime;

/**
 * Created by lukas on 07.04.14.
 */
public class UISupport
{
    public static String secondsToCountdownString(int seconds)
    {
        int minutes=0;
        int hours =0;
        String minuteString="";
        String secondsString="";
        String hourString="";
        String unit = "s";
        if(seconds<0)
        {
            minuteString="00";
            secondsString="00";
        }
        else
        {
            if (seconds >= 60)
            {
                minutes = seconds / 60;
                seconds = seconds - (minutes * 60);
            }
            if(minutes >= 60)
            {
                hours = minutes / 60;
                minutes = minutes - (hours * 60);
            }
            if(hours < 10)
            {
                hourString = "0" + hours;
            }
            else
            {
                hourString = "" + hours;
            }
            if (minutes < 10)
            {
                minuteString = "0" + minutes;
            }
            else
            {
                minuteString = "" + minutes;
            }
            if (seconds < 10)
            {
                secondsString = "0" + seconds;
            }
            else
            {
                secondsString = "" + seconds;
            }
        }
        String countdown = minuteString+":"+secondsString+" m";
        if(hours>0)
        {
            countdown = hourString+":"+minuteString+" h";
        }
        return countdown;
    }

    public static String getTimeAsString(DateTime time)
    {
        String timeAsString="";
        int hour = time.getHourOfDay();
        int minute = time.getMinuteOfHour();
        String shour = ""+hour;
        String sminute = ""+minute;
        if(hour<10)
        {
            shour="0"+shour;
        }
        if(minute<10)
        {
            sminute = "0"+sminute;
        }
        timeAsString = shour+":"+sminute;
        return timeAsString;
    }
}
