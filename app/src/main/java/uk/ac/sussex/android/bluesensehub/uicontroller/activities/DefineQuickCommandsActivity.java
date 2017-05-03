package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.model.SensorCommand;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.CommandsPrefsAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;

/**
 * Created by ThiasTux.
 */

public class DefineQuickCommandsActivity extends AppCompatActivity implements CommandsPrefsAdapter.ClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.commands_list)
    ListView commandsListView;
    @BindView(R.id.no_command_label)
    TextView noCommandTextView;

    ArrayList<SensorCommand> commandsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_quick_commands);

        ButterKnife.bind(this);

        toolbar.setTitle(R.string.define_commands);
        setSupportActionBar(toolbar);

        initializeCommandsList();

    }

    private void initializeCommandsList() {
        commandsList = getCommands();
        if (commandsList != null) {
            noCommandTextView.setVisibility(View.GONE);
            commandsListView.setVisibility(View.VISIBLE);
            CommandsPrefsAdapter adapter = new CommandsPrefsAdapter(this, commandsList);
            adapter.setClickListener(this);
            commandsListView.setAdapter(adapter);
        } else {
            commandsList = new ArrayList<>();
        }
    }

    private ArrayList<SensorCommand> getCommands() {
        ArrayList<SensorCommand> commands;
        SharedPreferences sharedPreferences = getSharedPreferences(Const.PREFS_NAME, 0);
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

    @OnClick(R.id.add_command_button)
    public void onAddCommandClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View addCommandView = inflater.inflate(R.layout.add_command, null);
        final TextView commandNameTextView = ButterKnife.findById(addCommandView, R.id.command_name);
        final TextView commandValueTextView = ButterKnife.findById(addCommandView, R.id.command_value);
        builder.setView(addCommandView)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String commandName = commandNameTextView.getText().toString();
                        String commandValue = commandValueTextView.getText().toString();
                        SensorCommand command = new SensorCommand(commandName, commandValue);
                        commandsList.add(command);
                        refreshCommandList();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }

    private void refreshCommandList() {
        SharedPreferences sharedPreferences = getSharedPreferences(Const.PREFS_NAME, 0);
        Gson gson = new Gson();
        String commandListString = gson.toJson(commandsList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.PREFS_SAVED_COMMANDS, commandListString);
        editor.apply();
        initializeCommandsList();
    }

    @Override
    public void onClick(View v, int position) {
        final int commPos = position;
        SensorCommand command = commandsList.get(commPos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View addCommandView = inflater.inflate(R.layout.add_command, null);
        final TextView commandNameTextView = ButterKnife.findById(addCommandView, R.id.command_name);
        commandNameTextView.setText(command.getName());
        final TextView commandValueTextView = ButterKnife.findById(addCommandView, R.id.command_value);
        commandValueTextView.setText(command.getValue());
        builder.setView(addCommandView)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String commandName = commandNameTextView.getText().toString();
                        String commandValue = commandValueTextView.getText().toString();
                        SensorCommand command = new SensorCommand(commandName, commandValue);
                        commandsList.set(commPos, command);
                        refreshCommandList();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();
    }
}
