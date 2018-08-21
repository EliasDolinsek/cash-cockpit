package com.dolinsek.elias.cashcockpit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.BackupHelper;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolbox;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;

import static com.dolinsek.elias.cashcockpit.components.BackupHelper.BACKUP_LOCATION_LOCAL;


/**
 * Created by elias on 12.01.2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int RC_SING_IN = 241;

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        Preference preferenceSingInOut = findPreference("preference_sign_in_out");
        if (currentUser != null){
            preferenceSingInOut.setTitle(R.string.label_sing_out);
            preferenceSingInOut.setOnPreferenceClickListener(preference -> {
                new SignOutDialogFragment().show(getChildFragmentManager(), "sign_out");
                return true;
            });
        } else {
            preferenceSingInOut.setTitle(R.string.label_sign_in);
            preferenceSingInOut.setOnPreferenceClickListener(preference -> {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(Arrays.asList(
                                        new AuthUI.IdpConfig.GoogleBuilder().build(),
                                        new AuthUI.IdpConfig.TwitterBuilder().build(),
                                        new AuthUI.IdpConfig.FacebookBuilder().build(),
                                        new AuthUI.IdpConfig.EmailBuilder().build()))
                                .build(),
                        RC_SING_IN);

                return true;
            });
        }

        findPreference("preference_delete_data").setOnPreferenceClickListener(preference -> {
            new DeleteDataDialogFragment().show(getChildFragmentManager(), "delete_data");
            return true;
        });

        findPreference("preference_make_backup").setOnPreferenceClickListener(preference -> {
            com.dolinsek.elias.cashcockpit.CreateBackupDialogFragment createBackupDialogFragment = new com.dolinsek.elias.cashcockpit.CreateBackupDialogFragment();
            createBackupDialogFragment.show(getChildFragmentManager(), "create_backup");
            return true;
        });

        findPreference("preference_synchronize_from_backup").setOnPreferenceClickListener(preference -> {
            ResetDataDialogFragment resetDataDialogFragment = new ResetDataDialogFragment();
            resetDataDialogFragment.show(getChildFragmentManager(), "reset_to_backup");
            return true;
        });

        if (currentUser == null){
            Preference backupLocationPreference = findPreference("preference_backup_location");
            Preference showDataPreference = findPreference("preference_show_data");

            backupLocationPreference.setEnabled(false);
            showDataPreference.setEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SING_IN && resultCode == Activity.RESULT_OK){
            Toolbox.restartCashCockpit(getActivity());
        }
    }

    public static class SignOutDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.dialog_title_sing_out)
                    .setMessage(R.string.dialog_msg_sing_out_description)
                    .setPositiveButton(R.string.dialog_action_sign_out, (dialogInterface, i) -> {
                        FirebaseAuth.getInstance().signOut();
                        AuthUI.getInstance().signOut(getContext());

                        deleteLocallySavedData();
                        Toolbox.restartCashCockpit(getContext());
                    }).setNegativeButton(R.string.dialog_action_close, null);

            return builder.create();
        }

        private void deleteLocallySavedData(){
            Database.setBankAccounts(new ArrayList<>());
            Database.setAutoPays(new ArrayList<>());
            Database.setPrimaryCategories(new ArrayList<>());
            Database.save(getContext());
        }
    }

    public static class ShowDataDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.dialog_title_all_your_saved_data).setPositiveButton(R.string.dialog_action_close, (dialog, which) -> dismiss()).setMessage(Database.getDataAsString());
            return builder.create();
        }
    }
}