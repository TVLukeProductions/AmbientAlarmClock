package de.lukeslog.alarmclock.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import de.lukeslog.alarmclock.R;
import de.lukeslog.alarmclock.actions.ActionManager;
import de.lukeslog.alarmclock.support.AlarmClockConstants;

/**
 * Created by lukas on 15.04.14.
 */
public class ActionListAdapter extends BaseAdapter
{
    public static String TAG = AlarmClockConstants.TAG;

    private final ArrayList<HashMap<String, String>> actiontypes;
    Context context;
    LayoutInflater inflater;

    public ActionListAdapter(Context context)
    {
        ActionManager.updateActionList();
        actiontypes = ActionManager.getActionTypes();
        this.context=context;
    }

    @Override
    public int getCount()
    {
        return actiontypes.size();
    }

    @Override
    public HashMap<String, String> getItem(int position)
    {
        return actiontypes.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.action_list_row, parent, false);
        try
        {
            HashMap<String, String> actionInfo = actiontypes.get(position);
            String actionName = actionInfo.get("actionName");
            String iconName = actionInfo.get("actionIcon");

            int iconId = getIconIdFromName(iconName);

            setActionName(itemView, actionName);

            setActionIcon(itemView, iconId);
        }
        catch (Exception e)
        {

        }
        return itemView;
    }

    private int getIconIdFromName(String iconName)
    {
        return context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
    }

    private void setActionIcon(View itemView, int iconID)
    {
        ImageView iconField = (ImageView) itemView.findViewById(R.id.actionicon);
        iconField.setImageResource(iconID);
    }

    private void setActionName(View itemView, String actionName)
    {
        TextView actionNamefield = (TextView) itemView.findViewById(R.id.actionname);
        actionNamefield.setText(actionName);
    }

    public String getActionTypeNameOnItemClicked(int position)
    {
        HashMap<String, String> actionInfo = actiontypes.get(position);
        String actionTypeName = actionInfo.get("actionType");
        return actionTypeName;
    }
}
