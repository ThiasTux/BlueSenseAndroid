package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.BluetoothServiceDelegate;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientBytesReceived;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnFailed;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnOngoing;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientDisconnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.services.BluetoothService;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.BluetoothState;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBSM;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTC;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTD;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTS;
import uk.ac.sussex.android.bluesensehub.utilities.Const;

/**
 * Created by ThiasTux.
 */

public class ConsoleSessionActivity extends AppCompatActivity implements BluetoothServiceDelegate {

    @BindView(R.id.console_textview)
    TextView consoleTextView;
    @BindView(R.id.commands_editext)
    AutoCompleteTextView commandsEditText;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private SharedPreferences commandsHistory;
    private Set<String> history;


    BlueSenseDevice device;
    private Map<String, BlueSenseDevice> devicesList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console_session);

        ButterKnife.bind(this);

        consoleTextView.setMovementMethod(new ScrollingMovementMethod());
        consoleTextView.setText("Session started.\n");

        setSupportActionBar(toolbar);

        devicesList = new HashMap<>();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Device");
            actionBar.setSubtitle("Status");
        }

        BluetoothService.setDelegate(this);
        startService(new Intent(this, BluetoothService.class)
                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        commandsHistory = getSharedPreferences(Const.PREFS_NAME, 0);
        history = commandsHistory.getStringSet(Const.PREFS_COMMANDS_HISTORY, new HashSet<String>());

        setAutoCompleteSource();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnectDevice(device.getAddress());
        savePrefs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stopServiceIntent = new Intent(ConsoleSessionActivity.this, BluetoothService.class);
        stopService(stopServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_console_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_change_device:
                chooseDevice();
            case R.id.action_refresh_connection:
                disconnectDevice(device.getAddress());
                connectDevice(device.getAddress());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setAutoCompleteSource() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, history.toArray(new String[history.size()]));
        commandsEditText.setAdapter(adapter);
        commandsEditText.setThreshold(1);
    }

    private void addSearchInput(String input) {
        if (!history.contains(input)) {
            history.add(input);
            setAutoCompleteSource();
        }
    }

    private void savePrefs() {
        SharedPreferences settings = getSharedPreferences(Const.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(Const.PREFS_COMMANDS_HISTORY, history);

        editor.apply();
    }


    private void connectDevice(String address) {
        startService(new Intent(this, BluetoothService.class)
                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                        new CommandBTC(address).getMessage()));
    }

    private void disconnectDevice(String address) {
        startService(new Intent(this, BluetoothService.class)
                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                        new CommandBTD(address).getMessage()));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void chooseDevice() {
        final String[] devices = devicesList.keySet().toArray(new String[devicesList.keySet().size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.choose_device)
                .setItems(devices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (device != null)
                            if (device.getStatus() == BluetoothState.STATE_CONNECTED) {
                                disconnectDevice(device.getAddress());
                            }
                        device = devicesList.get(devices[which]);
                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setTitle(device.getName());
                            actionBar.setSubtitle("Connecting");
                        }
                        consoleTextView.setText("");
                        connectDevice(device.getAddress());
                    }
                })
                .setCancelable(false);
        builder.create().show();
    }

    @OnClick(R.id.send_button)
    public void onSendClick() {
        String command = commandsEditText.getText().toString();
        addSearchInput(command);
        startService(new Intent(this, BluetoothService.class)
                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBSM(device.getAddress(), command + "\n").getMessage()));
        commandsEditText.setText("");

    }


    @Override
    public void onServiceStarted(List<BlueSenseDevice> devices) {
        for (BlueSenseDevice device : devices)
            devicesList.put(device.getAddress(), device);
        chooseDevice();
    }

    @Override
    public void onDeviceAdded(BlueSenseDevice device) {

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnected(ClientConnSuccess clientConnSuccess) {
        device.setStatus(BluetoothState.STATE_CONNECTED);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("Connected");
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnectionFailed(ClientConnFailed clientConnFailed) {
        ActionBar actionBar = getSupportActionBar();
        device = devicesList.get(clientConnFailed.getMAddress());
        device.setStatus(BluetoothState.STATE_NONE);
        if (actionBar != null) {
            actionBar.setSubtitle("Connection failed");
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceDisconnected(ClientDisconnSuccess clientDisconnSuccess) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("Disconnected");
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnection(ClientConnOngoing clientConnOngoing) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle("Connecting");
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageReceived(ClientBytesReceived clientBytesReceived) {
        consoleTextView.append(clientBytesReceived.getMessage());
    }
}
