package com.dolinsek.elias.cashcockpit;

import android.support.v4.app.DialogFragment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.model.AutoPay;
import com.dolinsek.elias.cashcockpit.model.BankAccount;
import com.dolinsek.elias.cashcockpit.model.Database;

import java.util.ArrayList;

public class BankAccountActivity extends AppCompatActivity implements DeleteBankAccountDialogFragment.DeleteBankAccountDialogListener{

    public static final String EXTRA_BANK_ACCOUNT_INDEX = "bankAccountIndex";

    private TextInputLayout mTilAccountName, mTilAccountEuros, mTilAccountCents;
    private EditText mEdtAccountName, mEdtAccountEuros, mEdtAccountCents;
    private Switch mSwPrimaryAccount;
    private Button mBtnCreate, mBtnDelete;

    private BankAccount bankAccount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_account);

        mTilAccountName = (TextInputLayout) findViewById(R.id.til_bank_account_name);
        mTilAccountEuros = (TextInputLayout) findViewById(R.id.til_bank_account_euros);
        mTilAccountCents = (TextInputLayout) findViewById(R.id.til_bank_account_cents);

        mEdtAccountName = (EditText) findViewById(R.id.edt_bank_account_name);
        mEdtAccountEuros = (EditText) findViewById(R.id.edt_bank_account_euros);
        mEdtAccountCents = (EditText) findViewById(R.id.edt_bank_account_cents);

        mSwPrimaryAccount = (Switch) findViewById(R.id.sw_bank_account_priamry_account);
        mBtnCreate = (Button) findViewById(R.id.btn_bank_account_create);
        mBtnDelete = (Button) findViewById(R.id.btn_bank_account_delete);

        if(getIntent().hasExtra(EXTRA_BANK_ACCOUNT_INDEX)){
            bankAccount = Database.getBankAccounts().get(getIntent().getIntExtra(EXTRA_BANK_ACCOUNT_INDEX, 0));
            mBtnDelete.setEnabled(!bankAccount.isPrimaryAccount());
        } else{
            mBtnDelete.setVisibility(View.GONE);
        }

        if(bankAccount != null){
            mEdtAccountName.setText(bankAccount.getName());
            mEdtAccountEuros.setText(String.valueOf(bankAccount.getBalance() / 100));
            mEdtAccountCents.setText(String.valueOf(bankAccount.getBalance() % 100));

            mSwPrimaryAccount.setEnabled(!bankAccount.isPrimaryAccount());
            mSwPrimaryAccount.setChecked(bankAccount.isPrimaryAccount());

            mBtnCreate.setText(getResources().getString(R.string.btn_save));
        }

        if(Database.getBankAccounts().size() == 0){
            mSwPrimaryAccount.setChecked(true);
            mSwPrimaryAccount.setEnabled(false);
        }

        mBtnCreate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                mTilAccountName.setError(null);
                mTilAccountEuros.setError(null);
                mTilAccountCents.setError(null);

                boolean accountNameAlreadyExists = false;
                for(int i = 0; i<Database.getBankAccounts().size(); i++){
                    if(Database.getBankAccounts().get(i).getName().equals(mEdtAccountName.getText().toString()))
                        accountNameAlreadyExists = true;
                }

                if(mEdtAccountName.getText().toString().trim().equals("")){
                    mTilAccountName.setError(getResources().getString(R.string.label_enter_bank_account_name));
                } else if(mEdtAccountEuros.getText().toString().equals("") && mEdtAccountCents.getText().toString().equals("")){
                    mTilAccountEuros.setError(getResources().getString(R.string.label_enter_euros));
                    mTilAccountCents.setError(" ");
                } else if(accountNameAlreadyExists && bankAccount == null){
                    mTilAccountName.setError(getResources().getString(R.string.label_bank_account_already_exits));
                } else {

                    //Read balance
                    long balance = 0;
                    if(!mEdtAccountEuros.getText().toString().equals(""))
                        balance = Long.valueOf(mEdtAccountEuros.getText().toString()) * 100;
                    if(!mEdtAccountCents.getText().toString().equals(""))
                        balance += Long.valueOf(mEdtAccountCents.getText().toString());

                    if(bankAccount == null){
                        //Create and save new bank account
                        Database.getBankAccounts().add(new BankAccount(mEdtAccountName.getText().toString(), balance, mSwPrimaryAccount.isChecked()));
                        Database.save(getApplicationContext());

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_created), Toast.LENGTH_LONG).show();

                        //Go back to MainActivity
                        finish();
                    } else {
                        //Save bank account changes
                        bankAccount.setName(mEdtAccountName.getText().toString());
                        bankAccount.setBalance(balance);

                        if(mSwPrimaryAccount.isChecked()){
                            for(int i = 0; i<Database.getBankAccounts().size(); i++){
                                Database.getBankAccounts().get(i).setPrimaryAccount(false);
                            }

                            bankAccount.setPrimaryAccount(true);
                        }

                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_changed), Toast.LENGTH_LONG).show();

                        //Save changes into file
                        Database.save(getApplicationContext());

                        //Go back to MainActivity
                        finish();
                    }
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
    }

    /**
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

        Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_bank_account_deleted), Toast.LENGTH_LONG).show();

        //Go back to MainActivity
        finish();
    }
}
