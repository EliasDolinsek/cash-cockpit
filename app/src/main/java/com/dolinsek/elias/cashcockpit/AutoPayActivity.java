package com.dolinsek.elias.cashcockpit;

import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

public class AutoPayActivity extends AppCompatActivity {

    private TextInputLayout mTilAutoPayName, mTilAmountEuros, mTilAmountCents;
    private TextInputEditText mEdtAutoPayName, mEdtAmountEuros, mEdtAmountCents;
    private Button mBtnSelectBankAccount, mBtnSelectSubcategory, mBtnCreate;
    private TextView mTxvSelectedBankAccount, mTxvSelectedCategory;

    private AutoPay autoPay;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_pay);

        mTilAutoPayName = (TextInputLayout) findViewById(R.id.til_auto_pay_name);
        mTilAmountEuros = (TextInputLayout) findViewById(R.id.til_auto_pay_euros);
        mTilAmountCents = (TextInputLayout) findViewById(R.id.til_auto_pay_cents);

        mEdtAutoPayName = (TextInputEditText) findViewById(R.id.edt_auto_pay_name);
        mEdtAmountEuros = (TextInputEditText) findViewById(R.id.edt_auto_pay_euros);
        mEdtAmountCents = (TextInputEditText) findViewById(R.id.edt_auto_pay_cents);

        mTxvSelectedCategory = (TextView) findViewById(R.id.txv_auto_pay_selected_category);
        mTxvSelectedBankAccount = (TextView) findViewById(R.id.txv_auto_pay_selected_bank_account);

        mBtnSelectBankAccount = (Button) findViewById(R.id.btn_auto_pay_select_bank_account);
        mBtnSelectSubcategory = (Button) findViewById(R.id.btn_auto_pay_select_subcategory);
        mBtnCreate = (Button) findViewById(R.id.btn_auto_pay_create);

        //TODO
        if(true)
            autoPay = new AutoPay(new Bill(0, "", null), AutoPay.TYPE_MONTHLY, "", null);

        mBtnSelectBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectBankAccountDialogFragment selectBankAccountDialogFragment = new SelectBankAccountDialogFragment();
                selectBankAccountDialogFragment.setOnSelectListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        autoPay.setBankAccount(Database.getBankAccounts().get(i));
                        mTxvSelectedBankAccount.setText(Database.getBankAccounts().get(i).getName());
                    }
                });

                selectBankAccountDialogFragment.show(getSupportFragmentManager(), "select_autoPay");
            }
        });

        mBtnSelectSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SelectSubcategoryDialogFragment selectSubcategoryDialogFragment = new SelectSubcategoryDialogFragment();
                selectSubcategoryDialogFragment.setOnSubcategorySelected(new PrimaryCategoryLightItemAdapter.SubcategorySelectionListener() {
                    @Override
                    public void onSubcategorySelected(Subcategory subcategory) {
                        autoPay.getBill().setSubcategory(subcategory);
                        mTxvSelectedCategory.setText(subcategory.getName());
                        selectSubcategoryDialogFragment.dismiss();
                    }
                });
                selectSubcategoryDialogFragment.show(getSupportFragmentManager(), "select_subcategory");
            }
        });

        mBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTilAutoPayName.setError(null);
                mTilAmountEuros.setError(null);
                mTilAmountCents.setError(null);
                mTxvSelectedBankAccount.setTextColor(getResources().getColor(R.color.colorPrimaryTextColor));
                mTxvSelectedCategory.setTextColor(getResources().getColor(R.color.colorPrimaryTextColor));

                if(mEdtAutoPayName.getText().toString().trim().equals("")){
                    mTilAutoPayName.setError(getResources().getString(R.string.label_enter_category_name));
                } else if(mEdtAmountEuros.getText().toString().equals("") && mEdtAmountCents.getText().toString().equals("")){
                    mTilAmountEuros.setError(getResources().getString(R.string.label_enter_euros));
                    mTilAmountCents.setError(" ");
                } else if(autoPay.getBankAccount() == null){
                    mTxvSelectedBankAccount.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                } else if(autoPay.getBill().getSubcategory() == null){
                    mTxvSelectedCategory.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                } else {

                    long euros = 0, cents = 0;
                    if(!mEdtAmountEuros.getText().toString().equals(""))
                        euros = Long.valueOf(mEdtAmountEuros.getText().toString()) * 100;
                    if(!mEdtAmountCents.getText().toString().equals(""))
                        cents = Long.valueOf(mEdtAmountCents.getText().toString());

                    autoPay.setName(mEdtAutoPayName.getText().toString());
                    autoPay.getBill().setAmount(euros + cents);

                    Database.getAutoPays().add(autoPay);
                    Database.save(getApplicationContext());
                    finish();
                }
            }
        });
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
}
