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

import com.dolinsek.elias.cashcockpit.components.Database;


/**
 * A simple {@link Fragment} subclass.
 */
public class BankAccountsFragment extends Fragment {

    /**
     * Displays a list of all bank accounts
     */
    private RecyclerView mRecyclerView;

    /**
     * Adapter for bank accounts
     */
    private BankAccountItemAdapter mBankAccountItemAdapter;

    /**
     * Button to create a new bank account. This Button get hidden when there aren't zero bank accounts
     */
    private Button mBtnCreateAccount;

    /**
     * FloatingActionButton to create a new bank account
     */
    private FloatingActionButton mFbtnAdd;

    public BankAccountsFragment() {
        mBankAccountItemAdapter = new BankAccountItemAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View inflatedView = inflater.inflate(R.layout.fragment_bank_accounts, container, false);

        //Setup RecyclerView
        mRecyclerView = (RecyclerView) inflatedView.findViewById(R.id.rv_bank_accounts);
        mRecyclerView.setAdapter(mBankAccountItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Setup FloatingActionButton
        mFbtnAdd = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_bank_accounts_add);
        mFbtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start CreateBankAccountsActivity
                Intent intent = new Intent(inflatedView.getContext(), BankAccountActivity.class);
                startActivity(intent);
            }
        });

        //Setup Button
        mBtnCreateAccount = (Button) inflatedView.findViewById(R.id.btn_bank_accounts_create);
        mBtnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start CreateBankAccountsActivity
                Intent intent = new Intent(inflatedView.getContext(), BankAccountActivity.class);
                startActivity(intent);
            }
        });

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Hides mFbtnAdd when there are zero bank accounts and hides mBtnCreateAccount if not
        if(Database.getBankAccounts().size() != 0){
            mBtnCreateAccount.setVisibility(View.GONE);
            mFbtnAdd.setVisibility(View.VISIBLE);
        } else
            mFbtnAdd.setVisibility(View.GONE);

        //Load bank accounts
        mBankAccountItemAdapter = new BankAccountItemAdapter();
        mRecyclerView.setAdapter(mBankAccountItemAdapter);
    }
}
