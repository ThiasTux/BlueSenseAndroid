package uk.ac.sussex.android.bluesensehub.controllers.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.BluetoothSPPClient;
import uk.ac.sussex.android.bluesensehub.controllers.BluetoothServiceDelegate;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnFailed;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientDisconnSuccess;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.BluetoothState;
import uk.ac.sussex.android.bluesensehub.model.CommandBase;
import uk.ac.sussex.android.bluesensehub.uicontroller.activities.MainActivity;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

public class BluetoothService extends Service {

    private static final String TAG = BluetoothService.class.getSimpleName();

    private Map<String, BluetoothSPPClient> mClientsMap;
    private Map<String, BlueSenseDevice> mDeviceMap;
    private BluetoothAdapter mBluetoothAdapter;
    private Notification mNotification;
    private static BluetoothServiceDelegate mDelegate;
    private boolean isServiceStarted = false;
    private boolean isAndroid = false;

    public BluetoothService() {
        if (mClientsMap == null)
            mClientsMap = new HashMap<>();
        if (mDeviceMap == null)
            mDeviceMap = new HashMap<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(this, "Bluetooth service started!", Toast.LENGTH_SHORT).show();

        mNotification = new NotificationCompat.Builder(this)
                .setContentTitle("BlueSenseHub")
                .setTicker("BlueSenseHub")
                .setContentText("No device connected")
                .setSmallIcon(R.drawable.ic_not)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setOngoing(true).build();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            if (intent.hasExtra(Const.COMMAND_SERVICE_INTENT_KEY)) {
                String message = intent.getStringExtra(Const.COMMAND_SERVICE_INTENT_KEY);
                try {
                    processCommands(CommandBase.parseMessage(message));
                } catch (Exception e) {
                    Log.e(TAG, "::onStartCommand Error processing message '" + message + "': " + e.getMessage());
                }
            }
        startForeground(Const.NOTIFICATION_ID.FOREGROUND_SERVICE, mNotification);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        disconnectAll();
        super.onDestroy();
    }

    private void processCommands(ArrayList<String> list) throws Exception {
        Iterator<String> itr = list.iterator();
        String address;
        while (itr.hasNext()) {
            String comm = itr.next();
            Log.i(TAG, "::processCommand Processing command '" + comm + "'");
            switch (comm) {
                case CommandBase.COMMAND_BLUETOOTH_START:
                    if (!isServiceStarted) {
                        if (mClientsMap != null)
                            for (String key : mClientsMap.keySet())
                                mClientsMap.get(key).stop();
                        mClientsMap = new HashMap<>();
                        mDeviceMap = new HashMap<>();
                        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
                        if (bondedDevices.size() > 0)
                            for (BluetoothDevice device : bondedDevices) {
                                if (Utils.isDeviceSupported(device))
                                    mDeviceMap.put(device.getAddress(), new BlueSenseDevice(device));
                            }
                        EventBus.getDefault().register(this);
                        isServiceStarted = true;
                    }
                    if (mDelegate != null) {
                        List<BlueSenseDevice> devices = new ArrayList<>();
                        for (String key : mDeviceMap.keySet())
                            devices.add(mDeviceMap.get(key));
                        mDelegate.onServiceStarted(devices);
                    }
                    break;
                case CommandBase.COMMAND_BLUETOOTH_CONNECT:
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    address = itr.next();
                    connectDevice(address);
                    break;
                case CommandBase.COMMAND_BLUETOOTH_DISCONNECT:
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    address = itr.next();
                    disconnectDevice(address);
                    break;
                case CommandBase.COMMAND_BLUETOOTH_STOP:
                    disconnectAll();
                    stopForeground(true);
                    stopSelf();
                    break;
                case CommandBase.COMMAND_NEW_DEVICE_PAIRED:
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    address = itr.next();
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    BlueSenseDevice blueSenseDevice = new BlueSenseDevice(device);
                    mDeviceMap.put(address, blueSenseDevice);
                    if (mDelegate != null)
                        mDelegate.onDeviceAdded(blueSenseDevice);
                case CommandBase.COMMAND_SEND_MESSAGE:
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    address = itr.next();
                    if (!itr.hasNext())
                        throw new Exception("Command '" + comm + "' is malformed or missing parameters");
                    String message = itr.next();
                    mClientsMap.get(address).write(message);
            }
        }
    }

    public void connectDevice(String address) {
        mClientsMap.put(address, new BluetoothSPPClient(address));
        mClientsMap.get(address).connect();
    }

    public void disconnectDevice(String address) {
        mClientsMap.get(address).stop();
    }

    public void disconnectAll() {
        for (String key : mClientsMap.keySet()) {
            mClientsMap.get(key).stop();
        }
        mClientsMap = new HashMap<>();
    }

    public List<BlueSenseDevice> getDevices() {
        List<BlueSenseDevice> tmp = new ArrayList<>();
        for (String key : mDeviceMap.keySet()) {
            tmp.add(mDeviceMap.get(key));
        }
        return tmp;
    }

    public class MyBluetoothServiceBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public List<String> getConnectedDevices() {
        return new ArrayList<>(mClientsMap.keySet());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnectionSuccess(ClientConnSuccess clientConnSuccess) {
        int numDevicesConnected = getNumDevicesConnected();
        mDeviceMap.get(clientConnSuccess.getMAddress()).setStatus(BluetoothState.STATE_CONNECTED);
        mNotification = new NotificationCompat.Builder(this)
                .setContentTitle("BlueSenseHub")
                .setTicker("Device: " + clientConnSuccess.getMAddress() + " connected!")
                .setContentText(getConnectionStatusString(numDevicesConnected))
                .setSmallIcon(R.drawable.ic_not)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Const.NOTIFICATION_ID.FOREGROUND_SERVICE, mNotification);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnectionFail(ClientConnFailed clientConnFailed) {
        int numDevicesConnected = getNumDevicesConnected();
        mClientsMap.remove(clientConnFailed.getMAddress());
        mDeviceMap.get(clientConnFailed.getMAddress()).setStatus(BluetoothState.STATE_NONE);
        mNotification = new NotificationCompat.Builder(this)
                .setContentTitle("BlueSenseHub")
                .setTicker("Device: " + clientConnFailed.getMAddress() + " connected!")
                .setContentText(getConnectionStatusString(numDevicesConnected))
                .setSmallIcon(R.drawable.ic_not)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Const.NOTIFICATION_ID.FOREGROUND_SERVICE, mNotification);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceDisconnectionSuccess(ClientDisconnSuccess clientDisconnSuccess) {
        int numDevicesConnected = getNumDevicesConnected();
        mClientsMap.remove(clientDisconnSuccess.getMAddress());
        mDeviceMap.get(clientDisconnSuccess.getMAddress()).setStatus(BluetoothState.STATE_NONE);
        mNotification = new NotificationCompat.Builder(this)
                .setContentTitle("BlueSenseHub")
                .setTicker("Device: " + clientDisconnSuccess.getMAddress() + " disconnected!")
                .setContentText(getConnectionStatusString(numDevicesConnected))
                .setSmallIcon(R.drawable.ic_not)
                .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(Const.NOTIFICATION_ID.FOREGROUND_SERVICE, mNotification);
    }

    private int getNumDevicesConnected() {
        int numConnected = 0;
        for (String key : mClientsMap.keySet()) {
            if (mClientsMap.get(key).getState() == BluetoothState.STATE_CONNECTED)
                numConnected++;
        }
        return numConnected;
    }

    private String getConnectionStatusString(int numDevicesConnected) {
        return (numDevicesConnected == 0 ? "No device connected" : numDevicesConnected + " devices connected");
    }

    public static void setDelegate(BluetoothServiceDelegate delegate) {
        if (delegate == null)
            return;
        mDelegate = delegate;
    }

    protected static BluetoothServiceDelegate getDelegate() {
        return mDelegate;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
