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

/**
 * Created by Elias Dolinsek on 14.08.2018 for cash-cockpit.
 */
public class CreateBackupDialogFragment extends DialogFragment{

    private AlertDialog alertDialog;
    private Button btnPositive, btnNegative;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(R.string.dialog_title_create_backup).setMessage(R.string.dialog_msg_backup_on_server_description)
                .setPositiveButton(R.string.dialog_action_create_backup, null)
                .setNegativeButton(R.string.dialog_action_cancel, null);


        alertDialog = alertBuilder.create();
        return alertDialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnNegative = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        btnPositive.setOnClickListener(v -> {
            showCreatingBackupView();
            btnNegative.setVisibility(View.GONE);
        });
    }

    private void showCreatingBackupView(){
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View inflatedView = layoutInflater.inflate(R.layout.dialog_create_backup_creating, null);
        showView(inflatedView);
    }

    private void showView(View view){
        getDialog().dismiss();
        alertDialog.setView(view);
        getDialog().create();
    }
}
