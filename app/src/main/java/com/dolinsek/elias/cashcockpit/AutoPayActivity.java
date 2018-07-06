package com.dolinsek.elias.cashcockpit;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import org.json.JSONException;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;

public class AutoPayActivity extends AppCompatActivity {

    public static final String EXTRA_AUTO_PAY_INDEX = "auto_pay";
    private static final int RQ_SELECT_CATEGORY = 439;

    private EditText mEdtAutoPayName, mEdtAmount;
    private Button mBtnSelectSubcategory, mBtnCreate, mBtnDelete;
    private TextView mTxvSelectedCategory, mTxvCurrencyShortcut;

    private Spinner mSpnSelectBankAccount, mSpnSelectAutoPayType, mSpnSelectAutoPayBillType;

    private AutoPay autoPay;
    private boolean editModeActive;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_pay);

        mEdtAutoPayName = findViewById(R.id.edt_auto_pay_name);
        mEdtAmount = findViewById(R.id.edt_auto_pay_amount);
        mEdtAmount.setSelection(mEdtAmount.getText().length());

        mTxvSelectedCategory = (TextView) findViewById(R.id.txv_auto_pay_selected_category);
        mTxvCurrencyShortcut = findViewById(R.id.txv_auto_pay_active_currency_shortcut);

        mBtnSelectSubcategory = (Button) findViewById(R.id.btn_auto_pay_select_subcategory);
        mBtnCreate = (Button) findViewById(R.id.btn_auto_pay_create);
        mBtnDelete = (Button) findViewById(R.id.btn_auto_pay_delete);

        mSpnSelectBankAccount = (Spinner) findViewById(R.id.spn_auto_pay_select_bank_account);
        mSpnSelectAutoPayType = (Spinner) findViewById(R.id.spn_auto_pay_select_type);
        mSpnSelectAutoPayBillType = (Spinner) findViewById(R.id.spn_auto_pay_select_bill_type);

        setupSpinners();
        displayCurrencyShortcut();

        if(getIntent().hasExtra(EXTRA_AUTO_PAY_INDEX)){
            editModeActive = true;

            int indexOfAutoPayInDatabase = getIntent().getIntExtra(EXTRA_AUTO_PAY_INDEX, 0);
            autoPay = Database.getAutoPays().get(indexOfAutoPayInDatabase);

            displayAutoPayDetails();
            setupViewsForActiveEditMode();
        } else {
            autoPay = new AutoPay();
            autoPay.setBill(new Bill());
        }

        mBtnSelectSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SelectCategoryActivity.class);

                if (autoPay.getBill().getSubcategory() != null){
                    Subcategory subcategoryOfAutoPay = autoPay.getBill().getSubcategory();
                    putSelectedSubcategoryIndexIntoIntent(subcategoryOfAutoPay, intent);
                }

                startActivityForResult(intent, RQ_SELECT_CATEGORY);
            }
        });

        mBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (everythingFilledOutCorrectly()){
                    long amount = getAmountInputAsLong();

                    autoPay.setName(mEdtAutoPayName.getText().toString());
                    autoPay.getBill().setAmount(amount);
                    autoPay.getBill().setDescription(autoPay.getName());

                    if(!editModeActive){
                        autoPay.addPaymentTimestamp();
                        Database.getAutoPays().add(autoPay);
                    }

                    Database.save(getApplicationContext());
                    finish();
                } else {
                    Toolkit.displayPleaseCheckInputsToast(getApplicationContext());
                }
            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteAutoPayDialog();
            }
        });

        mEdtAmount.addTextChangedListener(Currency.getActiveCurrency(getApplicationContext()).getCurrencyTextWatcher(mEdtAmount));
    }

    private boolean everythingFilledOutCorrectly() {
        if(mEdtAutoPayName.getText().toString().trim().equals("")){
            return false;
        } else if(mEdtAmount.getText().toString().equals("")){
            return false;
        } else if(autoPay.getBill().getSubcategory() == null){
            return false;
        } else {
            return true;
        }
    }

    private void showDeleteAutoPayDialog(){
        DeleteAutoPayDialogFragment deleteAutoPayDialogFragment = new DeleteAutoPayDialogFragment();
        deleteAutoPayDialogFragment.setAutoPay(autoPay);
        deleteAutoPayDialogFragment.show(getSupportFragmentManager(), "delete_auto_pay");
    }

    private void displayCurrencyShortcut(){
        String currencyShortcut = Currency.getActiveCurrency(getApplicationContext()).getCurrencyShortcut();
        mTxvCurrencyShortcut.setText(currencyShortcut);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_SELECT_CATEGORY && resultCode == RESULT_OK){
            int indexOfPrimaryCategoryInDatabase = data.getIntExtra(SelectCategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, 0);
            int indexOfSubcategoryInPrimaryCategory = data.getIntExtra(SelectCategoryActivity.EXTRA_SUBCATEGORY_INDEX, 0);

            Subcategory selectedSubcategory = Database.getPrimaryCategories().get(indexOfPrimaryCategoryInDatabase).getSubcategories().get(indexOfSubcategoryInPrimaryCategory);
            autoPay.getBill().setSubcategory(selectedSubcategory);
            updateTxtForSelectedSubcategory();
        }
    }

    private void updateTxtForSelectedSubcategory(){
        Subcategory selectedSubcategory = autoPay.getBill().getSubcategory();
        mTxvSelectedCategory.setVisibility(View.VISIBLE);
        mTxvSelectedCategory.setText(selectedSubcategory.getName());
    }

    private void setupViewsForActiveEditMode() {
        mSpnSelectAutoPayType.setSelection(autoPay.getType());

        mBtnCreate.setText(getResources().getString(R.string.btn_save));
        mBtnDelete.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            //Restores AutoPay so that unwanted changes don't get saved
            Database.load(getApplicationContext());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void putSelectedSubcategoryIndexIntoIntent(Subcategory subcategory, Intent intent){
        int indexOfPrimaryCategoryInDatabase = getIndexOfPrimaryCategoryInDatabase(subcategory.getPrimaryCategory());
        int indexOfSubcategoryInDatabase = getIndexOfSubcategoryInPrimaryCategory(subcategory);

        intent.putExtra(SelectCategoryActivity.SELECTED_PRIMARY_CATEGORY_INDEX, indexOfPrimaryCategoryInDatabase);
        intent.putExtra(SelectCategoryActivity.SELECTED_SUBCATEGORY_INDEX, indexOfSubcategoryInDatabase);
    }

    private void setupSpinners(){
        final ArrayAdapter<CharSequence> bankAccountsAdapter = new ArrayAdapter<>(this, R.layout.costum_spinner_layout, getNamesOfBankAccountsInDatabase());
        final ArrayAdapter<CharSequence> autoPayTypesAdapter = new ArrayAdapter<>(this, R.layout.costum_spinner_layout, getResources().getTextArray(R.array.auto_pay_types_array));
        final ArrayAdapter<CharSequence> billTypesAdapter = new ArrayAdapter<>(this, R.layout.costum_spinner_layout, getResources().getTextArray(R.array.bill_types_array));

        bankAccountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoPayTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        billTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpnSelectBankAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                autoPay.setBankAccount(Database.getBankAccounts().get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mSpnSelectAutoPayType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                autoPay.setType(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mSpnSelectAutoPayBillType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                autoPay.getBill().setType(index);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpnSelectBankAccount.setSelection(0);
        mSpnSelectAutoPayBillType.setSelection(1); //Bill-Type output
        mSpnSelectAutoPayType.setSelection(1); //AutoPay type monthly

        mSpnSelectBankAccount.setAdapter(bankAccountsAdapter);
        mSpnSelectAutoPayType.setAdapter(autoPayTypesAdapter);
        mSpnSelectAutoPayBillType.setAdapter(billTypesAdapter);

        setSpinnerArrowColorToWhite(mSpnSelectBankAccount);
        setSpinnerArrowColorToWhite(mSpnSelectAutoPayType);
        setSpinnerArrowColorToWhite(mSpnSelectAutoPayBillType);
    }

    private void setSpinnerArrowColorToWhite(Spinner spinner){
        spinner.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
    }

    private ArrayList<CharSequence> getNamesOfBankAccountsInDatabase(){
        ArrayList<CharSequence> bankAccountNames = new ArrayList<>();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            bankAccountNames.add(bankAccount.getName());
        }

        return bankAccountNames;
    }

    private void displayAutoPayDetails() {
        mEdtAutoPayName.setText(autoPay.getName());
        updateTxtForSelectedSubcategory();

        String formattedAmountOfAutoPay = Currency.getActiveCurrency(getApplicationContext()).formatAmountToReadableString(autoPay.getBill().getAmount());
        mEdtAmount.setText(formattedAmountOfAutoPay);

        int indexOfBankAccountInDatabase = getIndexOfBankAccountInDatabase(autoPay.getBankAccount());
        mSpnSelectAutoPayBillType.setSelection(autoPay.getBill().getType());
        mSpnSelectAutoPayType.setSelection(autoPay.getType());
        mSpnSelectBankAccount.setSelection(indexOfBankAccountInDatabase);
    }

    private int getIndexOfBankAccountInDatabase(BankAccount bankAccountToSearch){
        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            if (Database.getBankAccounts().get(i).equals(bankAccountToSearch)){
                return i;
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account in database!");
    }

    private int getIndexOfPrimaryCategoryInDatabase(PrimaryCategory primaryCategory){
        ArrayList<PrimaryCategory> primaryCategoriesInDatabase = Database.getPrimaryCategories();
        for (int i = 0; i<primaryCategoriesInDatabase.size(); i++){
            System.out.println(primaryCategory + " " + primaryCategoriesInDatabase.get(i));
            if (primaryCategory.equals(primaryCategoriesInDatabase.get(i))){
                return i;
            }
        }

        throw new Resources.NotFoundException("Couldn't find primary category in database!");
    }

    private int getIndexOfSubcategoryInPrimaryCategory(Subcategory subcategory){
        for (int i = 0; i<subcategory.getPrimaryCategory().getSubcategories().size(); i++){
            if (subcategory.equals(subcategory.getPrimaryCategory().getSubcategories().get(i))){
                return i;
            }
        }

        throw new Resources.NotFoundException("Couldn't find subcategory in database!");
    }

    private long getAmountInputAsLong(){
        return ((long) (Double.valueOf(mEdtAmount.getText().toString()) * 100));
    }

    public static class DeleteAutoPayDialogFragment extends DialogFragment {

        private AutoPay autoPayToDelete;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getResources().getString(R.string.dialog_msg_delete_auto_pay));

            builder.setPositiveButton(getResources().getString(R.string.dialog_action_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(autoPayToDelete != null){
                        Database.getAutoPays().remove(autoPayToDelete);
                        Database.save(getActivity());

                        getActivity().finish();
                    }
                }
            });

            return builder.create();
        }

        public void setAutoPay(AutoPay autoPay){
            this.autoPayToDelete = autoPay;
        }
    }
}
