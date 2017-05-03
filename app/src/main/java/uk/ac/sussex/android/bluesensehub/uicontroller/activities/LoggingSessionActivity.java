package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.BluetoothServiceDelegate;
import uk.ac.sussex.android.bluesensehub.controllers.services.BluetoothService;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.SensorCommand;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBSM;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTC;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTD;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTS;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.PagerAdapter;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.LoggingActionsButtonsFragment;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.LoggingStatusFragment;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

/**
 * Created by ThiasTux.
 */

public class LoggingSessionActivity extends AppCompatActivity implements BluetoothServiceDelegate, LoggingStatusFragment.DeviceHandler, LoggingActionsButtonsFragment.DeviceHandler {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private LoggingStatusFragment loggingStatusFragment;
    private LoggingActionsButtonsFragment loggingActionsButtonsFragment;

    @Getter
    ArrayList<BlueSenseDevice> devices = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging_session);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        ArrayList<String> addresses = intent.getStringArrayListExtra(Const.SELECTED_DEVICES);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Logging session");
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        for (String address : addresses) {
            devices.add(new BlueSenseDevice(adapter.getRemoteDevice(address)));
        }

        if (Utils.isTablet(this))
            setupFragments();
        else
            setupTabs();

        BluetoothService.setDelegate(this);
        startService(new Intent(this, BluetoothService.class)
                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBTS().getMessage()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_logging_session, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_refresh_connection:
                disconnectDevices();
                connectDevices();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceStarted(List<BlueSenseDevice> devices) {

    }

    @Override
    public void onDeviceAdded(BlueSenseDevice device) {

    }

    private void setupFragments() {
        loggingStatusFragment = (LoggingStatusFragment) getSupportFragmentManager()
                .findFragmentById(R.id.logging_status_fragment);
        loggingActionsButtonsFragment = (LoggingActionsButtonsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.logging_actions_buttons_fragment);
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), LoggingSessionActivity.this));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.fragment_tabs);
        tabLayout.setupWithViewPager(viewPager);

        loggingStatusFragment = (LoggingStatusFragment) ((PagerAdapter) viewPager.getAdapter()).getItem(0);
        loggingActionsButtonsFragment = (LoggingActionsButtonsFragment) ((PagerAdapter) viewPager.getAdapter()).getItem(1);

    }

    @Override
    public void connectDevices() {
        if (devices != null) {
            for (BlueSenseDevice device : devices) {
                startService(new Intent(this, BluetoothService.class)
                        .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                new CommandBTC(device.getAddress()).getMessage()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void disconnectDevices() {
        if (devices != null) {
            for (BlueSenseDevice device : devices) {
                startService(new Intent(this, BluetoothService.class)
                        .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                new CommandBTD(device.getAddress()).getMessage()));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendCommand(SensorCommand command) {
        if (devices != null) {
            for (BlueSenseDevice device : devices) {
                startService(new Intent(this, BluetoothService.class)
                        .putExtra(Const.COMMAND_SERVICE_INTENT_KEY, new CommandBSM(device.getAddress(), command.getValue() + "\n").getMessage()));
            }
        }
    }
}
