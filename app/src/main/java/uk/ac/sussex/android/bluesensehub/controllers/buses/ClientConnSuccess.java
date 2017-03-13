package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by ThiasTux.
 */
public class ClientConnSuccess {

    @Getter
    String mAddress;

    public ClientConnSuccess(String address) {
        mAddress = address;
    }

}

