package uk.ac.sussex.android.bluesensehub.uicontroller.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.model.BlueSenseDevice;
import uk.ac.sussex.android.bluesensehub.model.LogSensorSet;

/**
 * Created by ThiasTux.
 */

public class LogStatusFragment extends Fragment {

    SensorSetAdapter adapter;

    @BindView(R.id.sets_list_vp)
    ViewPager setListVP;

    ArrayList<LogSensorSet> sensorSets;
    private DeviceHandler activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (DeviceHandler) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_status, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sensorSets = activity.getSets();
        adapter = new SensorSetAdapter(getChildFragmentManager(), sensorSets);
        setListVP.setAdapter(adapter);
        final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                activity.disconnectDevices();
                activity.setDevices(adapter.getDevices(position));
                activity.connectDevices();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        setListVP.addOnPageChangeListener(onPageChangeListener);
        setListVP.post(new Runnable() {
            @Override
            public void run() {
                onPageChangeListener.onPageSelected(setListVP.getCurrentItem());
            }
        });

    }

    public interface DeviceHandler {
        ArrayList<LogSensorSet> getSets();

        void setDevices(ArrayList<BlueSenseDevice> devices);
        void connectDevices();

        void disconnectDevices();
    }

    public static class SensorSetAdapter extends FragmentPagerAdapter {

        private List<LogSensorSet> sensorSets;

        public SensorSetAdapter(FragmentManager fm, List<LogSensorSet> sensorSets) {
            super(fm);
            this.sensorSets = sensorSets;
        }

        public ArrayList<BlueSenseDevice> getDevices(int position) {
            return sensorSets.get(position).getDevices();
        }

        @Override
        public Fragment getItem(int position) {
            return LogSensorSetFragment.newInstance(position, sensorSets.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Set " + position;
        }

        @Override
        public int getCount() {
            return sensorSets.size();
        }
    }
}
