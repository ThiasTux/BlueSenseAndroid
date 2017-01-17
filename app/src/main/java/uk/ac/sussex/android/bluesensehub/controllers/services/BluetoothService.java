package uk.ac.sussex.android.bluesensehub.controllers.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;

import uk.ac.sussex.android.bluesensehub.controllers.BluetoothClient;

public class BluetoothService extends Service {

    private final MyBluetoothServiceBinder mBinder = new MyBluetoothServiceBinder();
    private BluetoothAdapter mAdapter;
    private Map<String, BluetoothClient> mClientsMap;

    public BluetoothService() {
        mClientsMap = new HashMap<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void connectDevice(BluetoothDevice device) {
        mClientsMap.put(device.getAddress(), new BluetoothClient(mAdapter, device.getAddress()));
        new Thread(mClientsMap.get(device.getAddress())).start();
    }

    public class MyBluetoothServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

}
