package uk.ac.sussex.android.bluesensehub.model.commands;

import uk.ac.sussex.android.bluesensehub.model.CommandBase;

/**
 * Created by ThiasTux.
 */

public class CommandBSS extends CommandBase {

    String[] addresses;

    public CommandBSS(String[] addresses) {
        this.addresses = addresses;
    }

    @Override
    public String getMessage() {
        String tmp = COMMAND_START
                + COMMAND_SESSION_SETUP + COMMAND_SEPARATOR;
        for (String s : addresses)
            tmp += s + PARAMETER_SEPARATOR;
        return tmp;
    }
}
