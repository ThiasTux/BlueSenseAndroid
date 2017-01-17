package uk.ac.sussex.android.bluesensehub.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mathias on 17/01/17.
 */
public class BluetoothConnector {

    private BluetoothSocketWrapper mBluetoothSocket;
    private BluetoothDevice mDevice;
    private boolean secure;
    private BluetoothAdapter mAdapter;
    private List<UUID> uuidCandidates;
    private int candidate;


    public BluetoothConnector(BluetoothDevice mDevice, boolean secure,
                              BluetoothAdapter mBluetoothAdapter, UUID mUuid) {

        this.mDevice = mDevice;
        this.secure = secure;
        this.mAdapter = mBluetoothAdapter;
        this.uuidCandidates = new ArrayList<>();
        this.uuidCandidates.add(mUuid);

    }

    public BluetoothSocketWrapper connect() throws IOException {

        boolean success = false;

        while (selectSocket()) {
            mAdapter.cancelDiscovery();

            try {
                mBluetoothSocket.connect();
                success = true;
                break;
            } catch (IOException e) {
                try {
                    mBluetoothSocket = new FallbackBluetoothSocket(mBluetoothSocket.getUnderlyingSocket());
                    Thread.sleep(500);
                    mBluetoothSocket.connect();
                    success = true;
                    break;
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

        if (!success) {
            throw new IOException("===> Could not connect to device: " + mDevice.getAddress());
        }

        return mBluetoothSocket;
    }

    private boolean selectSocket() throws IOException {
        if (candidate >= uuidCandidates.size()) {
            return false;
        }

        BluetoothSocket tmp;
        UUID uuid = uuidCandidates.get(candidate++);

        Log.e("BT", "===> Attempting to connect to Protocol: " + uuid);
        if (secure) {
            tmp = mDevice.createRfcommSocketToServiceRecord(uuid);
        } else {
            tmp = mDevice.createInsecureRfcommSocketToServiceRecord(uuid);
        }
        mBluetoothSocket = new NativeBluetoothSocket(tmp);

        return true;
    }

    public void close() {
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static interface BluetoothSocketWrapper {

        InputStream getInputStream() throws IOException;

        OutputStream getOutputStream() throws IOException;

        String getRemoteDeviceName();

        String getRemoteDeviceAddress();

        BluetoothSocket getUnderlyingSocket();

        void connect() throws IOException;

        void close() throws IOException;
    }

    public static class NativeBluetoothSocket implements BluetoothSocketWrapper {

        private BluetoothSocket mSocket;

        public NativeBluetoothSocket(BluetoothSocket tmp) {
            this.mSocket = tmp;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return mSocket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return mSocket.getOutputStream();
        }

        @Override
        public String getRemoteDeviceName() {
            return mSocket.getRemoteDevice().getName();
        }

        @Override
        public String getRemoteDeviceAddress() {
            return mSocket.getRemoteDevice().getName();
        }

        @Override
        public BluetoothSocket getUnderlyingSocket() {
            return mSocket;
        }

        @Override
        public void connect() throws IOException {
            mSocket.connect();
        }

        @Override
        public void close() throws IOException {
            mSocket.close();
        }
    }

    public static class FallbackBluetoothSocket extends NativeBluetoothSocket {

        private BluetoothSocket mFallbackSocket;

        public FallbackBluetoothSocket(BluetoothSocket tmp) {
            super(tmp);
            try {
                Class<?> clazz = tmp.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                mFallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return mFallbackSocket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return mFallbackSocket.getOutputStream();
        }


        @Override
        public void connect() throws IOException {
            mFallbackSocket.connect();
        }


        @Override
        public void close() throws IOException {
            mFallbackSocket.close();
        }
    }
}
