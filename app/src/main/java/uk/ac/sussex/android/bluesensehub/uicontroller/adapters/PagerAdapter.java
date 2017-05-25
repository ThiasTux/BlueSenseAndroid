package uk.ac.sussex.android.bluesensehub.uicontroller.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import uk.ac.sussex.android.bluesensehub.R;
import uk.ac.sussex.android.bluesensehub.uicontroller.activities.LogSessionActivity;
import uk.ac.sussex.android.bluesensehub.uicontroller.activities.StreamSessionActivity;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.LogActionsButtonsFragment;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.LogStatusFragment;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.StreamParametersFragment;
import uk.ac.sussex.android.bluesensehub.uicontroller.fragments.StreamStatusFragment;

/**
 * Created by ThiasTux.
 */

public class PagerAdapter extends FragmentPagerAdapter {

    private final int NUM_TABS = 2;
    private Context context;
    private String[] tabTitles = new String[NUM_TABS];
    private Fragment firstFragment;
    private Fragment secondFragment;

    public PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        if (context instanceof StreamSessionActivity) {
            tabTitles[0] = context.getResources().getString(R.string.parameters);
            tabTitles[1] = context.getResources().getString(R.string.status);

            firstFragment = new StreamParametersFragment();
            secondFragment = new StreamStatusFragment();

        } else if (context instanceof LogSessionActivity) {
            tabTitles[0] = context.getResources().getString(R.string.status);
            tabTitles[1] = context.getResources().getString(R.string.actions);

            firstFragment = new LogStatusFragment();
            secondFragment = new LogActionsButtonsFragment();
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return firstFragment;
        else
            return secondFragment;
    }

    @Override
    public int getCount() {
        return NUM_TABS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
