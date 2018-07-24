package com.dolinsek.elias.cashcockpit;

import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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
    private AmountInputFragment mFgmAmountInput;
    private DescriptionInputFragment mFgmNameInput;
    private BankAccount bankAccount = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_account);

        mFgmNameInput = (DescriptionInputFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_bank_account_name_input);
        mFgmAmountInput = (AmountInputFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_bank_account_amount_input);

        mChbPrimaryAccount = (CheckBox) findViewById(R.id.chb_bank_account_primary_account);

        mRvBills = (RecyclerView) findViewById(R.id.rv_bank_account_bills);
        mRvBills.setLayoutManager(new LinearLayoutManager(this));

        if(isEditModeRequired()){
            setupForEditMode();
        }

        if(bankAccount != null){
            displayBankAccountDetails();
        }

        if(Database.getBankAccounts().size() == 0){
            //Forces user to create a primary bank account
            mChbPrimaryAccount.setChecked(true);
            mChbPrimaryAccount.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        MenuInflater menuInflater = getMenuInflater();
        if (isEditModeRequired() && !bankAccount.isPrimaryAccount()){
            menuInflater.inflate(R.menu.save_delete_menu, menu);
        } else if (isEditModeRequired() && bankAccount.isPrimaryAccount()){
            menuInflater.inflate(R.menu.save_menu, menu);
        } else {
            menuInflater.inflate(R.menu.create_menu, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home: finish(); return true;
            case R.id.menu_create: createOrSaveBankAccountIfPossible(); return true;
            case R.id.menu_save: createOrSaveBankAccountIfPossible(); return true;
            case R.id.menu_delete: showDeleteBankAccountDialogFragment(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void createOrSaveBankAccountIfPossible(){
        String enteredBalance = mFgmAmountInput.getEnteredAmountAsString();
        if(mFgmNameInput.getEnteredDescriptionAsString().trim().equals("")){
            Toolkit.displayPleaseCheckInputsToast(getApplicationContext());
        } else if(enteredBalance.equals("") || enteredBalance.equals(".")){
            Toolkit.displayPleaseCheckInputsToast(getApplicationContext());
        } else if(doesEnteredNameAlreadyExist()){
            Toast.makeText(BankAccountActivity.this, getString(R.string.label_bank_account_already_exits), Toast.LENGTH_SHORT).show();
        } else {
            if(bankAccount == null){
                if(mChbPrimaryAccount.isChecked()){
                    setPrimaryAccountInAllBankAccountsInDatabseToFalse();
                }

                createAndSaveBankAccount();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_created), Toast.LENGTH_LONG).show();
                finish();
            } else {
                saveEnteredChanges();

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_changed), Toast.LENGTH_LONG).show();
                Database.save(getApplicationContext());
                finish();
            }
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
        long balance = mFgmAmountInput.getEnteredAmountAsLong();
        String enteredName = mFgmNameInput.getEnteredDescriptionAsString();
        boolean isAccountPrimaryAccount = isAccountSetToBePrimaryAccount();

        BankAccount newBankAccount = new BankAccount(enteredName, balance, isAccountPrimaryAccount);
        Database.getBankAccounts().add(newBankAccount);
        Database.save(getApplicationContext());
    }

    private void saveEnteredChanges(){
        long balance = mFgmAmountInput.getEnteredAmountAsLong();
        String enteredName = mFgmNameInput.getEnteredDescriptionAsString();
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

        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_deleted), Toast.LENGTH_LONG).show();
        finish();
    }

    private void setupForEditMode(){
        int bankAccountIndexInDatabase = getIntent().getIntExtra(EXTRA_BANK_ACCOUNT_INDEX, 0);
        bankAccount = Database.getBankAccounts().get(bankAccountIndexInDatabase);

        HistoryItemAdapter historyItemAdapter = HistoryItemAdapter.getBankAccountHistoryItemAdapter(bankAccount);
        mRvBills.setAdapter(historyItemAdapter);
    }

    private void displayBankAccountDetails(){
        mFgmNameInput.getEdtDescription().setText(bankAccount.getName());
        mFgmAmountInput.getEdtAmount().setText(Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(bankAccount.getBalance()));

        mChbPrimaryAccount.setEnabled(!bankAccount.isPrimaryAccount());
        mChbPrimaryAccount.setChecked(bankAccount.isPrimaryAccount());
    }

    private boolean doesEnteredNameAlreadyExist(){
        String enteredNameForBankAccount = mFgmNameInput.getEnteredDescriptionAsString();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            if (bankAccount.getName().equals(enteredNameForBankAccount) && !bankAccount.equals(this.bankAccount)){
                return true;
            }
        }

        return false;
    }
}
