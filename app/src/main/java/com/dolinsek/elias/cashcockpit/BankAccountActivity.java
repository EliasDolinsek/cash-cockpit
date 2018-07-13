package com.dolinsek.elias.cashcockpit;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
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
    private EditText mEdtAccountName, mEdtAccountAmount;
    private CheckBox mChbPrimaryAccount;
    private Button mBtnCreate, mBtnDelete;
    private TextView mTxvActiveCurrencyShortcut;

    private BankAccount bankAccount = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_account);

        mEdtAccountName = (EditText) findViewById(R.id.edt_bank_account_name);
        mEdtAccountAmount = (EditText) findViewById(R.id.edt_bank_account_amount);
        mTxvActiveCurrencyShortcut = findViewById(R.id.txv_bank_account_active_currency_shortcut);

        mChbPrimaryAccount = (CheckBox) findViewById(R.id.chb_bank_account_primary_account);
        mBtnCreate = (Button) findViewById(R.id.btn_bank_account_create);
        mBtnDelete = (Button) findViewById(R.id.btn_bank_account_delete);

        mRvBills = (RecyclerView) findViewById(R.id.rv_bank_account_bills);
        mRvBills.setLayoutManager(new LinearLayoutManager(this));

        displayActiveCurrencyShortcut();
        if(getIntent().hasExtra(EXTRA_BANK_ACCOUNT_INDEX)){
            setupForEditMode();
        } else{
            mBtnDelete.setVisibility(View.GONE);
        }

        if(bankAccount != null){
            displayBankAccountDetails();
        }

        if(Database.getBankAccounts().size() == 0){
            //Forces user to create a primary bank account
            mChbPrimaryAccount.setChecked(true);
            mChbPrimaryAccount.setEnabled(false);
        }

        mBtnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (isEverythingFilledOutCorrectly()){
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
                } else  {
                    Toolkit.displayPleaseCheckInputsToast(getApplicationContext());
                }

            }

        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DeleteBankAccountDialogFragment deleteBankAccountDialogFragment = new DeleteBankAccountDialogFragment();
                deleteBankAccountDialogFragment.show(getSupportFragmentManager(), "delete_bank_account");
            }

        });

        mEdtAccountAmount.addTextChangedListener(Currency.getActiveCurrency(getApplicationContext()).getCurrencyTextWatcher(mEdtAccountAmount));

    }

    private void createAndSaveBankAccount(){
        long balance = getEnteredBalance();
        String enteredName = getEnteredBankAccountName();
        boolean isAccountPrimaryAccount = isAccountSetToBePrimaryAccount();

        BankAccount newBankAccount = new BankAccount(enteredName, balance, isAccountPrimaryAccount);
        Database.getBankAccounts().add(newBankAccount);
        Database.save(getApplicationContext());
    }

    private void saveEnteredChanges(){
        long balance = getEnteredBalance();
        String enteredName = getEnteredBankAccountName();
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

    private String getEnteredBankAccountName(){
        return mEdtAccountName.getText().toString();
    }

    private long getEnteredBalance(){
        return formatDisplayedToUsableAmount(mEdtAccountAmount.getText().toString());
    }

    private void setPrimaryAccountInAllBankAccountsInDatabseToFalse(){
        for(int i = 0; i<Database.getBankAccounts().size(); i++){
            Database.getBankAccounts().get(i).setPrimaryAccount(false);
        }
    }

    private boolean isEverythingFilledOutCorrectly(){
        String enteredBalance = mEdtAccountAmount.getText().toString();
        if(mEdtAccountName.getText().toString().trim().equals("")){
            return false;
        } else if(enteredBalance.equals("") || enteredBalance.equals(".")){
            return false;
        } else if(doesEnteredNameAlreadyExist() && bankAccount == null){
            return false;
        } else {
            return true;
        }
    }

    private void displayActiveCurrencyShortcut(){
        String activeCurrencyShortcut = Currency.getActiveCurrency(getApplicationContext()).getCurrencyShortcut();
        mTxvActiveCurrencyShortcut.setText(activeCurrencyShortcut);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
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

        mBtnDelete.setEnabled(!bankAccount.isPrimaryAccount());

        HistoryItemAdapter historyItemAdapter = HistoryItemAdapter.getBankAccountHistoryItemAdapter(bankAccount);
        mRvBills.setAdapter(historyItemAdapter);
    }

    private void displayBankAccountDetails(){
        mEdtAccountName.setText(bankAccount.getName());
        mEdtAccountAmount.setText(Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(bankAccount.getBalance()));

        mChbPrimaryAccount.setEnabled(!bankAccount.isPrimaryAccount());
        mChbPrimaryAccount.setChecked(bankAccount.isPrimaryAccount());

        mBtnCreate.setText(getResources().getString(R.string.btn_save));
    }

    private boolean doesEnteredNameAlreadyExist(){
        String enteredNameForBankAccount = mEdtAccountName.getText().toString();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            if (bankAccount.getName().equals(enteredNameForBankAccount)){
                return true;
            }
        }

        return false;
    }

    private long formatDisplayedToUsableAmount(String displayedAmount){
        return  (long) (Double.valueOf(displayedAmount) * 100);
    }
}
