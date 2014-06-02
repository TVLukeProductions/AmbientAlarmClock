package de.lukeslog.alarmclock.main;

import org.joda.time.DateTime;

/**
 * Created by lukas on 29.04.14.
 */
public interface Timable
{
    public void notifyOfCurrentTime(DateTime currentTime);
}
