package de.lukeslog.alarmclock.startup;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.lukeslog.alarmclock.actions.ActionManager;
import de.lukeslog.alarmclock.actions.CountdownAction;
import de.lukeslog.alarmclock.actions.SendMailAction;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.datatabse.AmbientAlarmDatabase;
import de.lukeslog.alarmclock.dropbox.DropBox;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Day;

/**
 * Created by lukas on 31.03.14.
 */
public class ServiceStarter
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    private static ArrayList<Class> classesToBoot = new ArrayList<Class>();
    public static Context ctx;

    public static void start(Context context) throws NullPointerException
    {
        ctx=context;

        AmbientAlarmDatabase.createDataBase(context);
        AmbientAlarmManager.updateListFromDataBase();

        setupBootClassArray();
        startServices();

        DropBox.getDropboxAPI();
        DropBox.ListAllFolders();
    }

    private static void setUpTestData()
    {
        AmbientAlarm testalarm = new AmbientAlarm();
        DateTime alarmtime = new DateTime();
        testalarm.setAlarmTime(alarmtime.plusSeconds(70));
        testalarm.setActive(true);
        testalarm.setAlarmStateForDay(Day.MONDAY, true);
        testalarm.setAlarmStateForDay(Day.TUESDAY, false);
        testalarm.setAlarmStateForDay(Day.WEDNESDAY, true);
        testalarm.setAlarmStateForDay(Day.THURSDAY, true);
        testalarm.setAlarmStateForDay(Day.FRIDAY, true);
        testalarm.setAlarmStateForDay(Day.SUNDAY, true);

        SendMailAction sma = new SendMailAction("Send mail 8 hours before", "lukeslog@googlemail.com", "olol", "test");
        testalarm.registerAction("+5", sma);

        CountdownAction cda = new CountdownAction("A Countdown", 120);
        testalarm.registerAction("0", cda);

        AmbientAlarmManager.addNewAmbientAlarm(testalarm);
    }
    //This method adds the classes that ought to be started on Boot.
    //All Classes Need to be Services.
    private static void setupBootClassArray()
    {
        //classesToBoot.add(OldClockService.class);
        classesToBoot.add(ClockWorkService.class);
        //classesToBoot.add(AmbientAlarmManager.class);
        classesToBoot.add(NotificationService.class);
    }

    /*
     * This method starts all Services that need to be started when the system boots.
     */
    private static void startServices()
    {
        for(int i=0; i<classesToBoot.size(); i++)
        {
            startServiceClass(classesToBoot.get(i));
        }
    }

    private static void startServiceClass(Class serviceClassToStart)
    {
        try
        {
            Intent startIntent = new Intent(ctx, serviceClassToStart);
            ctx.startService(startIntent);
        }
        catch(Exception e)
        {
            //Error Handling
            Log.e(TAG, "Error on Autostart");
        }
    }
}
