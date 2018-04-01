package com.dolinsek.elias.cashcockpit;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;


public class BankAccountsFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private BankAccountItemAdapter mBankAccountItemAdapter;
    private Button mBtnCreateAccount;
    private FloatingActionButton mFbtnAdd;

    public BankAccountsFragment() {
        ArrayList<BankAccount> bankAccounts = Database.getBankAccounts();
        mBankAccountItemAdapter = new BankAccountItemAdapter(bankAccounts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View inflatedView = inflater.inflate(R.layout.fragment_bank_accounts, container, false);

        mRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.rv_bank_accounts);
        mRecyclerView.setAdapter(mBankAccountItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mFbtnAdd = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_bank_accounts_add);
        mFbtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(inflatedView.getContext(), BankAccountActivity.class);
                startActivity(intent);
            }
        });

        mBtnCreateAccount = (Button) inflatedView.findViewById(R.id.btn_bank_accounts_create);
        mBtnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(inflatedView.getContext(), BankAccountActivity.class);
                startActivity(intent);
            }
        });

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(Database.getBankAccounts().size() != 0){
            mBtnCreateAccount.setVisibility(View.GONE);
            mFbtnAdd.setVisibility(View.VISIBLE);
        } else{
            mFbtnAdd.setVisibility(View.GONE);
        }

        loadBankAccountItemAdapter();
    }

    private void loadBankAccountItemAdapter(){
        ArrayList<BankAccount> bankAccounts = Database.getBankAccounts();
        mBankAccountItemAdapter = new BankAccountItemAdapter(bankAccounts);
        mRecyclerView.setAdapter(mBankAccountItemAdapter);
    }
}
