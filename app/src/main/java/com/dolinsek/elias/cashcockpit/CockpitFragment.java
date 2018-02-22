package com.dolinsek.elias.cashcockpit;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitFragment extends Fragment {

    private static final String PRIMARY_CATEGORY = "primary_category";
    private static final String SUBCATEGORY = "subcategory";
    private static final String ACCOUNT = "account";
    private static final String TYPE = "type";

    private LinearLayout mLlSelectInfo;
    private TextView mTxvSelectInfo, mTxvSelectedCategory;
    private TextInputLayout mTilBillAmount, mTilBillDescription;
    private EditText mEdtBillAmount, mEdtBillDescription;
    private Button mBtnSelectCategory, mBtnAdd;
    private Spinner mSpnSelectBankAccount, mSpnSelectBillType;

    private BankAccount bankAccount;
    private Subcategory subcategory;
    private int billType = Bill.TYPE_OUTPUT;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_cockpit, container, false);

        mTilBillAmount = (TextInputLayout) inflatedView.findViewById(R.id.til_cockpit_bill_amount);
        mTilBillDescription = (TextInputLayout) inflatedView.findViewById(R.id.til_cockpit_bill_description);

        mEdtBillAmount = (EditText) inflatedView.findViewById(R.id.edt_cockpit_bill_amount);
        mEdtBillDescription = (EditText) inflatedView.findViewById(R.id.edt_cockpit_bill_description);

        mBtnSelectCategory = (Button) inflatedView.findViewById(R.id.btn_cockpit_select_category);
        mBtnAdd = (Button) inflatedView.findViewById(R.id.btn_cockpit_add);

        mLlSelectInfo = (LinearLayout) inflatedView.findViewById(R.id.ll_cockpit_select_info);
        mTxvSelectInfo = (TextView) inflatedView.findViewById(R.id.txv_cockpit_select_info);
        mTxvSelectedCategory = (TextView) inflatedView.findViewById(R.id.txv_cockpit_selected_category);

        mSpnSelectBankAccount = (Spinner) inflatedView.findViewById(R.id.spn_cockpit_select_bank_account);
        mSpnSelectBillType = (Spinner) inflatedView.findViewById(R.id.spn_cockpit_select_bill_type);

        final ArrayAdapter<CharSequence> selectBillTypeAdapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item, getResources().getTextArray(R.array.bill_types_array));
        selectBillTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnSelectBillType.setAdapter(selectBillTypeAdapter);
        mSpnSelectBillType.setSelection(1);
        mSpnSelectBillType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    billType = Bill.TYPE_INPUT;
                } else if(i == 1){
                    billType = Bill.TYPE_OUTPUT;
                } else {
                    billType = Bill.TYPE_TRANSFER;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(savedInstanceState != null){
            billType = savedInstanceState.getInt(TYPE, 2);
            mSpnSelectBillType.setSelection(billType);
            if(savedInstanceState.getInt(PRIMARY_CATEGORY, -1) != -1){
                subcategory = Database.getPrimaryCategories().get(savedInstanceState.getInt(PRIMARY_CATEGORY, 0)).getSubcategories().get(savedInstanceState.getInt(SUBCATEGORY, 0));
                mTxvSelectedCategory.setVisibility(View.VISIBLE);
                mTxvSelectedCategory.setText(subcategory.getPrimaryCategory().getName() + " " + Character.toString((char)0x00B7) + " " + subcategory.getName());
            }
        }

        if(Database.getBankAccounts().size() != 0) {
            final ArrayAdapter<CharSequence> selectBankAccountAdapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_spinner_item);
            selectBankAccountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            for(BankAccount bankAccount:Database.getBankAccounts()){
                selectBankAccountAdapter.add(bankAccount.getName());
            }

            mSpnSelectBankAccount.setAdapter(selectBankAccountAdapter);
            mSpnSelectBankAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    bankAccount = Database.getBankAccounts().get(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

            if(savedInstanceState != null) {
                int index = savedInstanceState.getInt(ACCOUNT, 0);
                mSpnSelectBankAccount.setSelection(index);
                bankAccount = Database.getBankAccounts().get(index);
            }
        }

        mEdtBillAmount.addTextChangedListener(Currency.Factory.getCurrencyTextWatcher(mEdtBillAmount));

        mBtnSelectCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SelectSubcategoryDialogFragment selectSubcategoryDialogFragment = new SelectSubcategoryDialogFragment();
                selectSubcategoryDialogFragment.setOnSubcategorySelected(new PrimaryCategoryLightItemAdapter.SubcategorySelectionListener() {
                    @Override
                    public void onSubcategorySelected(Subcategory subcategory) {
                        CockpitFragment.this.subcategory = subcategory;

                        mLlSelectInfo.setVisibility(View.GONE);
                        mTxvSelectedCategory.setVisibility(View.VISIBLE);
                        mTxvSelectedCategory.setText(subcategory.getPrimaryCategory().getName() + " " + Character.toString((char)0x00B7) + " " + subcategory.getName());
                        selectSubcategoryDialogFragment.dismiss();
                    }
                });

                hideKeyboard();
                selectSubcategoryDialogFragment.show(getFragmentManager(), "select_category");
            }
        });

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTilBillAmount.setError(null);
                mTilBillAmount.setErrorEnabled(false);
                mTilBillDescription.setErrorEnabled(false);
                mLlSelectInfo.setVisibility(View.GONE);

                if(mEdtBillAmount.getText().toString().equals("")){
                    mTilBillDescription.setErrorEnabled(true);
                    mTilBillAmount.setError(getResources().getString(R.string.label_enter_amount));
                    mTilBillDescription.setErrorEnabled(false);
                } else if(subcategory == null){
                    mLlSelectInfo.setVisibility(View.VISIBLE);
                    mTxvSelectInfo.setText(getResources().getString(R.string.label_need_to_select_category));
                } else {
                    long amount = ((long) (Double.valueOf(mEdtBillAmount.getText().toString()) * 100));
                    String description = mEdtBillDescription.getText().toString();

                    bankAccount.addBill(new Bill(amount, description, billType, subcategory));
                    try {
                        Database.save(getContext());
                        Database.load(getContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Show message
                    Toast.makeText(getContext(), getResources().getString(R.string.toast_bill_added), Toast.LENGTH_SHORT).show();

                    //Clears all fields
                    mEdtBillAmount.setText("");
                    mEdtBillDescription.setText("");
                    mEdtBillAmount.requestFocus();

                    hideKeyboard();

                }
            }
        });

        return inflatedView;
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Gets index of selected account
        int bankAccountIndex = 0;
        for(int i = 0; i<Database.getBankAccounts().size(); i++){
            if(Database.getBankAccounts().get(i).equals(this.bankAccount))
                bankAccountIndex = i;
        }

        //Gets index of selected category index
        int categoryIndex = 0;
        int subcategoryIndex = 0;
        if(subcategory != null){
            for(int i = 0; i<Database.getPrimaryCategories().size(); i++){
                if(Database.getPrimaryCategories().get(i).equals(subcategory.getPrimaryCategory())){
                    categoryIndex = i;
                    for(int y = 0; y<Database.getPrimaryCategories().get(i).getSubcategories().size(); y++){
                        if(Database.getPrimaryCategories().get(i).getSubcategories().get(y).equals(subcategory))
                            subcategoryIndex = y;
                    }
                }
            }

            outState.putInt(PRIMARY_CATEGORY, categoryIndex);
            outState.putInt(SUBCATEGORY, subcategoryIndex);
        } else if(bankAccount != null){
            outState.putInt(ACCOUNT, bankAccountIndex);
        }

        outState.putInt(TYPE, billType);
    }
}