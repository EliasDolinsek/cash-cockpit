package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.model.BankAccount;
import com.dolinsek.elias.cashcockpit.model.Currencies;
import com.dolinsek.elias.cashcockpit.model.Database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by elias on 13.01.2018.
 */

public class BankaccountItemAdapter extends RecyclerView.Adapter<BankaccountItemAdapter.BankaccountItemViewHolder>{

    ArrayList<BankAccount> mBankAccounts;

    public BankaccountItemAdapter(){
        mBankAccounts = Database.getBankAccounts();
        for(int i = 0; i<mBankAccounts.size(); i++){
            if(mBankAccounts.get(i).isPrimaryAccount()){
                BankAccount primaryBankAccount = mBankAccounts.get(i);
                mBankAccounts.remove(i);
                mBankAccounts.add(0, primaryBankAccount);
            }
        }
    }

    @Override
    public BankaccountItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BankaccountItemViewHolder(inflater.inflate(R.layout.list_item_bank_account, parent, false));
    }

    @Override
    public void onBindViewHolder(final BankaccountItemViewHolder holder, final int position) {
        BankAccount bankAccount = mBankAccounts.get(position);

        holder.mTxvName.setText(bankAccount.getName());
        holder.mTxvBalance.append(": " + Currencies.EURO.getCurrency().format(bankAccount.getBalance()));

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start CreateBankAccountActivity to edit the clicked bank account
                Intent intent = new Intent(holder.itemView.getContext(), BankAccountActivity.class);
                intent.putExtra(BankAccountActivity.EXTRA_BANK_ACCOUNT_INDEX, position);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        if(!bankAccount.isPrimaryAccount())
            holder.mTxvPrimaryAccount.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mBankAccounts.size();
    }

    public class BankaccountItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvName, mTxvBalance, mTxvPrimaryAccount;
        public CardView mCardView;

        public BankaccountItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_bank_account_name);
            mTxvBalance = (TextView) itemView.findViewById(R.id.txv_item_bank_account_balance);
            mTxvPrimaryAccount = (TextView) itemView.findViewById(R.id.txv_item_bank_account_primary_account);

            mCardView = (CardView) itemView.findViewById(R.id.cv_item_bank_account);
        }
    }
}
