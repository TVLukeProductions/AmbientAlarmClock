package de.lukeslog.alarmclock.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import org.joda.time.DateTime;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.ambientalarm.AmbientAlarmManager;
import de.lukeslog.alarmclock.support.AlarmClockConstants;
import de.lukeslog.alarmclock.support.Day;

/**
 * Created by lukas on 03.04.14.
 */
public class AmbientAlarmConfigurationActivity extends Activity
{

    public static final String PREFS_NAME = AlarmClockConstants.PREFS_NAME;
    public static String TAG = AlarmClockConstants.TAG;

    private static AmbientAlarm alarm;
    private static Context ctx;

    private static String alarmID ="";

    private UIUpdater updater;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ambient_alarm_configuration_activity);

        ctx = this;
        alarmID = getIntent().getStringExtra("ambientAlarmID");
        Log.d(TAG, "alarmID="+alarmID);
        if(alarmID.equals(""))
        {
            Log.d(TAG, "create new alarm");
            DateTime alarmtime = new DateTime();
            alarm = new AmbientAlarm();
            alarm.setAlarmTime(alarmtime.minusMinutes(1));
            alarm.setActive(false);
            alarm.setAlarmStateForDay(Day.MONDAY, true);
            alarm.setAlarmStateForDay(Day.TUESDAY, true);
            alarm.setAlarmStateForDay(Day.WEDNESDAY, true);
            alarm.setAlarmStateForDay(Day.THURSDAY, true);
            alarm.setAlarmStateForDay(Day.FRIDAY, true);
            alarm.setAlarmStateForDay(Day.SATURDAY, true);
            alarm.setAlarmStateForDay(Day.SUNDAY, true);
            alarm.setSnoozing(false);
            AmbientAlarmManager.addNewAmbientAlarm(alarm);
        }
        else
        {
            Log.d(TAG, "get Alarm from the Alarm manager");
            Log.d(TAG, "alarm!=null => "+ (alarm!=null));
            alarm = AmbientAlarmManager.getAlarmById(alarmID);
            Log.d(TAG, "alarm!=null => "+ (alarm!=null));
        }

        if(alarm.isCurrentlyLocked())
        {
            AmbientAlarmConfigurationActivity.this.finish();
        }

        configureBasicUI();

        fillinActions((LinearLayout) findViewById(R.id.actionlist));

        for(int i=0; i<((LinearLayout) findViewById(R.id.actionlist)).getChildCount(); i++)
        {
            if(i%2==0)
            {
                ((LinearLayout) findViewById(R.id.actionlist)).getChildAt(i).setBackgroundColor(0xfff00000);
            }
            else
            {
                ((LinearLayout) findViewById(R.id.actionlist)).getChildAt(i).setBackgroundColor(125432);
            }
        }

        addAddActionButton((LinearLayout) findViewById(R.id.addnew_layout));

        startUIUpdater();
    }

    private void startUIUpdater()
    {
        updater= new UIUpdater();
        updater.run();
    }

    @Override
    protected void onResume()
    {
        updater.onResume();
        fillinActions((LinearLayout) findViewById(R.id.actionlist));
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        updater.onPause();
        super.onPause();
    }


    private void addAddActionButton(LinearLayout configView)
    {
       ImageView addButton = (ImageView) findViewById(R.id.addactionimage);
       TextView addNewActionText = (TextView) findViewById(R.id.addactiontext);

        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                openNewActionActivity();
            }
        });
        addNewActionText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openNewActionActivity();
            }
        });
    }

    private void openNewActionActivity()
    {
        Intent intent = new Intent(this, NewAmbientAction.class);
        intent.putExtra("ambientAlarmID", alarm.getAlarmID());
        startActivity(intent);

    }

    private void configureBasicUI()
    {
        configureUIalarmSwritch();

        configureLock();

        configureUItimePicker();

        configureUIDaysOfTheWeek();

        configureUISnoozing();

        fillinActions((LinearLayout) findViewById(R.id.actionlist));
    }

    private void configureLock()
    {
        CheckBox locking = (CheckBox) findViewById(R.id.lockcheck);
        locking.setChecked(alarm.islocked());
        locking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                alarm.setLocked(b);
                updateDB();
            }
        });
    }

    private void configureUIDaysOfTheWeek()
    {
        ImageView monday = (ImageView) findViewById(R.id.monday);
        ImageView tuesday = (ImageView) findViewById(R.id.tuesday);
        ImageView wednesday = (ImageView) findViewById(R.id.wednesday);
        ImageView thursday = (ImageView) findViewById(R.id.thursday);
        ImageView friday = (ImageView) findViewById(R.id.friday);
        ImageView saturday = (ImageView) findViewById(R.id.saturday);
        ImageView sunday = (ImageView) findViewById(R.id.sunday);
        if(alarm.getActiveForDayOfTheWeek(Day.MONDAY))
        {
            monday.setImageResource(R.drawable.mo_a);
        }
        else
        {
            monday.setImageResource(R.drawable.mo);
        }
        if(alarm.getActiveForDayOfTheWeek(Day.TUESDAY))
        {
            tuesday.setImageResource(R.drawable.tu_a);
        }
        else
        {
            tuesday.setImageResource(R.drawable.tu);
        }
        if(alarm.getActiveForDayOfTheWeek(Day.WEDNESDAY))
        {
            wednesday.setImageResource(R.drawable.we_a);
        }
        else
        {
            wednesday.setImageResource(R.drawable.we);
        }
        if(alarm.getActiveForDayOfTheWeek(Day.THURSDAY))
        {
            thursday.setImageResource(R.drawable.th_a);
        }
        else
        {
            thursday.setImageResource(R.drawable.th);
        }
        if(alarm.getActiveForDayOfTheWeek(Day.FRIDAY))
        {
            friday.setImageResource(R.drawable.fr_a);
        }
        else
        {
            friday.setImageResource(R.drawable.fr);
        }
        if(alarm.getActiveForDayOfTheWeek(Day.SATURDAY))
        {
            saturday.setImageResource(R.drawable.sa_a);
        }
        else
        {
            saturday.setImageResource(R.drawable.sa);
        }
        if(alarm.getActiveForDayOfTheWeek(Day.SUNDAY))
        {
            sunday.setImageResource(R.drawable.su_a);
        }
        else
        {
            sunday.setImageResource(R.drawable.su);
        }
        monday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.setAlarmStateForDay(Day.MONDAY, !alarm.getActiveForDayOfTheWeek(Day.MONDAY));
                configureUIDaysOfTheWeek();
                updateDB();
            }
        });
        tuesday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.setAlarmStateForDay(Day.TUESDAY, !alarm.getActiveForDayOfTheWeek(Day.TUESDAY));
                configureUIDaysOfTheWeek();
                updateDB();
            }
        });
        wednesday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.setAlarmStateForDay(Day.WEDNESDAY, !alarm.getActiveForDayOfTheWeek(Day.WEDNESDAY));
                configureUIDaysOfTheWeek();
                updateDB();
            }
        });
        thursday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.setAlarmStateForDay(Day.THURSDAY, !alarm.getActiveForDayOfTheWeek(Day.THURSDAY));
                configureUIDaysOfTheWeek();
                updateDB();
            }
        });
        friday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.setAlarmStateForDay(Day.FRIDAY, !alarm.getActiveForDayOfTheWeek(Day.FRIDAY));
                configureUIDaysOfTheWeek();
                updateDB();
            }
        });
        saturday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.setAlarmStateForDay(Day.SATURDAY, !alarm.getActiveForDayOfTheWeek(Day.SATURDAY));
                configureUIDaysOfTheWeek();
                updateDB();
            }
        });
        sunday.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alarm.setAlarmStateForDay(Day.SUNDAY, !alarm.getActiveForDayOfTheWeek(Day.SUNDAY));
                configureUIDaysOfTheWeek();
                updateDB();
            }
        });
    }

    private void configureUISnoozing()
    {
        CheckBox snoozing = (CheckBox) findViewById(R.id.snoozing);
        snoozing.setChecked(alarm.isSnoozing());
        snoozing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                alarm.setSnoozing(b);
                updateDB();
            }
        });
        EditText snoozetime = (EditText)findViewById(R.id.snoozetimeinseconds);
        snoozetime.setText(""+alarm.getSnoozeTimeInSeconds());
        snoozetime.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String secs = editable.toString();
                if(secs.equals(""))
                {

                }
                else
                {
                    alarm.setSnoozeTimeInSeconds(Integer.parseInt(editable.toString()));
                }
                updateDB();
            }
        });
    }

    private void configureUItimePicker()
    {
        TimePicker ambientAlarmTime = (TimePicker) findViewById(R.id.ambientalarmtimepicker);
        ambientAlarmTime.setIs24HourView(true);
        ambientAlarmTime.setCurrentHour(alarm.getAlarmTime().getHourOfDay());
        ambientAlarmTime.setCurrentMinute(alarm.getAlarmTime().getMinuteOfHour());
        ambientAlarmTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener()
        {
            @Override
            public void onTimeChanged(TimePicker timePicker, int i, int i2)
            {
                DateTime alarmDateTime = new DateTime();
                alarmDateTime = alarmDateTime.withHourOfDay(i);
                alarmDateTime = alarmDateTime.withMinuteOfHour(i2);
                Log.d(TAG, "set this new alarm to "+alarmDateTime);
                alarm.setAlarmTime(alarmDateTime);
                updateDB();
            }
        });
    }

    private void configureUIalarmSwritch()
    {
        Switch ambientalarmswitch = (Switch) findViewById(R.id.ambientalarmswitch);
        Log.d(TAG, "alarmswitch!=null => "+(ambientalarmswitch!=null));
        Log.d(TAG, "alarm !=null => "+(alarm!=null));
        ambientalarmswitch.setChecked(alarm.isActive());
        ambientalarmswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                alarm.setActive(b);
                updateDB();
            }
        });
    }

    private void fillinActions(LinearLayout scrollView)
    {
        alarm.fillInActionView(scrollView);
    }

    private void updateDB()
    {
        AmbientAlarmManager.updateDataBaseEntry(alarm);
    }

    private class  UIUpdater implements Runnable
    {
        private Handler handler = new Handler();
        public static final int delay= 1000;
        int actions=0;

        @Override
        public void run()
        {
            //Log.d(TAG, "run");
            //if the number of registered actions has changed, redraw the list of actions
            if(actions!=alarm.numberOfRegisteredActions())
            {
                actions=alarm.numberOfRegisteredActions();
                fillinActions((LinearLayout) findViewById(R.id.actionlist));
                TextView x = new TextView(ctx);
                LinearLayout theView = (LinearLayout) findViewById(R.id.actionlist);
                theView.addView(x);
            }
            handler.removeCallbacks(this); // remove the old callback
            handler.postDelayed(this, delay); // register a new one
        }

        public void onPause()
        {
            Log.d(TAG, "Activity update on Pause ");
            handler.removeCallbacks(this); // stop the map from updating
        }

        public void onResume()
        {
            handler.removeCallbacks(this); // remove the old callback
            handler.postDelayed(this, delay); // register a new one
        }

    }
}
