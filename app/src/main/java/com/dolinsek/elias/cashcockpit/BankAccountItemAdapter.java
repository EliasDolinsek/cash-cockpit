package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
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
        mBankAccounts = Database.getBankAccounts();
    }

    @Override
    public BankAccountItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BankAccountItemViewHolder(inflater.inflate(R.layout.list_item_bank_account, parent, false));
    }

    @Override
    public void onBindViewHolder(final BankAccountItemViewHolder holder, final int position) {
        BankAccount bankAccount = mBankAccounts.get(position);

        holder.mTxvName.setText(bankAccount.getName());
        holder.mTxvDetails.setText(String.valueOf(bankAccount.getBills().size()) + " " + holder.itemView.getContext().getResources().getString(R.string.label_bills));

        String formattedBalance = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(bankAccount.getBalance());
        holder.mTxvBalance.setText(formattedBalance);

        if(!bankAccount.isPrimaryAccount()){
            holder.mTxvPrimaryAccount.setVisibility(View.GONE);
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

        public TextView mTxvName, mTxvDetails, mTxvPrimaryAccount, mTxvBalance;
        public CardView mCardView;

        public BankAccountItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_bank_account_name);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_bank_account_details);
            mTxvPrimaryAccount = (TextView) itemView.findViewById(R.id.txv_item_bank_account_primary_account);
            mTxvBalance = (TextView) itemView.findViewById(R.id.txv_item_bank_account_balance);

            mCardView = (CardView) itemView.findViewById(R.id.cv_item_bank_account);
        }
    }
}
