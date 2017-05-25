package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

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
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTC;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTD;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTDA;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTS;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandNBD;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.BlueSenseDevicesAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

public class MainActivity extends AppCompatActivity implements BlueSenseDevicesAdapter.ClickListener, BluetoothServiceDelegate {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bluesense_devices)
    RecyclerView bluesenseDevicesView;

    private BlueSenseDevicesAdapter adapter;
    private boolean doubleBackPressedOnce = false;
    private List<BlueSenseDevice> bluesenseDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.bluesense_devices_paired);
        }

        bluesenseDevicesView.setHasFixedSize(true);
        adapter = new BlueSenseDevicesAdapter(new ArrayList<BlueSenseDevice>());
        if (Utils.isTablet(this)) {
            bluesenseDevicesView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            bluesenseDevicesView.setLayoutManager(llm);
        }
        adapter.setClickListener(this);
        bluesenseDevicesView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth not supported. This application requires Bluetooth.",
                    Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, Const.REQUEST_ENABLE_BT);
        } else {
            BluetoothService.setDelegate(this);
            startService(new Intent(this, BluetoothService.class)
                    .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Intent stopServiceIntent = new Intent(MainActivity.this, BluetoothService.class);
        stopService(stopServiceIntent);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackPressedOnce) {
            Intent stopServiceIntent = new Intent(MainActivity.this, BluetoothService.class);
            stopService(stopServiceIntent);
            this.finish();
        }
        doubleBackPressedOnce = true;
        Toast.makeText(this, "Please, click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "This application cannot work without Bluetooth turned on.", Toast.LENGTH_LONG).show();
                finish();
            } else if (resultCode == Activity.RESULT_OK) {
                BluetoothService.setDelegate(this);
                startService(new Intent(this, BluetoothService.class)
                        .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));
            }
        } else if (requestCode == Const.REQUEST_DEVICE_PAIRING) {
            if (resultCode == Activity.RESULT_OK) {
                String address = ((BluetoothDevice) data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).getAddress();
                startService(new Intent(this, BluetoothService.class)
                        .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                new CommandNBD(address).getMessage()));
            }
        } else if (requestCode == Const.REQUEST_CONSOLE_SELECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(this, ConsoleSessionActivity.class);
                ArrayList<String> devices = new ArrayList<>();
                boolean[] status = data.getBooleanArrayExtra(Const.SELECTED_DEVICES);
                for (int i = 0; i < bluesenseDevices.size(); i++) {
                    if (status[i])
                        devices.add(bluesenseDevices.get(i).getAddress());
                }
                intent.putStringArrayListExtra(Const.SELECTED_DEVICES, devices);
                for (BlueSenseDevice device : bluesenseDevices) {
                    startService(new Intent(this, BluetoothService.class)
                            .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                    new CommandBTD(device.getAddress()).getMessage()));
                }
                disconnectAll();
                startActivity(intent);
            }
        } else if (requestCode == Const.REQUEST_STREAM_SELECT_DEVICES) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(this, StreamSessionActivity.class);
                List<String> devices = new ArrayList<>();
                boolean[] status = data.getBooleanArrayExtra(Const.SELECTED_DEVICES);
                for (int i = 0; i < bluesenseDevices.size(); i++) {
                    if (status[i])
                        devices.add(bluesenseDevices.get(i).getAddress());
                }
                intent.putExtra(Const.SELECTED_DEVICES, devices.toArray());
                for (BlueSenseDevice device : bluesenseDevices) {
                    startService(new Intent(this, BluetoothService.class)
                            .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                    new CommandBTD(device.getAddress()).getMessage()));
                }
                startActivity(intent);
            }
        } else if (requestCode == Const.REQUEST_LOG_SELECT_DEVICES) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(this, LogSessionActivity.class);
                intent.putExtra(Const.CHOSEN_LOG_SETUP,
                        data.getStringExtra(Const.CHOSEN_LOG_SETUP));
                startService(new Intent(this, BluetoothService.class)
                        .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                new CommandBTDA().getMessage()));
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_devices:
                pairNewDevices();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View v, int position) {
        int state = adapter.getItem(position).getStatus();
        if (state == BluetoothState.STATE_NONE) {
            startService(new Intent(this, BluetoothService.class)
                    .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                            new CommandBTC(adapter.getItem(position).getAddress()).getMessage()));
        } else {
            startService(new Intent(this, BluetoothService.class)
                    .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                            new CommandBTD(adapter.getItem(position).getAddress()).getMessage()));
        }
    }

    public void disconnectAll() {
        for (BlueSenseDevice device : bluesenseDevices) {
            if (device.getStatus() != BluetoothState.STATE_NONE) {
                startService(new Intent(this, BluetoothService.class)
                        .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                new CommandBTD(device.getAddress()).getMessage()));
                adapter.setStatus(device.getAddress(), BluetoothState.STATE_NONE);
                adapter.notifyItemChanged(device.getAddress());
            }
        }
    }

    @OnClick(R.id.fab_new_console)
    public void newConsoleSession() {
        disconnectAll();
        Intent intent = new Intent(this, ConsoleSessionActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.fab_new_streaming)
    public void newStreamingSession() {
        Intent intent = new Intent(this, SelectDevicesActivity.class);
        ArrayList<BluetoothDevice> tmp = new ArrayList<>();
        for (BlueSenseDevice device : bluesenseDevices)
            tmp.add(device.getDevice());
        intent.putParcelableArrayListExtra(Const.DEVICES_LIST, tmp);
        startActivityForResult(intent, Const.REQUEST_STREAM_SELECT_DEVICES);
    }

    @OnClick(R.id.fab_new_logging)
    public void newLoggingSession() {
        Intent intent = new Intent(this, SelectSetupActivity.class);
        ArrayList<BluetoothDevice> tmp = new ArrayList<>();
        for (BlueSenseDevice device : bluesenseDevices)
            tmp.add(device.getDevice());
        intent.putParcelableArrayListExtra(Const.DEVICES_LIST, tmp);
        startActivityForResult(intent, Const.REQUEST_LOG_SELECT_DEVICES);
    }

    private void pairNewDevices() {
        Intent intent = new Intent(this, ScanBlueSenseDevices.class);
        this.startActivityForResult(intent, Const.REQUEST_DEVICE_PAIRING);
    }

    private void addBondedDevices(List<BlueSenseDevice> devices) {
        adapter.removeAll();
        adapter.notifyDataSetChanged();
        adapter.addAll(devices);
        adapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnected(ClientConnSuccess clientConnSuccess) {
        adapter.setStatus(clientConnSuccess.getMAddress(), BluetoothState.STATE_CONNECTED);
        adapter.notifyItemChanged(clientConnSuccess.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnectionFailed(ClientConnFailed clientConnFailed) {
        Toast.makeText(this, "Connection failed: " + clientConnFailed.getMAddress(), Toast.LENGTH_SHORT).show();
        adapter.setStatus(clientConnFailed.getMAddress(), BluetoothState.STATE_NONE);
        adapter.notifyItemChanged(clientConnFailed.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceDisconnected(ClientDisconnSuccess clientDisconnSuccess) {
        adapter.setStatus(clientDisconnSuccess.getMAddress(), BluetoothState.STATE_NONE);
        adapter.notifyItemChanged(clientDisconnSuccess.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnecting(ClientConnOngoing clientConnOngoing) {
        adapter.setStatus(clientConnOngoing.getMAddress(), BluetoothState.STATE_CONNECTING);
        adapter.notifyItemChanged(clientConnOngoing.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onBytesReceived(ClientBytesReceived clientBytesReceived) {

    }

    @Override
    public void onServiceStarted(List<BlueSenseDevice> devices) {
        bluesenseDevices = devices;
        addBondedDevices(devices);
    }

    @Override
    public void onDeviceAdded(BlueSenseDevice device) {
        bluesenseDevices.add(device);
        adapter.addDevice(device);
        adapter.notifyDataSetChanged();
    }

}
