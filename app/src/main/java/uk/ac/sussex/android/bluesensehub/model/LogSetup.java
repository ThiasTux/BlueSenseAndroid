package uk.ac.sussex.android.bluesensehub.model;

import android.util.SparseArray;

import java.util.List;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ThiasTux.
 */

@Data
public class LogSetup {

    @NonNull
    String setupName;
    @NonNull
    int numSets;
    @NonNull
    SparseArray<List<String>> sensorsSets;

}
