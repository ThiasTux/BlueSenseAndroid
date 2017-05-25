package uk.ac.sussex.android.bluesensehub.uicontroller.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.model.LogSetup;

/**
 * Created by ThiasTux.
 */

public class SetupPrefsAdapter extends ArrayAdapter<LogSetup> {

    @BindView(android.R.id.text1)
    TextView setupNameTV;
    @BindView(android.R.id.text2)
    TextView numSetsTV;

    Context context;
    ClickListener clickListener;

    public SetupPrefsAdapter(Context context, ArrayList<LogSetup> setups) {
        super(context, 0, setups);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LogSetup setup = getItem(position);
        final int clickPosition = position;
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        ButterKnife.bind(this, convertView);
        setupNameTV.setText(setup.getSetupName());
        setupNameTV.setTextAppearance(context, android.R.style.TextAppearance_Large);
        numSetsTV.setText(Integer.toString(setup.getNumSets()));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null)
                    clickListener.onClick(v, clickPosition);
            }
        });
        return convertView;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(View v, int position);
    }
}
