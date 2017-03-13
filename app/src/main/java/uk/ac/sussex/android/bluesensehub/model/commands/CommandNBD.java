package uk.ac.sussex.android.bluesensehub.model.commands;

import uk.ac.sussex.android.bluesensehub.model.CommandBase;

/**
 * Created by ThiasTux.
 */
public class CommandNBD extends CommandBase {

    String mAddress;

    public CommandNBD(String address) {
        this.mAddress = address;
    }

    @Override
    public String getMessage() {
        return COMMAND_START
                + COMMAND_NEW_DEVICE_PAIRED + COMMAND_SEPARATOR
                + mAddress + PARAMETER_SEPARATOR;
    }
}
