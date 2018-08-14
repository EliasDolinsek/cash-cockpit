package com.dolinsek.elias.cashcockpit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Elias Dolinsek on 14.08.2018 for cash-cockpit.
 */
public class CreateBackupDialogFragment extends DialogFragment{

    private Button btnPositive, btnNegative;
    private ConstraintLayout clRoot;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View inflatedView = layoutInflater.inflate(R.layout.dialog_create_backup_creating, null);

        alertBuilder.setTitle(R.string.dialog_title_create_backup).setMessage(R.string.dialog_msg_backup_on_server_description)
                .setPositiveButton(R.string.dialog_action_create_backup, null)
                .setNegativeButton(R.string.dialog_action_cancel, null)
                .setView(inflatedView);

        clRoot = inflatedView.findViewById(R.id.cl_dialog_create_backup_creating_root);
        clRoot.setVisibility(View.GONE);

        return alertBuilder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog alertDialog = (AlertDialog)getDialog();
        btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnNegative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        btnPositive.setOnClickListener(v -> {
            alertDialog.setMessage(null);
            clRoot.setVisibility(View.VISIBLE);
            refreshDialog(alertDialog);
        });
    }

    private void refreshDialog(AlertDialog alertDialog){
        alertDialog.hide();
        alertDialog.show();
    }
}
