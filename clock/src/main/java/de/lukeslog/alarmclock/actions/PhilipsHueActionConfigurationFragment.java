package de.lukeslog.alarmclock.actions;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Collection;
import java.util.List;

import de.jaetzold.philips.hue.HueBridge;
import de.jaetzold.philips.hue.HueLightBulb;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 24.04.14.
 */
public class PhilipsHueActionConfigurationFragment extends Fragment
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    ProgressBar connectbar;
    Button connectbutton;
    TextView text;
    PhilipsHueAction action;
    AmbientAlarm alarm;

    /** Called when the activity is first created. */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        View fragment = inflater.inflate(R.layout.philips_hue_action_activity, container, false);

        //well... this is kinda evil.
        ActionActivity parent = (ActionActivity) getActivity();
        action = (PhilipsHueAction) parent.getAction();
        alarm = parent.getAlarm();

        collorpicker(fragment);

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
                    @SuppressWarnings("unchecked")
                    public void run()
                    {
                        List<HueBridge> bridges = HueBridge.discover();
                        for(HueBridge bridge : bridges)
                        {
                            Log.d(TAG, "Found " + bridge);
                            // You may need a better scheme to store your username that to just hardcode it.
                            // suggestion: Save a mapping from HueBridge.getUDN() to HueBridge.getUsername() somewhere.
                            bridge.setUsername(PhilipsHueAction.BRIDGEUSERNAME);
                            if(!bridge.authenticate(false))
                            {
                                Log.d(TAG, "Press the button on your Hue bridge in the next 30 seconds to grant access.");
                                if(bridge.authenticate(true))
                                {
                                    Log.d(TAG, "Access granted. username: " + bridge.getUsername());
                                    Collection<HueLightBulb> lights = (Collection<HueLightBulb>) bridge.getLights();
                                    Log.d(TAG, "Available LightBulbs: "+lights.size());
                                    for (HueLightBulb bulb : lights)
                                    {
                                        Log.d(TAG, bulb.toString());
                                        PhilipsHueAction.identifiy(bulb);
                                    }
                                    System.out.println("");
                                }
                                else
                                {
                                    Log.d(TAG, "Authentication failed.");
                                }
                            }
                            else
                            {
                                Log.d(TAG, "Already granted access. username: " + bridge.getUsername());
                                Collection<HueLightBulb> lights = (Collection<HueLightBulb>) bridge.getLights();
                                Log.d(TAG, "Available LightBulbs: "+lights.size());
                                for (HueLightBulb bulb : lights)
                                {
                                    Log.d(TAG, bulb.toString());
                                    PhilipsHueAction.identifiy(bulb);
                                }
                                Log.d(TAG, "");
                            }
                        }
                    }
                }).start();
                new Countdown().execute();
            }

        });
        return fragment;
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
                Log.d(TAG, ""+progress);
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
                Log.d(TAG, ""+progress);
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
                Log.d(TAG, ""+progress);
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
            Log.d(TAG, "doInBackground");
            for(int i=0; i<30; i++)
            {
                try
                {
                    Log.d(TAG, "sleep");
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {

                    e.printStackTrace();
                }
                Log.d(TAG, "pp");
                publishProgress(i);
            }
            return 0l;
        }

        protected void onProgressUpdate(Integer... progress)
        {
            if(progress[0]==0)
            {
                Log.d(TAG, "set visble");
                connectbar.setVisibility(View.VISIBLE);
                Log.d(TAG, "p=0 start disc and auth");
            }
            text.setText("Press the button on your Hue bridge in the next 30 seconds to grant access.");
            connectbutton.setClickable(false);
            Log.d(TAG, "int p");
            double px = progress[0];
            int p = (int) (px/(0.3));
            if(progress[0]==29)
            {
                p=100;
            }
            Log.d(TAG, "p="+p);
            connectbar.setProgress(p);
            Log.d(TAG, "done");
        }

        protected void onPostExecute(Long result)
        {
            Log.d(TAG, "on post execute");
            connectbar.setVisibility(View.GONE);
            connectbutton.setClickable(true);
            text.setText("");
            Log.d(TAG, "on Post Execute 2");
        }
    }
}
