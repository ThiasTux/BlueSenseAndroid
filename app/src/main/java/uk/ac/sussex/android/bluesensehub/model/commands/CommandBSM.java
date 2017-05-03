package uk.ac.sussex.android.bluesensehub.model.commands;

import uk.ac.sussex.android.bluesensehub.model.CommandBase;

/**
 * Created by ThiasTux.
 */

public class CommandBSM extends CommandBase {

    String mAddress;
    String mMessage;

    public CommandBSM(String address, String message) {
        mAddress = address;
        mMessage = message;
    }

    @Override
    public String getMessage() {
        return COMMAND_START
                + COMMAND_SEND_MESSAGE + COMMAND_SEPARATOR
                + mAddress + PARAMETER_SEPARATOR
                + mMessage + PARAMETER_SEPARATOR;
    }
}
