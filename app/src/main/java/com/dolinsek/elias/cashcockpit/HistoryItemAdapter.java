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

    private ArrayList<Bill> billsToDisplay;
    private boolean allowToEditBill;
    private int filterType;

    public static HistoryItemAdapter getDefaultHistoryItemAdapter(ArrayList<Bill> billsToDisplay, int filterType){
        HistoryItemAdapter historyItemAdapter = new HistoryItemAdapter();
        historyItemAdapter.filterType = filterType;
        historyItemAdapter.billsToDisplay = billsToDisplay;
        historyItemAdapter.allowToEditBill = true;
        historyItemAdapter.filterBills(historyItemAdapter.filterType);

        return historyItemAdapter;
    }

    public static HistoryItemAdapter getBankAccountHistoryItemAdapter(BankAccount bankAccount){
        HistoryItemAdapter historyItemAdapter = new HistoryItemAdapter();
        historyItemAdapter.filterType = FILTER_NEWEST_ITEM_FIRST;
        historyItemAdapter.billsToDisplay = bankAccount.getBills();
        historyItemAdapter.allowToEditBill = false;
        historyItemAdapter.filterBills(historyItemAdapter.filterType);

        return historyItemAdapter;
    }

    @Override
    public HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new HistoryViewHolder(layoutInflater.inflate(R.layout.list_item_history, parent, false));
    }

    @Override
    public void onBindViewHolder(final HistoryViewHolder holder, int position) {
        final Bill bill = billsToDisplay.get(position);
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

        if(allowToEditBill){
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
        return billsToDisplay.size();
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

    private void filterBills(int filterType){
        if (filterType == FILTER_NEWEST_ITEM_FIRST){
            filterBillsNewestFirst(billsToDisplay);
        } else if (filterType == FILTER_OLDEST_ITEM_FIRST){
            filterBillsOldestFirst(billsToDisplay);
        } else if (filterType == FILTER_HIGHEST_PRICE_FIRST){
            filterBillsHighestPriceFirst(billsToDisplay);
        } else if (filterType == FILTER_LOWEST_PRICE_FIRST){
            filterBillsLowestPriceFirst(billsToDisplay);
        } else {
            throw new IllegalArgumentException("Couldn't resolve " + filterType + " as a filter type");
        }
    }

    private void filterBillsNewestFirst(ArrayList<Bill> billsToFilter){
        Collections.sort(billsToFilter, new Comparator<Bill>() {
            @Override
            public int compare(Bill bill, Bill t1) {
                return Long.compare(t1.getCreationDate(), bill.getCreationDate());
            }
        });
    }

    private void filterBillsOldestFirst(ArrayList<Bill> billsToFilter){
        Collections.sort(billsToFilter, new Comparator<Bill>() {
            @Override
            public int compare(Bill bill, Bill t1) {
                return Long.compare(bill.getCreationDate(), t1.getCreationDate());
            }
        });
    }

    private void filterBillsHighestPriceFirst(ArrayList<Bill> billsToFilter){
        Collections.sort(billsToFilter, new Comparator<Bill>() {
            @Override
            public int compare(Bill bill, Bill t1) {
                return Long.compare(t1.getAmount(), bill.getAmount());
            }
        });
    }

    private void filterBillsLowestPriceFirst(ArrayList<Bill> billsToFilter){
        Collections.sort(billsToFilter, new Comparator<Bill>() {
            @Override
            public int compare(Bill bill, Bill t1) {
                return Long.compare(bill.getAmount(), t1.getAmount());
            }
        });
    }
}
