package com.dolinsek.elias.cashcockpit;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitFragment extends Fragment {

    private static final int NO_VALUE = -1;

    private static final String PRIMARY_CATEGORY = "primary_category";
    private static final String SUBCATEGORY = "subcategory";
    private static final String ACCOUNT = "account";
    private static final String TYPE = "type";

    private TextView mTxvBillCreationDate;
    private Button mBtnAdd;
    private CockpitChartFragment mFgmCockpitChart;
    private CardView mCvCockpitChartContainer;
    private LinearLayout mLlContentContainer;

    private AmountInputFragment mFgmBillAmountInput;
    private DescriptionInputFragment mFgmDescriptionInput;
    private BankAccountAndBillSelectionFragment mFgmBillTypeAndBankAccountSelection;
    private SelectCategoryFragment mFgmSelectCategory;

    private BankAccount bankAccountOfBill;
    private Subcategory selectedSubcategory;
    private int currentlySelectedBillType = Bill.TYPE_OUTPUT;

    private boolean editModeActive;
    private Bill bill;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_cockpit, container, false);

        mBtnAdd = inflatedView.findViewById(R.id.btn_cockpit_add_bill);
        mFgmCockpitChart = (CockpitChartFragment) getChildFragmentManager().findFragmentById(R.id.fgm_cockpit_chart);
        mFgmBillAmountInput = (AmountInputFragment) getChildFragmentManager().findFragmentById(R.id.fgm_cockpit_amount_input);
        mFgmDescriptionInput = (DescriptionInputFragment) getChildFragmentManager().findFragmentById(R.id.fgm_cockpit_description_input);
        mFgmBillTypeAndBankAccountSelection = (BankAccountAndBillSelectionFragment) getChildFragmentManager().findFragmentById(R.id.fgm_cockpit_bank_account_and_bill_type_selection);
        mFgmSelectCategory = (SelectCategoryFragment) getChildFragmentManager().findFragmentById(R.id.fgm_cockpit_select_category);

        mCvCockpitChartContainer = inflatedView.findViewById(R.id.cv_cockpit_chart_container);
        mLlContentContainer = inflatedView.findViewById(R.id.ll_cockpit_content_container);
        mTxvBillCreationDate = inflatedView.findViewById(R.id.txv_cockpit_bill_creation_date);

        mFgmBillTypeAndBankAccountSelection.setupBillTypeSelectionSpinnerOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentlySelectedBillType = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mFgmBillTypeAndBankAccountSelection.setupBankAccountSelectionSpinnerOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                bankAccountOfBill = Database.getBankAccounts().get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mFgmSelectCategory.setOnCategorySelectedListener(new SelectCategoryFragment.OnCategorySelectedListener() {
            @Override
            public void onSubcategorySelected(Subcategory subcategory) {
                selectedSubcategory = subcategory;
            }
        });

        if(editModeActive){
            setupForEditMode();
        }

        if(savedInstanceState != null){
            restoreFromSavedInstanceState(savedInstanceState);
        }

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(everythingFilledCorrectly()) {
                    long amount = mFgmBillAmountInput.getEnteredAmountAsLong();
                    String description = mFgmDescriptionInput.getEnteredDescriptionAsString();
                    bankAccountOfBill.addBill(new Bill(amount, description, currentlySelectedBillType, false, selectedSubcategory));

                    try {
                        Database.save(getContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getContext(), getResources().getString(R.string.toast_bill_added), Toast.LENGTH_SHORT).show();
                    clearFieldsFromUserInputs();
                    hideKeyboard();
                    refreshCockpitChart();
                } else {
                    Toolkit.displayPleaseCheckInputsToast(getContext());
                }
            }
        });

        if (getActivity() instanceof BillEditorActivity){
            mFgmSelectCategory.setSelectedSubcategory(selectedSubcategory);

            ((BillEditorActivity) getActivity()).setOnSaveOrDeleteActionRequired(new BillEditorActivity.OnSaveOrDeleteActionRequired() {
                @Override
                public void onSaveRequested() {
                    if(everythingFilledCorrectly()){

                        if(hasBillTypeChanged(bill.getType(), currentlySelectedBillType)){
                            updateBankAccountBalanceAfterBillTypeChanged();
                        }

                        updateBillWithUserInputs();

                        Database.save(getContext());
                        getActivity().finish();
                    } else {
                        Toolkit.displayPleaseCheckInputsToast(getContext());
                    }
                }

                @Override
                public void onDeleteRequested() {
                    DeleteBillDialogFragment deleteBillDialogFragment = new DeleteBillDialogFragment();
                    deleteBillDialogFragment.setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getAssociatedBankAccountOfBill(bill).getBills().remove(bill);
                            Database.save(getContext());
                            getActivity().finish();
                        }
                    });
                    deleteBillDialogFragment.show(getFragmentManager(), "delete_bill");
                }
            });
        }

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mFgmBillTypeAndBankAccountSelection.setBillTypeSelectionSpinnerSelection(currentlySelectedBillType);
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(selectedSubcategory != null){
            int primaryCategoryIndexInDatabase = getIndexOfPrimaryCategoryInDatabase(selectedSubcategory.getPrimaryCategory());
            int subcategoryIndexInDatabase = getIndexOfSubcategoryInPrimaryCategory(selectedSubcategory);

            outState.putInt(PRIMARY_CATEGORY, primaryCategoryIndexInDatabase);
            outState.putInt(SUBCATEGORY, subcategoryIndexInDatabase);
        } else if(bankAccountOfBill != null){
            int bankAccountIndexInDatabase = getIndexOfBankAccountInDatabase(bankAccountOfBill);
            outState.putInt(ACCOUNT, bankAccountIndexInDatabase);
        }

        outState.putInt(TYPE, currentlySelectedBillType);
    }

    private void hideChart(){
        mCvCockpitChartContainer.setVisibility(View.GONE);
    }

    public void setBillToEdit(Bill billToEdit){
        this.bill = billToEdit;
        BankAccount bankAccount = getAssociatedBankAccountOfBill(billToEdit);
        selectedSubcategory = bill.getSubcategory();
        this.bankAccountOfBill = bankAccount;

        editModeActive = true;
        currentlySelectedBillType = billToEdit.getType();
    }

    private boolean everythingFilledCorrectly(){
        String enteredBillAmount = mFgmBillAmountInput.getEnteredAmountAsString();
        if(enteredBillAmount.equals("") || enteredBillAmount.equals(".")){
            return false;
        } else if(selectedSubcategory == null){
            return false;
        } else {
            return true;
        }
    }

    public static class DeleteBillDialogFragment extends DialogFragment {

        private DialogInterface.OnClickListener onClickListenerListener;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
            alertBuilder.setMessage(R.string.dialog_msg_delete_bill);
            alertBuilder.setPositiveButton(R.string.dialog_action_delete, onClickListenerListener);

            return alertBuilder.create();
        }

        public void setOnPositiveClickListener(DialogInterface.OnClickListener onPositiveClickListener){
            this.onClickListenerListener = onPositiveClickListener;
        }
    }

    private void setupForEditMode(){
        String amountWithoutCurrencySymbol = Currency.getActiveCurrency(getContext()).formatAmountToReadableString(bill.getAmount());
        mFgmBillAmountInput.getEdtAmount().setText(amountWithoutCurrencySymbol);
        mFgmDescriptionInput.getEdtDescription().setText(bill.getDescription());


        setBackgroundColorToWhite();
        displayBillCreationDate();
        hideChart();

        mBtnAdd.setVisibility(View.GONE);
        mFgmBillTypeAndBankAccountSelection.getSpnBankAccountSelection().setEnabled(false);
    }

    private void setBackgroundColorToWhite(){
        mLlContentContainer.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    private void displayBillCreationDate(){
        String dateOfCreationDate = DateFormat.format("EEE dd.MM.yy kk.mm", bill.getCreationDate()).toString();
        mTxvBillCreationDate.setText(getString(R.string.label_bill_creation_date, dateOfCreationDate));
    }

    private void refreshCockpitChart(){
        mFgmCockpitChart.refreshData();
    }

    private void restoreFromSavedInstanceState(Bundle savedInstanceState){
        try {
            currentlySelectedBillType = savedInstanceState.getInt(TYPE, Bill.TYPE_OUTPUT);
            mFgmBillTypeAndBankAccountSelection.setBillTypeSelectionSpinnerSelection(currentlySelectedBillType);

            if(savedInstanceState.getInt(PRIMARY_CATEGORY, NO_VALUE) != NO_VALUE && savedInstanceState.getInt(SUBCATEGORY, NO_VALUE) != NO_VALUE){
                selectedSubcategory = Database.getPrimaryCategories().get(savedInstanceState.getInt(PRIMARY_CATEGORY, 0)).getSubcategories().get(savedInstanceState.getInt(SUBCATEGORY, 0));
                mFgmSelectCategory.setSelectedSubcategory(selectedSubcategory);
            }

            if (savedInstanceState.getInt(ACCOUNT, NO_VALUE) != NO_VALUE){
                int bankAccountIndex = savedInstanceState.getInt(ACCOUNT);

                mFgmBillTypeAndBankAccountSelection.setBankAccountSelectionSpinnerSelection(bankAccountIndex);
                bankAccountOfBill = Database.getBankAccounts().get(bankAccountIndex);
            } else {
                mFgmBillTypeAndBankAccountSelection.setBankAccountSelectionSpinnerSelection(0);
                bankAccountOfBill = Database.getBankAccounts().get(0);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getIndexOfSubcategoryInPrimaryCategory(Subcategory subcategory){
        for (int i = 0; i<subcategory.getPrimaryCategory().getSubcategories().size(); i++){
            if (subcategory.equals(subcategory.getPrimaryCategory().getSubcategories().get(i))){
                return i;
            }
        }

        throw new Resources.NotFoundException("Couldn't find subcategory in database!");
    }

    private int getIndexOfPrimaryCategoryInDatabase(PrimaryCategory primaryCategory){
        ArrayList<PrimaryCategory> primaryCategoriesInDatabase = Database.getPrimaryCategories();
        for (int i = 0; i<primaryCategoriesInDatabase.size(); i++){
            System.out.println(primaryCategory + " " + primaryCategoriesInDatabase.get(i));
            if (primaryCategory.equals(primaryCategoriesInDatabase.get(i))){
                return i;
            }
        }

        throw new Resources.NotFoundException("Couldn't find primary category in database!");
    }

    private int getIndexOfBankAccountInDatabase(BankAccount bankAccount){
        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            if (Database.getBankAccounts().get(i).equals(bankAccount)){
                return i;
            }
        }

        throw new Resources.NotFoundException("Couldn't find bank account in database!");
    }

    private void updateBillWithUserInputs(){
        bill.setDescription(mFgmDescriptionInput.getEnteredDescriptionAsString());
        bill.setSubcategory(selectedSubcategory);
        bill.setAmount(mFgmBillAmountInput.getEnteredAmountAsLong());
        bill.setType(currentlySelectedBillType);
    }

    private boolean hasBillTypeChanged(int oldType, int newType){
        if (oldType != newType)
            return true;
        else
            return false;
    }

    private void updateBankAccountBalanceAfterBillTypeChanged(){
        int oldBillType = bill.getType(), newBillType = currentlySelectedBillType;
        long newAmount = mFgmBillAmountInput.getEnteredAmountAsLong();

        if(oldBillType == Bill.TYPE_INPUT && newBillType != Bill.TYPE_INPUT){
            bankAccountOfBill.setBalance(bankAccountOfBill.getBalance() - newAmount);
        } else if((oldBillType == Bill.TYPE_OUTPUT || oldBillType == Bill.TYPE_TRANSFER) && newBillType == Bill.TYPE_INPUT){
            bankAccountOfBill.setBalance(bankAccountOfBill.getBalance() + newAmount);
        }
    }

    private void clearFieldsFromUserInputs(){
        mFgmBillAmountInput.getEdtAmount().setText("");
        mFgmDescriptionInput.getEdtDescription().setText("");
        mFgmBillAmountInput.getEdtAmount().requestFocus();
    }

    private BankAccount getAssociatedBankAccountOfBill(Bill bill){
        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill currentBill:bankAccount.getBills()){
                if (currentBill.equals(bill)){
                    return bankAccount;
                }
            }
        }

        throw new Resources.NotFoundException("Couldn't find associated bank account of bill!");
    }
}