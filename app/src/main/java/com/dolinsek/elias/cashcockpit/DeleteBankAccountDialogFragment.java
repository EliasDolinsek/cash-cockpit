package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * This gets displayed when the user wants to delete a account
 * Created by elias on 23.01.2018.
 */

public class DeleteBankAccountDialogFragment extends DialogFragment{

    //Listener for a positive click
    public interface DeleteBankAccountDialogListener{
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    private DeleteBankAccountDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(R.string.dialog_action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton(R.string.dialog_action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogPositiveClick(DeleteBankAccountDialogFragment.this);
            }
        }).setMessage(getResources().getString(R.string.dialog_msg_delete_account));

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            //Calls listener
            mListener = (DeleteBankAccountDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
