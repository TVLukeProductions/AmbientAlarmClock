package de.lukeslog.alarmclock.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;

import java.util.ArrayList;

import de.lukeslog.alarmclock.ambientalarm.AmbientAlarm;
import de.lukeslog.alarmclock.support.UISupport;
import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.support.Day;

public class AlarmListAdapter extends BaseAdapter
{

    ArrayList<AmbientAlarm> alarmList;
    Context context;
    LayoutInflater inflater;

    public AlarmListAdapter(Context context, ArrayList<AmbientAlarm> alarmList)
    {
        this.context=context;
        this.alarmList=alarmList;
    }

    @Override
    public int getCount()
    {
        return alarmList.size();
    }

    @Override
    public Object getItem(int i)
    {
        return alarmList.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.alarmlistrow, parent, false);
        try
        {
            AmbientAlarm ambientAlarm = alarmList.get(position);

            setAlarmIcon(itemView, ambientAlarm);
            setAlarmTime(itemView, ambientAlarm);
            if(ambientAlarm.isActive())
            {
                setRemainingTime(itemView, ambientAlarm.secondsToAlertTime(new DateTime()));
            }
            else
            {
                TextView remainingtime = (TextView) itemView.findViewById(R.id.remainingtimetoalarm);
                remainingtime.setText("");
            }
            setWeekdayImages(itemView, ambientAlarm);

        }
        catch(Exception e)
        {

        }
        return itemView;
    }

    private void setRemainingTime(View itemView, int i)
    {
        TextView remainingtime = (TextView) itemView.findViewById(R.id.remainingtimetoalarm);
        remainingtime.setText("("+ UISupport.secondsToCountdownString(i)+")");
    }

    private void setAlarmTime(View itemView, AmbientAlarm ambientAlarm)
    {
        TextView alarmtime = (TextView) itemView.findViewById(R.id.alarmtime);

        alarmtime.setText(UISupport.getTimeAsString(ambientAlarm.getAlarmTime()));
    }

    private void setAlarmIcon(View itemView, AmbientAlarm ambientAlarm)
    {
        ImageView alarmicon = (ImageView) itemView.findViewById(R.id.alarmicon);
        if(ambientAlarm.isActive())
        {
            if(ambientAlarm.isSnoozing())
            {
                alarmicon.setImageResource(R.drawable.alarmicon_a_s);
                if(ambientAlarm.isCurrentlyLocked())
                {
                    alarmicon.setImageResource(R.drawable.alarmicon_a_s_l);
                }
            }
            else
            {
                alarmicon.setImageResource(R.drawable.alarmicon_a);
                if(ambientAlarm.isCurrentlyLocked())
                {
                    alarmicon.setImageResource(R.drawable.alarmicon_a_l);
                }
            }
        }
        else
        {
            alarmicon.setImageResource(R.drawable.alarmicon_b);
        }
    }

    private void setWeekdayImages(View itemView, AmbientAlarm ambientAlarm)
    {
        ImageView monday = (ImageView) itemView.findViewById(R.id.monday);
        ImageView tuesday = (ImageView) itemView.findViewById(R.id.tuesday);
        ImageView wednesday = (ImageView) itemView.findViewById(R.id.wednesday);
        ImageView thursday = (ImageView) itemView.findViewById(R.id.thursday);
        ImageView friday = (ImageView) itemView.findViewById(R.id.friday);
        ImageView saturday = (ImageView) itemView.findViewById(R.id.saturday);
        ImageView sunday = (ImageView) itemView.findViewById(R.id.sunday);
        if(ambientAlarm.getActiveForDayOfTheWeek(Day.MONDAY))
        {
            monday.setImageResource(R.drawable.mo_a);
        }
        else
        {
            monday.setImageResource(R.drawable.mo);
        }
        if(ambientAlarm.getActiveForDayOfTheWeek(Day.TUESDAY))
        {
            tuesday.setImageResource(R.drawable.tu_a);
        }
        else
        {
            tuesday.setImageResource(R.drawable.tu);
        }
        if(ambientAlarm.getActiveForDayOfTheWeek(Day.WEDNESDAY))
        {
            wednesday.setImageResource(R.drawable.we_a);
        }
        else
        {
            wednesday.setImageResource(R.drawable.we);
        }
        if(ambientAlarm.getActiveForDayOfTheWeek(Day.THURSDAY))
        {
            thursday.setImageResource(R.drawable.th_a);
        }
        else
        {
            thursday.setImageResource(R.drawable.th);
        }
        if(ambientAlarm.getActiveForDayOfTheWeek(Day.FRIDAY))
        {
            friday.setImageResource(R.drawable.fr_a);
        }
        else
        {
            friday.setImageResource(R.drawable.fr);
        }
        if(ambientAlarm.getActiveForDayOfTheWeek(Day.SATURDAY))
        {
            saturday.setImageResource(R.drawable.sa_a);
        }
        else
        {
            saturday.setImageResource(R.drawable.sa);
        }
        if(ambientAlarm.getActiveForDayOfTheWeek(Day.SUNDAY))
        {
            sunday.setImageResource(R.drawable.su_a);
        }
        else
        {
            sunday.setImageResource(R.drawable.su);
        }
    }
}
