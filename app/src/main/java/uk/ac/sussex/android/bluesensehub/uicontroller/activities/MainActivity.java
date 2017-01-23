package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
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
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.BluetoothServiceDelegate;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnFailed;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.services.BluetoothService;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.BluetoothState;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTC;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTD;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTS;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.BlueSenseDevicesAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

public class MainActivity extends AppCompatActivity implements BlueSenseDevicesAdapter.ClickListener, BluetoothServiceDelegate {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bluesense_devices)
    RecyclerView bluesenseDevicesView;

    private BlueSenseDevicesAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothService mBluetoothService;
    private boolean doubleBackPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.bluesense_devices_paired);

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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth not supported. This application requires Bluetooth.",
                    Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, Const.REQUEST_ENABLE_BT);
        }

        BluetoothService.setDelegate(this);
        startService(new Intent(this, BluetoothService.class)
                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "This application cannot work without Bluetooth turned on.", Toast.LENGTH_LONG).show();
                finish();
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
                Utils.underDev(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void pairNewDevices() {
        Intent intent = new Intent(this, ScanBlueSenseDevices.class);
        this.startActivityForResult(intent, Const.REQUEST_DEVICE_PAIRING);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent stopServiceIntent = new Intent(MainActivity.this, BluetoothService.class);
        stopService(stopServiceIntent);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackPressedOnce) {
            super.onBackPressed();
            return;
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

    private void addBondedDevices(List<BlueSenseDevice> devices) {
        adapter.removeAll();
        adapter.notifyDataSetChanged();
        adapter.add(devices);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View v, int position) {
        int state = adapter.getItem(position).getState();
        if (state == BluetoothState.STATE_CONNECTED) {
            startService(new Intent(this, BluetoothService.class)
                    .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                            new CommandBTD(adapter.getItem(position).getAddress()).getMessage()));
            adapter.setStatus(position, BluetoothState.STATE_NONE);
            adapter.notifyItemChanged(position);
        } else {
            startService(new Intent(this, BluetoothService.class)
                    .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                            new CommandBTC(adapter.getItem(position).getAddress()).getMessage()));
            adapter.setStatus(position, BluetoothState.STATE_CONNECTING);
            adapter.notifyItemChanged(position);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnected(ClientConnSuccess clientConnSuccess) {
        adapter.setStatus(clientConnSuccess.getMAddress(), BluetoothState.STATE_CONNECTED);
        adapter.notifyItemChanged(clientConnSuccess.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectionFailed(ClientConnFailed clientConnFailed) {
        Toast.makeText(this, "Connection failed: " + clientConnFailed.getMAddress(), Toast.LENGTH_SHORT).show();
        adapter.setStatus(clientConnFailed.getMAddress(), BluetoothState.STATE_NONE);
        adapter.notifyItemChanged(clientConnFailed.getMAddress());
    }

    @Override
    public void onServiceStarted(List<BlueSenseDevice> devices) {
        addBondedDevices(devices);
    }
}
