package com.dolinsek.elias.cashcockpit;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BalanceChange;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;

public class BankAccountActivity extends AppCompatActivity implements DeleteBankAccountDialogFragment.DeleteBankAccountDialogListener{

    public static final String EXTRA_BANK_ACCOUNT_INDEX = "bankAccountIndex";

    private RecyclerView mRvBills;
    private CheckBox mChbPrimaryAccount;
    private BankAccount bankAccount = null;

    private TextInputLayout tilAmount, tilName;
    private TextInputEditText edtAmount, edtName;
    private Button btnCreateSave, btnCancelDelete;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_account);

        edtAmount = findViewById(R.id.edt_bank_account_amount);
        edtName = findViewById(R.id.edt_bank_account_name);

        tilAmount = findViewById(R.id.til_bank_account_amount);
        tilName = findViewById(R.id.til_bank_account_name);

        mChbPrimaryAccount = findViewById(R.id.chb_bank_account_primary_account);

        mRvBills = findViewById(R.id.rv_bank_account_bills);
        mRvBills.setLayoutManager(new LinearLayoutManager(this));

        btnCreateSave = findViewById(R.id.btn_bank_account_create_save);
        btnCancelDelete = findViewById(R.id.btn_bank_account_cancel_delete);

        if(isEditModeRequired()){
            setupForEditMode();
        }

        setupButtonTexts();

        if(bankAccount != null){
            displayBankAccountDetails();
        }

        if(Database.getBankAccounts().size() == 0){
            //Forces user to create a primary bank account
            mChbPrimaryAccount.setChecked(true);
            mChbPrimaryAccount.setEnabled(false);
        }

        btnCreateSave.setOnClickListener(v -> {
            createOrSaveBankAccountIfPossible();
        });

        btnCancelDelete.setOnClickListener(v -> {
            if (isEditModeRequired()){
                showDeleteBankAccountDialogFragment();
            } else {
                finish();
            }
        });
    }

    private void createOrSaveBankAccountIfPossible(){
        removeErrors();
        String enteredBalance = edtAmount.getText().toString();

        if(getEnteredNameAsString().trim().equals("")){
            tilName.setError(getString(R.string.label_enter_valid_name));
        } else if(enteredBalance.equals("") || enteredBalance.equals(".")){
            tilAmount.setError(getString(R.string.label_enter_valid_amount));
        } else if(doesEnteredNameAlreadyExist()){
            tilName.setError(getString(R.string.label_enter_name_already_exists));
        } else {
            if(bankAccount == null){
                setBankAccountToPrimaryAccountIfRequired();
                createAndSaveBankAccount();
            } else {
                saveEnteredChanges();
                Database.save(getApplicationContext());
            }

            finish();
        }
    }

    private void removeErrors(){
        tilName.setError(null);
        tilAmount.setError(null);
    }

    private void setBankAccountToPrimaryAccountIfRequired(){
        if (mChbPrimaryAccount.isChecked()){
            setPrimaryAccountInAllBankAccountsInDatabseToFalse();
        }
    }
    private void showDeleteBankAccountDialogFragment(){
        DeleteBankAccountDialogFragment deleteBankAccountDialogFragment = new DeleteBankAccountDialogFragment();
        deleteBankAccountDialogFragment.show(getSupportFragmentManager(), "delete_bank_account");
    }

    private boolean isEditModeRequired(){
        return getIntent().hasExtra(EXTRA_BANK_ACCOUNT_INDEX);
    }

    private void createAndSaveBankAccount(){
        long balance = getEnteredAmountAsLong();
        String enteredName = getEnteredNameAsString();
        boolean isAccountPrimaryAccount = isAccountSetToBePrimaryAccount();

        BankAccount newBankAccount = new BankAccount(enteredName, balance, isAccountPrimaryAccount);
        Database.getBankAccounts().add(newBankAccount);
        Database.save(getApplicationContext());
    }

    private void saveEnteredChanges(){
        long balance = getEnteredAmountAsLong();
        String enteredName = getEnteredNameAsString();
        boolean isAccountPrimaryAccount = isAccountSetToBePrimaryAccount();

        bankAccount.setName(enteredName);
        bankAccount.setBalance(balance);
        bankAccount.getBalanceChanges().add(new BalanceChange(System.currentTimeMillis(), balance));

        if (isAccountPrimaryAccount){
            setPrimaryAccountInAllBankAccountsInDatabseToFalse();
            bankAccount.setPrimaryAccount(true);
        }
    }

    private boolean isAccountSetToBePrimaryAccount(){
        return mChbPrimaryAccount.isChecked();
    }

    private void setPrimaryAccountInAllBankAccountsInDatabseToFalse(){
        for(int i = 0; i<Database.getBankAccounts().size(); i++){
            Database.getBankAccounts().get(i).setPrimaryAccount(false);
        }
    }
    /**
     * Returns a ArrayList of all AutoPays what belong to this account
     * @return associated AutoPays
     */
    private ArrayList<AutoPay> getAutoPays(){
        ArrayList<AutoPay> autoPays = new ArrayList<>();
        for(int i = 0; i<Database.getAutoPays().size(); i++){
            if(Database.getAutoPays().get(i).getBankAccount().equals(bankAccount))
                autoPays.add(Database.getAutoPays().get(i));
        }

        return autoPays;
    }

    /**
     * Gets called when the user clicks "delete" in DeleteBankAccountDialogFragment
     * @param dialog what is currently shown
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        ArrayList<AutoPay> autoPays = getAutoPays();
        for(int i = 0; i<getAutoPays().size(); i++){
            Database.getAutoPays().remove(autoPays.get(i));
        }

        Database.getBankAccounts().remove(bankAccount);
        Database.save(getApplicationContext());

        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_got_deleted_successfully), Toast.LENGTH_LONG).show();
        finish();
    }

    private void setupForEditMode(){
        int bankAccountIndexInDatabase = getIntent().getIntExtra(EXTRA_BANK_ACCOUNT_INDEX, 0);
        bankAccount = Database.getBankAccounts().get(bankAccountIndexInDatabase);

        HistoryItemAdapter historyItemAdapter = HistoryItemAdapter.getBankAccountHistoryItemAdapter(bankAccount);
        mRvBills.setAdapter(historyItemAdapter);
    }

    private void displayBankAccountDetails(){
        edtName.setText(bankAccount.getName());
       edtAmount.setText(Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(bankAccount.getBalance()));

        mChbPrimaryAccount.setEnabled(!bankAccount.isPrimaryAccount());
        mChbPrimaryAccount.setChecked(bankAccount.isPrimaryAccount());
    }

    private boolean doesEnteredNameAlreadyExist(){
        String enteredNameForBankAccount = getEnteredNameAsString();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            if (bankAccount.getName().equals(enteredNameForBankAccount) && !bankAccount.equals(this.bankAccount)){
                return true;
            }
        }

        return false;
    }

    private void setupButtonTexts(){
        if (isEditModeRequired()){
            btnCreateSave.setText(getString(R.string.btn_save));
            btnCancelDelete.setText(getString(R.string.btn_delete));
        } else {
            btnCreateSave.setText(getString(R.string.btn_create));
            btnCancelDelete.setText(getString(R.string.btn_cancel));
        }
    }

    private long getEnteredAmountAsLong(){
        return Toolkit.convertStringToLongAmount(edtAmount.getText().toString());
    }

    private String getEnteredNameAsString(){
        return edtName.getText().toString();
    }
}
