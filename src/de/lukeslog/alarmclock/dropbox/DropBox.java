package de.lukeslog.alarmclock.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;

import de.lukeslog.alarmclock.main.ClockService;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

public class DropBox 
{
    public static ArrayList<String> folders = new ArrayList<String>();
    public static boolean syncinprogress=false;
    public static String TAG = AlarmClockConstants.TAG;
    
    public static void ListAllFolders()
    {
    	final DropboxAPI<AndroidAuthSession> mApi = ClockService.mDBApi;
    	final ArrayList<String> folders2 = new ArrayList<String>();
    	Log.d(TAG, "listallfolders");
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
    	    		        		Log.i("clock", e.fileName());
    	    		        		folders2.add(e.fileName());
    	    		        	}
    	    		        }
    	    		    }
    	    		}
    	    		folders=folders2;
    	    	}
    	    	catch(Exception e)
    	    	{
    	    		Log.e(TAG, "Dropbox->"+e.getMessage());
    	    	}
    	    }
    	}).start();
    }
    
    public static void syncFiles(final SharedPreferences settings)
	{
    	try
    	{
			final String dropfolderstring = settings.getString("dropboxfolder", "");
			Log.d(TAG, "olol "+dropfolderstring);
			//TODO: check if we are in wifi.
			if(!syncinprogress)
			{
				final DropboxAPI<AndroidAuthSession> mApi = ClockService.mDBApi;
				new Thread(new Runnable() 
		    	{
		    	    public void run() 
		    	    {
		    	    	syncinprogress=true;
	        			File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/WakeUpSongs/");
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
		    	    		     	fileNames.clear();
		    	    		        for (int i = 0; i < contents1.size(); i++) 
		    	    		        {
		    	    		        	Entry e = contents1.get(i);
		    	    		            if(!e.isDir)
		    	    		            {
		    	    		            	//Log.d("clock", e.fileName());
		    	    		            	if(e.fileName().endsWith(".mp3") || e.fileName().endsWith(".wav") || e.fileName().endsWith(".mp4"))
		    	    		            	{
		    	    		            		FileOutputStream outputStream = null;
		    	    		            		try 
		    	    		            		{
			    	    		            		Log.d(TAG, "music file");
			    	    		            		fileNames.add(e.fileName());
			    	    		            		if(!e.modified.equals(settings.getString("lastchange"+e.fileName(), "")))//last change has changed
		   	    		            				{
			    	    		            			Log.d(TAG, "->");
			    	    		            			Editor edit = settings.edit();
			    	    		            			File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/WakeUpSongs/"+e.fileName());
			    	    		            			folder.mkdirs();
				    	    		            		Log.d(TAG, "have new file");
				    	    		            		outputStream = new FileOutputStream(file);
				    	    		            		Log.d(TAG, "have output stream for the file");
				    	    		            		Log.d(TAG, "String->"+dropboxDir1.fileName()+"/"+e.fileName());
				    	    		            		mApi.getFile(dropboxDir1.fileName()+"/"+e.fileName(), null, outputStream, null);
				    	    		            		Log.d(TAG, "stuff with stuff");
			    	    		            			edit.putString("lastchange"+e.fileName(), e.modified);
			    	    		            			edit.commit();
		   	    		            				}	
			    	    		            		else
			    	    		            		{
			    	    		            			Log.d(TAG, "  -> no change");
			    	    		            		}
			    	    		            	} 
		    	    		            		catch (Exception em) 
		    	    		            		{
		    	    		            		    System.out.println("Something went wrong: " + em);
		    	    		    	    	    	syncinprogress=false;
		    	    		            		} 
		    	    		            		finally 
		    	    		            		{
		    	    		            		    if (outputStream != null) 
		    	    		            		    {
		    	    		            		        try 
		    	    		            		        {
		    	    		            		            outputStream.close();
		    	    		            		        } 
		    	    		            		        catch (IOException ef) 
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
		    	    	catch (Exception ex) 
		    	    	{
		    	    		Log.e(TAG, "ERROR in the DropBox Class");
		    	    		Log.e(TAG, "->"+ex.getMessage());
			    	    	syncinprogress=false;
		    	    	}
		    	    	List<File> files = getListFiles(folder); 
		    	    	Log.d(TAG, "compare local files to delete those that are not needed no more");
		    	    	for(int i=0; i<files.size(); i++)
		    	    	{
		    	    		Log.d(TAG, files.get(i).getName());
		    	    		if(fileNames.contains(files.get(i).getName()))
		    	    		{
		    	    			Log.i(TAG, "-> keep");
		    	    		}
		    	    		else
		    	    		{
		    	    			Log.d(TAG, "DELETE"+files.get(i).getName());
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
}
