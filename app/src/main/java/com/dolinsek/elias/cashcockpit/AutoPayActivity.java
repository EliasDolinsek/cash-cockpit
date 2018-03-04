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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

public class AutoPayActivity extends AppCompatActivity {

    public static final String EXTRA_AUTO_PAY_INDEX = "auto_pay";

    private TextInputLayout mTilAutoPayName, mTilAmount;
    private TextInputEditText mEdtAutoPayName, mEdtAmount;
    private Button mBtnSelectSubcategory, mBtnCreate, mBtnDelete;
    private TextView mTxvSelectedCategory;

    private Spinner mSpnSelectBankAccount, mSpnSelectAutoPayType;

    private AutoPay autoPay;
    private boolean editMode;

    /**
     * Is a placeholder for a new selected subcategory if it's in edit mode
     */
    private Subcategory subcategoryPlaceholder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_pay);

        mTilAutoPayName = (TextInputLayout) findViewById(R.id.til_auto_pay_name);
        mTilAmount = (TextInputLayout) findViewById(R.id.til_auto_pay_amount);

        mEdtAutoPayName = (TextInputEditText) findViewById(R.id.edt_auto_pay_name);
        mEdtAmount = (TextInputEditText) findViewById(R.id.edt_auto_pay_amount);

        mTxvSelectedCategory = (TextView) findViewById(R.id.txv_auto_pay_selected_category);

        mBtnSelectSubcategory = (Button) findViewById(R.id.btn_auto_pay_select_subcategory);
        mBtnCreate = (Button) findViewById(R.id.btn_auto_pay_create);
        mBtnDelete = (Button) findViewById(R.id.btn_auto_pay_delete);

        mSpnSelectBankAccount = (Spinner) findViewById(R.id.spn_auto_pay_select_bank_account);
        final ArrayAdapter<CharSequence> bankAccounts = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_spinner_item);
        for(BankAccount bankAccount:Database.getBankAccounts()){
            bankAccounts.add(bankAccount.getName());
        }
        bankAccounts.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnSelectBankAccount.setAdapter(bankAccounts);
        mSpnSelectBankAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                autoPay.setBankAccount(Database.getBankAccounts().get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpnSelectAutoPayType = (Spinner) findViewById(R.id.spn_auto_pay_select_type);
        final ArrayAdapter<CharSequence> autoPayTypes = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.auto_pay_types_array));
        autoPayTypes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnSelectAutoPayType.setAdapter(autoPayTypes);
        mSpnSelectAutoPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                autoPay.setType(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(getIntent().hasExtra(EXTRA_AUTO_PAY_INDEX)){
            autoPay = Database.getAutoPays().get(getIntent().getIntExtra(EXTRA_AUTO_PAY_INDEX, 0));
            editMode = true;

            mEdtAutoPayName.setText(autoPay.getName());
            mEdtAmount.setText(Currency.Factory.getActiveCurrency(getApplicationContext()).formatAmountToString(autoPay.getBill().getAmount()).replace(Currency.Factory.getActiveCurrency(getApplicationContext()).getSymbol(), ""));

            mTxvSelectedCategory.setText(autoPay.getBill().getSubcategory().getName());

            mBtnCreate.setText(getResources().getString(R.string.btn_save));
            mBtnDelete.setVisibility(View.VISIBLE);

            mSpnSelectAutoPayType.setSelection(autoPay.getType());
            mSpnSelectAutoPayType.setEnabled(false);
            mSpnSelectBankAccount.setEnabled(false);
        } else {
            autoPay = new AutoPay(new Bill(0, "", Bill.TYPE_OUTPUT, null), AutoPay.TYPE_MONTHLY, "", null);
        }

        //Shows dialog what allows the user to select a subcategory
        mBtnSelectSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SelectSubcategoryDialogFragment selectSubcategoryDialogFragment = new SelectSubcategoryDialogFragment();
                selectSubcategoryDialogFragment.setOnSubcategorySelected(new PrimaryCategoryLightItemAdapter.SubcategorySelectionListener() {
                    @Override
                    public void onSubcategorySelected(Subcategory subcategory) {
                        if(editMode)
                            subcategoryPlaceholder = subcategory;
                        else
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
                mTilAmount.setError(null);
                mTxvSelectedCategory.setTextColor(getResources().getColor(R.color.colorPrimaryTextColor));

                if(mEdtAutoPayName.getText().toString().trim().equals("")){
                    mTilAutoPayName.setError(getResources().getString(R.string.label_enter_category_name));
                } else if(mEdtAmount.getText().toString().equals("")){
                    mTilAmount.setError(getResources().getString(R.string.label_enter_amount));
                } else if(autoPay.getBill().getSubcategory() == null){
                    mTxvSelectedCategory.setText(getResources().getString(R.string.label_need_to_select_category));
                } else {

                    //Gets amount
                    long amount = ((long) (Double.valueOf(mEdtAmount.getText().toString()) * 100));

                    //Sets changes
                    autoPay.setName(mEdtAutoPayName.getText().toString());
                    autoPay.getBill().setAmount(amount);

                    if(editMode){
                        autoPay.getBill().setSubcategory(subcategoryPlaceholder == null ? autoPay.getBill().getSubcategory() : subcategoryPlaceholder);
                    } else {
                        //Creates a new AutoPay
                        Database.getAutoPays().add(autoPay);
                    }

                    autoPay.managePayments();

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

        mEdtAmount.addTextChangedListener(Currency.Factory.getCurrencyTextWatcher(mEdtAmount));
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
