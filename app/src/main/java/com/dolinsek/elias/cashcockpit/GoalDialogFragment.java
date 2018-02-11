package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.design.widget.TextInputLayout;
import  android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Goal;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;

/**
 * DialogFragment for creating, editing and deleting goals
 * Created by elias on 23.01.2018.
 */

public class GoalDialogFragment extends DialogFragment{

    private TextInputLayout mTextInputLayout;
    private EditText mEdtGoalAmount;

    private PrimaryCategory primaryCategory;

    public void setPrimaryAccount(PrimaryCategory primaryCategory){
        this.primaryCategory = primaryCategory;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_goal, null);

        builder.setView(inflatedView);

        mTextInputLayout = (TextInputLayout) inflatedView.findViewById(R.id.til_dialog_goal);
        mEdtGoalAmount = (EditText) inflatedView.findViewById(R.id.edt_dialog_goal_amount);

        if(primaryCategory.getGoal().getAmount() != 0){
            mEdtGoalAmount.setText(String.valueOf(primaryCategory.getGoal().getAmount() / 100));

            builder.setTitle(getResources().getString(R.string.dialog_title_edit_goal));
            builder.setPositiveButton(getResources().getString(R.string.dialog_action_save), null);
            builder.setNegativeButton(getResources().getString(R.string.dialog_action_delete_goal), null);
        } else {
            builder.setTitle(getResources().getString(R.string.dialog_title_create_goal));
            builder.setPositiveButton(getResources().getString(R.string.dialog_action_create), null);
        }

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button mBtnPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                Button mBtnNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                long amount = 0;
                for(int i = 0; i<primaryCategory.getSubcategories().size(); i++)
                    amount += primaryCategory.getSubcategories().get(i).getGoal().getAmount();
                final long subcatgegoriesGoalAmount = amount;

                mBtnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mEdtGoalAmount.getText().toString().trim().equals("") || mEdtGoalAmount.getText().toString().equals("0"))
                            mTextInputLayout.setError(getResources().getString(R.string.label_enter_valid_amount));
                        else if(subcatgegoriesGoalAmount > Long.valueOf(mEdtGoalAmount.getText().toString()) * 100){
                            mTextInputLayout.setError(getResources().getString(R.string.label_more_goal_amount));
                        } else {
                            primaryCategory.setGoal(new Goal(Long.valueOf(mEdtGoalAmount.getText().toString()) * 100));

                            //Save data
                            Database.save(getContext());

                            dialog.dismiss();
                        }
                    }
                });

                if(mBtnNegative != null){
                    mBtnNegative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            primaryCategory.setGoal(new Goal(0));

                            //Save sata
                            Database.save(getContext());

                            dialog.dismiss();
                        }
                    });

                    if(subcatgegoriesGoalAmount != 0)
                        mBtnNegative.setEnabled(false);
                }
            }
        });

        return dialog;
    }
}
