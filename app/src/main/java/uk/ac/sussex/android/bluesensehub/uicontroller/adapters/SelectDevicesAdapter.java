package uk.ac.sussex.android.bluesensehub.uicontroller.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;

/**
 * Created by ThiasTux.
 */
public class SelectDevicesAdapter extends RecyclerView.Adapter<SelectDevicesAdapter.ViewHolder> {

    private List<BluetoothDevice> bluetoothDevices;
    private boolean[] status;
    private OnItemClickListener onItemClickListener;

    public SelectDevicesAdapter(List<BluetoothDevice> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices;
        status = new boolean[bluetoothDevices.size()];
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_device_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = bluetoothDevices.get(position);
        holder.deviceNameTextView.setText(device.getName());
        holder.deviceAddressTextView.setText(device.getAddress());
        holder.deviceSelectedCheckBox.setChecked(status[position]);
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setStatuses(boolean status) {
        for (int j = 0; j < this.status.length; j++)
            this.status[j] = status;
    }

    private void changeStatus(int position) {
        status[position] = !status[position];
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.device_name)
        TextView deviceNameTextView;
        @BindView(R.id.device_address)
        TextView deviceAddressTextView;
        @BindView(R.id.selected_checkbox)
        CheckBox deviceSelectedCheckBox;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                changeStatus(getAdapterPosition());
                onItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
