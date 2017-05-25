package uk.ac.sussex.android.bluesensehub.uicontroller.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnFailed;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnOngoing;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientDisconnSuccess;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.BluetoothState;
import uk.ac.sussex.android.bluesensehub.model.LogSensorSet;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.LogDevicesAdapter;

/**
 * Created by ThiasTux.
 */

public class LogSensorSetFragment extends Fragment {

    private static final String SET_ID = "Set";
    private static final String SENSOR_SET = "SetDevices";
    @BindView(R.id.device_list)
    RecyclerView devicesListRV;
    private LogDevicesAdapter adapter;
    private LogSensorSet logSensorSet;
    private ArrayList<BlueSenseDevice> blueSenseDevices;

    static LogSensorSetFragment newInstance(int position, LogSensorSet sensorSet) {
        LogSensorSetFragment fragment = new LogSensorSetFragment();
        Bundle args = new Bundle();

        args.putInt(SET_ID, position);

        args.putSerializable(SENSOR_SET, sensorSet);

        fragment.setArguments(args);

        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_set, container, false);
        ButterKnife.bind(this, view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());

        logSensorSet = (LogSensorSet) getArguments().getSerializable(SENSOR_SET);

        llm.setOrientation(LinearLayoutManager.VERTICAL);
        blueSenseDevices = logSensorSet.getDevices();
        adapter = new LogDevicesAdapter(blueSenseDevices);
        devicesListRV.setLayoutManager(llm);
        devicesListRV.setHasFixedSize(true);
        devicesListRV.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
    }

    public void setLogSensorSet(LogSensorSet logSensorSet) {
        this.logSensorSet = logSensorSet;
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnected(ClientConnSuccess clientConnSuccess) {
        adapter.setStatus(clientConnSuccess.getMAddress(), BluetoothState.STATE_CONNECTED);
        adapter.notifyItemChanged(clientConnSuccess.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnectionFailed(ClientConnFailed clientConnFailed) {
        adapter.setStatus(clientConnFailed.getMAddress(), BluetoothState.STATE_NONE);
        adapter.notifyItemChanged(clientConnFailed.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceDisconnected(ClientDisconnSuccess clientDisconnSuccess) {
        adapter.setStatus(clientDisconnSuccess.getMAddress(), BluetoothState.STATE_NONE);
        adapter.notifyItemChanged(clientDisconnSuccess.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeviceConnecting(ClientConnOngoing clientConnOngoing) {
        adapter.setStatus(clientConnOngoing.getMAddress(), BluetoothState.STATE_CONNECTING);
        adapter.notifyItemChanged(clientConnOngoing.getMAddress());
    }

}
