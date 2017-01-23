package uk.ac.sussex.android.bluesensehub.model.commands;

import uk.ac.sussex.android.bluesensehub.model.CommandBase;

/**
 * Created by mathias on 23/01/17.
 */

public class CommandBTD extends CommandBase {

    String mAddress;

    public CommandBTD(String address) {
        mAddress = address;
    }

    @Override
    public String getMessage() {
        return COMMAND_START
                + COMMAND_BLUETOOTH_DISCONNECT + COMMAND_SEPARATOR
                + mAddress + PARAMETER_SEPARATOR;
    }
}
