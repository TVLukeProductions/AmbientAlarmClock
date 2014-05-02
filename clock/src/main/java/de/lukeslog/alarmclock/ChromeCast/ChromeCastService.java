package de.lukeslog.alarmclock.ChromeCast;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.lang.reflect.Array;
import java.util.ArrayList;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

import static com.google.android.gms.cast.Cast.Listener;

/**
 * Created by lukas on 01.05.14.
 */
public class ChromeCastService extends Service
{
    public static final String ACTION_DISPLAY_DATA = "displaydata";
    public static final String ACTION_FIND_DEVICES = "finddevices";
    public static final String ACTION_STOP_DISPLAYING = "stopdisplaydata";

    public static String TAG = AlarmClockConstants.TAG+"_chromecast";

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private ConnectionCallbacks connectionCallbacks;
    private GoogleApiClient mApiClient;
    private String mSessionId;
    private boolean mApplicationStarted;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private boolean isPlaying;
    MediaMetadata mMediaMetadata;

    private static ArrayList<InfoOnRoute> infolist = new ArrayList<InfoOnRoute>();

    static ChromeCastService ctx;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "chromecast service start");
        findroute();
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "chromecast service");
        ctx=this;
        super.onCreate();

        registerIntentFilters();

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        // Create a MediaRouteSelector for the type of routes your app supports
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getResources()
                                .getString(R.string.app_id))).build();
        // Create a MediaRouter callback for discovery events
        mMediaRouterCallback = new MyMediaRouterCallback();

        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer.setOnStatusUpdatedListener(
                new RemoteMediaPlayer.OnStatusUpdatedListener()
        {
            @Override
            public void onStatusUpdated()
            {
                MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                Log.i(TAG, "Statusupdate->"+mediaStatus.getPlayerState());
                if(mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING || mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_BUFFERING)
                {
                    Log.d(TAG, "TRUE");
                    isPlaying = true;
                }
                if(mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_IDLE)
                {
                    Log.d(TAG, "FALSE");
                    isPlaying = false;
                }
            }
        });
        Log.d(TAG, "chromecast Service onCreate is done");

    }

    private void registerIntentFilters()
    {
        Log.d(TAG, "register intent filters");
        IntentFilter inf = new IntentFilter(ACTION_DISPLAY_DATA);
        IntentFilter inf2 = new IntentFilter(ACTION_FIND_DEVICES);
        IntentFilter inf3 = new IntentFilter(ACTION_STOP_DISPLAYING);
        registerReceiver(mReceiver, inf);
        registerReceiver(mReceiver, inf2);
        registerReceiver(mReceiver, inf3);
    }

    @Override
    public void onDestroy()
    {
        endSession();
        super.onDestroy();
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ACTION_DISPLAY_DATA))
            {
                Log.d(TAG, "ACTION DISPLAY DATA");
                String actionID = intent.getStringExtra("AmbientActionID");
                String devicename = intent.getStringExtra("ChromeCastDeviceName");
                selectRoute(devicename);
            }
            if(action.equals(ACTION_FIND_DEVICES))
            {
                Log.d(TAG, "ACTION FIND DEVICES");
                findroute();
            }
            if(action.equals(ACTION_STOP_DISPLAYING))
            {
                endSession();
            }
        }
    };

    private void selectRoute(String devicename)
    {
        for(InfoOnRoute info : infolist)
        {
            if(info.getInfo().getName().equals(devicename))
            {
                mMediaRouterCallback.onRouteSelected(info.getRouter(), info.getInfo());
            }
        }
    }

    private void findroute()
    {
        Log.d(TAG, "finroute()");
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent
                                .categoryForCast(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)).build();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    private void launchdevice()
    {
        Log.d(TAG, "launchdev");
        Listener castlistener = new Listener()
        {

            @Override
            public void onApplicationDisconnected(int errorCode)
            {
                Log.d(TAG, "application has stopped");
                endSession();
            }

        };
        Log.d(TAG, "now for the callbacks...");
        connectionCallbacks = new ConnectionCallbacks();
        ConnectionFailedListener mConnectionFailedListener = new ConnectionFailedListener();
        Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                .builder(mSelectedDevice, castlistener);
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptionsBuilder.build())
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();
        Log.d(TAG, "now we try to connect...");
        mApiClient.connect();
    }

    public static void stop()
    {
        if(ctx!=null)
        {
            ctx.endSession();
            ctx.stopSelf();
        }
    }

    public static ArrayList<String> getDevicesNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        for(InfoOnRoute info : infolist)
        {
            names.add(info.getInfo().getName());
        }
        return names;
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback
    {

        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo info)
        {
            Log.d(TAG, "onRouteSelected");
            // Handle route selection.

            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            Log.d(TAG, mSelectedDevice.getFriendlyName());
            launchdevice();

        }

        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo info)
        {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            mSelectedDevice = null;
        }

        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo info)
        {
            Log.d(TAG, "Route Added");
            Log.d(TAG, router.getDefaultRoute().getName());
            Log.d(TAG, router.getDefaultRoute().getId());
            Log.d(TAG, info.getName());
            Log.d(TAG, info.getId());
            InfoOnRoute ior = new InfoOnRoute(router, info);
            for(InfoOnRoute ix : infolist)
            {
                if(ix.getInfo().getName().equals(info.getName()))
                {
                    infolist.remove(ix);
                }
            }
            infolist.add(ior);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks
    {

        @Override
        public void onConnected(Bundle bundle)
        {
            Log.d(TAG, "onConnected");
            try
            {
                if (mApiClient != null)
                {
                    Log.d(TAG, "mApi is not null");
                    Cast.CastApi.launchApplication(mApiClient, CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID) .setResultCallback(
                            new ResultCallback<Cast.ApplicationConnectionResult>()
                            {
                                @Override
                                public void onResult(Cast.ApplicationConnectionResult applicationConnectionResult)
                                {
                                    try
                                    {
                                        Status status = applicationConnectionResult.getStatus();
                                        if (status.isSuccess())
                                        {
                                            displayimages();
                                        } else
                                        {
                                            Log.d(TAG, "status was not succes...");
                                            endSession();
                                        }
                                    }
                                    catch(Exception e)
                                    {
                                        Log.e(TAG, "Erroro in onResolt of Callback of the MastApi...");
                                    }
                                }
                            }
                    );
                }
            }
            catch(Exception e)
            {
                Log.e(TAG, "exception!!! "+e.getLocalizedMessage());
                endSession();
            }
        }


        @Override
        public void onConnectionSuspended(int i)
        {
            Log.d(TAG, "on connection suspended for callback");
        }
    }

    private void displayimages()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
                    ArrayList<MediaInfo> minfo = new ArrayList<MediaInfo>();
                    minfo.add(createMediaInfo("WACKEN", "https://farm8.staticflickr.com/7337/13089923454_c71416ee95_o.jpg"));
                    //minfo.add(createMediaInfo("TUTUT", "https://farm8.staticflickr.com/7448/13088237174_85814c9c52_o.jpg"));
                    minfo.add(createMediaInfo("Who let the dog out", "https://farm3.staticflickr.com/2330/13087938985_92ba837732_o.jpg"));
                    minfo.add(createMediaInfo("Audio test", "http://ondemand-mp3.dradio.de/file/dradio/2014/05/02/dlf_20140502_1417_ecd04bcd.mp3"));
                    minfo.add(createMediaInfo("Seconds Audio Test", "http://ondemand-mp3.dradio.de/file/dradio/2014/05/02/dlf_20140502_1400_20ce31d2.mp3"));
                    minfo.add(createMediaInfo("pic1", "https://farm8.staticflickr.com/7440/13814963745_751edf24b1_o.jpg"));

                    for(MediaInfo mediaInfo : minfo)
                    {
                        loadMediaToRemotePlayer(mediaInfo);
                        Thread.sleep(5000); //stuff needs to load first... and media type has to change
                        if(mediaInfo.getMetadata().getMediaType()==MediaMetadata.MEDIA_TYPE_PHOTO)
                        {
                            Log.d(TAG, "PHOTO...");
                            Thread.sleep(15000);
                        }
                        else
                        {
                            Log.d(TAG, "not a photo...");
                            while(isPlaying)
                            {
                                Log.d(TAG, "wait for stop playing...");
                                Thread.sleep(500);
                            }
                        }
                    }
                    Log.d(TAG, "end session...?");
                    new Handler().post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            endSession();
                        }
                    });
                }
                catch(Exception e)
                {
                    Log.e(TAG, "error...");
                    //endSession();

                }
            }
        }).start();
    }

    private void endSession()
    {
        Log.d(TAG, "endSession");
        try
        {
            mMediaRouter.removeCallback(mMediaRouterCallback);
        }
        catch (Exception e)
        {
            Log.e(TAG, "endSession exception 0: "+e.getLocalizedMessage());
        }
        try
        {
            Cast.CastApi.stopApplication(mApiClient);
        }
        catch(Exception e)
        {
            Log.e(TAG, "endSession exception 1: "+e.getLocalizedMessage());
        }
        try
        {
            mApiClient.disconnect();
            mApiClient = null;
            mSelectedDevice = null;
        }
        catch(Exception e)
        {
            Log.e(TAG, "endSession exception 2: "+e.getLocalizedMessage());
        }
    }

    private MediaInfo createMediaInfo(String name, String url)
    {
        String contenttype="image/jpg";
        mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        if(url.endsWith("mp4"))
        {
            contenttype="video/mp4";
            mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        }
        if(url.endsWith("gif"))
        {
            contenttype="image/gif";
            mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        }
        if(url.endsWith("jpeg"))
        {
            contenttype="image/jpeg";
            mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_PHOTO);
        }
        if(url.endsWith("mpeg"))
        {
            contenttype="video/mpeg";
            mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        }
        if(url.endsWith("mp3"))
        {
            contenttype="audio/mpeg";
            mMediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        }
        mMediaMetadata.putString(MediaMetadata.KEY_TITLE, name);
        MediaInfo mediaInfo = new MediaInfo.Builder(
                url)
                .setContentType(contenttype)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mMediaMetadata)
                .build();
        return mediaInfo;
    }

    private void loadMediaToRemotePlayer(MediaInfo mediaInfo)
    {
        try
        {
            mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
                    .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>()
                    {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult)
                        {
                            Status status = mediaChannelResult.getStatus();
                            Log.d(TAG, "on Result from image thing..." + status.getStatus());
                        }
                    });
        }
        catch (Exception e)
        {
            Log.e(TAG, "Problem while loading media", e);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener
    {
        @Override
        public void onConnectionFailed(ConnectionResult result)
        {
            Log.e(TAG, "onConnectionFailed ");

            endSession();
        }
    }

    private class InfoOnRoute
    {
        private MediaRouter router;
        private MediaRouter.RouteInfo info;

        public InfoOnRoute(MediaRouter router, MediaRouter.RouteInfo info)
        {
            this.router = router;
            this.info = info;
        }

        public MediaRouter getRouter()
        {
            return router;
        }

        public RouteInfo getInfo()
        {
            return info;
        }
    }

}
