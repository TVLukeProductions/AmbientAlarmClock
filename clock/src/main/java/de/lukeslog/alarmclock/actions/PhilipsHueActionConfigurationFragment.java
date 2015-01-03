package de.lukeslog.alarmclock.actions;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHLight;

import java.util.ArrayList;
import java.util.List;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;

public class PhilipsHueActionConfigurationFragment extends Fragment
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    ProgressBar connectbar;
    Button connectbutton;
    TextView text;
    PhilipsHueAction action;
    AmbientAlarm alarm;
    static List<PHLight> lights = new ArrayList<PHLight>();
    View fragment;

    /** Called when the activity is first created. */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

       fragment = inflater.inflate(R.layout.philips_hue_action_activity, container, false);
        Logger.d(TAG+"_HueConfig", "config screen.");
        ActionActivity parent = (ActionActivity) getActivity();
        action = (PhilipsHueAction) parent.getAction();
        alarm = parent.getAlarm();

        action.connectToHueLights();

        collorpicker(fragment);

        fadeincheckbox(fragment);

        pupulatebulbselection();

        connectbar = (ProgressBar) fragment.findViewById(R.id.progressBar1);
        connectbutton = (Button) fragment.findViewById(R.id.findhue);
        text = (TextView) fragment.findViewById(R.id.secondstext);
        
        connectbutton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        Logger.d(TAG+"_HueConfig", "CONNECT TO HUE!!!");
                        PhilipsHueAction.phHueSDK.setAppName(PhilipsHueAction.BRIDGEUSERNAME);
                        Logger.d(TAG+"_HueConfig", "CONNECT TO HUE!!!");
                        PhilipsHueAction.phHueSDK.setDeviceName(android.os.Build.MODEL);
                        Logger.d(TAG+"_HueConfig", "CONNECT TO HUE!!!");
                        PhilipsHueAction.phHueSDK.getNotificationManager().registerSDKListener(PhilipsHueAction.philipsHuelistener);
                        Logger.d(TAG+"_HueConfig", "CONNECT TO HUE!!!");
                        PHBridgeSearchManager sm = (PHBridgeSearchManager) PhilipsHueAction.phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
                        Logger.d(TAG+"_HueConfig", "CONNECT TO HUE!!!");
                        sm.search(true, true);
                        Logger.d(TAG + "_HueConfig", "CONNECT TO HUE!!!");
                    }
                }).start();
                new Countdown().execute();
            }

        });
        return fragment;
    }


    private void pupulatebulbselection()
    {
        Logger.d(TAG, "try to populate the lightbulb section");
        new FindLightBulbs().execute();
    }

    private class FindLightBulbs extends AsyncTask<Integer, Integer, Long>
    {
        protected Long doInBackground(Integer... urls)
        {
            try
            {
                if(!PhilipsHueAction.lastKnownIP.equals("")) {
                        lights = PhilipsHueAction.getLights();
                }
                else {
                    Logger.d(TAG, "the LAst Known IP is somehow not set");
                }
            }
            catch (Exception e)
            {
                Logger.e(TAG+"_HueConfig", "ERROR"+e.toString() );
            }
            return 0l;
        }

        protected void onProgressUpdate(Integer... progress)
        {

        }

        protected void onPostExecute(Long result)
        {
            Logger.d(TAG+"_HueConfig", "onPostExecute");
            if(lights.size()>0)
            {
                LinearLayout space = (LinearLayout) fragment.findViewById(R.id.selectbulbs);
                Logger.d(TAG + "_HueConfig", "LIGHTS? " + lights.size());
                for (PHLight light : lights) {
                    final String lightName = light.getName();
                    CheckBox l = new CheckBox(fragment.getContext());
                    l.setText(light.getName());
                    if (action.getLightsToTurnOn().contains("_" + lightName + "_")) {
                        l.setChecked(true);
                    }
                    l.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                action.setLightsToTurnOn(action.getLightsToTurnOn() + "_" + lightName + "_");
                            } else {
                                action.setLightsToTurnOn(action.getLightsToTurnOn().replace("_" + lightName + "_", ""));
                            }

                        }
                    });
                    space.addView(l);
                }
            }
            else {
                new FindLightBulbs().execute();
                Logger.d(TAG, "lights is null");
            }
        }
    }

    private void fadeincheckbox(View fragment)
    {
        CheckBox fadein = (CheckBox) fragment.findViewById(R.id.fadein);
        fadein.setChecked(action.fadein);
        fadein.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                action.fadein=isChecked;
            }
        });
    }

    private void collorpicker(final View fragment)
    {
        SeekBar red = (SeekBar) fragment.findViewById(R.id.red);
        red.setMax(255);
        red.setProgress(action.getRed());
        SeekBar green = (SeekBar) fragment.findViewById(R.id.green);
        green.setMax(255);
        green.setProgress(action.getGreen());
        SeekBar blue = (SeekBar) fragment.findViewById(R.id.blue);
        blue.setMax(255);
        blue.setProgress(action.getBlue());

        setexamplecolor(fragment);

        red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                Logger.d(TAG, ""+progress);
                action.setRed(progress);
                setexamplecolor(fragment);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                Logger.d(TAG, ""+progress);
                action.setGreen(progress);
                setexamplecolor(fragment);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                Logger.d(TAG, ""+progress);
                action.setBlue(progress);
                setexamplecolor(fragment);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        Button test = (Button) fragment.findViewById(R.id.huetestbutton);
        test.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                action.turnOnTheLights();
            }
        });
    }

    private void setexamplecolor(View fragment)
    {
        TextView example = (TextView) fragment.findViewById(R.id.example);
        int r=action.getRed();
        int g=action.getGreen();
        int b=action.getBlue();
        example.setBackgroundColor(Color.rgb(r, g, b));

        SeekBar red = (SeekBar) fragment.findViewById(R.id.red);
        red.setBackgroundColor(Color.rgb(action.getRed(), 0, 0));
        SeekBar green = (SeekBar) fragment.findViewById(R.id.green);
        green.setBackgroundColor(Color.rgb(0, action.getGreen(), 0));
        SeekBar blue = (SeekBar) fragment.findViewById(R.id.blue);
        blue.setBackgroundColor(Color.rgb(0,0,action.getBlue()));
    }

    /**
     * @author lukas
     *
     */
    private class Countdown extends AsyncTask<Integer, Integer, Long>
    {
        protected Long doInBackground(Integer... urls)
        {
            Logger.d(TAG, "doInBackground");
            for(int i=0; i<30; i++)
            {
                try
                {
                    Logger.d(TAG, "sleep");
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {

                    e.printStackTrace();
                }
                Logger.d(TAG, "pp");
                publishProgress(i);
            }
            return 0l;
        }

        protected void onProgressUpdate(Integer... progress)
        {
            try {
                if (progress[0] == 0) {
                    Logger.d(TAG, "set visble");
                    connectbar.setVisibility(View.VISIBLE);
                    Logger.d(TAG, "p=0 start disc and auth");
                }
                String thirty = getResources().getString(R.string.thirtyhue);
                text.setText(thirty);
                connectbutton.setClickable(false);
                Logger.d(TAG, "int p");
                double px = progress[0];
                int p = (int) (px / (0.3));
                if (progress[0] == 29) {
                    p = 100;
                }
                Logger.d(TAG, "p=" + p);
                connectbar.setProgress(p);
                Logger.d(TAG, "done");
            }
            catch(Exception e)
            {
                //This can happen if the activity restarts during countdown... better to catch it.
            }
        }

        protected void onPostExecute(Long result)
        {
            Logger.d(TAG, "on post execute");
            connectbar.setVisibility(View.GONE);
            connectbutton.setClickable(true);
            text.setText("");
            Logger.d(TAG, "on Post Execute 2");
            pupulatebulbselection();
            Logger.d(TAG, "on Post Execute 3");

        }
    }
}
