package uk.ac.sussex.android.bluesensehub.uicontroller.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import uk.ac.sussex.android.bluesensehub.R;

/**
 * Created by ThiasTux.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


}
