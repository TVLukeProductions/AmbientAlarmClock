package tests;

import android.test.InstrumentationTestCase;

/**
 * Created by lukas on 07.04.14.
 */
public class ExampleUnitTest extends InstrumentationTestCase
{
    public void testMath() throws Exception {
        final int expected = 1;
        final int reality = 1;
        assertEquals(expected, reality);
    }
}
