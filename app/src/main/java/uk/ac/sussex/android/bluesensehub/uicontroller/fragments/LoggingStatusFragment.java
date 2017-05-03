package uk.ac.sussex.android.bluesensehub.uicontroller.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientBytesReceived;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnFailed;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnOngoing;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientConnSuccess;
import uk.ac.sussex.android.bluesensehub.controllers.buses.ClientDisconnSuccess;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.BluetoothState;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.LogDevicesAdapter;

/**
 * Created by ThiasTux.
 */

public class LoggingStatusFragment extends Fragment {

    @BindView(R.id.device_list)
    RecyclerView devicesListRV;

    private DeviceHandler activity;
    private ArrayList<BlueSenseDevice> devices;
    private LogDevicesAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (DeviceHandler) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logging_status, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        devices = activity.getDevices();

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        adapter = new LogDevicesAdapter(devices);
        devicesListRV.setLayoutManager(llm);
        devicesListRV.setHasFixedSize(true);
        devicesListRV.setAdapter(adapter);

        activity.connectDevices();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public interface DeviceHandler {
        ArrayList<BlueSenseDevice> getDevices();

        void connectDevices();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnected(ClientConnSuccess clientConnSuccess) {
        adapter.setStatus(clientConnSuccess.getMAddress(), BluetoothState.STATE_CONNECTED);
        adapter.notifyItemChanged(clientConnSuccess.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnectionFailed(ClientConnFailed clientConnFailed) {
        Toast.makeText(getContext(), "Connection failed: " + clientConnFailed.getMAddress(), Toast.LENGTH_SHORT).show();
        adapter.setStatus(clientConnFailed.getMAddress(), BluetoothState.STATE_NONE);
        adapter.notifyItemChanged(clientConnFailed.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceDisconnected(ClientDisconnSuccess clientDisconnSuccess) {
        adapter.setStatus(clientDisconnSuccess.getMAddress(), BluetoothState.STATE_NONE);
        adapter.notifyItemChanged(clientDisconnSuccess.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeviceConnecting(ClientConnOngoing clientConnOngoing) {
        adapter.setStatus(clientConnOngoing.getMAddress(), BluetoothState.STATE_CONNECTING);
        adapter.notifyItemChanged(clientConnOngoing.getMAddress());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBytesReceived(ClientBytesReceived clientBytesReceived) {
        /*adapter.appendTextToDevice(clientBytesReceived.getAddress(), clientBytesReceived.getMessage());
        adapter.notifyItemChanged(clientBytesReceived.getMessage());*/
    }
}
