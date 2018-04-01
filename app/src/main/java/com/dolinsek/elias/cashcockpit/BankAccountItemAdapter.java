package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;


public class BankAccountItemAdapter extends RecyclerView.Adapter<BankAccountItemAdapter.BankAccountItemViewHolder>{

    ArrayList<BankAccount> mBankAccounts;

    public BankAccountItemAdapter(ArrayList<BankAccount> bankAccountsToDisplay){
        mBankAccounts = bankAccountsToDisplay;
    }

    @Override
    public BankAccountItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BankAccountItemViewHolder(inflater.inflate(R.layout.list_item_bank_account, parent, false));
    }

    @Override
    public void onBindViewHolder(final BankAccountItemViewHolder holder, final int position) {
        BankAccount bankAccount = mBankAccounts.get(position);

        displayData(bankAccount, holder);

        if(bankAccount.isPrimaryAccount()){
            markAsPrimaryAccount(holder);
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    private void displayData(BankAccount bankAccount, BankAccountItemViewHolder holder){
        holder.mTxvName.setText(bankAccount.getName());

        String formattedBalance = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(bankAccount.getBalance());
        String numberOfBillsOfBankAccount = String.valueOf(bankAccount.getBills().size());
        String labelBills = holder.itemView.getContext().getString(R.string.label_bills);

        String detailsToDisplay = formattedBalance + " " + Character.toString((char)0x00B7) + " " + numberOfBillsOfBankAccount + " " +  labelBills;
        holder.mTxvDetails.setText(detailsToDisplay);
    }

    private void markAsPrimaryAccount(BankAccountItemViewHolder holder){
        holder.mTxvDetails.append(" " + Character.toString((char)0x00B7) + " ");
        holder.mTxvPrimaryAccount.setText(holder.itemView.getContext().getResources().getString(R.string.label_primary_account));
    }
}
