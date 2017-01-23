package uk.ac.sussex.android.bluesensehub.uicontroller.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.BluetoothState;

/**
 * Created by mathias on 16/01/17.
 */
public class BlueSenseDevicesAdapter extends RecyclerView.Adapter<BlueSenseDevicesAdapter.ViewHolder> {

    private Map<String, BlueSenseDevice> deviceMap = new HashMap<>();
    private List<String> deviceAddresses = new ArrayList<>();
    private ClickListener clickListener;

    public BlueSenseDevicesAdapter(ArrayList<BlueSenseDevice> devicesList) {
        for (BlueSenseDevice device : devicesList) {
            deviceAddresses.add(device.getAddress());
            deviceMap.put(device.getAddress(), device);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluesense_device_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BlueSenseDevice device = deviceMap.get(deviceAddresses.get(position));
        holder.deviceNameTextView.setText(device.getName());
        holder.deviceAddressTextView.setText(device.getAddress());
        int state = device.getState();
        switch (state) {
            case BluetoothState.STATE_CONNECTING:
                holder.deviceStatusTextView.setText(R.string.connecting);
                break;
            case BluetoothState.STATE_CONNECTED:
                holder.deviceStatusTextView.setText(R.string.connected);
                break;
            default:
                holder.deviceStatusTextView.setText(R.string.disconnected);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return deviceAddresses.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void addDevice(BlueSenseDevice device) {
        deviceAddresses.add(device.getAddress());
        deviceMap.put(device.getAddress(), device);
    }

    public void removeAll() {
        deviceAddresses = new ArrayList<>();
        deviceMap = new HashMap<>();
    }

    public BlueSenseDevice getItem(int position) {
        return deviceMap.get(deviceAddresses.get(position));
    }

    public void setStatus(int position, int state) {
        deviceMap.get(deviceAddresses.get(position)).setState(state);
    }

    public void setStatus(String address, int state) {
        deviceMap.get(address).setState(state);
    }

    public void notifyItemChanged(String mAddress) {
        BlueSenseDevicesAdapter.this.notifyItemChanged(deviceAddresses.indexOf(mAddress));
    }

    public void add(List<BlueSenseDevice> bluetoothDevices) {
        for (BlueSenseDevice device : bluetoothDevices) {
            deviceAddresses.add(device.getAddress());
            deviceMap.put(device.getAddress(), device);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.device_name)
        TextView deviceNameTextView;
        @BindView(R.id.device_address)
        TextView deviceAddressTextView;
        @BindView(R.id.device_status)
        TextView deviceStatusTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null)
                clickListener.onItemClick(view, getPosition());
        }
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }
}
