package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {

    private static final String EXTRA_BANK_ACCOUNTS_STATISTICS_FRAGMENT = "bankAccountsStatisticsFragment";
    private static final String EXTRA_BILLS_STATISTICS_FRAGMENT = "billStatisticsFragment";
    private static final String EXTRA_CATEGORIES_STATISTICS_FRAGMENT = "categoriesStatisticsFragment";
    private static final String EXTRA_GOALS_STATISTICS_FRAGMENT = "goalsStatisticsFragment";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private BankAccountsStatisticsFragment bankAccountsStatisticsFragment;
    private BillsStatisticsFragment billsStatisticsFragment;
    private CategoriesStatisticsFragment categoriesStatisticsFragment;
    private GoalsStatisticsFragment goalsStatisticsFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_statistics, container, false);

        setupFragments();

        mTabLayout = (TabLayout) inflatedView.findViewById(R.id.tl_statistics);
        mViewPager = (ViewPager) inflatedView.findViewById(R.id.vp_statistics);

        mViewPager.setAdapter(new Adapter(getChildFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);

        return inflatedView;
    }

    private class Adapter extends FragmentPagerAdapter {

        private static final int TABS = 4;

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:return bankAccountsStatisticsFragment;
                case 1: return billsStatisticsFragment;
                case 2: return categoriesStatisticsFragment;
                case 3: return goalsStatisticsFragment;
                default: throw new IllegalStateException("Couldn't find a fragment for psoition " + position);
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0: return getString(R.string.tab_accounts);
                case 1: return getString(R.string.tab_bills);
                case 2: return getString(R.string.tab_categories);
                default: return getString(R.string.tab_goals);
            }
        }

        @Override
        public int getCount() {
            return TABS;
        }
    }

    private void setupFragments(){
        bankAccountsStatisticsFragment = new BankAccountsStatisticsFragment();
        billsStatisticsFragment = new BillsStatisticsFragment();
        categoriesStatisticsFragment = new CategoriesStatisticsFragment();
        goalsStatisticsFragment = new GoalsStatisticsFragment();
    }
}
