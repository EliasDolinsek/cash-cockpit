package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DatabaseFragment extends Fragment {

    /**
     * Tab layout for tab-navigation
     */
    private TabLayout mTabLayout;

    /**
     * ViewPager what contains fragments for navigation
     */
    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Inflates View
        View inflatedView = inflater.inflate(R.layout.fragment_database, container, false);

        mViewPager = (ViewPager) inflatedView.findViewById(R.id.vp_options);
        mViewPager.setAdapter(new Adapter(getChildFragmentManager()));

        //Sets ViewPager
        mTabLayout = (TabLayout) inflatedView.findViewById(R.id.tab_layout_options);
        mTabLayout.setupWithViewPager(mViewPager);

        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class Adapter extends FragmentPagerAdapter {

        //Number of fragments/tabs
        private static final int TABS = 3;

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: return new BankAccountsFragment();
                case 1: return new AutoPaysFragment();
                case 2: return new CategoriesFragment();
                default: return new BankAccountsFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0: return getString(R.string.tab_bank_accounts);
                case 1: return getString(R.string.tab_auto_pays);
                case 2: return getString(R.string.tab_categories);
            }

            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            return TABS;
        }
    }

}
