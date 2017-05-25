package uk.ac.sussex.android.bluesensehub.uicontroller.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.model.LogSetup;

/**
 * Created by ThiasTux.
 */

public class SelectSetupAdapter extends RecyclerView.Adapter<SelectSetupAdapter.ViewHolder> {

    @BindView(android.R.id.text1)
    TextView setupNameTV;
    private OnItemClickListener onItemClickListener;

    private Context context;
    private ArrayList<LogSetup> setups;

    public SelectSetupAdapter(Context context, ArrayList<LogSetup> setups) {
        this.context = context;
        this.setups = setups;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LogSetup setup = setups.get(position);
        holder.setupNameTV.setText(setup.getSetupName());
        if (Build.VERSION.SDK_INT < 23) {
            holder.setupNameTV.setTextAppearance(context, android.R.style.TextAppearance_Large);
        } else {
            holder.setupNameTV.setTextAppearance(android.R.style.TextAppearance_Large);
        }
    }

    @Override
    public int getItemCount() {
        return setups.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onSetupItemClick(View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(android.R.id.text1)
        TextView setupNameTV;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onSetupItemClick(v, getAdapterPosition());
            }
        }
    }
}
