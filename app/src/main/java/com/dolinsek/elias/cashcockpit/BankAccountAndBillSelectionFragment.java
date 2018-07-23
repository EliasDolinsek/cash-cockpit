package com.dolinsek.elias.cashcockpit;


import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BankAccountAndBillSelectionFragment extends Fragment {

    private Spinner spnBankAccountSelection, spnBillSelection;
    private Button btnCreateBankAccount;
    private ImageView imvBillType;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_bank_account_and_bill_selection, container, false);;

        spnBankAccountSelection = inflatedView.findViewById(R.id.spn_bank_account_and_bill_selection_bank_account);
        spnBillSelection = inflatedView.findViewById(R.id.spn_bank_account_and_bill_selection_bill_type);
        btnCreateBankAccount = inflatedView.findViewById(R.id.btn_bank_account_and_bill_selection_click_to_create_bank_account);
        imvBillType = inflatedView.findViewById(R.id.imv_bank_account_and_bill_selection_bill);

        setupSpinnersStyles();
        setupBillTypeSelectionSpinner();
        setupBankAccountSelectionSpinner();

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Database.getBankAccounts().size() == 0){
            imvBillType.setVisibility(View.GONE);
            spnBankAccountSelection.setVisibility(View.GONE);
            spnBillSelection.setVisibility(View.GONE);
            btnCreateBankAccount.setVisibility(View.VISIBLE);
            btnCreateBankAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), BankAccountActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            btnCreateBankAccount.setVisibility(View.GONE);
            imvBillType.setVisibility(View.VISIBLE);
            spnBankAccountSelection.setVisibility(View.VISIBLE);
            spnBillSelection.setVisibility(View.VISIBLE);
        }
    }

    private void setupSpinnersStyles(){
        spnBankAccountSelection.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        spnBillSelection.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
    }

    private void setupBillTypeSelectionSpinner(){
        final ArrayAdapter<String> selectBillTypeAdapter = new ArrayAdapter<>(getContext(), R.layout.costum_spinner_layout, getResources().getStringArray(R.array.bill_types_array));
        selectBillTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBillSelection.setAdapter(selectBillTypeAdapter);
    }

    public void setupBillTypeSelectionSpinnerOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener){
        spnBillSelection.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void setBillTypeSelectionSpinnerSelection(int index){
        spnBillSelection.setSelection(index);
    }

    private void setupBankAccountSelectionSpinner(){
        final ArrayAdapter<CharSequence> selectBankAccountAdapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.costum_spinner_layout, getBankAccountsNames());
        selectBankAccountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnBankAccountSelection.setAdapter(selectBankAccountAdapter);
    }

     public void setupBankAccountSelectionSpinnerOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener){
        spnBankAccountSelection.setOnItemSelectedListener(onItemSelectedListener);
     }

     public void setBankAccountSelectionSpinnerSelection(int index){
        spnBankAccountSelection.setSelection(index);
     }

    private String[] getBankAccountsNames(){
        String[] bankAccountsNames = new String[Database.getBankAccounts().size()];

        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            bankAccountsNames[i] = Database.getBankAccounts().get(i).getName();
        }

        return bankAccountsNames;
    }

    public void hide(){
        getChildFragmentManager().beginTransaction().hide(this).commit();
    }

    public void show(){
        getChildFragmentManager().beginTransaction().show(this).commit();
    }

    public Spinner getSpnBankAccountSelection() {
        return spnBankAccountSelection;
    }

    public Spinner getSpnBillSelection() {
        return spnBillSelection;
    }

    public Button getBtnCreateBankAccount() {
        return btnCreateBankAccount;
    }
}
