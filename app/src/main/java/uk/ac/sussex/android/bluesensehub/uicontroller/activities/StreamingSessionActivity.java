package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.BluetoothServiceDelegate;
import uk.ac.sussex.android.bluesensehub.controllers.services.BluetoothService;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTS;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.PagerAdapter;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.StreamingParametersFragment;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.StreamingStatusFragment;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

/**
 * Created by ThiasTux.
 */
public class StreamingSessionActivity extends AppCompatActivity implements BluetoothServiceDelegate {

    private List<BlueSenseDevice> devices;
    private StreamingParametersFragment parametersFragment;
    private StreamingStatusFragment statusFragment;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming_session);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        String[] addresses = intent.getStringArrayExtra(Const.SELECTED_DEVICES);

        BluetoothService.setDelegate(this);
        startService(new Intent(this, BluetoothService.class)
                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("Streaming session");

        if (Utils.isTablet(this))
            setupFragments();
        else
            setupTabs();
    }


    @Override
    public void onServiceStarted(List<BlueSenseDevice> devices) {

    }

    @Override
    public void onDeviceAdded(BlueSenseDevice device) {

    }

    private void setupFragments() {
        parametersFragment = (StreamingParametersFragment) getSupportFragmentManager().findFragmentById(R.id.session_parameters_fragment);
        statusFragment = (StreamingStatusFragment) getSupportFragmentManager().findFragmentById(R.id.session_status_fragment);
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), StreamingSessionActivity.this));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.fragment_tabs);
        tabLayout.setupWithViewPager(viewPager);

        parametersFragment = (StreamingParametersFragment) ((PagerAdapter) viewPager.getAdapter()).getItem(0);
        statusFragment = (StreamingStatusFragment) ((PagerAdapter) viewPager.getAdapter()).getItem(1);
    }
}
