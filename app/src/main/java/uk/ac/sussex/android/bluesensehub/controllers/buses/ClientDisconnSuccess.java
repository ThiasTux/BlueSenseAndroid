package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by ThiasTux.
 */
public class ClientDisconnSuccess {
    @Getter
    String mAddress;

    public ClientDisconnSuccess(String address) {
        mAddress = address;
    }
}
