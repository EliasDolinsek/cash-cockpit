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
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolbox;

import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

public class DeleteDataDialogFragment extends DialogFragment {

    private Button btnPositive, btnNegative;
    private TextView txvDescription, txvCurrentStatus;
    private ProgressBar pgbIndicator;
    private ImageView imvDone;
    private boolean finished, cancelWaitingForInternetConnection;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setTitle(R.string.dialog_title_delete_data)
                .setPositiveButton(R.string.dialog_action_server, null)
                .setNegativeButton(R.string.dialog_action_local, null);

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

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (finished){
            Toolbox.restartCashCockpit(getContext());
        }
    }

    private void setupForDescriptionView(){
        txvDescription.setVisibility(View.VISIBLE);
        txvCurrentStatus.setVisibility(View.GONE);
        pgbIndicator.setVisibility(View.GONE);
        imvDone.setVisibility(View.GONE);

        txvDescription.setText(R.string.label_delete_data_description);
        setupButtonsForDescriptionView();
        finished = false;
    }

    private void setupButtonsForDescriptionView(){
        BackupHelper.OnCompleteListener onCompleteListener = successfully -> {
            finished = true;
            if (successfully){
                getActivity().runOnUiThread(this::setupForDeletedView);
            } else {
                getActivity().runOnUiThread(this::setupForErrorView);
            }
        };

        btnPositive.setOnClickListener(view -> {
            getActivity().runOnUiThread(this::setupForWaitingInternetConnectionView);
            cancelWaitingForInternetConnection = false;
            new Thread(() -> {
                waitForInternetConnection();
                if (!cancelWaitingForInternetConnection){
                    getActivity().runOnUiThread(this::setupForDeletingDataView);
                    removeDataInDatabase();
                    deleteServerData(onCompleteListener);
                }
            }).start();
        });

        btnNegative.setOnClickListener(view -> {
            setupForDeletingDataView();
            deleteLocalData();
            setupForDeletedView();
            finished = true;
        });

    }

    private void setupForWaitingInternetConnectionView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.VISIBLE);
        imvDone.setVisibility(View.GONE);

        txvCurrentStatus.setText(R.string.label_waiting_for_internet_connection);
        setupButtonsForWaitingForInternetConnectionView();
    }

    private void setupButtonsForWaitingForInternetConnectionView(){
        btnPositive.setVisibility(View.VISIBLE);
        btnNegative.setVisibility(View.GONE);

        btnPositive.setText(R.string.dialog_action_cancel);
        btnPositive.setOnClickListener(view -> {
            cancelWaitingForInternetConnection = true;
            dismiss();
        });
    }

    private void setupForDeletingDataView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.VISIBLE);
        imvDone.setVisibility(View.GONE);

        txvCurrentStatus.setText(R.string.label_deleting_data);
        setupButtonsForDeletingDataView();
    }

    private void setupButtonsForDeletingDataView(){
        btnPositive.setVisibility(View.VISIBLE);
        btnNegative.setVisibility(View.GONE);

        btnPositive.setText(R.string.dialog_action_restart);
        btnPositive.setEnabled(false);
    }

    private void setupForDeletedView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.GONE);
        imvDone.setVisibility(View.VISIBLE);

        txvCurrentStatus.setText(R.string.label_data_got_deleted);
        setupButtonsForFinishedView();
    }

    private void setupForErrorView(){
        txvDescription.setVisibility(View.GONE);
        txvCurrentStatus.setVisibility(View.VISIBLE);
        pgbIndicator.setVisibility(View.GONE);
        imvDone.setVisibility(View.VISIBLE);

        txvDescription.setText(R.string.label_something_went_wrong);
        imvDone.setImageDrawable(getResources().getDrawable(R.drawable.ic_error));

        setupButtonsForFinishedView();
    }

    private void setupButtonsForFinishedView(){
        btnPositive.setEnabled(true);
        btnPositive.setText(R.string.dialog_action_restart);
        btnPositive.setOnClickListener(view -> dismiss());
    }

    private void waitForInternetConnection(){
        while (true){
            if (Toolbox.connectedToInternet(getContext()) || cancelWaitingForInternetConnection){
                return;
            }
        }
    }

    private void deleteServerData(BackupHelper.OnCompleteListener onCompleteListener){
        BackupHelper backupHelper = new BackupHelper(getContext());
        backupHelper.setOnCompleteListener(onCompleteListener);

        removeDataInDatabase();
        backupHelper.createServerBackup();
    }

    private void deleteLocalData(){
        removeDataInDatabase();
        Database.save(getContext());

        BackupHelper backupHelper = new BackupHelper(getContext());
        backupHelper.setOnCompleteListener(successfully -> { });
        backupHelper.createLocalBackup();
    }

    private void removeDataInDatabase(){
        Database.setBankAccounts(new ArrayList<>());
        Database.setPrimaryCategories(new ArrayList<>());
        Database.setAutoPays(new ArrayList<>());
    }
}
