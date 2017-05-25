package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.SetsSelectDevicesAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;

/**
 * Created by ThiasTux.
 */

public class AddSetupActivity extends Activity implements SetsSelectDevicesAdapter.OnItemClickListener {

    @BindView(R.id.new_setup_name)
    EditText newSetupNameET;
    @BindView(R.id.sets_number)
    EditText setsNumET;
    @BindView(R.id.list_devices)
    RecyclerView listDevicesRV;
    @BindView(R.id.confirm_button)
    Button confirmButton;
    @BindView(R.id.cancel_button)
    Button cancelButton;
    private List<BluetoothDevice> bluetoothDevices;
    private ArrayList<BluetoothDevice> selectedDevices;
    private boolean[] status;
    private int[] setNums;
    private SetsSelectDevicesAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_setup);

        ButterKnife.bind(this);

        bluetoothDevices = new ArrayList<>();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices())
            if (device.getName().contains("BlueSense"))
                bluetoothDevices.add(device);

        status = new boolean[bluetoothDevices.size()];
        setNums = new int[bluetoothDevices.size()];

        setResult(Activity.RESULT_CANCELED);

        adapter = new SetsSelectDevicesAdapter(bluetoothDevices, this);
        adapter.setOnItemClickListener(this);
        listDevicesRV.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listDevicesRV.setLayoutManager(llm);
        listDevicesRV.setAdapter(adapter);

        setsNumET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (!str.equals("")) {
                    int value = Integer.parseInt(str);
                    List<Integer> list = new ArrayList<>();
                    for (int i = 0; i < value; i++) {
                        list.add(i);
                    }
                    adapter.setSetPossibleValues(list);
                    adapter.notifyDataSetChanged();
                }
            }
        });

    }

    @OnClick(R.id.confirm_button)
    public void onConfirmButtonClick() {
        String setupName = newSetupNameET.getText().toString();
        int totalSetNum = Integer.parseInt(setsNumET.getText().toString());
        SparseArray<List<String>> loggingSetMap = new SparseArray<>();
        if (setupName.trim().isEmpty())
            Toast.makeText(this, R.string.no_setup_name, Toast.LENGTH_SHORT).show();
        else {
            boolean tmp = false;
            for (int i = 0; i < status.length; i++) {
                if (status[i]) {
                    int setNum = adapter.getDeviceSetNum(i);
                    List<String> listTmp = loggingSetMap.get(setNum);
                    if (listTmp != null) {
                        listTmp = loggingSetMap.get(setNum);
                    } else {
                        listTmp = new ArrayList<>();
                    }
                    listTmp.add(adapter.getDevice(i).getAddress());
                    loggingSetMap.put(setNum, listTmp);
                }
                tmp = tmp | status[i];
            }
            if (!tmp)
                Toast.makeText(this, R.string.no_device_selected, Toast.LENGTH_SHORT).show();
            else {
                saveSetup(new LogSetup(setupName, totalSetNum, loggingSetMap));
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
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
        boolean tmp = true;
        for (boolean b : status)
            tmp = tmp & b;
        adapter.notifyItemChanged(position);
    }

    private void saveSetup(LogSetup setup) {
        ArrayList<LogSetup> logSetups;
        SharedPreferences sharedPreferences = getSharedPreferences(Const.PREFS_NAME, 0);
        String savedLoggingSetups = sharedPreferences.getString(Const.PREFS_SAVED_SETUPS, null);
        Gson gson = new Gson();
        if (savedLoggingSetups != null) {
            Type setupsListType = new TypeToken<ArrayList<LogSetup>>() {
            }.getType();
            logSetups = gson.fromJson(savedLoggingSetups, setupsListType);
        } else {
            logSetups = new ArrayList<>();
        }
        logSetups.add(setup);
        savedLoggingSetups = gson.toJson(logSetups);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.PREFS_SAVED_SETUPS, savedLoggingSetups);
        editor.apply();
    }

}
