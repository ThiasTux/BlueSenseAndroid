package uk.ac.sussex.android.bluesensehub.model;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ThiasTux.
 */

@Data
public class SensorCommand {

    @NonNull
    String name;
    @NonNull
    String value;
    boolean favorite;

}
