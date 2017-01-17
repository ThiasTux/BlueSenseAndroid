package uk.ac.sussex.android.bluesensehub.uicontroller.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;

/**
 * Created by mathias on 16/01/17.
 */
public class BlueSenseDevicesAdapter extends RecyclerView.Adapter<BlueSenseDevicesAdapter.ViewHolder> {

    private List<BluetoothDevice> deviceList;
    private ClickListener clickListener;

    public BlueSenseDevicesAdapter(ArrayList<BluetoothDevice> devicesList) {
        this.deviceList = devicesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluesense_device_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = deviceList.get(position);
        holder.deviceNameTextView.setText(device.getName());
        holder.deviceAddressTextView.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void addDevice(BluetoothDevice device) {
        deviceList.add(device);
    }

    public void removeAll() {
        deviceList = new ArrayList<>();
    }

    public BluetoothDevice getItem(int position) {
        return deviceList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.device_name)
        TextView deviceNameTextView;
        @BindView(R.id.device_address)
        TextView deviceAddressTextView;

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
