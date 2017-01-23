package uk.ac.sussex.android.bluesensehub.controllers;

import java.util.List;

import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;

/**
 * Created by mathias on 23/01/17.
 */

public interface BluetoothServiceDelegate {

    void onServiceStarted(List<BlueSenseDevice> devices);

}
