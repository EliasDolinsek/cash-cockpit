package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.dolinsek.elias.cashcockpit.components.Database;

/**
 * Created by elias on 04.02.2018.
 */

public class SelectBankAccountDialogFragment extends DialogFragment {

    private int selectedBankAccount = 0;
    private DialogInterface.OnClickListener onClickListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());

        //Gets names of bank account
        String[] bankAccountNames = new String[Database.getBankAccounts().size()];
        for(int i = 0; i<Database.getBankAccounts().size(); i++){
            bankAccountNames[i] = Database.getBankAccounts().get(i).getName();
        }

        if(bankAccountNames.length == 0){
            alertBuilder.setMessage(getResources().getString(R.string.label_no_bank_accounts));
            alertBuilder.setPositiveButton(getResources().getString(R.string.dialog_action_create), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //Start BankAccountActivity
                    Intent intent = new Intent(getContext(), BankAccountActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            alertBuilder.setItems(bankAccountNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selectedBankAccount = i;
                    if(onClickListener != null)
                        onClickListener.onClick(dialogInterface, i);
                }
            });
        }

        return alertBuilder.create();
    }

    public void setOnSelectListener(DialogInterface.OnClickListener onClickListener){
        this.onClickListener = onClickListener;
    }
}
