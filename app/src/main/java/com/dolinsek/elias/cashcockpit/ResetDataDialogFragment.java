package com.dolinsek.elias.cashcockpit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BackupHelper;
import com.dolinsek.elias.cashcockpit.components.Toolbox;

public class ResetDataDialogFragment extends DialogFragment {

    private TextView txvDescription, txvCurrentStatus;
    private ProgressBar pgbIndicator;
    private ImageView imvDone;
    private Button btnPositive, btnNegative;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setTitle(R.string.dialog_title_reset_data);

        alertBuilder.setPositiveButton(R.string.dialog_action_reset, null)
                    .setNegativeButton(R.string.dialog_action_cancel, null);

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View inflatedView = layoutInflater.inflate(R.layout.dialog_backup_manager, null);

        txvDescription = inflatedView.findViewById(R.id.txv_dialog_backup_manager_description);
        txvCurrentStatus = inflatedView.findViewById(R.id.txv_dialog_backup_manager_current_status);
        pgbIndicator = inflatedView.findViewById(R.id.pgb_dialog_backup_manager_indicator);
        imvDone = inflatedView.findViewById(R.id.imv_dialog_backup_manager_done);

        alertBuilder.setView(inflatedView);
        return alertBuilder.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog alertDialog = (AlertDialog)getDialog();
        btnPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        setupForDescriptionView();
    }

    private void setupForDescriptionView(){
        txvDescription.setVisibility(View.VISIBLE);
        txvCurrentStatus.setVisibility(View.GONE);
        pgbIndicator.setVisibility(View.GONE);
        imvDone.setVisibility(View.GONE);
        setupButtonsForDescriptionView();

        if (BackupHelper.getBackupLocation(getContext()).equals(BackupHelper.BACKUP_LOCATION_SERVER)){
            txvDescription.setText("Bla Bla Bla (Server)");
        } else {
            txvDescription.setText("Bla Bla Bla (Local)");
        }
    }

    private void setupForWaitingForInternetConnectionView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.VISIBLE);
        imvDone.setVisibility(View.GONE);
        setupButtonsForWaitingForInternetConnectionView();

        txvCurrentStatus.setText(R.string.label_waiting_for_internet_connection);
    }

    private void setupForResettingView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.VISIBLE);
        imvDone.setVisibility(View.GONE);
        setupButtonsForResettingView();

        txvCurrentStatus.setText(R.string.label_resetting_data);
    }

    private void setupForResetView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.GONE);
        imvDone.setVisibility(View.VISIBLE);
        setupButtonsForResetView();

        getDialog().setOnDismissListener(dialogInterface -> Toolbox.restartCashCockpit(getContext()));
        txvCurrentStatus.setText(R.string.label_data_got_reset);
    }

    private void setupForErrorView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.GONE);
        imvDone.setVisibility(View.VISIBLE);
        setupButtonsForResetView();

        imvDone.setImageDrawable(getResources().getDrawable(R.drawable.ic_error));
        txvCurrentStatus.setText(R.string.label_something_went_wrong);
    }

    private void setupButtonsForDescriptionView(){
        BackupHelper backupHelper = new BackupHelper(getActivity());
        backupHelper.setOnCompleteListener(successfully -> {
            if (successfully){
                getActivity().runOnUiThread(this::setupForResetView);
            } else {
                getActivity().runOnUiThread(this::setupForErrorView);
            }
        });

        btnPositive.setOnClickListener(view -> {
            if (BackupHelper.getBackupLocation(getContext()).equals(BackupHelper.BACKUP_LOCATION_SERVER)){
                setupForWaitingForInternetConnectionView();
                new Thread(() -> {
                    waitForInternetConnectionOrDialogClose();
                    if (getDialog() != null && getDialog().isShowing()){
                        getActivity().runOnUiThread(() -> setupForResettingView());
                        backupHelper.overrideDataWithBackup();
                    }
                }).start();
            } else {
                setupForResettingView();
                backupHelper.overrideDataWithBackup();
            }
        });

        btnNegative.setOnClickListener(view -> {
            getDialog().dismiss();
        });
    }

    private void setupButtonsForWaitingForInternetConnectionView(){
        btnPositive.setVisibility(View.GONE);
        btnNegative.setOnClickListener(view -> dismiss());
    }

    private void setupButtonsForResettingView(){
        btnPositive.setVisibility(View.GONE);
        btnNegative.setEnabled(false);
    }

    private void setupButtonsForResetView(){
        btnPositive.setVisibility(View.GONE);
        btnNegative.setEnabled(true);
        btnNegative.setText(R.string.dialog_action_restart);
    }

    private void waitForInternetConnectionOrDialogClose(){
        while (true){
            if (getDialog() == null ||!getDialog().isShowing() || Toolbox.connectedToInternet(getContext())){
                return;
            }
        }
    }
}
