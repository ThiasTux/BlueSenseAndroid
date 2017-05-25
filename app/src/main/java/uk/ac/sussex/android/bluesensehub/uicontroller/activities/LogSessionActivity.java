package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lombok.Getter;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.BluetoothServiceDelegate;
import uk.ac.sussex.android.bluesensehub.controllers.services.BluetoothService;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.LogSensorSet;
import uk.ac.sussex.android.bluesensehub.model.LogSetup;
import uk.ac.sussex.android.bluesensehub.model.SensorCommand;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBSM;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTC;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTD;
import uk.ac.sussex.android.bluesensehub.model.commands.CommandBTS;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.PagerAdapter;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.LogActionsButtonsFragment;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.LogStatusFragment;
import uk.ac.sussex.android.bluesensehub.utilities.Const;
import uk.ac.sussex.android.bluesensehub.utilities.Utils;

/**
 * Created by ThiasTux.
 */

public class LogSessionActivity extends AppCompatActivity implements BluetoothServiceDelegate, LogStatusFragment.DeviceHandler, LogActionsButtonsFragment.DeviceHandler {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private LogStatusFragment logStatusFragment;
    private LogActionsButtonsFragment logActionsButtonsFragment;

    @Getter
    ArrayList<LogSensorSet> sensorSets = new ArrayList<>();

    ArrayList<BlueSenseDevice> devices = new ArrayList<>();

    private boolean isServiceStarted = false;
    private boolean isPolicyOngoing = false;

    LogSetup setup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_session);

        ButterKnife.bind(this);

        setup = getSetup(getIntent());


        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Logging session");
        }

        for (int i = 0; i < setup.getSensorsSets().size(); i++) {
            sensorSets.add(new LogSensorSet(i, setup.getSensorsSets().get(i)));
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
            case R.id.action_connect:
                connectDevices();
                break;
            case R.id.action_disconnect:
                disconnectDevices();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onServiceStarted(List<BlueSenseDevice> devices) {
        isServiceStarted = true;
        connectDevices();
    }

    @Override
    public void onDeviceAdded(BlueSenseDevice device) {

    }

    private LogSetup getSetup(Intent intent) {
        String stringSetup = intent.getStringExtra(Const.CHOSEN_LOG_SETUP);
        Gson gson = new Gson();
        return gson.fromJson(stringSetup, LogSetup.class);
    }

    private void setupFragments() {
        logStatusFragment = (LogStatusFragment) getSupportFragmentManager()
                .findFragmentById(R.id.logging_status_fragment);
        logActionsButtonsFragment = (LogActionsButtonsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.logging_actions_buttons_fragment);
    }

    private void setupTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), LogSessionActivity.this));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.fragment_tabs);
        tabLayout.setupWithViewPager(viewPager);

        logStatusFragment = (LogStatusFragment) ((PagerAdapter) viewPager.getAdapter()).getItem(0);
        logActionsButtonsFragment = (LogActionsButtonsFragment) ((PagerAdapter) viewPager.getAdapter()).getItem(1);

    }

    public void connectDevices() {
        final ArrayList<BlueSenseDevice> devices = this.devices;
        if (devices != null) {
            if (devices.size() <= Const.MAX_DEVICES) {
                final Context context = this;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (BlueSenseDevice device : devices) {
                            startService(new Intent(context, BluetoothService.class)
                                    .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                            new CommandBTC(device.getAddress()).getMessage()));
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } else {
                isPolicyOngoing = true;
            }
        }
    }

    public void disconnectDevices() {
        final ArrayList<BlueSenseDevice> devices = this.devices;
        if (devices != null) {
            final Context context = this;
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    for (BlueSenseDevice device : devices) {
                        startService(new Intent(context, BluetoothService.class)
                                .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                        new CommandBTD(device.getAddress()).getMessage()));
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void sendCommand(final SensorCommand command) {
        if (devices != null) {
            if (!isPolicyOngoing) {
                final Context context = this;
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (BlueSenseDevice device : devices) {
                            startService(new Intent(context, BluetoothService.class)
                                    .putExtra(Const.COMMAND_SERVICE_INTENT_KEY,
                                            new CommandBSM(device.getAddress(), command.getValue() + "\n").getMessage()));
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            } else {

            }
        }
    }

    @Override
    public ArrayList<LogSensorSet> getSets() {
        return sensorSets;
    }

    @Override
    public void setDevices(ArrayList<BlueSenseDevice> devices) {
        this.devices = devices;
    }

}
