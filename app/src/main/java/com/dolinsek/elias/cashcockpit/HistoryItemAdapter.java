package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by elias on 22.02.2018.
 */

public class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.HistoryViewHolder> {

    public static final int FILTER_NEWEST_ITEM_FIRST = 0;
    public static final int FILTER_OLDEST_ITEM_FIRST = 1;
    public static final int FILTER_HIGHEST_PRICE_FIRST = 2;
    public static final int FILTER_LOWEST_PRICE_FIRST = 3;

    ArrayList<Bill> bills;
    private boolean editMode;

    public HistoryItemAdapter(int filter, BankAccount bankAccount, boolean editMode){
        bills = new ArrayList<>();
        this.editMode = editMode;

        if(filter == FILTER_NEWEST_ITEM_FIRST || filter == FILTER_OLDEST_ITEM_FIRST){
            ArrayList<Long> times = new ArrayList<>();
            if(bankAccount == null){
                for(int i = 0; i< Database.getBankAccounts().size(); i++){
                    for(Bill bill:Database.getBankAccounts().get(i).getBills())
                        times.add(bill.getCreationDate());
                }
            } else {
                for(Bill bill:bankAccount.getBills()){
                    times.add(bill.getCreationDate());
                }
            }

            long allTimes[] = new long[times.size()];
            for(int i = 0; i<times.size(); i++){
                allTimes[i] = (long)times.toArray()[i];
            }

            Arrays.sort(allTimes);
            if(filter == FILTER_NEWEST_ITEM_FIRST){
                for(int i = allTimes.length; i != 0; i--){
                    if(bankAccount == null){
                        for(BankAccount currentBankAccount:Database.getBankAccounts()){
                            for(Bill bill:currentBankAccount.getBills()){
                                if(bill.getCreationDate() == allTimes[i-1]){
                                    bills.add(bill);
                                }
                            }
                        }
                    } else {
                        for (Bill bill:bankAccount.getBills()){
                            if(bill.getCreationDate() == allTimes[i-1]){
                                bills.add(bill);
                            }
                        }
                    }
                }
            } else {
                for(int i = 0; i < allTimes.length; i++){
                    for(BankAccount currentBankAccount:Database.getBankAccounts()){
                        for(Bill bill:currentBankAccount.getBills()){
                            if(bill.getCreationDate() == allTimes[i]){
                                bills.add(bill);
                            }
                        }
                    }
                }
            }
        } else {
            if(bankAccount == null){
                for(BankAccount currentBankAccount:Database.getBankAccounts()){
                    for(Bill bill:currentBankAccount.getBills())
                        bills.add(bill);
                }
            } else {
                for(Bill bill:bankAccount.getBills())
                    bills.add(bill);
            }

            if(filter == FILTER_LOWEST_PRICE_FIRST){
                Collections.sort(bills, new Comparator<Bill>() {
                    @Override
                    public int compare(Bill bill, Bill t1) {
                        return Long.valueOf(bill.getAmount()).compareTo(t1.getAmount());
                    }
                });
            } else {
                Collections.sort(bills, new Comparator<Bill>() {
                    @Override
                    public int compare(Bill bill, Bill t1) {
                        return Long.valueOf(t1.getAmount()).compareTo(bill.getAmount());
                    }
                });
            }
        }

    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new HistoryViewHolder(layoutInflater.inflate(R.layout.list_item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder holder, int position) {
        final Bill bill = bills.get(position);
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

        holder.mTxvAmount.setText(Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(bill.getAmount()));
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

        if(editMode){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Gets position of bill and bank account in database
                    int billPosition = 0;
                    int bankAccountPosition = 0;
                    for(int i = 0; i<Database.getBankAccounts().size(); i++){
                        for(int y = 0; y<Database.getBankAccounts().get(i).getBills().size(); y++){
                            if(Database.getBankAccounts().get(i).getBills().get(y).equals(bill)){
                                billPosition = y;
                                bankAccountPosition = i;
                                break;
                            }
                        }
                    }

                    //Start BillEditorActivity
                    Intent intent = new Intent(holder.itemView.getContext(), BillEditorActivity.class);
                    intent.putExtra(BillEditorActivity.EXTRA_BILL_TO_EDIT, billPosition);
                    intent.putExtra(BillEditorActivity.EXTRA_BILL_TO_EDIT_BANK_ACCOUNT, bankAccountPosition);
                    holder.itemView.getContext().startActivity(intent);
                }
            });
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
