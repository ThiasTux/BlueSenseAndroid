package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.ScanBlueSenseDevicesAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;

/**
 * Created by mathias on 16/01/17.
 */
public class ScanBlueSenseDevices extends Activity implements ScanBlueSenseDevicesAdapter.OnItemClickListener {

    private static final long DISCOVERY_INTERVAL = 30000;
    private BluetoothAdapter mBluetoothAdapter;

    @BindView(R.id.scanned_devices_list)
    RecyclerView scanDevicesView;
    @BindView(R.id.swipe_scan_layout)
    SwipeRefreshLayout swypeScanDeviceLayout;

    private ScanBlueSenseDevicesAdapter adapter;
    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_bluesense_devices);

        ButterKnife.bind(this);

        setResult(Activity.RESULT_CANCELED);

        adapter = new ScanBlueSenseDevicesAdapter(new ArrayList<BluetoothDevice>());
        scanDevicesView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        scanDevicesView.setLayoutManager(llm);
        adapter.setOnItemClickListener(this);
        scanDevicesView.setAdapter(adapter);

        swypeScanDeviceLayout.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent));
        swypeScanDeviceLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startDiscovery();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startDiscovery();

    }

    private void startDiscovery() {
        int hasPermission = ActivityCompat.checkSelfPermission(ScanBlueSenseDevices.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            doDiscovery();
            return;
        }

        ActivityCompat.requestPermissions(ScanBlueSenseDevices.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Const.REQUEST_COARSE_LOCATION_PERMISSIONS);
    }

    private void doDiscovery() {
        adapter.removeAll();
        adapter.notifyDataSetChanged();
        mBluetoothAdapter.startDiscovery();
        if (!swypeScanDeviceLayout.isRefreshing())
            swypeScanDeviceLayout.setRefreshing(true);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopDiscovery();
            }
        }, DISCOVERY_INTERVAL);
    }

    private void stopDiscovery() {
        mBluetoothAdapter.cancelDiscovery();
        swypeScanDeviceLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBluetoothAdapter != null)
            stopDiscovery();

        this.unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                swypeScanDeviceLayout.setRefreshing(false);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapter.addDevice(device);
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR) == BluetoothDevice.BOND_BONDED)
                    returnToMainActivity(device);

            }
        }
    };

    private void returnToMainActivity(BluetoothDevice device) {
        Intent intent = new Intent();
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onItemClick(View v, int position) {
        BluetoothDevice device = adapter.getItem(position);
        device.createBond();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Const.REQUEST_COARSE_LOCATION_PERMISSIONS: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doDiscovery();
                } else {
                    Snackbar.make(ScanBlueSenseDevices.this.getCurrentFocus(),
                            "This application need these permissions.",
                            Snackbar.LENGTH_LONG).show();
                    stopDiscovery();
                    ScanBlueSenseDevices.this.finish();
                }
            }
        }
    }
}
