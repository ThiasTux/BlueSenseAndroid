package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by mathias on 18/01/17.
 */
public class ClientConnSuccess {

    @Getter
    String mAddress;

    public ClientConnSuccess(String address) {
        mAddress = address;
    }

}

