package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

/**
 * Created by Elias Dolinsek on 21.10.2018 for cash-cockpit.
 */
public class DeleteBillDialogFragment extends DialogFragment{

    private DialogInterface.OnClickListener onPositiveClickListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.dialog_title_delete_bill)
                .setMessage(R.string.dialog_msg_delete_bill)
                .setPositiveButton(R.string.dialog_action_delete, onPositiveClickListener);

        return builder.create();
    }

    public void setOnDialogPositiveClickListener(DialogInterface.OnClickListener onClickListener) {
        this.onPositiveClickListener = onClickListener;
    }
}
