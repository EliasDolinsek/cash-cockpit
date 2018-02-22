package com.dolinsek.elias.cashcockpit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by elias on 22.02.2018.
 */

public class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.HistoryViewHolder> {

    ArrayList<Bill> bills;

    public HistoryItemAdapter(){
        bills = new ArrayList<>();

        ArrayList<Long> times = new ArrayList<>();
        for(int i = 0; i< Database.getBankAccounts().size(); i++){
            for(Bill bill:Database.getBankAccounts().get(i).getBills())
                times.add(bill.getCreationDate());
        }

        long allTimes[] = new long[times.size()];
        for(int i = 0; i<times.size(); i++){
            allTimes[i] = (long)times.toArray()[i];
        }

        Arrays.sort(allTimes);
        for(int i = allTimes.length; i != 0; i--){
            for(BankAccount bankAccount:Database.getBankAccounts()){
                for(Bill bill:bankAccount.getBills()){
                    if(bill.getCreationDate() == allTimes[i-1]){
                        bills.add(bill);
                    }
                }
            }
        }

    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new HistoryViewHolder(layoutInflater.inflate(R.layout.list_item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(HistoryViewHolder holder, int position) {
        Bill bill = bills.get(position);
        System.out.println(bill.getType());
        if(bill.getType() == Bill.TYPE_INPUT){
            holder.mTxvTagOutput.setVisibility(View.GONE);
            holder.mTxvTagTransfer.setVisibility(View.GONE);
        } else if(bill.getType() == Bill.TYPE_OUTPUT){
            holder.mTxvTagInput.setVisibility(View.GONE);
            holder.mTxvTagTransfer.setVisibility(View.GONE);
        } else {
            holder.mTxvTagInput.setVisibility(View.GONE);
            holder.mTxvTagOutput.setVisibility(View.GONE);
        }

        holder.mTxvAmount.setText(Currency.Factory.getActiveCurrency(holder.itemView.getContext()).formatAmountToString(bill.getAmount()));
        holder.mTxvCategory.setText(bill.getSubcategory().getName());

        //Gets bank account-name
        String bankAccountName = null;
        for(BankAccount bankAccount:Database.getBankAccounts()){
            for(PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
                if(bill.getSubcategory().getPrimaryCategory().equals(primaryCategory))
                    bankAccountName = bankAccount.getName();
            }
        }

        if(bill.getDescription().equals("")){
            holder.mTxvDetails.setText(new SimpleDateFormat("dd.MM HH:mm").format(new Date(bill.getCreationDate())));
        } else {
            holder.mTxvDetails.setText(new SimpleDateFormat("dd.MM HH:mm").format(new Date(bill.getCreationDate())) + " " + Character.toString((char)0x00B7) + " " + bill.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvTagInput, mTxvTagOutput, mTxvTagTransfer, mTxvAmount, mTxvCategory, mTxvDetails;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            mTxvTagInput = (TextView) itemView.findViewById(R.id.txv_item_history_input);
            mTxvTagOutput = (TextView) itemView.findViewById(R.id.txv_item_history_output);
            mTxvTagTransfer = (TextView) itemView.findViewById(R.id.txv_item_history_transfer);
            mTxvAmount = (TextView) itemView.findViewById(R.id.txv_item_history_amount);
            mTxvCategory = (TextView) itemView.findViewById(R.id.txv_item_history_category);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_history_details);
        }
    }
}
