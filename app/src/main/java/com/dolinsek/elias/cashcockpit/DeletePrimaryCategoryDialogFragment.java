package com.dolinsek.elias.cashcockpit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.dolinsek.elias.cashcockpit.components.AutoPay;

import java.util.ArrayList;

/**
 * DialogFragment for confirming that the user wants to delete a specified primary category including all it's subcategories
 * Created by elias on 27.01.2018.
 */

public class DeletePrimaryCategoryDialogFragment extends DialogFragment {

    private ArrayList<AutoPay> autoPays;

    public void setAutoPaysToDelete(ArrayList<AutoPay> autoPays){
        this.autoPays = autoPays;
    }

    public interface DeletePrimaryCategoryListener{
        public void onDialogPositiveClick();
    }

    private DeletePrimaryCategoryListener mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setPositiveButton(R.string.dialog_action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogPositiveClick();
            }
        }).setNegativeButton(R.string.dialog_action_cancel, null).setMessage(getResources().getString(R.string.dialog_msg_delete_primary_category));

        return alertDialog.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DeletePrimaryCategoryListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
