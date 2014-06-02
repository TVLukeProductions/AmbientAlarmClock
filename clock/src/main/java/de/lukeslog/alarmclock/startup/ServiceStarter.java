package de.lukeslog.alarmclock.startup;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

//import de.lukeslog.alarmclock.ChromeCast.ChromeCastService;
import de.lukeslog.alarmclock.MediaPlayer.MediaPlayerService;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.datatabse.AmbientAlarmDatabase;
import de.lukeslog.alarmclock.ambientService.dropbox.DropBox;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;
import de.lukeslog.alarmclock.support.Radiostations;

/**
 * Created by lukas on 31.03.14.
 */
public class ServiceStarter
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    private static ArrayList<Class> classesToBoot = new ArrayList<Class>();
    public static Context ctx=null;

    public static void start(Context context) throws NullPointerException
    {
        ctx=context;

        AmbientAlarmDatabase.createDataBase(context);
        AmbientAlarmManager.updateListFromDataBase();

        setupBootClassArray();
        Radiostations.setUp();
        startServices();

        DropBox.getDropboxAPI();
        DropBox.ListAllFolders();
    }

    //This method adds the classes that ought to be started on Boot.
    //All Classes Need to be Services.
    private static void setupBootClassArray()
    {
        classesToBoot.add(ClockWorkService.class);
        classesToBoot.add(MediaPlayerService.class);
        classesToBoot.add(NotificationService.class);
        //classesToBoot.add(ChromeCastService.class);
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
            Logger.e(TAG, "Error on Autostart");
        }
    }
}
