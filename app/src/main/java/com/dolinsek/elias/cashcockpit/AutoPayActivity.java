package com.dolinsek.elias.cashcockpit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;

public class AutoPayActivity extends AppCompatActivity {

    public static final String EXTRA_AUTO_PAY_INDEX = "auto_pay";

    private AmountInputFragment mFgmAmountInput;
    private DescriptionInputFragment mFgmNameInput;
    private BankAccountAndBillSelectionFragment mFgmBankAccountAndBillSelection;
    private SelectCategoryFragment mFgmCategorySelection;

    private Spinner mSpnSelectAutoPayType;

    private AutoPay autoPay;
    private BankAccount bankAccountToAddBill;
    private Subcategory selectedSubcategory;
    private int selectedAutoPayBillType;

    private boolean editModeActive;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_pay);

        mFgmNameInput = (DescriptionInputFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_auto_pay_name_input);
        mFgmAmountInput = (AmountInputFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_auto_pay_amount_input);
        mFgmBankAccountAndBillSelection = (BankAccountAndBillSelectionFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_auto_pay_bank_account_and_bill_type_selection);
        mFgmCategorySelection = (SelectCategoryFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_auto_pay_category_selection);

        mSpnSelectAutoPayType = findViewById(R.id.spn_auto_pay_select_type);
        setupSpinners();

        if(getIntent().hasExtra(EXTRA_AUTO_PAY_INDEX)){
            editModeActive = true;
            int indexOfAutoPayInDatabase = getIntent().getIntExtra(EXTRA_AUTO_PAY_INDEX, 0);
            autoPay = Database.getAutoPays().get(indexOfAutoPayInDatabase);
            mSpnSelectAutoPayType.setSelection(autoPay.getType());

            selectedAutoPayBillType = autoPay.getBill().getType();
            selectedSubcategory = autoPay.getBill().getSubcategory();

            displayAutoPayDetails();
        } else {
            autoPay = new AutoPay();
            autoPay.setBill(new Bill());
        }

        if (editModeActive){
            mFgmCategorySelection.setSelectedSubcategory(autoPay.getBill().getSubcategory());
        }

        mFgmCategorySelection.setOnCategorySelectedListener(new SelectCategoryFragment.OnCategorySelectedListener() {
            @Override
            public void onSubcategorySelected(Subcategory selectedSubcategory) {
                AutoPayActivity.this.selectedSubcategory = selectedSubcategory;
            }
        });
    }

    private void createOrSaveAutoPayIfPossible(){
        if (everythingFilledOutCorrectly()){
            long amount = mFgmAmountInput.getEnteredAmountAsLong();

            autoPay.setName(mFgmNameInput.getEnteredDescriptionAsString());
            autoPay.getBill().setAmount(amount);
            autoPay.setBankAccount(bankAccountToAddBill);
            autoPay.getBill().setDescription(autoPay.getName());
            autoPay.getBill().setSubcategory(selectedSubcategory);
            autoPay.getBill().setType(selectedAutoPayBillType);
            if(!editModeActive){
                autoPay.addPaymentTimestamp();
                Database.getAutoPays().add(autoPay);
            }

            Database.save(getApplicationContext());
            finish();
        } else {
            Toolkit.displayPleaseCheckInputsToast(getApplicationContext());
        }
    }

    private boolean everythingFilledOutCorrectly() {
        String enteredAmount = mFgmAmountInput.getEnteredAmountAsString();
        if(mFgmNameInput.getEnteredDescriptionAsString().trim().equals("")){
            return false;
        } else if(enteredAmount.equals("") || enteredAmount.equals(".")){
            return false;
        } else if(selectedSubcategory == null){
            return false;
        } else {
            return true;
        }
    }

    private void showDeleteAutoPayDialog(){
        DeleteAutoPayDialogFragment deleteAutoPayDialogFragment = new DeleteAutoPayDialogFragment();
        deleteAutoPayDialogFragment.setAutoPay(autoPay);
        deleteAutoPayDialogFragment.show(getSupportFragmentManager(), "delete_auto_pay");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        MenuInflater menuInflater = getMenuInflater();
        if (editModeActive){
            menuInflater.inflate(R.menu.save_delete_menu, menu);
        } else {
            menuInflater.inflate(R.menu.create_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: finish(); return true;
            case R.id.menu_create: createOrSaveAutoPayIfPossible(); return true;
            case R.id.menu_save: createOrSaveAutoPayIfPossible(); return true;
            case R.id.menu_delete: showDeleteAutoPayDialog(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void setupSpinners(){
        final ArrayAdapter<CharSequence> autoPayTypesAdapter = new ArrayAdapter<>(this, R.layout.costum_spinner_layout, getResources().getTextArray(R.array.auto_pay_types_array));
        autoPayTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpnSelectAutoPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                autoPay.setType(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mFgmBankAccountAndBillSelection.setupBillTypeSelectionSpinnerOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                selectedAutoPayBillType = index;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mFgmBankAccountAndBillSelection.setupBankAccountSelectionSpinnerOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bankAccountToAddBill = Database.getBankAccounts().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpnSelectAutoPayType.setSelection(1); //AutoPay type monthly
        mSpnSelectAutoPayType.setAdapter(autoPayTypesAdapter);
        setSpinnerArrowColorToWhite(mSpnSelectAutoPayType);
    }

    private void setSpinnerArrowColorToWhite(Spinner spinner){
        spinner.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
    }


    private void displayAutoPayDetails() {
        mFgmNameInput.getEdtDescription().setText(autoPay.getName());

        String formattedAmountOfAutoPay = Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(autoPay.getBill().getAmount());
        mFgmAmountInput.getEdtAmount().setText(formattedAmountOfAutoPay);

        int indexOfBankAccountInDatabase = getIndexOfBankAccountInDatabase(autoPay.getBankAccount());
        mFgmBankAccountAndBillSelection.setBillTypeSelectionSpinnerSelection(selectedAutoPayBillType);
        mSpnSelectAutoPayType.setSelection(autoPay.getType());
        mFgmBankAccountAndBillSelection.setBankAccountSelectionSpinnerSelection(indexOfBankAccountInDatabase);
    }

    private int getIndexOfBankAccountInDatabase(BankAccount bankAccountToSearch){
        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            if (Database.getBankAccounts().get(i).equals(bankAccountToSearch)){
                return i;
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account in database!");
    }

    public static class DeleteAutoPayDialogFragment extends DialogFragment {

        private AutoPay autoPayToDelete;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.dialog_msg_delete_auto_pay));

            builder.setPositiveButton(getResources().getString(R.string.dialog_action_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(autoPayToDelete != null){
                        Database.getAutoPays().remove(autoPayToDelete);
                        Database.save(getActivity());

                        getActivity().finish();
                    }
                }
            });

            return builder.create();
        }

        public void setAutoPay(AutoPay autoPay){
            this.autoPayToDelete = autoPay;
        }
    }
}
