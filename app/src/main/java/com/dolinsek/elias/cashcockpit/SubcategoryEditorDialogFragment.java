package com.dolinsek.elias.cashcockpit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Goal;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

import java.util.ArrayList;

/**
 * DialogFragment for creating, editing and deleting subcategory
 * Created by elias on 24.01.2018.
 */

public class SubcategoryEditorDialogFragment extends DialogFragment{

    private LinearLayout mLlDeleteInformations;
    private PrimaryCategory mPrimaryCategory;
    private Subcategory mSubcategory;
    private boolean mEditMode = false;

    private TextInputLayout mTilSubcategoryName, mTilGoalAmount;
    private EditText mEdtSubcategoryName, mEdtGoalAmount;
    private CheckBox mChbGoalEnabled;
    private ImageView mImvFavored;

    //Interface what gets called when the dialog dismisses
    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if(mSubcategory == null)
            throw new IllegalStateException("No subcategory set (setSubcategory)");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_subcategory_editor, null);

        mLlDeleteInformations = (LinearLayout) inflatedView.findViewById(R.id.ll_item_subcategory_deletion_informations);
        mTilSubcategoryName = (TextInputLayout) inflatedView.findViewById(R.id.til_item_subcategory_editor_name);
        mTilGoalAmount = (TextInputLayout) inflatedView.findViewById(R.id.til_item_subcategory_editor_goal_amount);

        mEdtSubcategoryName = (EditText) inflatedView.findViewById(R.id.edt_item_subcategory_editor_name);
        mEdtGoalAmount = (EditText) inflatedView.findViewById(R.id.edt_item_subcategory_editor_goal_amount);

        mChbGoalEnabled = (CheckBox) inflatedView.findViewById(R.id.chb_item_subcategory_editor_goal_enabled);
        mImvFavored = (ImageView) inflatedView.findViewById(R.id.imv_item_subcategory_editor_favored);
        setupImage(mSubcategory.isFavoured());

        //Enable/Disable goal
        mChbGoalEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mEdtGoalAmount.setEnabled(checked);
            }
        });

        //Changes favored state on click
        mImvFavored.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSubcategory.setFavoured(!mSubcategory.isFavoured());
                setupImage(mSubcategory.isFavoured());
            }
        });

        //If edit mode is active it displays details
        if(mEditMode){
            mEdtSubcategoryName.setText(mSubcategory.getName());
            if(mSubcategory.getGoal().getAmount() != 0){
                mEdtGoalAmount.setText(Currency.getActiveCurrency(getContext()).formatAmountToReadableString(mSubcategory.getGoal().getAmount()));
                mChbGoalEnabled.setChecked(true);
            }


        }

        //Sets View
        builder.setView(inflatedView);

        //Setups buttons depending if the edit mode is active
        if(mEditMode){
            builder.setPositiveButton(getResources().getString(R.string.dialog_action_save), null);
            builder.setNegativeButton(getResources().getString(R.string.dialog_action_delete), null);
            builder.setTitle(getResources().getString(R.string.dialog_title_edit_subcategory));
        } else {
            builder.setPositiveButton(getResources().getString(R.string.dialog_action_create), null);
            builder.setTitle(getResources().getString(R.string.dialog_title_create_subcategory));
        }

        mEdtGoalAmount.addTextChangedListener(Currency.getActiveCurrency(getContext()).getCurrencyTextWatcher(mEdtGoalAmount));

        //Sets up button clicks
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button mBtnPositive = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mBtnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Remove error
                        mEdtSubcategoryName.setError(null);

                        //Checks if name already exists
                        boolean nameAlreadyExits = false;
                        if(!mEditMode){
                            for(int i = 0; i<mPrimaryCategory.getSubcategories().size(); i++){
                                if(mPrimaryCategory.getSubcategories().get(i).getName().equals(mEdtSubcategoryName.getText().toString()))
                                    nameAlreadyExits = true;
                            }
                        }

                        if(mEdtSubcategoryName.getText().toString().trim().equals("")){
                            mTilSubcategoryName.setError(getResources().getString(R.string.label_enter_category_name));
                        } else if(nameAlreadyExits) {
                            mTilSubcategoryName.setError(getResources().getString(R.string.label_category_name_already_exists));
                        } else if(mChbGoalEnabled.isChecked() && mEdtGoalAmount.getText().toString().equals("")){
                            mTilGoalAmount.setError(getResources().getString(R.string.label_enter_euros));
                        } else {

                            //If the name starts with a space it removes it so that the category-sorter works properly
                            String name = mEdtSubcategoryName.getText().toString();
                            while(true){
                                if(name.startsWith(" "))
                                    name = name.substring(1, name.length());
                                else
                                    break;
                            }

                            //Saves name
                            mSubcategory.setName(name);

                            //Saves goal-status
                            if(mChbGoalEnabled.isChecked()){
                                long goalBefore = mSubcategory.getGoal().getAmount();
                                if (mSubcategory.getGoal().getAmount() == 0){
                                    mSubcategory.setGoal(new Goal((long) (Double.valueOf(mEdtGoalAmount.getText().toString()) * 100)));
                                } else {
                                    mSubcategory.getGoal().setAmount((long) (Double.valueOf(mEdtGoalAmount.getText().toString()) * 100));
                                }
                                mPrimaryCategory.getGoal().setAmount(mPrimaryCategory.getGoal().getAmount() + mSubcategory.getGoal().getAmount() - goalBefore);
                            } else {
                                mPrimaryCategory.getGoal().setAmount(mPrimaryCategory.getGoal().getAmount() - mSubcategory.getGoal().getAmount());
                                mSubcategory.setGoal(new Goal(0));
                            }

                            //Adds a new Subcategory if it isn't in edit mode
                            if(!mEditMode)
                                mPrimaryCategory.addSubcategory(mSubcategory);

                            dialog.dismiss();
                        }
                    }
                });

                final Button mBtnNegative = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                if(mBtnNegative != null){
                    mBtnNegative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mLlDeleteInformations.setVisibility(View.VISIBLE);
                            mBtnNegative.setText(getResources().getString(R.string.dialog_action_confirm_deletion));

                            mBtnNegative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    //Deletes Bills
                                    for (BankAccount bankAccount:Database.getBankAccounts()){
                                        for (Bill bill:bankAccount.getBills()){
                                            ArrayList<Bill> billsToRemove = new ArrayList<>();
                                            if (bill.getSubcategory().equals(mSubcategory)){
                                                billsToRemove.add(bill);
                                            }

                                            bankAccount.getBills().removeAll(billsToRemove);
                                        }
                                    }


                                    //Deletes AutoPays
                                    for(int i = 0; i<Database.getAutoPays().size(); i++){
                                        if(Database.getAutoPays().get(i).getBill().getSubcategory().equals(mSubcategory)){
                                            Database.getAutoPays().remove(Database.getAutoPays().get(i));
                                        }
                                    }

                                    //Deletes subcategory
                                    mPrimaryCategory.getSubcategories().remove(mSubcategory);

                                    //Sets goal
                                    mPrimaryCategory.getGoal().setAmount(mPrimaryCategory.getGoal().getAmount() - mSubcategory.getGoal().getAmount());
                                    dialog.dismiss();
                                }
                            });
                        }
                    });
                }
            }
        });

        dialog.setOnDismissListener(mOnDismissListener);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        //Calls interface in activity if it implements the right inteface
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    /**
     * Sets up the icons what display if the subcategory is favored or not
     * @param favored if the subcategory is favored or not
     */
    private void setupImage(boolean favored){
        if(favored)
            mImvFavored.setImageResource(R.drawable.ic_favorite);
        else
            mImvFavored.setImageResource(R.drawable.ic_not_favorite);
    }

    /**
     * Sets primary category including subcategory to edit and enables edit mode
     * @param primaryCategory primary category
     * @param subcategoryIndex index in array for subcategories in primary category
     */
    public void setPrimaryCategory(PrimaryCategory primaryCategory, int subcategoryIndex){
        mSubcategory = primaryCategory.getSubcategories().get(subcategoryIndex);
        mPrimaryCategory = primaryCategory;

        mEditMode = true;
    }

    /**
     * Sets primary category and edit mode keeps disabled
     * @param primaryCategory primary category
     */
    public void setPrimaryCategory(PrimaryCategory primaryCategory){
        mSubcategory = new Subcategory("", new Goal(0), primaryCategory, false);
        mPrimaryCategory = primaryCategory;
    }
}
