package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.app.Activity;
import android.os.Bundle;

import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.SettingsFragment;

/**
 * Created by ThiasTux.
 */

public class PreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);


        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
