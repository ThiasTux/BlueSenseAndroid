package uk.ac.sussex.android.bluesensehub.uicontroller.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.model.LogSetup;
import uk.ac.sussex.android.bluesensehub.uicontroller.adapters.SetupPrefsAdapter;
import uk.ac.sussex.android.bluesensehub.utilities.Const;

/**
 * Created by ThiasTux.
 */

public class DefineLogSetupsActivity extends AppCompatActivity implements SetupPrefsAdapter.ClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.setups_list)
    ListView setupsListView;
    @BindView(R.id.no_setup_label)
    TextView noSetupTextView;
    ArrayList<LogSetup> setupList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_log_setups);

        ButterKnife.bind(this);

        toolbar.setTitle(R.string.logging_setups);
        setSupportActionBar(toolbar);

        initializeSetupList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_define_log_setups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_reset_setups:
                resetSetups();
                initializeSetupList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeSetupList() {
        setupList = getSetupList();
        if (setupList != null) {
            noSetupTextView.setVisibility(View.GONE);
            setupsListView.setVisibility(View.VISIBLE);
            SetupPrefsAdapter adapter = new SetupPrefsAdapter(this, setupList);
            adapter.setClickListener(this);
            setupsListView.setAdapter(adapter);
        } else {
            setupList = new ArrayList<>();
            noSetupTextView.setVisibility(View.VISIBLE);
            setupsListView.setVisibility(View.GONE);
        }

    }

    private void resetSetups() {
        SharedPreferences sharedPreferences = getSharedPreferences(Const.PREFS_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Const.PREFS_SAVED_SETUPS, null);
        editor.apply();
    }

    private ArrayList<LogSetup> getSetupList() {
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

    @OnClick(R.id.add_setup_button)
    public void onAddSetupClick() {
        Intent intent = new Intent(this, AddSetupActivity.class);
        startActivityForResult(intent, Const.REQUEST_ADD_SETUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_ADD_SETUP) {
            if (resultCode == RESULT_OK) {
                initializeSetupList();
            }
        }
    }

    @Override
    public void onClick(View v, int position) {

    }
}
