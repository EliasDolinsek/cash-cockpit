package com.dolinsek.elias.cashcockpit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.BackupHelper;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


/**
 * Created by elias on 12.01.2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        findPreference("preference_sign_out").setOnPreferenceClickListener(preference -> {
            AuthUI.getInstance().signOut(getContext());
            Toast.makeText(getContext(), getString(R.string.toast_singed_out_successfully), Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return true;
        });

        findPreference("preference_delete_data").setOnPreferenceClickListener(preference -> {
            new DeleteDataDialogFragment().show(getChildFragmentManager(), "delete_data");
            return true;
        });

        findPreference("preference_show_data").setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), "Not implemented yet!", Toast.LENGTH_SHORT).show();
            //new ShowDataDialogFragment().show(getChildFragmentManager(), "show_data");
            return true;
        });

        findPreference("preference_make_backup").setOnPreferenceClickListener(preference -> {
            new BackupHelper(getActivity()).createBackup();
            return true;
        });

        findPreference("preference_synchronize_from_backup").setOnPreferenceClickListener(preference -> {
            new BackupHelper(getActivity()).overrideDataWithLocalBackup();
            return true;
        });
    }

    public static class DeleteDataDialogFragment extends DialogFragment{

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.dialog_title_delete_all_data)).setMessage(R.string.dialog_msg_delete_all_data_conformation)
            .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                Database.deleteDatabase();
                Database.save(getContext());
                Toast.makeText(getContext(), getString(R.string.toast_data_got_deleted_successfully), Toast.LENGTH_SHORT).show();
            }).setNegativeButton(R.string.dialog_action_cancel, (dialog, which) -> dismiss());

            return builder.create();
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