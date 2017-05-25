package uk.ac.sussex.android.bluesensehub.model;

import android.bluetooth.BluetoothAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ThiasTux.
 */

@Data
public class LogSensorSet implements Serializable {

    @NonNull
    int id;
    @NonNull
    ArrayList<BlueSenseDevice> devices;

    public LogSensorSet(int id, List<String> devicesAddresses) {
        this.id = id;
        devices = new ArrayList<>();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        for (String address : devicesAddresses)
            devices.add(new BlueSenseDevice(adapter.getRemoteDevice(address)));
    }

}
