package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by mathias on 23/01/17.
 */
public class ClientDisconnSuccess {
    @Getter
    String mAddress;

    public ClientDisconnSuccess(String address) {
        mAddress = address;
    }
}
