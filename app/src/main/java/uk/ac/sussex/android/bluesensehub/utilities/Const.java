package uk.ac.sussex.android.bluesensehub.utilities;

/**
 * Created by mathias on 16/01/17.
 */
public class Const {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final String BT_ADDRESS_PREFIX = "00:06:66";
    public static final String BT_NAME_PREFIX = "BlueSense";

    public static final int REQUEST_DEVICE_PAIRING = 2;
    public static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 3;
    public static final String UUID = "e0917680-d427-11e4-8830-";
    public static final String MAIN_ACTIVITY_ACTION = "bluesensehub.action.main";
    public static final int BLUETOOTH_SERVICE_ID = 101;
    private static final String PACKAGE_NAME = "uk.ac.sussex.android.bluesensehub";
    public static final String COMMAND_SERVICE_INTENT_KEY = PACKAGE_NAME + ".COMMAND_SERVICE_INTENT_KEY";

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
