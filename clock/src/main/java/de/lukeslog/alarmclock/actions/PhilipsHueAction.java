package de.lukeslog.alarmclock.actions;

import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

import de.jaetzold.philips.hue.ColorHelper;
import de.jaetzold.philips.hue.HueBridge;
import de.jaetzold.philips.hue.HueLightBulb;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.R;

/**
 * Created by lukas on 24.04.14.
 */
public class PhilipsHueAction extends AmbientAction
{
    public static String BRIDGEUSERNAME = "552627b33010930f275b72ab1c7be258";
    List<HueBridge> bridges;
    Collection<HueLightBulb> lights;
    boolean lightshowX=true;

    public PhilipsHueAction(String actionName)
    {
        super(actionName);

        connectToHueLights();
    }

    public PhilipsHueAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
    }

    @Override
    public void action(boolean isFirstAlert)
    {
        if(isFirstAlert)
        {
            turnOnTheLightsSlowly();
        }
        else
        {
            turnOnTheLights();
        }
    }

    @Override
    public void snooze()
    {
        turnOfTheLights();
    }


    @Override
    public void awake()
    {

    }

    @Override
    public void tick(DateTime now)
    {

    }

    @Override
    public void defineSettingsView(final LinearLayout configView, AmbientAlarm alarm)
    {
        LinearLayout mainLayout = createLayout(configView, alarm);


        TextView name = createNameTextView(configView);

        ImageView icon = createActionIcon(configView);

        mainLayout.addView(icon);
        mainLayout.addView(name);
        configView.addView(mainLayout);
    }

    private TextView createNameTextView(LinearLayout configView)
    {
        TextView name = new TextView(configView.getContext());
        name.setText(getActionName());
        return name;
    }

    private ImageView createActionIcon(LinearLayout configView)
    {
        ImageView icon = new ImageView(configView.getContext());
        icon.setImageResource(R.drawable.light_action_icon);
        TableRow.LayoutParams params = new TableRow.LayoutParams(100, TableLayout.LayoutParams.WRAP_CONTENT);
        icon.setLayoutParams(params);
        return icon;
    }

    @Override
    protected ActionConfigBundle setConfigurationData()
    {
        ActionConfigBundle configBundle = new ActionConfigBundle();
        return configBundle;
    }

    @Override
    public Class getConfigActivity()
    {
        return PhilipsHueActionConfigurationFragment.class;
    }

    @Override
    public void updateUI(AmbientAlarm ambientAlarm, AmbientAlarmActivity alarmActivity)
    {
        //nothing to do;
    }

    private void turnOnTheLights()
    {
        try
        {
            lights(0);
        }
        catch(Exception e)
        {

        }
    }

    private void turnOnTheLightsSlowly()
    {
        try
        {
            lights(50);
        }
        catch(Exception e)
        {

        }
    }

    private void turnOfTheLights()
    {
        new Thread(new Runnable()
        {
            @SuppressWarnings("unchecked")
            public void run()
            {
                try
                {
                    bridges = HueBridge.discover();
                }
                catch(Exception e)
                {
                    Log.e(TAG, e.getMessage());
                }
                for(HueBridge bridge : bridges)
                {
                    bridge.setUsername(BRIDGEUSERNAME);
                    if(bridge.authenticate(true))
                    {
                        Log.d(TAG, "Access granted. username: " + bridge.getUsername());
                        try
                        {
                            lights = (Collection<HueLightBulb>) bridge.getLights();
                        }
                        catch(Exception e)
                        {
                            Log.e(TAG, e.getMessage());
                        }
                        Log.d(TAG, "Available LightBulbs : "+lights.size());
                        for (HueLightBulb bulb : lights)
                        {
                            try
                            {
                                Log.d(TAG, bulb.toString());
                                bulb.setOn(false);
                            }
                            catch(Exception e)
                            {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                    }
                    else
                    {
                        Log.d(TAG, "Authentication failed.");
                    }
                }
            }
        }).start();
    }

    private void connectToHueLights()
    {
        new Thread(new Runnable()
        {
            @SuppressWarnings("unchecked")
            public void run()
            {
                bridges = HueBridge.discover();
                for(HueBridge bridge : bridges)
                {
                    bridge.setUsername(BRIDGEUSERNAME);
                    if(bridge.authenticate(true))
                    {
                        Log.d(TAG, "Access granted. username: " + bridge.getUsername());
                        lights = (Collection<HueLightBulb>) bridge.getLights();
                        Log.d(TAG, "Available LightBulbs: "+lights.size());
                        for (HueLightBulb bulb : lights)
                        {
                            Log.d(TAG, bulb.toString());
                            //identifiy(bulb);
                        }
                        Log.d(TAG, "");
                    }
                    else
                    {
                        Log.d(TAG, "Authentication failed.");
                    }
                }
            }
        }).start();
    }

    private void lights(final int i)
    {
        new Thread(new Runnable()
        {
            @SuppressWarnings("unchecked")
            public void run()
            {
                try
                {
                    bridges = HueBridge.discover();
                }
                catch(Exception e)
                {
                    Log.e(TAG, e.getMessage());
                }
                for(HueBridge bridge : bridges)
                {
                    bridge.setUsername(BRIDGEUSERNAME);
                    if(bridge.authenticate(true))
                    {
                        Log.d(TAG, "Access granted. username: " + bridge.getUsername());
                        try
                        {
                            lights = (Collection<HueLightBulb>) bridge.getLights();
                        }
                        catch(Exception e)
                        {
                            Log.e(TAG, e.getMessage());
                        }
                        Log.d(TAG, "Available LightBulbs : "+lights.size());
                        for (HueLightBulb bulb : lights)
                        {
                            try
                            {
                                setHueColor(bulb, 255.0, 255.0, 255.0, i);
                                Thread.sleep(500);

                            }
                            catch(Exception e)
                            {
                                Log.e(TAG, e.getMessage());
                            }

                        }
                        //System.out.println("");
                    }
                    else
                    {
                        Log.d(TAG, "Authentication failed.");
                    }
                }
            }
        }).start();
    }

    public static void identifiy(final HueLightBulb bulb)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Log.d(TAG, bulb.toString());
                    boolean originalyon=false;
                    if(bulb.getOn())
                    {
                        originalyon=true;
                    }
                    Integer bri = null;
                    Integer hu = null;
                    Integer sa = null;
                    double cix = 0;
                    double ciy = 0;
                    int ct = 0;
                    if(originalyon)
                    {
                        bri = bulb.getBrightness();
                        hu = bulb.getHue();
                        sa = bulb.getSaturation();
                        cix = bulb.getCiex();
                        ciy = bulb.getCiey();
                        ct = bulb.getColorTemperature();
                        bulb.setOn(false);
                    }
                    try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    bulb.setOn(true);
                    bulb.setBrightness(ColorHelper.convertRGB2Hue("255255255").get("bri"));
                    bulb.setHue(ColorHelper.convertRGB2Hue("255255255").get("hue"));
                    bulb.setSaturation(ColorHelper.convertRGB2Hue("255255255").get("sat"));
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {

                        e.printStackTrace();
                    }
                    bulb.setOn(false);
                    try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    if(originalyon)
                    {
                        bulb.setOn(true);
                        bulb.setBrightness(bri);
                        bulb.setHue(hu);
                        bulb.setSaturation(sa);
                        bulb.setCieXY(cix, ciy);
                        bulb.setColorTemperature(ct);
                    }
                }
                catch(Exception e)
                {
                    Log.e(TAG, "error while setting lights 2");
                }
            }
        }).start();
    }

    public static void setHueColor(final HueLightBulb bulb, double r, double g, double b, final int fadein)
    {
        //method from http://www.everyhue.com/vanilla/discussion/166/hue-rgb-to-hsv-algorithm/p1
        //r = (float(rInt) / 255)
        r=r/255.0;
        //g = (float(gInt) / 255)
        g=g/255.0;
        //b = (float(bInt) / 255)
        b=b/255.0;

        if (r > 0.04045)
        {
            r = Math.pow(((r + 0.055) / 1.055), 2.4);
        }
        else
        {
            r = r / 12.92;
        }
        if (g > 0.04045)
        {
            g = Math.pow(((g + 0.055) / 1.055), 2.4);
        }
        else
        {
            g = g / 12.92;
        }
        if (b > 0.04045)
        {
            b = Math.pow(((b + 0.055) / 1.055), 2.4);
        }
        else
        {
            b = b / 12.92;
        }

        r = r * 100;
        g = g * 100;
        b = b * 100;

        //Observer = 2deg, Illuminant = D65
        //These are tristimulus values
        //X from 0 to 95.047
        //Y from 0 to 100.000
        //Z from 0 to 108.883
        double X = r * 0.4124 + g * 0.3576 + b * 0.1805;
        double Y = r * 0.2126 + g * 0.7152 + b * 0.0722;
        double Z = r * 0.0193 + g * 0.1192 + b * 0.9505;

        //Compute xyY
        double sum = X + Y + Z;
        double chroma_x = 0;
        double chroma_y = 0;
        if (sum > 0)
        {
            chroma_x = X / (X + Y + Z); //x
            chroma_y = Y / (X + Y + Z); //y
        }
        final double ch_x =chroma_x;
        final double ch_y = chroma_y;
        //int brightness = (int)(Math.floor(Y / 100 *254)); //luminosity, Y
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {

                    Log.d(TAG, "1");
                    bulb.setOn(true);
                    Log.d(TAG, "12");
                    bulb.setBrightness(0);
                    Log.d(TAG, "3");
                    bulb.setCieXY(ch_x , ch_y);
                    Log.d(TAG, "4");
                    if(fadein>0)
                    {
                        int steps = 255/fadein;
                        for(int i=0; i<=255; i=i+steps)
                        {
                            bulb.setBrightness(i);
                            Log.d(TAG, ""+i);
                            try
                            {
                                Thread.sleep(5000);
                            }
                            catch(Exception h)
                            {
                                Log.e(TAG, "thread sleep exception");
                            }

                        }
                    }
                    else
                    {
                        bulb.setBrightness(255);
                    }
                }
                catch(Exception e)
                {
                    Log.e(TAG, "there was an error when setting the lightbulb");
                }
            }
        }).start();
    }
}
