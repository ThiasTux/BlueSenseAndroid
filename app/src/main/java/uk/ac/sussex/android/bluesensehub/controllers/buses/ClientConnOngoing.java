package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by ThiasTux.
 */

public class ClientConnOngoing {

    @Getter
    String mAddress;

    public ClientConnOngoing(String address) {
        this.mAddress = address;
    }
}
