package tests.lukeslog.alarmclock.ambientalarm;

import android.test.InstrumentationTestCase;

import junit.framework.Assert;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.Day;

/**
 * Created by lukas on 15.04.14.
 */
public class AmbientAlarmTests extends InstrumentationTestCase
{


    public void testDaySettingsForNewAlarm() throws Exception
    {
        AmbientAlarm a = new AmbientAlarm();
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.MONDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.TUESDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.WEDNESDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.THURSDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.FRIDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.SATURDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.SUNDAY));
    }


    public void testDaySettingsForNonExistingDays() throws Exception
    {

        AmbientAlarm a = new AmbientAlarm();
        try
        {
            a.getActiveForDayOfTheWeek(8);
            Assert.fail("Calling getActiveForDayOfTheWeek() should have thrown an exception for the number 8");
        }
        catch(ArrayIndexOutOfBoundsException e)
        {
            //success
        }
    }

    public void testDaySettingsAfterDaysAreSet() throws Exception
    {
        AmbientAlarm a = new AmbientAlarm();
        a.setAlarmStateForDay(Day.MONDAY, true);
        a.setAlarmStateForDay(Day.THURSDAY, true);
        a.setAlarmStateForDay(Day.FRIDAY, true);
        a.setAlarmStateForDay(Day.SUNDAY, true);
        assertEquals(true, a.getActiveForDayOfTheWeek(Day.MONDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.TUESDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.WEDNESDAY));
        assertEquals(true, a.getActiveForDayOfTheWeek(Day.THURSDAY));
        assertEquals(true, a.getActiveForDayOfTheWeek(Day.FRIDAY));
        assertEquals(false, a.getActiveForDayOfTheWeek(Day.SATURDAY));
        assertEquals(true, a.getActiveForDayOfTheWeek(Day.SUNDAY));
    }

    public void testFindNextAlarm() throws Exception
    {

    }


}
