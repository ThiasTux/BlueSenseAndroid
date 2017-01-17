package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.services.BluetoothService;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.BlueSenseDevicesAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

public class MainActivity extends AppCompatActivity implements BlueSenseDevicesAdapter.ClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.bluesense_devices)
    RecyclerView bluesenseDevicesView;

    private BlueSenseDevicesAdapter adapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothService mBluetoothService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.bluesense_devices_paired);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluesenseDevicesView.setHasFixedSize(true);
        adapter = new BlueSenseDevicesAdapter(new ArrayList<BluetoothDevice>());
        if (Utils.isTablet(this)) {
            bluesenseDevicesView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            LinearLayoutManager llm = new LinearLayoutManager(this);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            bluesenseDevicesView.setLayoutManager(llm);
        }
        adapter.setClickListener(this);
        bluesenseDevicesView.setAdapter(adapter);

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth not supported. This application requires Bluetooth.",
                    Toast.LENGTH_LONG).show();
            MainActivity.this.finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Const.REQUEST_ENABLE_BT);
        }

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
        addBondedDevices();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.MyBluetoothServiceBinder binder =
                    (BluetoothService.MyBluetoothServiceBinder) service;
            mBluetoothService = binder.getService();
            Toast.makeText(MainActivity.this, "BluetoothService started", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };

    private void addBondedDevices() {
        Set<BluetoothDevice> bluetoothDevices = mBluetoothAdapter.getBondedDevices();
        adapter.removeAll();
        adapter.notifyDataSetChanged();

        if (bluetoothDevices.size() > 0)
            for (BluetoothDevice device : bluetoothDevices) {
                if (Utils.isDeviceSupported(device))
                    adapter.addDevice(device);
            }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View v, int position) {
        mBluetoothService.connectDevice(adapter.getItem(position));
    }
}
