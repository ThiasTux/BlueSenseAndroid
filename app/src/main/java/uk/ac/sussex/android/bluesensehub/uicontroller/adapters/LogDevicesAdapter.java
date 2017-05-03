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
 * Created by ThiasTux.
 */

public class LogDevicesAdapter extends RecyclerView.Adapter<LogDevicesAdapter.ViewHolder> {

    private Map<String, BlueSenseDevice> deviceMap = new HashMap<>();
    private List<String> deviceAddresses = new ArrayList<>();

    public LogDevicesAdapter(ArrayList<BlueSenseDevice> devicesList) {
        for (BlueSenseDevice device : devicesList) {
            deviceAddresses.add(device.getAddress());
            deviceMap.put(device.getAddress(), device);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.loggin_device_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BlueSenseDevice device = deviceMap.get(deviceAddresses.get(position));
        holder.deviceNameTV.setText(device.getName());
        holder.deviceAddressTV.setText(device.getAddress());
        int state = device.getStatus();
        switch (state) {
            case BluetoothState.STATE_CONNECTING:
                holder.deviceStatusTV.setText(R.string.connecting);
                break;
            case BluetoothState.STATE_CONNECTED:
                holder.deviceStatusTV.setText(R.string.connected);
                break;
            default:
                holder.deviceStatusTV.setText(R.string.disconnected);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return deviceAddresses.size();
    }

    public void setStatus(String mAddress, int state) {
        deviceMap.get(mAddress).setStatus(state);
    }

    public void notifyItemChanged(String mAddress) {
        LogDevicesAdapter.this.notifyItemChanged(deviceAddresses.indexOf(mAddress));
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.device_name)
        TextView deviceNameTV;
        @BindView(R.id.device_address)
        TextView deviceAddressTV;
        @BindView(R.id.device_status)
        TextView deviceStatusTV;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
