package uk.ac.sussex.android.bluesensehub.model;

import android.bluetooth.BluetoothDevice;

import lombok.Data;

/**
 * Created by mathias on 18/01/17.
 */

@Data
public class BlueSenseDevice {

    private BluetoothDevice device;
    private int state;

    public BlueSenseDevice(BluetoothDevice device) {
        this.device = device;
        state = BluetoothState.STATE_NONE;
    }

    public String getName() {
        return device.getName();
    }

    public String getAddress() {
        return device.getAddress();
    }

}
