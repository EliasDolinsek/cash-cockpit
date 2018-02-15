package com.dolinsek.elias.cashcockpit;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Subcategory;


/**
 * A simple {@link Fragment} subclass.
 */
public class CockpitFragment extends Fragment {

    private TextInputLayout mTilBillAmount, mTilBillDescription;
    private EditText mEdtBillAmount, mEdtBillDescription;
    private RadioGroup mRgBillTypes;
    private RadioButton mRbTypeInput, mRbTypeOutput, mRbTypeTransfer;
    private Button mBtnSelectBankAccount, mBtnSelectCategory, mBtnAdd;
    private TextView mTxvSelectedBankAccount, mTxvSelectedCategory;

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

        mBtnSelectBankAccount = (Button) inflatedView.findViewById(R.id.btn_cockpit_select_bank_account);
        mBtnSelectCategory = (Button) inflatedView.findViewById(R.id.btn_cockpit_select_category);
        mBtnAdd = (Button) inflatedView.findViewById(R.id.btn_cockpit_add);

        mTxvSelectedBankAccount = (TextView) inflatedView.findViewById(R.id.txv_cockpit_bank_account);
        mTxvSelectedCategory = (TextView) inflatedView.findViewById(R.id.txv_cockpit_category);

        mEdtBillAmount.addTextChangedListener(Currency.Factory.getCurrencyTextWatcher(mEdtBillAmount));
        mRgBillTypes.check(mRbTypeInput.getId());

        mBtnSelectBankAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectBankAccountDialogFragment selectBankAccountDialogFragment = new SelectBankAccountDialogFragment();
                selectBankAccountDialogFragment.setOnSelectListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bankAccount = Database.getBankAccounts().get(i);
                        mTxvSelectedBankAccount.setText(bankAccount.getName());
                    }
                });

                selectBankAccountDialogFragment.show(getFragmentManager(), "select_bank_account");
            }
        });

        mBtnSelectCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SelectSubcategoryDialogFragment selectSubcategoryDialogFragment = new SelectSubcategoryDialogFragment();
                selectSubcategoryDialogFragment.setOnSubcategorySelected(new PrimaryCategoryLightItemAdapter.SubcategorySelectionListener() {
                    @Override
                    public void onSubcategorySelected(Subcategory subcategory) {
                        CockpitFragment.this.subcategory = subcategory;
                        mTxvSelectedCategory.setText(subcategory.getName());
                        selectSubcategoryDialogFragment.dismiss();
                    }
                });
                selectSubcategoryDialogFragment.show(getFragmentManager(), "select_category");
            }
        });

        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTilBillAmount.setError(null);
                mTilBillAmount.setErrorEnabled(false);

                if(mEdtBillAmount.getText().toString().equals("")){
                    mTilBillDescription.setErrorEnabled(true);
                    mTilBillAmount.setError(getResources().getString(R.string.label_enter_amount));
                } else if(bankAccount == null){
                    mTxvSelectedBankAccount.setText(getResources().getString(R.string.label_need_to_select_bank_account));
                } else if(subcategory == null){
                    mTxvSelectedCategory.setText(getResources().getString(R.string.label_need_to_select_category));
                } else {
                    long amount = ((long) (Double.valueOf(mEdtBillAmount.getText().toString()) * 100));
                    String description = mEdtBillDescription.getText().toString();

                    int type;
                    if(mRgBillTypes.getCheckedRadioButtonId() == mRbTypeInput.getId()) {
                        type = Bill.TYPE_INPUT;
                    } else if(mRgBillTypes.getCheckedRadioButtonId() == mRbTypeOutput.getId()) {
                        type = Bill.TYPE_OUTPUT;
                    } else{
                        type = Bill.TYPE_TRANSFER;
                    }

                    bankAccount.addBill(new Bill(amount, description, subcategory));

                    //Show message
                    Snackbar snackbar = Snackbar.make(getView(), "Added successfully", Snackbar.LENGTH_SHORT);
                    snackbar.show();

                    //Clears all fields
                    mEdtBillAmount.setText("");
                    mEdtBillDescription.setText("");
                    mTxvSelectedCategory.setText("");
                    mTxvSelectedBankAccount.setText("");

                    bankAccount = null;
                    subcategory = null;

                    //Hides keyboard
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                }
            }
        });

        return inflatedView;
    }

}
