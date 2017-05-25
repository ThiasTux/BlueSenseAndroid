package uk.ac.sussex.android.bluesensehub.model.commands;

import uk.ac.sussex.android.bluesensehub.model.CommandBase;

/**
 * Created by ThiasTux.
 */

public class CommandBTDA extends CommandBase {
    @Override
    public String getMessage() {
        return COMMAND_START
                + COMMAND_BLUETOOTH_STOP + COMMAND_SEPARATOR;
    }
}
