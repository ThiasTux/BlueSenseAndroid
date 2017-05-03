package uk.ac.sussex.android.bluesensehub.controllers.buses;

import lombok.Getter;

/**
 * Created by ThiasTux.
 */

public class ClientBytesReceived {

    @Getter
    String message;
    @Getter
    String address;

    public ClientBytesReceived(String address, String message) {
        this.address = address;
        this.message = message;
    }

    public ClientBytesReceived(String address, byte[] message) {
        this.address = address;
        this.message = new String(message);
    }
}
