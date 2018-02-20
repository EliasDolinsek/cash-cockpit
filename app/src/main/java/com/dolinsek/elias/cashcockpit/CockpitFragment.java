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


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitFragment extends Fragment {

    private LinearLayout mLlSelectInfo;
    private TextView mTxvSelectInfo;
    private TextInputLayout mTilBillAmount, mTilBillDescription;
    private EditText mEdtBillAmount, mEdtBillDescription;
    private RadioGroup mRgBillTypes;
    private RadioButton mRbTypeInput, mRbTypeOutput, mRbTypeTransfer;
    private Button mBtnSelectCategory, mBtnAdd, mBtnCreateBankAccount;
    private Spinner mSpnSelectBankAccount;

    private BankAccount bankAccount;
    private Subcategory subcategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_cockpit, container, false);

        mTilBillAmount = (TextInputLayout) inflatedView.findViewById(R.id.til_cockpit_bill_amount);
        mTilBillDescription = (TextInputLayout) inflatedView.findViewById(R.id.til_cockpit_bill_description);

        mEdtBillAmount = (EditText) inflatedView.findViewById(R.id.edt_cockpit_bill_amount);
        mEdtBillDescription = (EditText) inflatedView.findViewById(R.id.edt_cockpit_bill_description);

        mRgBillTypes = (RadioGroup) inflatedView.findViewById(R.id.rg_cockpit_bill_types);
        mRbTypeInput = (RadioButton) inflatedView.findViewById(R.id.rb_cockpit_bill_type_input);
        mRbTypeOutput = (RadioButton) inflatedView.findViewById(R.id.rb_cockpit_bill_type_output);
        mRbTypeTransfer = (RadioButton) inflatedView.findViewById(R.id.rb_cockpit_bill_type_transfer);

        mBtnSelectCategory = (Button) inflatedView.findViewById(R.id.btn_cockpit_select_category);
        mBtnAdd = (Button) inflatedView.findViewById(R.id.btn_cockpit_add);
        mBtnCreateBankAccount = (Button) inflatedView.findViewById(R.id.btn_cockpit_create_bank_account);

        mLlSelectInfo = (LinearLayout) inflatedView.findViewById(R.id.ll_cockpit_select_info);
        mTxvSelectInfo = (TextView) inflatedView.findViewById(R.id.txv_cockpit_select_info);

        mSpnSelectBankAccount = (Spinner) inflatedView.findViewById(R.id.spn_cockpit_select_bank_account);

        mEdtBillAmount.addTextChangedListener(Currency.Factory.getCurrencyTextWatcher(mEdtBillAmount));
        mRgBillTypes.check(mRbTypeOutput.getId());

        mBtnSelectCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SelectSubcategoryDialogFragment selectSubcategoryDialogFragment = new SelectSubcategoryDialogFragment();
                selectSubcategoryDialogFragment.setOnSubcategorySelected(new PrimaryCategoryLightItemAdapter.SubcategorySelectionListener() {
                    @Override
                    public void onSubcategorySelected(Subcategory subcategory) {
                        CockpitFragment.this.subcategory = subcategory;
                        mBtnSelectCategory.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mBtnSelectCategory.setText(subcategory.getName());

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
                } else if(bankAccount == null){
                    mLlSelectInfo.setVisibility(View.VISIBLE);
                    mTxvSelectInfo.setText(getResources().getString(R.string.label_need_to_select_bank_account));
                } else if(subcategory == null){
                    mLlSelectInfo.setVisibility(View.VISIBLE);
                    mTxvSelectInfo.setText(getResources().getString(R.string.label_need_to_select_category));
                } else {
                    long amount = ((long) (Double.valueOf(mEdtBillAmount.getText().toString()) * 100));
                    String description = mEdtBillDescription.getText().toString();

                    int type;
                    if(mRgBillTypes.getCheckedRadioButtonId() == mRbTypeInput.getId()) {
                        type = Bill.TYPE_INPUT;
                    } else if(mRgBillTypes.getCheckedRadioButtonId() == mRbTypeOutput.getId()){
                        type = Bill.TYPE_OUTPUT;
                    } else {
                        type = Bill.TYPE_TRANSFER;
                    }

                    bankAccount.addBill(new Bill(amount, description, type, subcategory));

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

        mBtnCreateBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start BankAccountActivity
                Intent intent = new Intent(getContext(), BankAccountActivity.class);
                startActivity(intent);
            }
        });

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

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

        if(Database.getBankAccounts().size() != 0){
            bankAccount = Database.getBankAccounts().get(0);
            mBtnCreateBankAccount.setVisibility(View.GONE);
            mSpnSelectBankAccount.setVisibility(View.VISIBLE);
        } else {
            mBtnCreateBankAccount.setVisibility(View.VISIBLE);
            mSpnSelectBankAccount.setVisibility(View.GONE);
        }
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

}
