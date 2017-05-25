package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.SelectDevicesAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;

/**
 * Created by ThiasTux.
 */
public class SelectDevicesActivity extends Activity implements SelectDevicesAdapter.OnItemClickListener {

    @BindView(R.id.list_devices)
    RecyclerView selectDevicesView;
    @BindView(R.id.confirm_button)
    Button confirmButton;
    @BindView(R.id.cancel_button)
    Button cancelButton;
    @BindView(R.id.select_all_button)
    Button selectAllButton;
    @BindView(R.id.deselect_all_button)
    Button deselectAllButton;
    private List<BluetoothDevice> bluetoothDevices;
    private ArrayList<BluetoothDevice> selectedDevices;
    private boolean[] status;
    private SelectDevicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_devices);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        bluetoothDevices = intent.getParcelableArrayListExtra(Const.DEVICES_LIST);
        selectedDevices = new ArrayList<>();

        status = new boolean[bluetoothDevices.size()];

        setResult(Activity.RESULT_CANCELED);

        adapter = new SelectDevicesAdapter(bluetoothDevices);
        adapter.setOnItemClickListener(this);
        selectDevicesView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        selectDevicesView.setLayoutManager(llm);
        selectDevicesView.setAdapter(adapter);

    }

    @OnClick(R.id.confirm_button)
    public void onConfirmButtonClick() {
        boolean tmp = false;
        for (int i = 0; i < status.length; i++) {
            boolean b = status[i];
            if (b)
                selectedDevices.add(bluetoothDevices.get(i));
            tmp = tmp | b;
        }
        if (!tmp)
            Toast.makeText(this, R.string.no_device_selected, Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Const.SELECTED_DEVICES, selectedDevices);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @OnClick(R.id.cancel_button)
    public void onCancelButtonClick() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }

    @OnClick(R.id.select_all_button)
    public void onSelectAllClick() {
        selectAllButton.setVisibility(View.GONE);
        deselectAllButton.setVisibility(View.VISIBLE);
        int size = bluetoothDevices.size();
        if (size > 7)
            Toast.makeText(this, "Maximum number of devices reached. First seven selected.", Toast.LENGTH_SHORT).show();
        for (int j = 0; j < size; j++)
            status[j] = true;
        adapter.setStatuses(true);
        adapter.notifyDataSetChanged();
    }

    @OnClick(R.id.deselect_all_button)
    public void onDeselectAllClick() {
        selectAllButton.setVisibility(View.VISIBLE);
        deselectAllButton.setVisibility(View.GONE);
        for (int j = 0; j < status.length; j++)
            status[j] = false;
        adapter.setStatuses(false);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View v, int position) {
        status[position] = !status[position];
        boolean tmp = true;
        for (boolean b : status)
            tmp = tmp & b;
        if (tmp) {
            selectAllButton.setVisibility(View.GONE);
            deselectAllButton.setVisibility(View.VISIBLE);
        } else {
            selectAllButton.setVisibility(View.VISIBLE);
            deselectAllButton.setVisibility(View.GONE);
        }
        adapter.notifyItemChanged(position);
    }

}
