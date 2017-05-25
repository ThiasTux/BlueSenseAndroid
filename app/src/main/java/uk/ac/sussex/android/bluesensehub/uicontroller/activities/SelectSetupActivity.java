package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.model.LogSetup;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.SelectDevicesAdapter;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.SelectSetupAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;

/**
 * Created by ThiasTux.
 */

public class SelectSetupActivity extends Activity implements SelectDevicesAdapter.OnItemClickListener, SelectSetupAdapter.OnItemClickListener {

    @BindView(R.id.list_setup)
    RecyclerView selectSetupRV;
    @BindView(R.id.list_devices)
    RecyclerView selectDevicesRV;
    @BindView(R.id.confirm_button)
    Button confirmButton;
    @BindView(R.id.cancel_button)
    Button cancelButton;

    private List<BluetoothDevice> bluetoothDevices;
    private boolean[] status;
    private SelectDevicesAdapter deviceAdapter;
    private SelectSetupAdapter setupAdapter;
    private ArrayList<LogSetup> setups;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_setup);

        ButterKnife.bind(this);

        Intent intent = getIntent();

        bluetoothDevices = intent.getParcelableArrayListExtra(Const.DEVICES_LIST);

        status = new boolean[bluetoothDevices.size()];

        setups = getSetups();

        setResult(Activity.RESULT_CANCELED);

        setupAdapter = new SelectSetupAdapter(this, setups);
        setupAdapter.setOnItemClickListener(this);
        selectSetupRV.setHasFixedSize(true);
        LinearLayoutManager setupllm = new LinearLayoutManager(this);
        setupllm.setOrientation(LinearLayoutManager.VERTICAL);
        selectSetupRV.setLayoutManager(setupllm);
        selectSetupRV.setAdapter(setupAdapter);

        deviceAdapter = new SelectDevicesAdapter(bluetoothDevices);
        deviceAdapter.setOnItemClickListener(this);
        selectDevicesRV.setHasFixedSize(true);
        LinearLayoutManager devicellm = new LinearLayoutManager(this);
        devicellm.setOrientation(LinearLayoutManager.VERTICAL);
        selectDevicesRV.setLayoutManager(devicellm);
        selectDevicesRV.setAdapter(deviceAdapter);

    }

    @OnClick(R.id.confirm_button)
    public void onConfirmButtonClick() {
        boolean tmp = false;
        List<String> addresses = new ArrayList<>();
        for (int i = 0; i < status.length; i++) {
            boolean b = status[i];
            if (b) {
                addresses.add(bluetoothDevices.get(i).getAddress());
            }
            tmp = tmp | b;
        }
        if (!tmp)
            Toast.makeText(this, R.string.no_device_selected, Toast.LENGTH_SHORT).show();
        else {
            SparseArray<List<String>> selectedDevices = new SparseArray<>();
            selectedDevices.put(0, addresses);
            LogSetup setup = new LogSetup("Dummy", 1, selectedDevices);
            Gson gson = new Gson();
            Intent intent = new Intent();
            intent.putExtra(Const.CHOSEN_LOG_SETUP, gson.toJson(setup));
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

    @Override
    public void onItemClick(View v, int position) {
        status[position] = !status[position];
        deviceAdapter.notifyItemChanged(position);
    }

    public ArrayList<LogSetup> getSetups() {
        ArrayList<LogSetup> setups;
        SharedPreferences sharedPreferences = getSharedPreferences(Const.PREFS_NAME, 0);
        String savedSetups = sharedPreferences.getString(Const.PREFS_SAVED_SETUPS, null);
        Gson gson = new Gson();
        if (savedSetups != null) {
            Type setupsListType = new TypeToken<ArrayList<LogSetup>>() {
            }.getType();
            setups = gson.fromJson(savedSetups, setupsListType);
            return setups;
        } else {
            return null;
        }
    }

    @Override
    public void onSetupItemClick(View v, int position) {
        LogSetup setup = setups.get(position);
        Gson gson = new Gson();
        Intent intent = new Intent();
        intent.putExtra(Const.CHOSEN_LOG_SETUP, gson.toJson(setup));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
