package uk.ac.sussex.android.bluesensehub.utilities;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.Toast;

import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.uicontroller.activities.MainActivity;

/**
 * Created by ThiasTux.
 */
public class Utils {

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static boolean isDeviceSupported(BluetoothDevice device) {
        return device.getAddress().startsWith(Const.BT_ADDRESS_PREFIX) && device.getName().startsWith(Const.BT_NAME_PREFIX);
    }

    public static void underDev(MainActivity mainActivity) {
        Toast.makeText(mainActivity, "Under development!", Toast.LENGTH_LONG).show();
    }
}
