package de.lukeslog.alarmclock.actions;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.lukeslog.alarmclock.support.Logger;
import de.lukeslog.alarmclock.ui.AmbientAlarmActivity;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.R;

/**
 * Created by lukas on 24.04.14.
 */
public class PhilipsHueAction extends AmbientAction
{
    public static String BRIDGEUSERNAME = "552627b33010930f275b72ab1c7be258";
    static PHHueSDK phHueSDK = PHHueSDK.getInstance();
    static PHBridge bridge;
    static List<PHLight> lights = new ArrayList<PHLight>();
    boolean fadein = false;
    int red=255;
    int green=255;
    int blue =255;
    String lightsToTurnOn="";
    public static String lastKnownIP="";

    public PhilipsHueAction(String actionName)
    {
        super(actionName);

        connectToHueLights();
    }

    public PhilipsHueAction(ActionConfigBundle configBundle)
    {
        super(configBundle);
        try
        {
            red = Integer.parseInt(configBundle.getString("red"));
            green = Integer.parseInt(configBundle.getString("green"));
            blue = Integer.parseInt(configBundle.getString("blue"));
            fadein = configBundle.getString("fadein").equals("1");
            lightsToTurnOn = configBundle.getString("lightsToTurnOn");
            lastKnownIP = configBundle.getString("lastKnownIP");
        }
        catch(Exception e)
        {
            Logger.e(TAG, "Exception 1 "+e);
        }
    }

    public static List<PHLight> getLights() {
        Logger.d(TAG, "get Lights");
        if(lights.size()==0) {
            Logger.d(TAG, "lights is null-<-<-<-");
            if(bridge!=null) {

            } else {
                connectToHueLights();
            }
            if(bridge!=null) {
                PHBridgeResourcesCache cache = bridge.getResourceCache();
                // And now you can get any resource you want, for example:
                lights = cache.getAllLights();
                Logger.d(TAG, "and now?");
                if (lights != null) {
                    Logger.d(TAG, "no more");
                } else {
                    Logger.d(TAG, "still null... WHAT THE FUCK.");
                    Logger.d(TAG, "" + bridge.getResourceCache().getLights().size());
                }
            }
        }
        Logger.d(TAG, "Lights sizes: "+lights.size());
        return lights;
    }

    @Override
    public void action(boolean isFirstAlert)
    {
        Logger.e(TAG, "----------------------------------");
        Logger.e(TAG, "PHILIPS HUE ACTION LOG");
        if(fadein)
        {
            turnOnTheLightsSlowly();
        }
        else
        {
            turnOnTheLights();
        }
        Logger.e(TAG, "----------------------------------");
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
    public int getPriority()
    {
        return priority;
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
        configBundle.putString("red", ""+red);
        configBundle.putString("green", ""+green);
        configBundle.putString("blue", ""+blue);
        if(fadein)
        {
            configBundle.putString("fadein", "1");
        }
        else
        {
            configBundle.putString("fadein", "0");
        }
        configBundle.putString("lightsToTurnOn", lightsToTurnOn);
        configBundle.putString("lastKnownIP", lastKnownIP);
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

    public int getRed()
    {
        return red;
    }

    public int getGreen()
    {
        return green;
    }

    public int getBlue()
    {
        return blue;
    }

    public String getLightsToTurnOn()
    {
        return lightsToTurnOn;
    }

    public void setRed(int red)
    {
        this.red = red;
    }

    public void setGreen(int green)
    {
        this.green = green;
    }

    public void setBlue(int blue)
    {
        this.blue = blue;
    }

    public void setLightsToTurnOn(String lightsToTurnOn)
    {
        this.lightsToTurnOn = lightsToTurnOn;
    }

    protected void turnOnTheLights()
    {
        if(lights==null  || lights.size()==0) {
            connectToHueLights();
            int counter=0;
            while((lights==null  || lights.size()==0) && counter<100){
                try {
                    Thread.sleep(10);
                    counter++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lights(0);
        }
        try
        {
            lights(0);
        }
        catch(Exception e)
        {
            Logger.e(TAG, "Exception 2 "+e);
        }
    }

    protected void turnOnTheLightsSlowly()
    {
        if(lights==null || lights.size()==0) {
            connectToHueLights();
            int counter=0;
            while((lights==null  || lights.size()==0) && counter<100){
                try {
                    Thread.sleep(100);
                    counter++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            lights(50);
        }
        try
        {
            lights(50);
        }
        catch(Exception e)
        {
            Logger.e(TAG, "Exception 3 "+e);
        }
    }

    private static void authenticateHueSDK() {
        Logger.d(TAG, "SDK Authentication...");
        phHueSDK.setAppName(PhilipsHueAction.BRIDGEUSERNAME);
        phHueSDK.setDeviceName(android.os.Build.MODEL);
        phHueSDK.getNotificationManager().registerSDKListener(philipsHuelistener);


    }

    private void turnOfTheLights()
    {
        if(lights==null) {
            connectToHueLights();
        }
        if(lights!=null) {
            Logger.d(TAG, "Available LightBulbs : " + lights.size());
            for (PHLight bulb : lights) {
                if (lightsToTurnOn.contains(bulb.getName())) {
                    try {
                        setHueColor(bulb, 0, 0, 0, 0);
                    } catch (Exception e) {
                        Logger.e(TAG, e.getMessage());
                        Logger.e(TAG, "Exception 4 "+e);
                    }
                }
            }
        }
    }

    public static void connectToHueLights()
    {
        if(!lastKnownIP.equals("")) {
            Logger.d(TAG, "lastknownIP is not empty...");
            PHAccessPoint accessPoint = new PHAccessPoint();
            accessPoint.setIpAddress(lastKnownIP);
            accessPoint.setUsername(BRIDGEUSERNAME);
            connectToAccessPoint(accessPoint);
        }
        else {
            Logger.d(TAG, "lastKnownIP is empty...");
        }
    }

    public static void connectToAccessPoint(PHAccessPoint accessPoint) {
        Logger.d(TAG, "this should be an acces Point now...");
        if (lastKnownIP.equals("")) {
            lastKnownIP = accessPoint.getIpAddress();
        }
        Logger.d(TAG, "We should get the IP " + lastKnownIP);
        if (phHueSDK == null) {
            Logger.d(TAG, "sdk is null, we need to do studd");
            authenticateHueSDK();
        }
        if (phHueSDK != null) {
            Logger.d(TAG, "the sdk is not null");
            try {
                phHueSDK.setAppName(PhilipsHueAction.BRIDGEUSERNAME);
                phHueSDK.setDeviceName(android.os.Build.MODEL);
                phHueSDK.getNotificationManager().registerSDKListener(PhilipsHueAction.philipsHuelistener);
                accessPoint.setUsername(BRIDGEUSERNAME);
                Logger.d(TAG, "sdk has a name now... I guess!");
                phHueSDK.connect(accessPoint);
            } catch (Exception e) {
                Logger.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    public static void findBridges()
    {
        Logger.d(TAG, "XxXxXxX");
        List<PHBridge> bridges  = phHueSDK.getAllBridges();
        Logger.d(TAG, "we have "+bridges.size()+" bridges in the list");
        if(bridges.size()>0)
        {
            Logger.d(TAG, "now"+bridges.get(0));
            phHueSDK.setSelectedBridge(bridges.get(0));
            bridge=bridges.get(0);
            if(bridge!=null) {
                Logger.d(TAG, "OK, at least right now bridge is not null");
            }
            bridge.getResourceCache();
            getLights();
        } else if (bridges.size()==0) {
            //TODO: Stuff needs to be done
            if(bridge!=null){
                getLights();
            }
        }
        Logger.d(TAG, "done");
    }


    private void lights(final int i) {
        Logger.d(TAG, "Available LightBulbs : "+lights.size());
        for (PHLight bulb : lights) {
            if(lightsToTurnOn.contains(bulb.getName())) {
                try {
                    setHueColor(bulb, red, green, blue, i);
                }
                catch (Exception e) {
                    Logger.e(TAG, e.getMessage());
                    Logger.e(TAG, "Exception 5 "+e);
                }
            }
        }
    }

    public static void identifiy(final PHLight bulb)
    {
        PHLightState originalState = bulb.getLastKnownLightState();
        setHueColor(bulb, 255, 0, 255, 0);
        try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
        setHueColor(bulb, originalState, 0);
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        Logger.e(TAG, "Exception 6 "+e);
                        e.printStackTrace();
                    }
        setHueColor(bulb, 255, 0, 255, 0);
                    try
                    {
                        Thread.sleep(250);
                    }
                    catch (InterruptedException e)
                    {
                        Logger.e(TAG, "Exception 7 "+e);
                        e.printStackTrace();
                    }
        setHueColor(bulb, originalState, 0);
    }

    public static void setHueColor(final PHLight bulb, final int r, final int g, final int b, final int fadeIn)
    {
                float xy[] = PHUtilities.calculateXYFromRGB(r, g, b, bulb.getModelNumber());
                PHLightState lightState = new PHLightState();
                lightState.setX(xy[0]);
                lightState.setY(xy[1]);
                setHueColor(bulb, lightState, fadeIn);
    }

    public static void setHueColor(final PHLight bulb, final PHLightState lightState, final int fadeIn)
    {
        Logger.d(TAG, "we are trying to turn the bulb "+bulb.getName()+" ON");
        Logger.d(TAG, ""+bulb.getLightType().name());
        Logger.d(TAG, ""+bulb.supportsBrightness());
        Logger.d(TAG, ""+bulb.getIdentifier());
        if(bridge!=null) {
            new Thread(new Runnable() {
                public void run() {
                    lightState.setTransitionTime(fadeIn * 70);
                    lightState.setOn(true);
                    bridge.updateLightState(bulb, lightState);
                    bridge.updateLightState(bulb, lightState, new PHLightListener() {
                        @Override
                        public void onReceivingLightDetails(PHLight phLight) {

                        }

                        @Override
                        public void onReceivingLights(List<PHBridgeResource> phBridgeResources) {

                        }

                        @Override
                        public void onSearchComplete() {

                        }

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(int i, String s) {
                            Logger.e(TAG, s);
                        }

                        @Override
                        public void onStateUpdate(Map<String, String> stringStringMap, List<PHHueError> phHueErrors) {

                        }
                    });
                }
            }).start();
        }
    }

    // Local SDK Listener
    protected static PHSDKListener philipsHuelistener = new PHSDKListener() {

        @Override
        public void onAccessPointsFound(List accessPoint) {
            // Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
            // and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.
            Logger.d(TAG, "PHSDKListener: Access Point");
            if(accessPoint!=null) {
                Logger.d(TAG, "AccessPointList not null");
                if (accessPoint.size() > 0) {
                    Logger.d(TAG, "lets try to connect");
                    PHAccessPoint a = (PHAccessPoint) accessPoint.get(0);
                    connectToAccessPoint(a);
                }
            }
        }

        @Override
        public void onCacheUpdated(List cacheNotificationsList, PHBridge bridge) {
            // Here you receive notifications that the BridgeResource Cache was updated. Use the PHMessageType to
            // check which cache was updated, e.g.
            Logger.d(TAG, "PHSDKListener: on Cache Update");
            if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
                Logger.d(TAG, "Lights Cache Updated ");
            }
        }

        @Override
        public void onBridgeConnected(PHBridge b)
        {
            Logger.d(TAG, "PHSDKListener: on Bridge Connected");
            phHueSDK.setSelectedBridge(b);
            bridge=b;
            phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            // Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
            // At this point you are connected to a bridge so you should pass control to your main program/activity.
            // Also it is recommended you store the connected IP Address/ Username in your app here.
            // This will allow easy automatic connection on subsequent use.
            PHBridgeResourcesCache cache = phHueSDK.getSelectedBridge().getResourceCache();
            // And now you can get any resource you want, for example:
            lights = cache.getAllLights();
            findBridges();
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            Logger.d(TAG, "PHSDKListener: onAuthenticationRequred");
            phHueSDK.startPushlinkAuthentication(accessPoint);
            // Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
            // you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.
        }

        @Override
        public void onConnectionResumed(PHBridge b) {
            Logger.d(TAG, "PHSDKListener: onConnectionResumed");
            if(bridge==null) {
                bridge = b;
            }
        }

        @Override
        public void onConnectionLost(PHAccessPoint accessPoint) {
            Logger.d(TAG, "PHSDKListener: on Connection Lost");
            // Here you would handle the loss of connection to your bridge.
        }

        @Override
        public void onError(int code, final String message) {
            // Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
        }

        @Override
        public void onParsingErrors(List parsingErrorsList) {
            // Any JSON parsing errors are returned here.  Typically your program should never return these.
        }
    };
}
