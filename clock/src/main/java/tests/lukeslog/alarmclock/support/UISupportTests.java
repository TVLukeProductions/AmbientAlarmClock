package tests.lukeslog.alarmclock.support;

import android.test.InstrumentationTestCase;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.support.UISupport;

/**
 * Created by lukas on 07.04.14.
 */
public class UISupportTests extends InstrumentationTestCase
{
    public void testSecondsToCountdownStringFor0() throws Exception
    {
        assertEquals("00:00", UISupport.secondsToCountdownString(0));
    }

    public void testSecondsToCountdownStringForMinusFour() throws Exception
    {
        assertEquals("00:00", UISupport.secondsToCountdownString(-4));
    }

    public void testSecondsToCountdownStringForSixty() throws Exception
    {
        assertEquals("01:00", UISupport.secondsToCountdownString(60));
    }

    public void testSecondsToCountdownStringForEightyOne() throws Exception
    {
        assertEquals("01:21", UISupport.secondsToCountdownString(81));
    }

    public void testSecondsToCountdownStringForTreeThousand() throws Exception
    {
        assertEquals("50:00", UISupport.secondsToCountdownString(3000));
    }

    public void testGetTimeAsStringForTwelveThirty() throws Exception
    {
        DateTime dt = new DateTime();
        dt = dt.withHourOfDay(12);
        dt = dt.withMinuteOfHour(30);
        assertEquals("12:30", UISupport.getTimeAsString(dt));
    }

}
