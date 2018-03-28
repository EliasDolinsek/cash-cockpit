package com.dolinsek.elias.cashcockpit;

import android.support.v4.app.DialogFragment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;

public class BankAccountActivity extends AppCompatActivity implements DeleteBankAccountDialogFragment.DeleteBankAccountDialogListener{

    public static final String EXTRA_BANK_ACCOUNT_INDEX = "bankAccountIndex";

    private TextInputLayout mTilAccountName, mTilAccountAmount;
    private RecyclerView mRvBills;
    private EditText mEdtAccountName, mEdtAccountAmount;
    private Switch mSwPrimaryAccount;
    private Button mBtnCreate, mBtnDelete;

    private BankAccount bankAccount = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_account);

        mTilAccountName = (TextInputLayout) findViewById(R.id.til_bank_account_name);
        mTilAccountAmount = (TextInputLayout) findViewById(R.id.til_bank_account_amount);

        mEdtAccountName = (EditText) findViewById(R.id.edt_bank_account_name);
        mEdtAccountAmount = (EditText) findViewById(R.id.edt_bank_account_amount);

        mSwPrimaryAccount = (Switch) findViewById(R.id.sw_bank_account_priamry_account);
        mBtnCreate = (Button) findViewById(R.id.btn_bank_account_create);
        mBtnDelete = (Button) findViewById(R.id.btn_bank_account_delete);

        mRvBills = (RecyclerView) findViewById(R.id.rv_bank_account_bills);
        mRvBills.setLayoutManager(new LinearLayoutManager(this));

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
            mSwPrimaryAccount.setChecked(true);
            mSwPrimaryAccount.setEnabled(false);
        }

        mBtnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean doesEnteredNameForAccountAlreadyExist = doesEnteredNameAlreadyExist();
                hideErrors();

                if(mEdtAccountName.getText().toString().trim().equals("")){
                    mTilAccountName.setError(getResources().getString(R.string.label_enter_bank_account_name));
                } else if(mEdtAccountAmount.getText().toString().equals("")){
                    mTilAccountAmount.setError(getResources().getString(R.string.label_enter_euros));;
                } else if(doesEnteredNameForAccountAlreadyExist && bankAccount == null){
                    mTilAccountName.setError(getResources().getString(R.string.label_bank_account_already_exits));
                } else {
                    long balance = formatDisplayedToUsableAmount(mEdtAccountAmount.getText().toString());
                    //TODO continue here to refactor
                    //When it's not in edit mode it creates a new bank account and saves it
                    if(bankAccount == null){

                        //Sets this account as primary account it the user wants it
                        if(mSwPrimaryAccount.isChecked()){
                            for(int i = 0; i<Database.getBankAccounts().size(); i++){
                                Database.getBankAccounts().get(i).setPrimaryAccount(false);
                            }
                        }

                        //Create and save it
                        Database.getBankAccounts().add(new BankAccount(mEdtAccountName.getText().toString(), balance, mSwPrimaryAccount.isChecked()));
                        Database.save(getApplicationContext());

                        //Displays that everything went ok
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_created), Toast.LENGTH_LONG).show();

                        //Go back to MainActivity
                        finish();
                    } else {

                        //Saves bank account changes
                        bankAccount.setName(mEdtAccountName.getText().toString());
                        bankAccount.setBalance(balance);

                        //Sets this account as primary account it the user wants it
                        if(mSwPrimaryAccount.isChecked()){
                            for(int i = 0; i<Database.getBankAccounts().size(); i++){
                                Database.getBankAccounts().get(i).setPrimaryAccount(false);
                            }

                            bankAccount.setPrimaryAccount(true);
                        }

                        //Displays that everything went ok
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_changed), Toast.LENGTH_LONG).show();

                        //Saves changes into file
                        Database.save(getApplicationContext());

                        //Go back to MainActivity
                        finish();
                    }
                }

            }

        });

        //Displays a DeleteBankAccountDialogFragment
        mBtnDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DeleteBankAccountDialogFragment deleteBankAccountDialogFragment = new DeleteBankAccountDialogFragment();
                deleteBankAccountDialogFragment.show(getSupportFragmentManager(), "delete_bank_account");
            }

        });

        mEdtAccountAmount.addTextChangedListener(Currency.getActiveCurrency(getApplicationContext()).getCurrencyTextWatcher(mEdtAccountAmount));

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

        //Go back to MainActivity
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

        //Deletes AutoPays
        ArrayList<AutoPay> autoPays = getAutoPays();
        for(int i = 0; i<getAutoPays().size(); i++){
            Database.getAutoPays().remove(autoPays.get(i));
        }

        //Delete bank account
        Database.getBankAccounts().remove(bankAccount);
        Database.save(getApplicationContext());

        //Displays that the bank account got deleted successfully
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_deleted), Toast.LENGTH_LONG).show();

        //Go back to MainActivity
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

        mSwPrimaryAccount.setEnabled(!bankAccount.isPrimaryAccount());
        mSwPrimaryAccount.setChecked(bankAccount.isPrimaryAccount());

        mBtnCreate.setText(getResources().getString(R.string.btn_save));
    }

    private void hideErrors(){
        mTilAccountName.setError(null);
        mTilAccountAmount.setError(null);
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
