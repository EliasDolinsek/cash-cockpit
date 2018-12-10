package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.support.design.chip.Chip;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;
import java.util.Calendar;

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

        String formattedAccountBalance = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithoutCentsWithCurrencySymbol(bankAccount.getBalance());
        String formattedBillsText = holder.itemView.getContext().getString(R.string.label_item_bank_account_bills, bankAccount.getBills().size());

        holder.mTxvName.setText(bankAccount.getName());

        holder.chipBalance.setText(formattedAccountBalance);
        holder.chipTrend.setText(getBalanceTrendInPercentAsString(bankAccount));
        holder.chipBills.setText(formattedBillsText);

        setupBankAccountStatusTxv(holder, bankAccount);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(holder.itemView.getContext(), BankAccountActivity.class);
            intent.putExtra(BankAccountActivity.EXTRA_BANK_ACCOUNT_INDEX, position);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mBankAccounts.size();
    }

    public class BankAccountItemViewHolder extends RecyclerView.ViewHolder{

        TextView mTxvName, txvPrimarySecondaryAccount;
        Chip chipBalance, chipTrend, chipBills;

        public BankAccountItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = itemView.findViewById(R.id.txv_item_bank_account_name);
            txvPrimarySecondaryAccount = itemView.findViewById(R.id.txv_item_bank_account_primary_secondary_account);

            chipBalance = itemView.findViewById(R.id.chip_item_bank_account_balance);
            chipTrend = itemView.findViewById(R.id.chip_item_bank_account_trend);
            chipBills = itemView.findViewById(R.id.chip_item_bank_account_bills);

        }
    }

    private String getBalanceTrendInPercentAsString(BankAccount bankAccount){
        int balanceTrend = getBalanceTrendInPercent(bankAccount);
        if (balanceTrend > 0){
            return "+" + balanceTrend + "%";
        } else if (balanceTrend == 0){
            return "\u00B1" + balanceTrend + "%";
        } else {
            return balanceTrend + "%";
        }
    }

    private int getBalanceTrendInPercent(BankAccount bankAccount){
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);

            long currentMonthBalance = Toolkit.getLastBalanceChangeOfBankAccountAndMonth(bankAccount, System.currentTimeMillis()).getNewBalance();
            long lastMonthBalance = Toolkit.getLastBalanceChangeOfBankAccountAndMonth(bankAccount, calendar.getTimeInMillis()).getNewBalance();

            if (lastMonthBalance < 0){
                lastMonthBalance = Math.abs(lastMonthBalance);
                currentMonthBalance += lastMonthBalance;
            }

            return (int) Math.round(100.0 / lastMonthBalance * currentMonthBalance) - 100;
        } catch (Exception e){
            return 0;
        }
    }

    private void setupBankAccountStatusTxv(BankAccountItemViewHolder holder, BankAccount bankAccount){
        Context context = holder.itemView.getContext();
        if (bankAccount.isPrimaryAccount()){
            holder.txvPrimarySecondaryAccount.setText(context.getString(R.string.label_primary_account));
            holder.txvPrimarySecondaryAccount.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
            holder.txvPrimarySecondaryAccount.setText(context.getString(R.string.label_secondary_account));
            holder.txvPrimarySecondaryAccount.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }
    }
}
