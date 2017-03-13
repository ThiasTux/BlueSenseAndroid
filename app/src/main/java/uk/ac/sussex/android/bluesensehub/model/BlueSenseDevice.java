package uk.ac.sussex.android.bluesensehub.model;

import android.bluetooth.BluetoothDevice;

import lombok.Data;


/**
 * Created by ThiasTux.
 */

@Data
public class BlueSenseDevice {

    private BluetoothDevice device;
    private int status;

    public BlueSenseDevice(BluetoothDevice device) {
        this.device = device;
        status = BluetoothState.STATE_NONE;
    }

    public String getName() {
        return device.getName();
    }

    public String getAddress() {
        return device.getAddress();
    }

}
