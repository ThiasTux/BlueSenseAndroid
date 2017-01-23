package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by mathias on 18/01/17.
 */

public class ClientConnAttemptFailed {

    @Getter
    String mAddress;

    public ClientConnAttemptFailed(String mAddress) {
        this.mAddress = mAddress;
    }
}
