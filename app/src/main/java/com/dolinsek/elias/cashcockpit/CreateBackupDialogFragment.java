package com.dolinsek.elias.cashcockpit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BackupHelper;

import java.util.Timer;

/**
 * Created by Elias Dolinsek on 14.08.2018 for cash-cockpit.
 */
public class CreateBackupDialogFragment extends DialogFragment{

    private TextView txvCurrentState, txvCreateBackupDescription;
    private Button btnPositive, btnNegative;
    private ConstraintLayout clRoot;
    private ImageView imvBackupDone;
    private ProgressBar pgbIndicator;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View inflatedView = layoutInflater.inflate(R.layout.dialog_create_backup_creating, null);

        alertBuilder.setTitle(R.string.dialog_title_create_backup)
                .setPositiveButton(R.string.dialog_action_create_backup, null)
                .setNegativeButton(R.string.dialog_action_cancel, null)
                .setView(inflatedView);

        imvBackupDone = inflatedView.findViewById(R.id.imv_dialog_create_backup_done);
        txvCurrentState = inflatedView.findViewById(R.id.txv_dialog_create_backup_creating);
        clRoot = inflatedView.findViewById(R.id.cl_dialog_create_backup_creating_root);
        pgbIndicator = inflatedView.findViewById(R.id.pgb_dialog_create_backup_indicator);
        txvCreateBackupDescription = inflatedView.findViewById(R.id.txv_dialog_create_backup_description);

        return alertBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog alertDialog = (AlertDialog)getDialog();
        btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        setupDisplayBackupDescriptionView();

        btnPositive.setOnClickListener(v -> {
            txvCreateBackupDescription.setVisibility(View.GONE);
            if (BackupHelper.getBackupLocation(getContext()).equals(BackupHelper.BACKUP_LOCATION_SERVER)){
                setupWaitingForInternetConnectionView();
            } else {
                setupCreatingBackupView();
            }
        });
    }

    private void setupDisplayBackupDescriptionView(){
        imvBackupDone.setVisibility(View.GONE);
        pgbIndicator.setVisibility(View.GONE);
        txvCurrentState.setVisibility(View.GONE);

        if (BackupHelper.getBackupLocation(getActivity()).equals(BackupHelper.BACKUP_LOCATION_SERVER)){
            txvCreateBackupDescription.setText(R.string.dialog_msg_create_server_backup_description);
        } else {
            txvCreateBackupDescription.setText(R.string.dialog_msg_create_local_backup_description);
        }
    }

    private void setupWaitingForInternetConnectionView(){
        imvBackupDone.setVisibility(View.GONE);
        pgbIndicator.setVisibility(View.VISIBLE);
        txvCurrentState.setVisibility(View.VISIBLE);
        btnPositive.setEnabled(false);

        new Thread(() -> {
            while (true){
                if (isConnectedToInternet()){
                    getActivity().runOnUiThread(() -> setupCreatingBackupView());
                    return;
                }
            }
        }).start();
    }

    private void setupCreatingBackupView(){
        btnPositive.setVisibility(View.GONE);
        txvCurrentState.setVisibility(View.VISIBLE);
        btnNegative.setText(getString(R.string.dialog_action_close));
        btnNegative.setEnabled(false);
        txvCurrentState.setText(R.string.label_creating_backup);

        BackupHelper backupHelper = new BackupHelper(getActivity());
        backupHelper.setOnCompleteListener(successfully -> {
            if (successfully){
                getActivity().runOnUiThread(() -> setupBackupCreatedView());
            } else {
                getActivity().runOnUiThread(() -> setupErrorView());
            }
        });

        backupHelper.createBackup();
    }

    private void setupBackupCreatedView(){
        btnNegative.setEnabled(true);
        imvBackupDone.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.GONE);
        txvCurrentState.setText(R.string.label_backup_got_created);
    }

    private void setupErrorView(){
        btnNegative.setEnabled(true);
        imvBackupDone.setImageDrawable(getResources().getDrawable(R.drawable.ic_error));
        imvBackupDone.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.GONE);
        txvCurrentState.setText(R.string.label_something_went_wrong);
    }

    protected boolean isConnectedToInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

}
