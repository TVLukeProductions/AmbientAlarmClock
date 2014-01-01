package de.lukeslog.alarmclock.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;


import de.lukeslog.alarmclock.main.ClockService;

public class DropBox 
{
    final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
    public static ArrayList<String> folders = new ArrayList<String>();
    public static boolean syncinprogress=false;
    
    public static void ListAllFolders()
    {
    	final DropboxAPI<AndroidAuthSession> mApi = ClockService.mDBApi;
    	folders.clear();
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
    	    		        		folders.add(e.fileName());
    	    		        	}
    	    		        }
    	    		    }
    	    		}
    	    	}
    	    	catch(Exception e)
    	    	{
    	    		Log.e("clock", "->"+e.getMessage());
    	    	}
    	    }
    	}).start();
    }
    
    public static void syncFiles(final SharedPreferences settings)
	{
		final String dropfolderstring = settings.getString("dropboxfolder", "");
		Log.d("clock", "olol "+dropfolderstring);
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
    	    		ArrayList<String> fileNames = new ArrayList<String>();
	    	    	try
	    	    	{ 
	    	    		Log.d("clock", "try");
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
	    	    		            String a = e.fileName();  
	    	    		            if(!e.isDir)
	    	    		            {
	    	    		            	Log.d("clock", e.fileName());
	    	    		            	if(e.fileName().endsWith(".mp3") || e.fileName().endsWith(".wav") || e.fileName().endsWith(".mp4"))
	    	    		            	{
	    	    		            		FileOutputStream outputStream = null;
	    	    		            		try 
	    	    		            		{
		    	    		            		Log.d("clock", "music file");
		    	    		            		fileNames.add(e.fileName());
		    	    		            		if(!e.modified.equals(settings.getString("lastchange"+e.fileName(), "")))//last change has changed
	   	    		            				{
		    	    		            			Editor edit = settings.edit();
		    	    		            			File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Music/WakeUpSongs/"+e.fileName());
		    	    		            			folder.mkdirs();
			    	    		            		Log.d("clock", "have new file");
			    	    		            		outputStream = new FileOutputStream(file);
			    	    		            		Log.d("clock", "have output stream for the file");
			    	    		            		Log.d("clock", "String->"+dropboxDir1.fileName()+"/"+e.fileName());
			    	    		            		mApi.getFile(dropboxDir1.fileName()+"/"+e.fileName(), null, outputStream, null);
			    	    		            		Log.d("clock", "stuff with stuff");
		    	    		            			edit.putString("lastchange"+e.fileName(), e.modified);
		    	    		            			edit.commit();
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
	    	    		Log.e("clock", "ERROR in the DropBox Class");
	    	    		Log.e("clock", "->"+ex.getMessage());
		    	    	syncinprogress=false;
	    	    	}
	    	    	List<File> files = getListFiles(folder); 
	    	    	Log.d("clock", "compare local files to delete those that are not needed no more");
	    	    	for(int i=0; i<files.size(); i++)
	    	    	{
	    	    		Log.d("clock", files.get(i).getName());
	    	    		if(fileNames.contains(files.get(i).getName()))
	    	    		{
	    	    			
	    	    		}
	    	    		else
	    	    		{
	    	    			Log.d("clock", "DELETE"+files.get(i).getName());
	    	    			files.get(i).delete();
	    	    		}
	    	    	}
	    	    	syncinprogress=false;
	    	    }
	    	}).start();
		}
    }
    
    //thanks http://stackoverflow.com/questions/9530921/list-all-the-files-from-all-the-folder-in-a-single-list
    private static List<File> getListFiles(File parentDir) 
    {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = parentDir.listFiles();
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
}
