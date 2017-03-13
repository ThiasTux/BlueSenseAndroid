package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by ThiasTux.
 */

public class ClientConnAttemptFailed {

    @Getter
    String mAddress;

    public ClientConnAttemptFailed(String mAddress) {
        this.mAddress = mAddress;
    }
}
