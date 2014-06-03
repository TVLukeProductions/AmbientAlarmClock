package de.lukeslog.alarmclock.ambientService.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.startup.ServiceStarter;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;

public class DropBox 
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static ArrayList<String> folders = new ArrayList<String>();
    public static boolean syncinprogress=false;
    public static String TAG = AlarmClockConstants.TAG;

    // In the class declaration section:
    public static DropboxAPI<AndroidAuthSession> mDBApi;
    final static private Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    public static void ListAllFolders()
    {
    	final DropboxAPI<AndroidAuthSession> mApi = mDBApi;
    	final ArrayList<String> folders2 = new ArrayList<String>();
    	Logger.d(TAG, "listallfolders");
    	new Thread(new Runnable() 
    	{
    	    public void run() 
    	    {
    	    	try
    	    	{ 
    	    		Entry dropboxDir1 = mApi.metadata("/", 0, null, true, null);    
    	    		if (dropboxDir1.isDir) 
    	    		{ 
    	    			List<Entry> contents1 = dropboxDir1.contents;
    	    			if (contents1 != null) 
    	    		    {
    	    		        for (int i = 0; i < contents1.size(); i++) 
    	    		        {
    	    		        	Entry e = contents1.get(i);
    	    		        	if(e.isDir)
    	    		        	{
    	    		        		Logger.i("clock", e.fileName());
    	    		        		folders2.add(e.fileName());
    	    		        	}
    	    		        }
    	    		    }
    	    		}
    	    		folders=folders2;
    	    	}
    	    	catch(Exception e)
    	    	{
    	    		Logger.e(TAG, "Dropbox->"+e.getMessage());
    	    	}
    	    }
    	}).start();
    }
    
    public static void syncFiles(final String dropfolderstring, final String alarmID, final String actionsubfolder)
	{
        final SharedPreferences settings = ClockWorkService.settings;
    	try
    	{
			Logger.d(TAG, "olol "+dropfolderstring);
			if(!dropfolderstring.equals(""))
			{
				final DropboxAPI<AndroidAuthSession> mApi = mDBApi;
				new Thread(new Runnable()
		    	{
		    	    public void run()
		    	    {
                        //only one action gets to sync at any time...
                        int cc=0;
                        while(syncinprogress && cc<100)
                        {
                            try
                            {
                                Thread.sleep(10000);
                                cc++;
                                //enough with the waiting
                                if(cc==99)
                                {
                                    syncinprogress=false;
                                }
                                Logger.i(TAG, "waiting for dropbox sync");
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                            //wait untill other processes have stoped syncing their dropbox
                        }
		    	    	syncinprogress=true;
	        			File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/AAC/"+alarmID+"/"+actionsubfolder+"/");
	        			folder.mkdirs();
	    	    		ArrayList<String> fileNames = new ArrayList<String>();
		    	    	try
		    	    	{ 
		    	    		//Log.d("clock", "try");
		    	    		//ArrayList<String> folderName=new ArrayList<String>();
	
		    	    		Entry dropboxDir1 = mApi.metadata("/"+dropfolderstring, 0, null, true, null);    
		    	    		if (dropboxDir1.isDir) 
		    	    		{ 
		    	    			List<Entry> contents1 = dropboxDir1.contents;
		    	    		    if (contents1 != null)
                                {
                                    if (connectedToWifi())
                                    {
                                        fileNames.clear();
                                        for (int i = 0; i < contents1.size(); i++)
                                        {
                                            Entry e = contents1.get(i);
                                            if (!e.isDir)
                                            {
                                                //Log.d("clock", e.fileName());
                                                if (e.fileName().endsWith(".mp3") || e.fileName().endsWith(".wav") || e.fileName().endsWith(".mp4"))
                                                {
                                                    if (connectedToWifi())
                                                    {
                                                        FileOutputStream outputStream = null;
                                                        try
                                                        {
                                                            Logger.d(TAG, "music file " + e.fileName());
                                                            fileNames.add(e.fileName());
                                                            String thelastchange = e.modified;
                                                            if (!(thelastchange.equals(settings.getString("lastchange" + e.fileName(), ""))))//last change has changed
                                                            {
                                                                Logger.d(TAG, e.fileName() + " needs an update->");
                                                                Editor edit = settings.edit();
                                                                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/AAC/" + alarmID + "/" + actionsubfolder + "/" + e.fileName());
                                                                folder.mkdirs();
                                                                //Log.d(TAG, "have new file");
                                                                outputStream = new FileOutputStream(file);
                                                                //Log.d(TAG, "have output stream for the file");
                                                                //Log.d(TAG, "String->"+dropboxDir1.fileName()+"/"+e.fileName());
                                                                mApi.getFile(dropboxDir1.fileName() + "/" + e.fileName(), null, outputStream, null);
                                                                //Log.d(TAG, "stuff with stuff");
                                                                edit.putString("lastchange" + e.fileName(), e.modified);
                                                                edit.commit();
                                                            } else
                                                            {
                                                                Logger.d(TAG, "  -> no change");
                                                            }
                                                        } catch (Exception em)
                                                        {
                                                            //Log.e("Something went wrong: " + em);
                                                            syncinprogress = false;
                                                        } finally
                                                        {
                                                            if (outputStream != null)
                                                            {
                                                                try
                                                                {
                                                                    outputStream.close();
                                                                } catch (IOException ef)
                                                                {

                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
		    	    		}
		    	    	}
		    	    	catch (Exception ex) 
		    	    	{
		    	    		Logger.e(TAG, "ERROR in the DropBox Class");
		    	    		Logger.e(TAG, "->"+ex.getMessage());
			    	    	syncinprogress=false;
		    	    	}
		    	    	List<File> files = getListFiles(folder); 
		    	    	Logger.d(TAG, "DROPBOX: compare local files to delete those that are not needed no more");
		    	    	for(int i=0; i<files.size(); i++)
		    	    	{
		    	    		//Log.d(TAG, files.get(i).getName());
		    	    		if(fileNames.contains(files.get(i).getName()))
		    	    		{
		    	    			//Log.i(TAG, "-> keep");
		    	    		}
		    	    		else
		    	    		{
		    	    			Logger.e(TAG, "DELETE" + files.get(i).getName());
		    	    			files.get(i).delete();
		    	    			Editor edit = settings.edit();
		    	    			edit.putString("lastchange"+files.get(i).getName(), "");
		            			edit.commit();
		    	    		}
		    	    	}
		    	    	syncinprogress=false;
		    	    }
		    	}).start();
			}
    	}
    	catch(Exception e)
    	{
    		//Hotfix. I know this is bad style but this has sometimes lead to bad exceptions and this functionaliyt is just not so important to let it potentially crash the app...
    		//TODO: better error handling 
    	}
    }

    private static boolean connectedToWifi()
    {
        Logger.d(TAG, "ARE WE CONNECTED TO WIFI?!?!?!?");
        ConnectivityManager connManager = (ConnectivityManager) ClockWorkService.getClockworkContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(mWifi.isConnected())
        {
            Logger.d(TAG, "WE ARE. BUT IS THERE INTERNET?");
            try
            {
                InetAddress host = InetAddress.getByName("http://www.google.com");
                boolean r = host.isReachable(1000);
                Logger.d(TAG, "wifi connected, google is reachable = "+r);
                return r;
            }
            catch (UnknownHostException e)
            {
                Logger.e(TAG, "1. "+e.getMessage());
                e.printStackTrace();
            }
            catch (IOException e)
            {
                Logger.e(TAG, "2. "+e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    //thanks http://stackoverflow.com/questions/9530921/list-all-the-files-from-all-the-folder-in-a-single-list
    private static List<File> getListFiles(File parentDir)
    {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
        if(files!=null)
        {
	        for (File file : files) 
	        {
	            if (file.isDirectory()) 
	            {
	            } 
	            else 
	            {
	            	inFiles.add(file);
	            }
	        }
	        return inFiles;
        }
        return inFiles;
    }

    public static DropboxAPI <AndroidAuthSession> getDropboxAPI() throws NullPointerException
    {
        AppKeyPair appKeys = new AppKeyPair(DropBoxConstants.appKey, DropBoxConstants.appSecret);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        SharedPreferences settings = ServiceStarter.ctx.getSharedPreferences(PREFS_NAME, 0);
        String dbkey = settings.getString("DB_KEY", "");
        String dbsecret= settings.getString("DB_SECRET", "");
        if (dbkey.equals("")) return null;
        AccessTokenPair access = new AccessTokenPair(dbkey, dbsecret);
        mDBApi.getSession().setAccessTokenPair(access);
        Logger.d(TAG, "GETTING DROPBOX API AND IT LOOKS LIKE THIS "+(mDBApi!=null));
        return mDBApi;
    }

    public static void connectToDropBox(Activity activity)
    {
        mDBApi.getSession().startAuthentication(activity);
    }

    public static void disconnectFromDrobox(Context ctx)
    {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        Editor edit = settings.edit();
        edit.putString("DB_KEY", "");
        edit.putString("DB_SECRET", "");
        edit.commit();
        DropBox.syncinprogress=false;
    }

    public static boolean connectedToDropBox(Context ctx)
    {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return !settings.getString("DB_KEY", "").equals("");
    }

    public static void storeKeys(String key, String secret, Context ctx)
    {
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        Editor edit = settings.edit();
        edit.putString("DB_KEY", key);
        edit.putString("DB_SECRET", secret);
        edit.commit();

    }
}
