package uk.ac.sussex.android.bluesensehub.uicontroller.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.model.SensorCommand;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.CommandsAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

/**
 * Created by ThiasTux.
 */

public class LoggingActionsButtonsFragment extends Fragment implements CommandsAdapter.ClickListener {

    @BindView(R.id.commands_list)
    RecyclerView commandsListRV;
    @BindView(R.id.no_command_label)
    TextView noCommandLabelTV;

    private DeviceHandler activity;
    private CommandsAdapter adapter;
    private ArrayList<SensorCommand> sensorCommandsList;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (DeviceHandler) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logging_actions_buttons, container, false);
        ButterKnife.bind(this, view);

        sensorCommandsList = getSavedCommands();

        if (sensorCommandsList != null) {
            noCommandLabelTV.setVisibility(View.GONE);
            commandsListRV.setVisibility(View.VISIBLE);

            adapter = new CommandsAdapter(getContext(), sensorCommandsList);
            adapter.setClickListener(this);

            int columnNum = (Utils.isTablet(getContext())) ? 3 : 2;
            commandsListRV.setLayoutManager(new GridLayoutManager(getContext(), columnNum));
            commandsListRV.setAdapter(adapter);
        } else {
            noCommandLabelTV.setVisibility(View.VISIBLE);
            commandsListRV.setVisibility(View.GONE);
        }

        return view;
    }

    private ArrayList<SensorCommand> getSavedCommands() {
        ArrayList<SensorCommand> commands;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Const.PREFS_NAME, 0);
        String savedCommands = sharedPreferences.getString(Const.PREFS_SAVED_COMMANDS, null);
        if (savedCommands != null) {
            Gson gson = new Gson();
            Type commandsListType = new TypeToken<ArrayList<SensorCommand>>() {
            }.getType();
            commands = gson.fromJson(savedCommands, commandsListType);
            return commands;
        } else {
            return null;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onItemClick(View v, int position) {
        SensorCommand command = sensorCommandsList.get(position);
        if (activity != null)
            activity.sendCommand(command);
    }

    public interface DeviceHandler {
        void sendCommand(SensorCommand command);
    }
}
