package com.dolinsek.elias.cashcockpit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

public class AutoPayActivity extends AppCompatActivity {

    public static final String EXTRA_AUTO_PAY_INDEX = "auto_pay";

    private TextInputLayout mTilAutoPayName, mTilAmountEuros, mTilAmountCents;
    private TextInputEditText mEdtAutoPayName, mEdtAmountEuros, mEdtAmountCents;
    private Button mBtnSelectBankAccount, mBtnSelectSubcategory, mBtnCreate, mBtnDelete;
    private TextView mTxvSelectedBankAccount, mTxvSelectedCategory;
    private RadioGroup mRgAutoPayType;
    private RadioButton mRbWeekly, mRbMonthly, mRbYearly;

    private AutoPay autoPay;
    private boolean editMode;

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
        mBtnDelete = (Button) findViewById(R.id.btn_auto_pay_delete);

        mRbWeekly = (RadioButton) findViewById(R.id.rb_auto_pay_weekly);
        mRbMonthly = (RadioButton) findViewById(R.id.rb_auto_pay_monthly);
        mRbYearly = (RadioButton) findViewById(R.id.rb_auto_pay_yearly);
        mRgAutoPayType = (RadioGroup) findViewById(R.id.rg_auto_pay_types);

        if(getIntent().hasExtra(EXTRA_AUTO_PAY_INDEX)){
            autoPay = Database.getAutoPays().get(getIntent().getIntExtra(EXTRA_AUTO_PAY_INDEX, 0));
            editMode = true;

            mEdtAutoPayName.setText(autoPay.getName());
            mEdtAmountEuros.setText(String.valueOf(autoPay.getBill().getAmount() / 100));
            mEdtAmountCents.setText(String.valueOf(autoPay.getBill().getAmount() % 100));

            mTxvSelectedBankAccount.setText(autoPay.getBankAccount().getName());
            mTxvSelectedCategory.setText(autoPay.getBill().getSubcategory().getName());

            mBtnCreate.setText(getResources().getString(R.string.btn_save));
            mBtnDelete.setVisibility(View.VISIBLE);

            if(autoPay.getType() == AutoPay.TYPE_MONTHLY)
                mRgAutoPayType.check(mRbMonthly.getId());
            else if(autoPay.getType() == AutoPay.TYPE_WEEKLY)
                mRgAutoPayType.check(mRbWeekly.getId());
            else
                mRgAutoPayType.check(mRbYearly.getId());
        } else {
            autoPay = new AutoPay(new Bill(0, "", null), AutoPay.TYPE_MONTHLY, "", null);
        }

        //Shows dialog what allows the user to select a bank account
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

        //Shows dialog what allows the user to select a subcategory
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

                //Removes errors
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

                    //Gets amount
                    long euros = 0, cents = 0;
                    if(!mEdtAmountEuros.getText().toString().equals(""))
                        euros = Long.valueOf(mEdtAmountEuros.getText().toString()) * 100;
                    if(!mEdtAmountCents.getText().toString().equals(""))
                        cents = Long.valueOf(mEdtAmountCents.getText().toString());

                    //Gets type
                    int type = AutoPay.TYPE_MONTHLY;
                    if(mRgAutoPayType.getCheckedRadioButtonId() != -1){
                        if(mRbWeekly.getId() == mRgAutoPayType.getCheckedRadioButtonId())
                            type = AutoPay.TYPE_WEEKLY;
                        else if(mRbMonthly.getId() == mRgAutoPayType.getCheckedRadioButtonId())
                            type = AutoPay.TYPE_MONTHLY;
                    }

                    //Sets changes
                    autoPay.setName(mEdtAutoPayName.getText().toString());
                    autoPay.getBill().setAmount(euros + cents);
                    autoPay.setType(type);

                    if(!editMode)
                        //Creates a new AutoPay
                        Database.getAutoPays().add(autoPay);

                    //Saves changes
                    Database.save(getApplicationContext());

                    //Go back to MainActivity
                    finish();
                }
            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteAutoPayDialogFragment deleteAutoPayDialogFragment = new DeleteAutoPayDialogFragment();
                deleteAutoPayDialogFragment.setAutoPay(autoPay);
                deleteAutoPayDialogFragment.show(getSupportFragmentManager(), "delete_auto_pay");
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

    public static class DeleteAutoPayDialogFragment extends DialogFragment {

        private AutoPay autoPay;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.dialog_msg_delete_auto_pay));
            builder.setPositiveButton(getResources().getString(R.string.dialog_action_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(autoPay != null){
                        Database.getAutoPays().remove(autoPay);
                        Database.save(getActivity());
                        getActivity().finish();
                    }
                }
            });

            return builder.create();
        }

        public void setAutoPay(AutoPay autoPay){
            this.autoPay = autoPay;
        }
    }
}
