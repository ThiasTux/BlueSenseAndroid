package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by mathias on 18/01/17.
 */

public class ClientConnFailed {

    @Getter
    String mAddress;

    public ClientConnFailed(String mAddress) {
        this.mAddress = mAddress;
    }
}
