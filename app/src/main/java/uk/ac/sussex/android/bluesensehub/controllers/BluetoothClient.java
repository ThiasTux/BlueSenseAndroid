package uk.ac.sussex.android.bluesensehub.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientBytesReceived;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnFailed;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientDisconnSuccess;

/**
 * Created by ThiasTux.
 */

public class BluetoothClient implements Runnable {

    private static final boolean CONTINUE_READ_WRITE = true;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private UUID mUuid;
    private String mMacAddress;

    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStreamWriter mOutputStreamWriter;

    private BluetoothConnector mBluetoothConnector;

    public BluetoothClient(BluetoothAdapter bluetoothAdapter, String macAddress) {
        mBluetoothAdapter = bluetoothAdapter;
        mMacAddress = macAddress;
        mUuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public void run() {

        mDevice = mBluetoothAdapter.getRemoteDevice(mMacAddress);
        int attempts = 0;

        while (mInputStream == null && attempts < 3) {
            mBluetoothConnector = new BluetoothConnector(mDevice, false, mBluetoothAdapter, mUuid);

            try {
                mSocket = mBluetoothConnector.connect().getUnderlyingSocket();
                mInputStream = mSocket.getInputStream();
            } catch (IOException e) {
                Log.e("", "===> mSocket IOException", e);
                //e.printStackTrace();
                attempts++;
            }
        }

        if (mSocket == null) {
            Log.e("", "===> mSocket IOException");
            EventBus.getDefault().post(new ClientConnFailed(mMacAddress));
            return;
        }

        try {

            mOutputStreamWriter = new OutputStreamWriter(mSocket.getOutputStream());

            int bufferSize = 1024;
            int bytesRead = -1;
            byte[] buffer = new byte[bufferSize];

            EventBus.getDefault().post(new ClientConnSuccess(mDevice.getAddress()));

            while (CONTINUE_READ_WRITE) {

                final StringBuilder sb = new StringBuilder();
                bytesRead = mInputStream.read(buffer);
                if (bytesRead != -1) {
                    String result = "";
                    while ((bytesRead == bufferSize) && (buffer[bufferSize] != 0)) {
                        result += new String(buffer, 0, bytesRead);
                        bytesRead = mInputStream.read(buffer);
                    }
                    result += new String(buffer, 0, bytesRead);
                    sb.append(result);
                }

                EventBus.getDefault().post(new ClientBytesReceived(sb.toString(), mMacAddress));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String message) {
        try {
            mOutputStreamWriter.write(message);
            mOutputStreamWriter.flush();
        } catch (IOException e) {
            Log.e("", "===> Client write");
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (mSocket != null) {
            try {
                mInputStream.close();
                mInputStream = null;
                mOutputStreamWriter.close();
                mOutputStreamWriter = null;
                mSocket.close();
                mSocket = null;
                mBluetoothConnector.close();
                EventBus.getDefault().post(new ClientDisconnSuccess(mMacAddress));
            } catch (IOException e) {
                Log.e("", "===> Close connection");
                e.printStackTrace();
            }
        }
    }
}
