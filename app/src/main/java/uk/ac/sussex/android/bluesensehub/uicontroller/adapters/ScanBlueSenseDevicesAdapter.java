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
public class ScanBlueSenseDevicesAdapter extends RecyclerView.Adapter<ScanBlueSenseDevicesAdapter.ViewHolder> {

    private List<BluetoothDevice> bluetoothDevices;
    private OnItemClickListener onItemClickListener;

    public ScanBlueSenseDevicesAdapter(ArrayList<BluetoothDevice> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.scanned_device_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = bluetoothDevices.get(position);
        holder.deviceNameTextView.setText(device.getName());
        holder.deviceAddressTextView.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void removeAll() {
        bluetoothDevices = new ArrayList<>();
    }

    public void addDevice(BluetoothDevice device) {
        bluetoothDevices.add(device);
    }

    public BluetoothDevice getItem(int position) {
        return bluetoothDevices.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.device_name)
        TextView deviceNameTextView;
        @BindView(R.id.device_address)
        TextView deviceAddressTextView;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, getPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
