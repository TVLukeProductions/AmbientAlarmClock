package de.lukeslog.alarmclock.actions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.lukeslog.alarmclock.MediaPlayer.MediaPlayerService;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.main.ClockWorkService;
import de.lukeslog.alarmclock.ambientService.dropbox.DropBox;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Logger;
import de.lukeslog.alarmclock.support.Radiostations;

/**
 * Created by lukas on 24.04.14.
 */
public class MusicActionConfigurationFragment extends Fragment
{
    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;
    ArrayList<String> localFolderList;
    MusicAction action;
    AmbientAlarm alarm;

    /** Called when the activity is first created. */
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final View fragment = inflater.inflate(R.layout.music_action_activity, container, false);

        final SharedPreferences settings = ClockWorkService.settings;
        //well... this is kinda evil.
        ActionActivity parent = (ActionActivity) this.getActivity();
        action = (MusicAction) parent.getAction();
        alarm = parent.getAlarm();

        configureRadioSelection(fragment);

        boolean uselocalchecked = action.isUseLocal();
        boolean usedropboxchecked = action.isUseDropbox();
        final String dropfolderstring = action.getDropboxFolder();
        String localfolderstring = action.getLocalFolder();

        final Spinner dpfolderlist = (Spinner) fragment.findViewById(R.id.spinnerdpf);
        final Spinner localfolderlist = (Spinner) fragment.findViewById(R.id.spinnerlocalf);
        CheckBox fadeinbox = (CheckBox) fragment.findViewById(R.id.fade);

        fadeinbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                action.setFadein(isChecked);
            }
        });
        fadeinbox.setChecked(action.isFadein());

        DropBox.ListAllFolders();

        final EditText dropboxfolder = (EditText) fragment.findViewById(R.id.dropboxfolder);
        dropboxfolder.setText(dropfolderstring);
        dropboxfolder.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                action.setDropBoxFolder(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        EditText localfolder = (EditText) fragment.findViewById(R.id.localfolder);
        localfolder.setText(localfolderstring);
        localfolder.addTextChangedListener(new TextWatcher()
        {

            public void afterTextChanged(Editable s)
            {
                action.setLocalFolder(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        Logger.d(TAG, "2 use local" + uselocalchecked);
        Logger.d(TAG, "2 use dp"+usedropboxchecked);
        final CheckBox use_local = (CheckBox) fragment.findViewById(R.id.use_local);

        Logger.d(TAG, "3 use local"+uselocalchecked);
        Logger.d(TAG, "3 use dp"+usedropboxchecked);
        final CheckBox use_dropbox = (CheckBox) fragment.findViewById(R.id.use_dropbox);

        Logger.d(TAG, "4 use local"+uselocalchecked);
        Logger.d(TAG, "4 use dp"+usedropboxchecked);

        //FILLING THE SPINER FOR LOCAL FOLDERS
        localFolderList = new ArrayList<String>();
        //get the folders
        File filesystem = Environment.getExternalStorageDirectory();
        createFolderList(filesystem, 0);
        //clean folderlist
        for(int i=0; i<localFolderList.size(); i++)
        {

        }
        final List<String> localfolderspinerArray = new ArrayList<String>();
        int lsf = settings.getInt("selectedLocalFolder", 0);
        for(int i=0; i<localFolderList.size(); i++)
        {
            localfolderspinerArray.add(localFolderList.get(i));
            if(localFolderList.get(i).equals(localfolderstring))
            {
                lsf=i;
            }
        }
        localfolderlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                Logger.d(TAG, "selected");
                final EditText localfolder = (EditText) fragment.findViewById(R.id.localfolder);
                localfolder.setText(localfolderspinerArray.get(arg2));
                action.setLocalFolder(localfolderspinerArray.get(arg2));
                //use_local.setChecked(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

                Logger.d(TAG, "not selected");
            }
        });
        ArrayAdapter<String> localadapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, localfolderspinerArray);
        localadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        localadapter.notifyDataSetChanged();
        localfolderlist.setAdapter(localadapter);
        localfolderlist.setClickable(true);
        localfolderlist.setSelected(true);
        if(localFolderList.size()>=lsf)
        {
            localfolderlist.setSelection(lsf);
        }
        localadapter.notifyDataSetChanged();
        Logger.d(TAG, "5 use ocal"+uselocalchecked);
        Logger.d(TAG, "5 use dp"+usedropboxchecked);

        //---------------------------------------
        //FROM HERE ON WE FILL THE DROPBOX SPINER
        ArrayList<String> folderlist = DropBox.folders;
        Logger.d(TAG, "folderlistsize="+folderlist.size());
        final List<String> spinnerArray = new ArrayList<String>();
        int sf = settings.getInt("selectedfolder", 0);

        for(int i=0; i<folderlist.size(); i++)
        {
            spinnerArray.add(folderlist.get(i));
            if(folderlist.get(i).equals(dropfolderstring))
            {
                sf=i;
            }
        }
        dpfolderlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                Logger.d(TAG, "selected");
                final EditText dropboxfolder = (EditText) fragment.findViewById(R.id.dropboxfolder);
                dropboxfolder.setText(spinnerArray.get(arg2));
                //use_dropbox.setChecked(true);
                action.setDropBoxFolder(spinnerArray.get(arg2));
                AmbientAlarm alarm = AmbientAlarmManager.getAlarmByRegisteredAction(action.actionID);
                if(alarm!=null)
                {
                    DropBox.syncFiles(action.getDropboxFolder(), alarm.getAlarmID(), action.actionID);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

                Logger.d(TAG, "not selected");
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.notifyDataSetChanged();
        dpfolderlist.setAdapter(adapter);
        dpfolderlist.setClickable(true);
        dpfolderlist.setSelected(true);
        if(DropBox.folders.size()>=sf)
        {
            dpfolderlist.setSelection(sf);
        }
        adapter.notifyDataSetChanged();

        Logger.d(TAG, "6 use local"+uselocalchecked);
        Logger.d(TAG, "6 use dp"+usedropboxchecked);

        //make sure these are exclusive
        use_local.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                if(arg1)
                {
                    use_dropbox.setChecked(false);

                    action.setUseDropbox(false);
                }
            }
        });
        use_dropbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1)
            {
                if(arg1)
                {

                    final ArrayList<String> spinnerArray = DropBox.folders;
                    if(spinnerArray.size()>0)
                    {
                        final EditText dropboxfolder = (EditText) fragment.findViewById(R.id.dropboxfolder);
                        int df = dpfolderlist.getSelectedItemPosition();
                        if (df < 0)
                        {
                            df=0;
                        }
                        dropboxfolder.setText(spinnerArray.get(df));
                        use_local.setChecked(false);
                        action.setUseDropbox(true);
                        action.setUselocal(false);
                    }
                    else
                    {
                        use_dropbox.setChecked(false);
                        use_local.setChecked(true);

                        action.setUseDropbox(false);
                        action.setUselocal(true);
                    }
                }
            }
        });
        Logger.d(TAG, "7 use local"+uselocalchecked);
        Logger.d(TAG, "7 use dp"+usedropboxchecked);
        use_local.setChecked(uselocalchecked);
        use_dropbox.setChecked(usedropboxchecked);
        action.setUseDropbox(usedropboxchecked);
        action.setUselocal(uselocalchecked);
        return fragment;
    }

    private void configureRadioSelection(View fragment)
    {
        final Spinner radioselect = (Spinner) fragment.findViewById(R.id.spinnerradio);
        final List<String> list2 = new ArrayList<String>();
        HashMap<String, String> stations = Radiostations.stations;
        Set<String> stationnames = stations.keySet();
        Iterator<String> it = stationnames.iterator();
        while(it.hasNext())
        {
            String x = it.next();
            Logger.d(TAG, x);
            list2.add(x);
        }
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list2);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        radioselect.setAdapter(dataAdapter2);
        radioselect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                action.setRadioStation(list2.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
        String selectedstation = action.getRadioURL();
        for(int i=0; i<list2.size(); i++)
        {
            if(list2.get(i).equals(selectedstation))
            {
                radioselect.setSelection(i);
            }
        }
        final Button testlisten = (Button) fragment.findViewById(R.id.testradio);
        testlisten.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(testlisten.getText().equals("Stop"))
                {
                    Intent startmusic = new Intent();
                    startmusic.setAction(MediaPlayerService.ACTION_STOP_MUSIC);
                    startmusic.putExtra("AmbientActionID", action.getActionID());
                    ClockWorkService.getClockworkContext().sendBroadcast(startmusic);
                    testlisten.setText("Test");
                }
                else
                {
                    Intent startmusic = new Intent();
                    startmusic.setAction(MediaPlayerService.ACTION_SWITCH_TO_RADIO);
                    startmusic.putExtra("AmbientActionID", action.getActionID());
                    ClockWorkService.getClockworkContext().sendBroadcast(startmusic);
                    testlisten.setText("Stop");
                }
            }
        });
    }

    private void createFolderList(File f, int depth)
    {
        //Log.d(TAG, "checkfolderlist");
        if(f.isDirectory() && (depth<3 || localFolderList.size()<100))
        {
            File[] filelist = f.listFiles();
            localFolderList.add(f.getAbsolutePath());
            for(int j=0; j<filelist.length; j++)
            {
                if(filelist[j].isDirectory())
                {
                    createFolderList(filelist[j], depth+1);
                }
            }
        }
    }
}
