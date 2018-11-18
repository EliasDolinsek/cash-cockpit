package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

public class BillActivity extends AppCompatActivity {

    private static final int RC_SELECT_CATEGORY = 518;

    private TextInputLayout tilAmount, tilDescription;
    private TextInputEditText edtAmount, edtDescription;
    private Button btnSelectCategory, btnAddBill;

    private ChipGroup cgBillType, cgBankAccount;

    private int selectedBillType, previousSelectedBillTypeChipId;
    private Subcategory selectedSubcategory;
    private BankAccount selectedBankAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        tilAmount = findViewById(R.id.til_bill_amount);
        edtAmount = findViewById(R.id.edt_bill_amount);

        tilDescription = findViewById(R.id.til_bill_description);
        edtDescription = findViewById(R.id.edt_bill_description);

        btnSelectCategory = findViewById(R.id.btn_bill_select_category);
        btnAddBill = findViewById(R.id.btn_bill_add_bill);

        cgBillType = findViewById(R.id.cg_bill_bill_types);
        cgBankAccount = findViewById(R.id.cg_bill_bank_account);

        cgBillType.setOnCheckedChangeListener((chipGroup, i) -> {
            switch (i){
                case 0: selectedBillType = Bill.TYPE_INPUT; break;
                case 1: selectedBillType = Bill.TYPE_OUTPUT; break;
                case 2: selectedBillType = Bill.TYPE_TRANSFER; break;
            }

            previousSelectedBillTypeChipId = i;
        });

        edtAmount.addTextChangedListener(Currency.getActiveCurrency(this).getCurrencyTextWatcher(edtAmount));

        btnSelectCategory.setOnClickListener(view -> {
            Intent intent = new Intent(this, SelectCategoryActivity.class);
            if (selectedSubcategory != null){
                putSelectedCategoryIndexesIntoIntent(intent);
            }
            startActivityForResult(intent, RC_SELECT_CATEGORY);
        });

        btnAddBill.setOnClickListener(view -> {
            addBillFromInputsIfFilledOut();
        });

        addChipsForBankAccounts();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SELECT_CATEGORY && resultCode == RESULT_OK){
            selectedSubcategory = getSubcategoryFromIntentExtras(data);
            displaySelectedSubcategoryName();
        }
    }

    private void addChipsForBankAccounts(){
        for (BankAccount bankAccount:Database.getBankAccounts()){
            addChipForBankAccount(bankAccount, bankAccount.isPrimaryAccount());
        }
    }

    private void addChipForBankAccount(BankAccount bankAccount, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(bankAccount.getName());
        chip.setCheckable(true);
        chip.setClickable(true);
        chip.setCheckedIconVisible(false);

        cgBankAccount.addView(chip);
        if (checked){
            cgBankAccount.check(chip.getId());
        }
    }

    private Subcategory getSubcategoryFromIntentExtras(Intent intent){
        int primaryCategoryIndex = intent.getIntExtra(SelectCategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, 0);
        int subcategoryIndex = intent.getIntExtra(SelectCategoryActivity.EXTRA_SUBCATEGORY_INDEX, 0);

        return Database.getPrimaryCategories().get(primaryCategoryIndex).getSubcategories().get(subcategoryIndex);
    }

    private void displaySelectedSubcategoryName(){
        btnSelectCategory.setText(selectedSubcategory.getName());
    }

    private void putSelectedCategoryIndexesIntoIntent(Intent intent){
        intent.putExtra(SelectCategoryActivity.SELECTED_PRIMARY_CATEGORY_INDEX, Toolkit.getIndexOfPrimaryCategoryInDatabase(selectedSubcategory.getPrimaryCategory()));
        intent.putExtra(SelectCategoryActivity.SELECTED_SUBCATEGORY_INDEX, Toolkit.getIndexOfSubcategoryInPrimaryCategory(selectedSubcategory));
    }

    private void addBillFromInputsIfFilledOut(){
        if (checkEverythingFilledOutAndDisplayErrorIfNot()){
            Bill bill = new Bill(getEnteredAmountAsLong(), getValidDescription(), selectedSubcategory.getName(), selectedSubcategory.getPrimaryCategory().getName(), getSelectedBillType(), false, System.currentTimeMillis());
            getSelectedBankAccount().addBill(bill);
            Database.save(this);
            clearInputs();
        }
    }

    private boolean checkEverythingFilledOutAndDisplayErrorIfNot(){
        boolean everythingFilledOutCorrectly = true;
        if (!isValidAmountEntered()){
            everythingFilledOutCorrectly = false;
            tilAmount.setErrorEnabled(true);
            tilAmount.setError(getString(R.string.label_enter_valid_amount));
        } else {
            tilAmount.setErrorEnabled(false);
            tilAmount.setError(null);
        }

        if (selectedSubcategory == null) {
            Toast.makeText(this, getString(R.string.toast_no_category_selected), Toast.LENGTH_SHORT).show();
            everythingFilledOutCorrectly = false;
        }

        return everythingFilledOutCorrectly;
    }

    private boolean isValidAmountEntered(){
        return getEnteredAmountAsLong() != 0;
    }

    private long getEnteredAmountAsLong(){
        try {
            String enteredAmount = edtAmount.getText().toString();
            return (long) (Double.valueOf(enteredAmount) * 100);
        } catch (Exception e){
            return 0;
        }
    }

    private int getSelectedBillType(){
        switch (cgBillType.getCheckedChipId()){
            case R.id.chip_bill_input: return Bill.TYPE_INPUT;
            case R.id.chip_bill_transfer: return Bill.TYPE_TRANSFER;
            default: return Bill.TYPE_OUTPUT;
        }
    }

    private BankAccount getSelectedBankAccount(){
        for (int i = 0; i<cgBankAccount.getChildCount(); i++){
            if (((Chip)cgBankAccount.getChildAt(i)).isChecked()){
                System.out.println(i);
                return Database.getBankAccounts().get(i);
            }
        }

        return Database.getBankAccounts().get(0);
    }

    private String getValidDescription(){
        String enteredDescription = edtDescription.getText().toString();
        return !enteredDescription.equals("") ? enteredDescription : getString(R.string.label_no_description);
    }

    private void clearInputs(){
        edtAmount.setText("");
        edtDescription.setText("");
    }
}
