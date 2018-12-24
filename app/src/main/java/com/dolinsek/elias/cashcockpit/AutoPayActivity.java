package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

public class AutoPayActivity extends AppCompatActivity {

    public static final String EXTRA_AUTO_PAY_INDEX = "auto_pay";
    private static final int RC_SELECT_CATEGORY = 396;

    private TextInputLayout tilAmount, tilDescription;
    private TextInputEditText edtAmount, edtDescription;
    private ChipGroup cgBankAccounts, cgBillType, cgAutoPayType;

    private AutoPay autoPay;
    private Button btnSelectCategory;
    private Subcategory selectedSubcategory;
    private int selectedBillType = Bill.TYPE_OUTPUT, selectedAutoPayType = AutoPay.TYPE_MONTHLY;

    private Button btnCancelDelete, btnCreateSave;

    private boolean editModeActive;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_pay);

        tilAmount = findViewById(R.id.til_auto_pay_amount);
        tilDescription = findViewById(R.id.til_auto_pay_description);

        edtAmount = findViewById(R.id.edt_auto_pay_amount);
        edtDescription = findViewById(R.id.edt_auto_pay_description);

        btnSelectCategory = findViewById(R.id.btn_auto_pay_select_category);

        cgBankAccounts = findViewById(R.id.cg_auto_pay_bank_accounts);
        cgBillType = findViewById(R.id.cg_auto_pay_bill_types);
        cgAutoPayType = findViewById(R.id.cg_auto_pay_types);

        btnCancelDelete = findViewById(R.id.btn_auto_pay_cancel_delete);
        btnCreateSave = findViewById(R.id.btn_auto_pay_create_save);

        edtAmount.addTextChangedListener(Currency.getActiveCurrency(getApplicationContext()).getCurrencyTextWatcher(edtAmount));
        setupChipGroups();

        if(getIntent().hasExtra(EXTRA_AUTO_PAY_INDEX)){
            editModeActive = true;
            int indexOfAutoPayInDatabase = getIntent().getIntExtra(EXTRA_AUTO_PAY_INDEX, 0);

            autoPay = Database.getAutoPays().get(indexOfAutoPayInDatabase);
            selectedSubcategory = autoPay.getBill().getSubcategory();

            displayAutoPayDetails();
        } else {
            autoPay = new AutoPay();
            autoPay.setBill(new Bill());
        }

        setupButtons();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SELECT_CATEGORY && resultCode == RESULT_OK){
            int primaryCategoryIndex = data.getIntExtra(SelectCategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, 0);
            int subcategoryIndex = data.getIntExtra(SelectCategoryActivity.EXTRA_SUBCATEGORY_INDEX, 0);
            selectedSubcategory = Database.getPrimaryCategories().get(primaryCategoryIndex).getSubcategories().get(subcategoryIndex);

            btnSelectCategory.setText(selectedSubcategory.getName());
        }
    }

    private void setupButtons(){
        btnCreateSave.setOnClickListener(v -> createOrSafeAutoPayIfPossible());

        if (editModeActive){
            btnCreateSave.setText(getString(R.string.btn_save));
            btnCancelDelete.setText(getString(R.string.btn_delete));
            btnCancelDelete.setOnClickListener(v -> showDeleteAutoPayDialog());
        } else {
            btnCreateSave.setText(getString(R.string.btn_create));
            btnCancelDelete.setText(getString(R.string.btn_cancel));
            btnCancelDelete.setOnClickListener(v -> finish());
        }

        btnSelectCategory.setOnClickListener(v -> {
            Intent intent = new Intent(this, SelectCategoryActivity.class);
            if (selectedSubcategory != null){
                intent.putExtra(SelectCategoryActivity.SELECTED_SUBCATEGORY_INDEX, Toolkit.getIndexOfSubcategoryInPrimaryCategory(selectedSubcategory));
                intent.putExtra(SelectCategoryActivity.SELECTED_PRIMARY_CATEGORY_INDEX, Toolkit.getIndexOfPrimaryCategoryInDatabase(selectedSubcategory.getPrimaryCategory()));
            }

            startActivityForResult(intent, RC_SELECT_CATEGORY);
        });
    }

    private void setupChipGroups(){
        Toolkit.ActivityToolkit.addBankAccountChipsToChipGroup(cgBankAccounts, this);
        cgBillType.setOnCheckedChangeListener((chipGroup, i) -> {
            switch (i){
                case R.id.chip_auto_pay_bill_type_input: selectedBillType = Bill.TYPE_INPUT; break;
                case R.id.chip_auto_pay_bill_type_output: selectedBillType = Bill.TYPE_OUTPUT; break;
            }
        });

        cgAutoPayType.setOnCheckedChangeListener((chipGroup, i) -> {
            switch (i){
                case R.id.chip_auto_pay_type_weekly: selectedAutoPayType = AutoPay.TYPE_WEEKLY; break;
                case R.id.chip_auto_pay_type_monthly: selectedAutoPayType = AutoPay.TYPE_MONTHLY; break;
                case R.id.chip_auto_pay_type_yearly: selectedAutoPayType = AutoPay.TYPE_YEARLY; break;
            }
        });
    }

    private void createOrSafeAutoPayIfPossible(){
        if (everythingFilledOutCorrectlyAndDisplayErrorIfNot()){
            long amount = Toolkit.convertStringToLongAmount(edtAmount.getText().toString());
            BankAccount selectedBankAccount= Toolkit.ActivityToolkit.getSelectedBankAccountFromChipGroup(cgBankAccounts);

            autoPay.setName(edtDescription.getText().toString());
            autoPay.setType(selectedAutoPayType);
            autoPay.setBankAccount(selectedBankAccount);
            autoPay.getBill().setAmount(amount);
            autoPay.getBill().setDescription(autoPay.getName());
            autoPay.getBill().setSubcategory(selectedSubcategory);
            autoPay.getBill().setType(selectedBillType);
            autoPay.addPaymentTimestamp();

            if(!editModeActive){
                Database.getAutoPays().add(autoPay);
            }

            Database.save(getApplicationContext());
            finish();
        }
    }

    private boolean everythingFilledOutCorrectlyAndDisplayErrorIfNot() {
        removeErrors();
        String enteredAmount = edtAmount.getText().toString();

        if (enteredAmount.equals("") || enteredAmount.equals(".")){
            tilAmount.setError(getString(R.string.label_enter_valid_amount));
            return false;
        } else if(edtDescription.getText().toString().trim().equals("")){
             tilDescription.setError(getString(R.string.label_enter_valid_description));
             return false;
         } else if(selectedSubcategory == null){
            Toast.makeText(this, getString(R.string.toast_no_category_selected), Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void removeErrors(){
        tilDescription.setErrorEnabled(false);
        tilAmount.setErrorEnabled(false);

        tilDescription.setError(null);
        tilAmount.setError(null);

        tilDescription.setErrorEnabled(true);
        tilAmount.setErrorEnabled(true);
    }

    private void showDeleteAutoPayDialog(){
        DeleteAutoPayDialogFragment deleteAutoPayDialogFragment = new DeleteAutoPayDialogFragment();
        deleteAutoPayDialogFragment.setAutoPay(autoPay);
        deleteAutoPayDialogFragment.show(getSupportFragmentManager(), "delete_auto_pay");
    }


    private void displayAutoPayDetails() {
        String formattedAmountOfAutoPay = Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(autoPay.getBill().getAmount());
        edtAmount.setText(formattedAmountOfAutoPay);
        edtDescription.setText(autoPay.getName());
        btnSelectCategory.setText(selectedSubcategory.getName());

        int indexOfBankAccountInDatabase = getIndexOfBankAccountInDatabase(autoPay.getBankAccount());
        ((Chip)cgBankAccounts.getChildAt(indexOfBankAccountInDatabase)).setChecked(true);

        switch (autoPay.getType()){
            case AutoPay.TYPE_WEEKLY: ((Chip)cgAutoPayType.getChildAt(0)).setChecked(true); break;
            case AutoPay.TYPE_MONTHLY: ((Chip)cgAutoPayType.getChildAt(1)).setChecked(true); break;
            case AutoPay.TYPE_YEARLY: ((Chip)cgAutoPayType.getChildAt(2)).setChecked(true); break;
        }
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
            builder.setMessage(getResources().getString(R.string.dialog_msg_confirm_auto_pay_deletion));

            builder.setPositiveButton(getResources().getString(R.string.dialog_action_delete), (dialogInterface, i) -> {
                if(autoPayToDelete != null){
                    Database.getAutoPays().remove(autoPayToDelete);
                    Database.save(getActivity());

                    getActivity().finish();
                }
            });

            return builder.create();
        }

        public void setAutoPay(AutoPay autoPay){
            this.autoPayToDelete = autoPay;
        }
    }
}
