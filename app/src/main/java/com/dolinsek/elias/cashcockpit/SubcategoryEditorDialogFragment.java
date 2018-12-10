package com.dolinsek.elias.cashcockpit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.CategoriesSorter;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Goal;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;

/**
 * DialogFragment for creating, editing and deleting subcategory
 * Created by elias on 24.01.2018.
 */

public class SubcategoryEditorDialogFragment extends DialogFragment{

    private LinearLayout mLlDeleteInformations, mLlEdtGoalAmountContainer;
    private PrimaryCategory mPrimaryCategory;
    private Subcategory mSubcategory;
    private boolean mEditMode = false;

    private TextInputLayout mTilSubcategoryName, mTilGoalAmount;
    private EditText mEdtSubcategoryName, mEdtGoalAmount;
    private CheckBox mChbGoalEnabled;
    private ImageView mImvFavored;

    private DialogInterface.OnDismissListener mOnDismissListener;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        if(mSubcategory == null){
            throw new NullPointerException("No subcategory set");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflatedView = inflater.inflate(R.layout.dialog_subcategory_editor, null);

        mLlDeleteInformations = inflatedView.findViewById(R.id.ll_item_subcategory_deletion_informations);
        mLlEdtGoalAmountContainer = inflatedView.findViewById(R.id.ll_subcategory_editor_edt_goal_amount_container);
        mTilSubcategoryName = inflatedView.findViewById(R.id.til_item_subcategory_editor_name);
        mTilGoalAmount = inflatedView.findViewById(R.id.til_item_subcategory_editor_goal_amount);

        mEdtSubcategoryName = inflatedView.findViewById(R.id.edt_item_subcategory_editor_name);
        mEdtGoalAmount = inflatedView.findViewById(R.id.edt_item_subcategory_editor_goal_amount);

        mChbGoalEnabled = inflatedView.findViewById(R.id.chb_item_subcategory_editor_goal_enabled);
        mImvFavored = inflatedView.findViewById(R.id.imv_item_subcategory_editor_favored);
        setupImage(mSubcategory.isFavoured());

        setupButtonClickListener();
        setupGoalAmountTxv();
        setupFmlEdtGoalAmountOnClickToEnableGoal();

        if(mEditMode){
            displaySubcategoryDetails();
            setupButtonsForEditMode(builder);
        } else {
            setupViewsForCreateMode(builder);
        }

        builder.setView(inflatedView);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button mBtnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            mBtnPositive.setOnClickListener(view -> {
                removeAllErrorsFromViews();

                boolean nameAlreadyExits = doesNameForSubcategoryAlreadyExist();
                String enteredGoalAmount = mEdtGoalAmount.getText().toString();
                if(mEdtSubcategoryName.getText().toString().trim().equals("")){
                    Toolkit.displayPleaseCheckInputsToast(getContext());
                } else if(nameAlreadyExits) {
                    Toolkit.displayPleaseCheckInputsToast(getContext());
                } else if(mChbGoalEnabled.isChecked() && (enteredGoalAmount.equals("") || enteredGoalAmount.equals("."))){
                    Toolkit.displayPleaseCheckInputsToast(getContext());
                } else {

                    String name = mEdtSubcategoryName.getText().toString();
                    removeAllWhiteSpacesAtBeginning(name);

                    changeSubcategoryName(name);
                    setGoalForSubcategory();

                    if(!mEditMode){
                        addSubcategoryToPrimaryCategory(mPrimaryCategory, mSubcategory);
                    }

                    dialog.dismiss();
                }
            });

            final Button mBtnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 8, 0);
            mBtnNegative.setLayoutParams(layoutParams);

            if(mBtnNegative != null){
                mBtnNegative.setOnClickListener(view -> {
                    mLlDeleteInformations.setVisibility(View.VISIBLE);
                    mBtnNegative.setText(getResources().getString(R.string.dialog_action_confirm_deletion));

                    mBtnNegative.setOnClickListener(view1 -> {
                        deleteAllBillsOfSubcategory(mSubcategory);
                        deleteAllAutoPaysOfSubcategory(mSubcategory);

                        mPrimaryCategory.getSubcategories().remove(mSubcategory);
                        mPrimaryCategory.getGoal().setAmount(mPrimaryCategory.getGoal().getAmount() - mSubcategory.getGoal().getAmount());

                        dialog.dismiss();
                    });
                });
            }
        });

        dialog.setOnDismissListener(mOnDismissListener);

        return dialog;
    }

    private void setupFmlEdtGoalAmountOnClickToEnableGoal(){
        mLlEdtGoalAmountContainer.setOnClickListener(v -> mChbGoalEnabled.setChecked(true));
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null){
            mOnDismissListener.onDismiss(dialog);
        }
    }

    private void setupImage(boolean favored){
        if(favored){
            mImvFavored.setImageResource(R.drawable.ic_favorite);
        } else {
            mImvFavored.setImageResource(R.drawable.ic_not_favorite);
        }
    }

    public void setupForEditMode(PrimaryCategory primaryCategory, Subcategory subcategoryToEdit){
        mSubcategory = subcategoryToEdit;
        mPrimaryCategory = primaryCategory;
        mEditMode = true;
    }

    public void setupForCreateMode(PrimaryCategory primaryCategoryForCreation){
        mSubcategory = new Subcategory("", new Goal(0), primaryCategoryForCreation.getName(), false);
        mPrimaryCategory = primaryCategoryForCreation;
        mEditMode = false;
    }

    private void setupButtonClickListener(){
        mChbGoalEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> mEdtGoalAmount.setEnabled(true));

        mImvFavored.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSubcategory.setFavoured(!mSubcategory.isFavoured());
                setupImage(mSubcategory.isFavoured());
            }
        });
    }

    private void displaySubcategoryDetails(){
        mEdtSubcategoryName.setText(mSubcategory.getName());
        if(mSubcategory.getGoal().getAmount() != 0){
            displayGoalOfSubcategory();
        }
    }

    private void displayGoalOfSubcategory(){
        String formattedGoalAmount = Currency.getActiveCurrency(getContext()).formatAmountToReadableString(mSubcategory.getGoal().getAmount());
        mEdtGoalAmount.setText(formattedGoalAmount);
        mChbGoalEnabled.setChecked(true);
    }

    private void setupButtonsForEditMode(AlertDialog.Builder builder){
        builder.setPositiveButton(getResources().getString(R.string.dialog_action_save), null);
        builder.setNegativeButton(getResources().getString(R.string.dialog_action_delete), null);
        builder.setTitle(getResources().getString(R.string.dialog_title_edit_subcategory));
    }

    private void setupViewsForCreateMode(AlertDialog.Builder builder){
        builder.setPositiveButton(getResources().getString(R.string.dialog_action_create), null);
        builder.setTitle(getResources().getString(R.string.dialog_title_create_subcategory));
    }

    private void setupGoalAmountTxv(){
        mEdtGoalAmount.addTextChangedListener(Currency.getActiveCurrency(getContext()).getCurrencyTextWatcher(mEdtGoalAmount));
        mEdtGoalAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mChbGoalEnabled.setChecked(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void removeAllErrorsFromViews(){
        mEdtSubcategoryName.setError(null);
    }

    private boolean doesNameForSubcategoryAlreadyExist(){
        String enteredName = mEdtSubcategoryName.getText().toString();
        for(int i = 0; i<mPrimaryCategory.getSubcategories().size(); i++){
            Subcategory currentSubcategory = mPrimaryCategory.getSubcategories().get(i);
            if(currentSubcategory.getName().equals(enteredName) && !currentSubcategory.equals(mSubcategory))
                return true;
        }

        return false;
    }

    private void removeAllWhiteSpacesAtBeginning(String value){
        while(true){
            if(value.startsWith(" ")){
                value = value.substring(1, value.length());
            } else {
                break;
            }
        }
    }

    private void addSubcategoryToPrimaryCategory(PrimaryCategory primaryCategory, Subcategory subcategoryToAdd){
        primaryCategory.addSubcategory(subcategoryToAdd);
    }

    private void setGoalForSubcategory(){
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
    }

    private void deleteAllBillsOfSubcategory(Subcategory subcategory){
        for (BankAccount bankAccount:Database.getBankAccounts()){
            ArrayList<Bill> billsToRemove = new ArrayList<>();
            for (Bill bill:bankAccount.getBills()){
                if (bill.getSubcategory().equals(subcategory)){
                    billsToRemove.add(bill);
                }
            }

            bankAccount.getBills().removeAll(billsToRemove);
        }
    }

    private void deleteAllAutoPaysOfSubcategory(Subcategory subcategory){
        for(int i = 0; i<Database.getAutoPays().size(); i++){
            ArrayList<AutoPay> autoPaysToDelete = new ArrayList<>();
            if(Database.getAutoPays().get(i).getBill().getSubcategory().equals(subcategory)){
                autoPaysToDelete.add(Database.getAutoPays().get(i));
            }

            Database.getAutoPays().removeAll(autoPaysToDelete);
        }
    }

    private void changeSubcategoryName(String newName){
        updateSubcategoryNameInComponents(mSubcategory.getName(), newName);
        mSubcategory.setName(newName);
    }

    private void updateSubcategoryNameInComponents(String oldName, String newName){
        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                if (bill.getSubcategoryName().equals(oldName)){
                    bill.setSubcategoryName(newName);
                }
            }
        }

        for (AutoPay autoPay:Database.getAutoPays()){
            if (autoPay.getBill().getSubcategoryName().equals(oldName)){
                autoPay.getBill().setSubcategoryName(newName);
            }
        }
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener){
        this.mOnDismissListener = onDismissListener;
    }
}
