package uk.ac.sussex.android.bluesensehub.model;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ThiasTux.
 */

public abstract class CommandBase {

    public static final String TAG = CommandBase.class.getSimpleName();

    public static final int MAX_LENGTH = 60;

    protected static final String COMMAND_START = "##";
    protected static final String COMMAND_SEPARATOR = "!";
    protected static final String PARAMETER_SEPARATOR = ";";
    protected static final String FILLING_TOKEN = "*";

    public static final String COMMAND_BLUETOOTH_START = "BTS";
    public static final String COMMAND_BLUETOOTH_CONNECT = "BTC";
    public static final String COMMAND_BLUETOOTH_DISCONNECT = "BTD";
    public static final String COMMAND_SESSION_SETUP = "BSS";
    public static final String COMMAND_NEW_DEVICE_PAIRED = "NDP";

    public abstract String getMessage();

    public static ArrayList<String> parseMessage(String message) {
        ArrayList<String> ret = new ArrayList<>();
        Log.i(TAG, "::parseCommand Parsing message '" + message + "'. Size in bytes: " + message.length());
        for (String command : message.split(COMMAND_START)) {
            String code = command.split(COMMAND_SEPARATOR)[0];
            if (!code.equals("")) {
                ret.add(code);
                if (command.split(COMMAND_SEPARATOR).length > 1) {
                    String[] params = command.split(COMMAND_SEPARATOR)[1].split(PARAMETER_SEPARATOR);
                    for (String param : params) {
                        if (param.indexOf(FILLING_TOKEN) == -1) {
                            ret.add(param);
                        }
                    }
                }
            }
        }
        return ret;
    }
}
