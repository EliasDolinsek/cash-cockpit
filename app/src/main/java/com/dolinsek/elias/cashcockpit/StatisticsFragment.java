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
    private static final String EXTRA_CATEGORIES_STATISTICS_FRAGMENT = "categoriesStatisticsFragment";
    private static final String EXTRA_GOALS_STATISTICS_FRAGMENT = "goalsStatisticsFragment";

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private BankAccountsStatisticsFragment bankAccountsStatisticsFragment;
    private CategoriesStatisticsFragment categoriesStatisticsFragment;
    private GoalsStatisticsFragment goalsStatisticsFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_statistics, container, false);

        loadFragments(savedInstanceState);

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
                case 1: return new BankAccountsFragment();
                case 2: return categoriesStatisticsFragment;
                case 3: return goalsStatisticsFragment;
                default: return new BankAccountsFragment();
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

    private void loadFragments(Bundle savedInstanceState){
        if (savedInstanceState != null){
            bankAccountsStatisticsFragment = (BankAccountsStatisticsFragment) getChildFragmentManager().getFragment(savedInstanceState, EXTRA_BANK_ACCOUNTS_STATISTICS_FRAGMENT);
            categoriesStatisticsFragment = (CategoriesStatisticsFragment) getChildFragmentManager().getFragment(savedInstanceState, EXTRA_CATEGORIES_STATISTICS_FRAGMENT);
            goalsStatisticsFragment = (GoalsStatisticsFragment) getChildFragmentManager().getFragment(savedInstanceState, EXTRA_GOALS_STATISTICS_FRAGMENT);
        } else {
            bankAccountsStatisticsFragment = new BankAccountsStatisticsFragment();
            categoriesStatisticsFragment = new CategoriesStatisticsFragment();
            goalsStatisticsFragment = new GoalsStatisticsFragment();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getChildFragmentManager().putFragment(outState, EXTRA_BANK_ACCOUNTS_STATISTICS_FRAGMENT, bankAccountsStatisticsFragment);
        getChildFragmentManager().putFragment(outState, EXTRA_CATEGORIES_STATISTICS_FRAGMENT, categoriesStatisticsFragment);
        getChildFragmentManager().putFragment(outState, EXTRA_GOALS_STATISTICS_FRAGMENT, goalsStatisticsFragment);
    }
}
