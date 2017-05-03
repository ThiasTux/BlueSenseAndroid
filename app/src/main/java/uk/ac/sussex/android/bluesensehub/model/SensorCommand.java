package uk.ac.sussex.android.bluesensehub.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Created by ThiasTux.
 */

@Data
@RequiredArgsConstructor(suppressConstructorProperties = true)
public class SensorCommand {

    @NonNull
    String name;
    @NonNull
    String value;
    boolean favorite;

}
