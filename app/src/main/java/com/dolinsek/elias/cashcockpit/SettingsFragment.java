package com.dolinsek.elias.cashcockpit;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by elias on 12.01.2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}