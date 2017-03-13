package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by ThiasTux.
 */

public class ClientConnFailed {

    @Getter
    String mAddress;

    public ClientConnFailed(String mAddress) {
        this.mAddress = mAddress;
    }
}
