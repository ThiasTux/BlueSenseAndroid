package uk.ac.sussex.android.bluesensehub.utilities;

/**
 * Created by ThiasTux.
 */
public class Const {

    public static final int REQUEST_ENABLE_BT = 1;
    public static final String BT_ADDRESS_PREFIX = "00:06:66";
    public static final String BT_NAME_PREFIX = "BlueSense";

    public static final int REQUEST_DEVICE_PAIRING = 2;
    public static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 3;
    public static final String UUID = "e0917680-d427-11e4-8830-";
    private static final String PACKAGE_NAME = "uk.ac.sussex.android.bluesensehub";
    public static final String COMMAND_SERVICE_INTENT_KEY = PACKAGE_NAME + ".COMMAND_SERVICE_INTENT_KEY";
    public static final int REQUEST_CONSOLE_SELECT_DEVICE = 401;
    public static final int REQUEST_STREAM_SELECT_DEVICES = 402;
    public static final int REQUEST_LOG_SELECT_DEVICES = 403;
    public static final String DEVICES_LIST = PACKAGE_NAME + ".DEVICES_LIST";
    public static final String SELECTED_DEVICES = PACKAGE_NAME + ".SELECTED_DEVICES";

    public static final String PREFS_NAME = PACKAGE_NAME + ".sharedPrefs";
    public static final String PREFS_COMMANDS_HISTORY = PREFS_NAME + "command_history";
    public static final String PREFS_SAVED_COMMANDS = PREFS_NAME + ".saved_commands";

    public static final String DEFINE_COMMANDS = PACKAGE_NAME + ".DEFINE_COMMANDS";

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }
}
