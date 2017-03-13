package uk.ac.sussex.android.bluesensehub.controllers;

import java.util.List;

import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;

/**
 * Created by ThiasTux.
 */

public interface BluetoothServiceDelegate {

    void onServiceStarted(List<BlueSenseDevice> devices);

    void onDeviceAdded(BlueSenseDevice device);

}
