package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        final BankAccount bankAccountOfBill = getBankAccountOfBill(bill);

        setupTxvBackgroundFromBillType(holder.mTxvAmount, bill);
        holder.mTxvAmount.setText(Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(bill.getAmount()));
        displayDescription(bill.getDescription(), holder);

        String billDetails = bankAccountOfBill.getName() + " " + Character.toString((char)0x00B7) + " " + bill.getSubcategory().getName();
        holder.mTxvDetails.setText(billDetails);

        String dateOfCreationDate = DateFormat.format("EEEE dd.MM", bill.getCreationDate()).toString();
        holder.mTxvDate.setText(dateOfCreationDate);

        if(allowToEditBill){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEditDialogForSubcategory(bill, bankAccountOfBill, holder.itemView.getContext());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return billsToDisplay.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvDate, mTxvAmount, mTxvDescription, mTxvDetails;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            mTxvDate = (TextView) itemView.findViewById(R.id.txv_item_history_date);
            mTxvAmount = (TextView) itemView.findViewById(R.id.txv_item_history_amount);
            mTxvDescription = (TextView) itemView.findViewById(R.id.txv_item_history_description);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_history_details);
        }
    }

    private void displayDescription(String description, HistoryViewHolder holder){
        TextView txvDescription = holder.mTxvDescription;
        if (description.equals("")){
            txvDescription.setText(R.string.label_no_description);
            txvDescription.setTypeface(txvDescription.getTypeface(), Typeface.ITALIC);
        } else {
            txvDescription.setText(description);
            txvDescription.setTypeface(Typeface.DEFAULT); //To avoid problems
        }
    }

    private void showEditDialogForSubcategory(Bill bill, BankAccount bankAccountOfBill, Context context){
        int bankAccountIndex = getIndexOfBankAccountInDatabase(bankAccountOfBill);
        int billPosition = getIndexOfBillInBankAccount(bill, bankAccountOfBill);

        Intent intent = new Intent(context, BillEditorActivity.class);
        intent.putExtra(BillEditorActivity.EXTRA_BILL_TO_EDIT, billPosition);
        intent.putExtra(BillEditorActivity.EXTRA_BILL_TO_EDIT_BANK_ACCOUNT, bankAccountIndex);
        context.startActivity(intent);
    }

    private void setupTxvBackgroundFromBillType(TextView textView, Bill bill){
        Context context = textView.getContext();
        if (bill.getType() == Bill.TYPE_INPUT){
            textView.setBackground(context.getDrawable(R.drawable.border_green));
        } else if (bill.getType() == Bill.TYPE_OUTPUT){
            textView.setBackground(context.getDrawable(R.drawable.border_red));
        } else if (bill.getType() == Bill.TYPE_TRANSFER){
            textView.setBackground(context.getDrawable(R.drawable.border_orange));
        } else {
            throw new IllegalArgumentException("Couldn't resolve " + bill.getType() + " as a bill type!");
        }

    }

    private int getIndexOfBankAccountInDatabase(BankAccount bankAccount){
        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            if (Database.getBankAccounts().get(i).equals(bankAccount)){
                return i;
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account in database!");
    }

    private int getIndexOfBillInBankAccount(Bill bill, BankAccount bankAccount){
        for (int i = 0; i<bankAccount.getBills().size(); i++){
            if (bill.equals(bankAccount.getBills().get(i))){
                return i;
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account of bill!");
    }

    private BankAccount getBankAccountOfBill(Bill bill){
        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill currentBill:bankAccount.getBills()){
                if (currentBill.equals(bill)){
                    return bankAccount;
                }
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account of bill!");
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
