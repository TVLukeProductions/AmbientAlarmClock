package tests.lukeslog.alarmclock.actions;

import android.test.InstrumentationTestCase;

import de.lukeslog.alarmclock.actions.CountdownAction;
import de.lukeslog.alarmclock.support.UISupport;

/**
 * Created by lukas on 07.04.14.
 */
public class CountDownActionTests extends InstrumentationTestCase
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

}
