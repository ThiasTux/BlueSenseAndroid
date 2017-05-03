package uk.ac.sussex.android.bluesensehub.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientBytesReceived;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnFailed;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnOngoing;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientDisconnSuccess;
import uk.ac.sussex.android.bluesensehub.model.BluetoothState;

/**
 * Created by ThiasTux.
 */

public class BluetoothSPPClient {

    // Debugging
    private static final String TAG = "Bluetooth Service";

    private static final UUID UUID_OTHER_DEVICE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final BluetoothDevice mDevice;

    private BluetoothSPPClient.ConnectThread mConnectThread;
    private BluetoothSPPClient.ConnectedThread mConnectedThread;
    private int mState;

    private boolean isManual = false;
    private int attempt = 0;

    // Constructor. Prepares a new BluetoothChat session
    // context : The UI Activity Context
    // handler : A Handler to send messages back to the UI Activity
    public BluetoothSPPClient(String address) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BluetoothState.STATE_NONE;
        mDevice = mAdapter.getRemoteDevice(address);
    }


    // Set the current state of the chat connection
    // state : An integer defining the current connection state
    private synchronized void setState(int state) {
        if (mState != state) {
            Log.d(TAG, "setState() " + mState + " -> " + state);
            mState = state;
            switch (mState) {
                case BluetoothState.STATE_CONNECTED:
                    EventBus.getDefault().post(new ClientConnSuccess(mDevice.getAddress()));
                    break;
                case BluetoothState.STATE_CONNECTING:
                    EventBus.getDefault().post(new ClientConnOngoing(mDevice.getAddress()));
                    break;
                case BluetoothState.STATE_NONE:
                    EventBus.getDefault().post(new ClientDisconnSuccess(mDevice.getAddress()));
                    break;
                case BluetoothState.STATE_NULL:
                    EventBus.getDefault().post(new ClientConnFailed(mDevice.getAddress()));
                    mState = BluetoothState.STATE_NONE;
                    break;
            }
        }
    }

    // Return the current connection state.
    public synchronized int getState() {
        return mState;
    }

    // Start the ConnectThread to initiate a connection to a remote device
    // device : The BluetoothDevice to connect
    // secure : Socket Security type - Secure (true) , Insecure (false)
    public synchronized void connect() {
        // Cancel any thread attempting to make a connection
        if (mState == BluetoothState.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new BluetoothSPPClient.ConnectThread();
        mConnectThread.start();
        attempt++;
        setState(BluetoothState.STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    public synchronized void connected(BluetoothSocket socket) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new BluetoothSPPClient.ConnectedThread(socket);
        mConnectedThread.start();

        setState(BluetoothState.STATE_CONNECTED);
    }

    // Stop all threads
    public synchronized void stop() {
        isManual = true;
        attempt = 0;
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(BluetoothState.STATE_NONE);
    }

    public void write(String message) {
        write(message.getBytes());
    }

    // Write to the ConnectedThread in an unsynchronized manner
    // out : The bytes to write
    public void write(byte[] out) {
        // Create temporary object
        BluetoothSPPClient.ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != BluetoothState.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    // Indicate that the connection attempt failed and notify the UI Activity
    private void connectionFailed() {
        if (attempt <= 3) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BluetoothSPPClient.this.connect();
        } else {
            attempt = 0;
            setState(BluetoothState.STATE_NULL);
        }
    }

    // Indicate that the connection was lost and notify the UI Activity
    private void connectionLost() {
        if (!isManual)
            BluetoothSPPClient.this.connect();
    }

    // This thread runs while attempting to make an outgoing connection
    // with a device. It runs straight through
    // the connection either succeeds or fails
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread() {
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = mDevice.createInsecureRfcommSocketToServiceRecord(UUID_OTHER_DEVICE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothSPPClient.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // This thread runs during a connection with a remote device.
    // It handles all incoming and outgoing transmissions.
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int bytesRead = -1;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    final StringBuilder sb = new StringBuilder();
                    bytesRead = mmInStream.read(buffer);
                    if (bytesRead != -1) {
                        String result = "";
                        while ((bytesRead == bufferSize) && (buffer[bufferSize - 1] != 0)) {
                            result += new String(buffer, 0, bytesRead);
                            bytesRead = mmInStream.read(buffer);
                        }
                        result += new String(buffer, 0, bytesRead);
                        sb.append(result);
                    }

                    EventBus.getDefault().post(new ClientBytesReceived(mDevice.getAddress(), sb.toString().replace("\t", "    ")));
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}
