package uk.ac.sussex.android.bluesensehub.uicontroller.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;

/**
 * Created by ThiasTux.
 */

public class SetsSelectDevicesAdapter extends RecyclerView.Adapter<SetsSelectDevicesAdapter.ViewHolder> {

    private List<BluetoothDevice> bluetoothDevices;
    private OnItemClickListener onItemClickListener;
    private boolean[] status;
    private List<Integer> setPossibleValues;

    public int getDeviceSetNum(int i) {
        return deviceSetNums[i];
    }

    private int[] deviceSetNums;
    private int totalSetNum = 0;
    private Context context;

    public SetsSelectDevicesAdapter(List<BluetoothDevice> bluetoothDevices, Context context) {
        this.bluetoothDevices = bluetoothDevices;
        int size = bluetoothDevices.size();
        this.status = new boolean[size];
        this.deviceSetNums = new int[size];
        for (int i = 0; i < size; i++)
            deviceSetNums[i] = 0;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_device_set_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = bluetoothDevices.get(position);
        holder.deviceNameTV.setText(device.getName());
        holder.deviceAddressTV.setText(device.getAddress());
        holder.deviceSelectedCB.setChecked(status[position]);
        if (totalSetNum != 0) {
            ArrayAdapter<Integer> adapter = new ArrayAdapter<>(
                    context, android.R.layout.simple_spinner_item, setPossibleValues);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.deviceSetNumberSP.setAdapter(adapter);
            int selectedSet = deviceSetNums[position];
            if (selectedSet != 0)
                holder.deviceSetNumberSP.setSelection(selectedSet);
        }
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    public void setSetPossibleValues(List<Integer> setPossibleValues) {
        this.setPossibleValues = setPossibleValues;
        this.totalSetNum = setPossibleValues.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public BluetoothDevice getDevice(int i) {
        return bluetoothDevices.get(i);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AdapterView.OnItemSelectedListener {

        @BindView(R.id.device_name)
        TextView deviceNameTV;
        @BindView(R.id.device_address)
        TextView deviceAddressTV;
        @BindView(R.id.selected_checkbox)
        CheckBox deviceSelectedCB;
        @BindView(R.id.sets_number)
        Spinner deviceSetNumberSP;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            deviceSetNumberSP.setOnItemSelectedListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                changeStatus(getAdapterPosition());
                onItemClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            deviceSetNumberSP.setSelection(position);
            deviceSetNums[getAdapterPosition()] = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private void changeStatus(int adapterPosition) {
        status[adapterPosition] = !status[adapterPosition];
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }
}
