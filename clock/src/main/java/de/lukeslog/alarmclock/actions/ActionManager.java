package de.lukeslog.alarmclock.actions;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import org.knopflerfish.framework.FrameworkFactoryImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 15.04.14.
 */
public class ActionManager extends Service
{

    ArrayList<String> actionTypes = new ArrayList<String>();
    HashMap<String, Bundle> installedActions = new HashMap<String, Bundle>();
    Context context;

    Framework mFramework;

    String testName = "de.lukeslog.exampleplugin3";

    SharedPreferences settings;
    public static String TAG = AlarmClockConstants.TAG;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        //Log.d(TAG, "ClockWorkService onStartCommand()");
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this;
        //Log.d(TAG, "ClockWorkService onCreate()");
        settings = ClockWorkService.settings;

        setupOSGi();

        clearinstalation();

        installBundle("https://dl.dropbox.com/s/e6kn8harcsf7y17/de.lukeslog.exampleplugin3_1.0.0.jar");



        for(int i=0; i<100; i++)
        {
            Bundle[] b = mFramework.getBundleContext().getBundles();
            Log.d(TAG, "" + b.length);
            for (int j = 0; j < b.length; j++)
            {
                Log.d(TAG, b[j].getLocation());
                Log.d(TAG, b[j].getSymbolicName());
                if (b[j].getSymbolicName().equals(testName))
                {
                    i = 106;
                }
            }
            try
            {
                Thread.sleep(500);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        startBundles();
    }

    private void clearinstalation()
    {
        Bundle[] b2 = mFramework.getBundleContext().getBundles();
        for(int j=0; j<b2.length; j++)
        {
            if(b2[j].getSymbolicName().equals("System Bundle"))
            {

            }
            else
            {
                try
                {
                    b2[j].uninstall();
                }
                catch (Exception e)
                {

                }
            }
        }

    }

    private void startBundles()
    {
        //Log.d(TAG, "starting bundle " + bundle);

        long bid = -1;
        Bundle[] bl = mFramework.getBundleContext().getBundles();
        for (int i = 0; bl != null && i < bl.length; i++)
        {
            if (bl[i].getSymbolicName().equals(testName))
            {
                bid = bl[i].getBundleId();
            }
        }

        Log.d(TAG, "Bundle ID="+bid);

        Bundle b = mFramework.getBundleContext().getBundle(bid);
        if (b == null)
        {
            Log.e(TAG, "can't start bundle cause its null");
            return;
        }

        Log.d(TAG, b.toString());
        //try
        //{
            b.start();

            Log.d(TAG, "bundle " + b.getSymbolicName() + "/" + b.getBundleId() + "/"
                    + b + " started");
        //}
        //catch (Exception be)
        //{
        //    Log.e(TAG, "<<<"+be.toString());
        //}


    }

    private void setupOSGi()
    {
        Log.d(TAG, "set up OSGi");
        Map<String, String> fwprops = new Hashtable<String, String>();

        // add any framework properties to fwprops
        fwprops.put("org.osgi.framework.storage", "sdcard/fwdir");

        FrameworkFactory ff = new FrameworkFactoryImpl();
        mFramework = ff.newFramework(fwprops);

        try
        {

            mFramework.init();

        }
        catch (Exception e)
        {
            // framework initialization failed

            Log.d(TAG, e.getStackTrace().toString());

        }
    }

    private void installBundle(final String bundleURL)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                InputStream bundle = getBundlefromURL(bundleURL);
                installBundle(bundleURL, bundle);
                closeInputStream(bundle);
            }
        }).start();
    }

    private InputStream getBundlefromURL(String bundleURL)
    {
        InputStream bundle = null;
        Log.d(TAG, "installing bundle " + bundleURL);

        try
        {
            bundle = new URL(bundleURL).openStream();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
        return bundle;
    }

    private void installBundle(String bundleURL, InputStream bundle)
    {
        try
        {
            mFramework.getBundleContext().installBundle(bundleURL, bundle);
            Log.d(TAG, "bundle " + bundleURL + " installed");
        }
        catch (Exception be)
        {
            Log.e(TAG, be.toString());
        }
    }

    private void closeInputStream (InputStream bundle)
    {
        try
        {
            bundle.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.toString());
        }
    }
}
