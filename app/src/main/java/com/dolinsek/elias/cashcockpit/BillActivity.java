package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private static final int VIBRATION_DURATION = 100;

    public static final String EXTRA_BILL_TO_EDIT = "extraBillToEditIndex";
    public static final String EXTRA_BILL_TO_EDIT_BANK_ACCOUNT = "extraBillBankAccountIndex";

    private static final String EXTRA_SELECTED_PRIMARY_CATEGORY_INDEX = "selectedPrimaryCategoryIndex";
    private static final String EXTRA_SELECTED_SUBCATEGORY_INDEX = "selectedSubcategoryIndex";
    private static final String EXTRA_SELECTED_BILL_TYPE = "selectedBillType";
    private static final String EXTRA_SELECTED_BANK_ACCOUNT_INDEX = "selectedBankAccountIndex";

    private static final int NO_CATEGORY = -1;

    private TextInputLayout tilAmount, tilDescription;
    private TextInputEditText edtAmount, edtDescription;
    private Button btnSelectCategory, btnAddDeleteBill, btnSaveBill;

    private ChipGroup cgBillType, cgBankAccount;
    private Subcategory selectedSubcategory;

    //editMode-Variables
    private boolean editMode;
    private Bill billToEdit;
    private BankAccount bankAccountOfBillToEdit;
    int bankAccountOfBillToEditIndex, billToEditIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        tilAmount = findViewById(R.id.til_bill_amount);
        edtAmount = findViewById(R.id.edt_bill_amount);

        edtAmount.addTextChangedListener(Currency.getActiveCurrency(getApplicationContext()).getCurrencyTextWatcher(edtAmount));

        tilDescription = findViewById(R.id.til_bill_description);
        edtDescription = findViewById(R.id.edt_bill_description);

        btnSelectCategory = findViewById(R.id.btn_bill_select_category);
        btnAddDeleteBill = findViewById(R.id.btn_bill_add_delete_bill);
        btnSaveBill = findViewById(R.id.btn_bill_save_bill);

        cgBillType = findViewById(R.id.cg_auto_pay_bill_types);
        cgBankAccount = findViewById(R.id.cg_bill_bank_account);

        edtAmount.addTextChangedListener(Currency.getActiveCurrency(this).getCurrencyTextWatcher(edtAmount));

        if (getIntent().hasExtra(EXTRA_BILL_TO_EDIT) && getIntent().hasExtra(EXTRA_BILL_TO_EDIT_BANK_ACCOUNT)){
            editMode = true;
            loadEditVariablesFromIntentExtras();
            hideBankAccountSelection();
        }

        setupButtons();
        setupBankAccountSelection();

        if (savedInstanceState != null){
            loadDataFromSavedInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (editMode){
            setupForEditMode();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedSubcategory != null){
            outState.putInt(EXTRA_SELECTED_PRIMARY_CATEGORY_INDEX, Toolkit.getIndexOfPrimaryCategoryInDatabase(selectedSubcategory.getPrimaryCategory()));
            outState.putInt(EXTRA_SELECTED_SUBCATEGORY_INDEX, Toolkit.getIndexOfSubcategoryInPrimaryCategory(selectedSubcategory));

            outState.putInt(EXTRA_SELECTED_BILL_TYPE, getSelectedBillType());
            outState.putInt(EXTRA_SELECTED_BANK_ACCOUNT_INDEX, Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(cgBankAccount));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SELECT_CATEGORY && resultCode == RESULT_OK){
            selectedSubcategory = getSubcategoryFromIntentExtras(data);
            displaySelectedSubcategoryName();
        }
    }

    private void loadDataFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            loadSelectedSubcategoryFromSavedInstanceState(savedInstanceState);
            loadChipSelectionsFromSavedInstanceState(savedInstanceState);
        }
    }

    private void loadChipSelectionsFromSavedInstanceState(Bundle savedInstanceState) {
        ((Chip)cgBankAccount.getChildAt(savedInstanceState.getInt(EXTRA_SELECTED_BANK_ACCOUNT_INDEX))).setChecked(true);
        ((Chip)cgBillType.getChildAt(savedInstanceState.getInt(EXTRA_SELECTED_BILL_TYPE))).setChecked(true);
    }

    private void loadSelectedSubcategoryFromSavedInstanceState(Bundle savedInstanceState) {
        int primaryCategoryIndex = savedInstanceState.getInt(EXTRA_SELECTED_PRIMARY_CATEGORY_INDEX, NO_CATEGORY);
        int selectedSubcategoryIndex = savedInstanceState.getInt(EXTRA_SELECTED_SUBCATEGORY_INDEX, NO_CATEGORY);

        if (primaryCategoryIndex != NO_CATEGORY && selectedSubcategoryIndex != NO_CATEGORY){
            selectedSubcategory = Database.getPrimaryCategories().get(primaryCategoryIndex).getSubcategories().get(selectedSubcategoryIndex);
            btnSelectCategory.setText(selectedSubcategory.getName());
        }
    }

    private void setupBankAccountSelection() {
        Toolkit.ActivityToolkit.addBankAccountChipsToChipGroup(cgBankAccount, this);
        ((Chip)cgBankAccount.getChildAt(0)).setChecked(true);
    }

    private void hideBankAccountSelection(){
        findViewById(R.id.ll_bill_bank_account_selection).setVisibility(View.GONE);
        findViewById(R.id.hsv_bill_bank_account_selection).setVisibility(View.GONE);
    }

    private void setupButtons(){
        btnSelectCategory.setOnClickListener(view -> {
            Intent intent = new Intent(this, SelectCategoryActivity.class);
            if (selectedSubcategory != null){
                putSelectedCategoryIndexesIntoIntent(intent);
            }
            startActivityForResult(intent, RC_SELECT_CATEGORY);
        });

        if (editMode){
            btnAddDeleteBill.setText(R.string.btn_delete);
            btnAddDeleteBill.setOnClickListener(view -> showDeleteBillDialogFragment());
            btnSaveBill.setOnClickListener(view -> saveBillFromInputsIfFilledOut());
        } else {
            btnSaveBill.setVisibility(View.GONE);
            btnAddDeleteBill.setText(R.string.btn_add_bill);
            btnAddDeleteBill.setOnClickListener(view -> addBillFromInputsIfFilledOut());
        }
    }

    private void showDeleteBillDialogFragment(){
        DeleteBillDialogFragment deleteBillDialogFragment = new DeleteBillDialogFragment();
        deleteBillDialogFragment.setOnDialogPositiveClickListener((dialog, which) -> {
            bankAccountOfBillToEdit.getBills().remove(billToEdit);
            Database.save(BillActivity.this);
            finish();
        });
        deleteBillDialogFragment.show(getSupportFragmentManager(), "delete_bill");
    }

    private void setupForEditMode(){
        String formattedAmount = Currency.getActiveCurrency(this).formatAmountToReadableStringWithCurrencySymbol(billToEdit.getAmount());
        edtAmount.setText(formattedAmount);
        edtAmount.setSelection(edtAmount.getText().length());

        edtDescription.setText(billToEdit.getDescription());
        edtDescription.setSelection(edtDescription.getText().length());

        ((Chip)cgBillType.getChildAt(billToEdit.getType())).setChecked(true);
        ((Chip)cgBankAccount.getChildAt(bankAccountOfBillToEditIndex)).setChecked(true);

        btnSelectCategory.setText(billToEdit.getSubcategoryName());
    }

    private void loadEditVariablesFromIntentExtras(){
        bankAccountOfBillToEditIndex = getIntent().getIntExtra(EXTRA_BILL_TO_EDIT_BANK_ACCOUNT, 0);
        billToEditIndex = getIntent().getIntExtra(EXTRA_BILL_TO_EDIT, 0);

        bankAccountOfBillToEdit = Database.getBankAccounts().get(bankAccountOfBillToEditIndex);
        billToEdit = bankAccountOfBillToEdit.getBills().get(billToEditIndex);

        selectedSubcategory = billToEdit.getSubcategory();
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
            BankAccount bankAccount = Database.getBankAccounts().get(Toolkit.ActivityToolkit.getIndexOfSelectedChipInChipGroup(cgBankAccount));
            bankAccount.addBill(bill);

            Database.save(this);
            vibrateDevice();
            clearInputs();
        }
    }

    private void saveBillFromInputsIfFilledOut(){
        if (checkEverythingFilledOutAndDisplayErrorIfNot()){
            billToEdit.setAmount(getEnteredAmountAsLong());
            billToEdit.setDescription(getValidDescription());
            billToEdit.setSubcategory(selectedSubcategory);
            billToEdit.setType(getSelectedBillType());

            Database.save(this);
            finish();
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
            case R.id.chip_auto_pay_bill_type_input: return Bill.TYPE_INPUT;
            default: return Bill.TYPE_OUTPUT;
        }
    }

    private String getValidDescription(){
        String enteredDescription = edtDescription.getText().toString();
        return !enteredDescription.equals("") ? enteredDescription : getString(R.string.label_no_description);
    }

    private void clearInputs(){
        edtAmount.setText("");
        edtDescription.setText("");
    }

    private void vibrateDevice(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(VIBRATION_DURATION);
        }
    }
}
