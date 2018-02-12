package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Currencies;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;

/**
 * Created by elias on 13.01.2018.
 */

public class BankAccountItemAdapter extends RecyclerView.Adapter<BankAccountItemAdapter.BankAccountItemViewHolder>{

    /**
     * Contains bank accounts to display
     */
    ArrayList<BankAccount> mBankAccounts;

    public BankAccountItemAdapter(){
        //Gets bank accounts from database
        mBankAccounts = Database.getBankAccounts();

        //Sorts the primary bank account to the first position
        for(int i = 0; i<mBankAccounts.size(); i++){
            if(mBankAccounts.get(i).isPrimaryAccount()){
                BankAccount primaryBankAccount = mBankAccounts.get(i);
                mBankAccounts.remove(i);
                mBankAccounts.add(0, primaryBankAccount);
            }
        }
    }

    @Override
    public BankAccountItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BankAccountItemViewHolder(inflater.inflate(R.layout.list_item_bank_account, parent, false));
    }

    @Override
    public void onBindViewHolder(final BankAccountItemViewHolder holder, final int position) {
        BankAccount bankAccount = mBankAccounts.get(position);

        //Display data
        holder.mTxvName.setText(bankAccount.getName());
        holder.mTxvDetails.setText(Currencies.EURO.getCurrency().format(bankAccount.getBalance()) + " " + Character.toString((char)0x00B7) + " " + String.valueOf(bankAccount.getBills().size()) + " " + holder.itemView.getContext().getResources().getString(R.string.label_bills));

        //Displays if the current bank account is the primary bank account
        if(bankAccount.isPrimaryAccount()){
            holder.mTxvDetails.append(" " + Character.toString((char)0x00B7) + " ");
            holder.mTxvPrimaryAccount.setText(holder.itemView.getContext().getResources().getString(R.string.label_primary_account));
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start CreateBankAccountActivity to edit the clicked bank account
                Intent intent = new Intent(holder.itemView.getContext(), BankAccountActivity.class);
                intent.putExtra(BankAccountActivity.EXTRA_BANK_ACCOUNT_INDEX, position);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBankAccounts.size();
    }

    public class BankAccountItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvName, mTxvDetails, mTxvPrimaryAccount;
        public CardView mCardView;

        public BankAccountItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_bank_account_name);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_bank_account_details);
            mTxvPrimaryAccount = (TextView) itemView.findViewById(R.id.txv_item_bank_account_primary_account);

            mCardView = (CardView) itemView.findViewById(R.id.cv_item_bank_account);
        }
    }
}
