package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Toolbox;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

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
        historyItemAdapter.cleanUpBillsToDisplay();

        return historyItemAdapter;
    }

    public static HistoryItemAdapter getBankAccountHistoryItemAdapter(BankAccount bankAccount){
        HistoryItemAdapter historyItemAdapter = new HistoryItemAdapter();
        historyItemAdapter.filterType = FILTER_NEWEST_ITEM_FIRST;
        historyItemAdapter.billsToDisplay = bankAccount.getBills();
        historyItemAdapter.allowToEditBill = false;
        historyItemAdapter.filterBills(historyItemAdapter.filterType);
        historyItemAdapter.cleanUpBillsToDisplay();

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

        displayBillType(bill, holder);
        displayDescription(bill.getDescription(), holder);
        displayDateAndAmount(bill, holder);
        displayCategoryImage(bill, holder);

        setupBillActionButtonsClickListeners(bill, holder);
        if(allowToEditBill){
            holder.itemView.setOnClickListener(view -> showHideBillActionButtons(holder));
        }
    }

    @Override
    public int getItemCount() {
        return billsToDisplay.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{

        public View mViewDivider;
        public ImageView mImvPrimaryCategory;
        public LinearLayout mLlBillActionButtonsContainer;
        public TextView mTxvBillType, mTxvDescription, mTxvDateAmount;
        public Button btnEdit, btnDelete;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            mViewDivider = itemView.findViewById(R.id.view_item_history_divider);
            mImvPrimaryCategory = itemView.findViewById(R.id.imv_item_history_category);
            mLlBillActionButtonsContainer = itemView.findViewById(R.id.ll_item_history_bill_action_buttons_container);

            mTxvBillType = itemView.findViewById(R.id.txv_item_history_bill_type);
            mTxvDescription = itemView.findViewById(R.id.txv_item_history_description);
            mTxvDateAmount = itemView.findViewById(R.id.txv_item_history_date_amount);

            btnEdit = itemView.findViewById(R.id.btn_item_history_edit);
            btnDelete = itemView.findViewById(R.id.btn_item_history_delete);
        }
    }

    private void cleanUpBillsToDisplay(){
        ArrayList<Bill> allBillsInDatabase = Toolkit.getAllBills();
        ArrayList<Bill> cleanedUpBillsToDisplay = new ArrayList<>();
        for (Bill bill:billsToDisplay){
            if (allBillsInDatabase.contains(bill)){
                cleanedUpBillsToDisplay.add(bill);
            }
        }

        billsToDisplay = cleanedUpBillsToDisplay;
    }

    private void displayBillType(Bill bill, HistoryViewHolder holder){
        Context context = holder.itemView.getContext();

        if (bill.isAutoPayBill()){
            holder.mTxvBillType.setText(context.getString(R.string.label_auto_pay) + "");
        } else {
            holder.mTxvBillType.setText("");
        }

        if (bill.getType() == Bill.TYPE_INPUT){
            holder.mTxvBillType.setText(context.getString(R.string.label_input));
            holder.mTxvBillType.setTextColor(context.getResources().getColor(R.color.colorBillTypeInput));
        } else if (bill.getType() == Bill.TYPE_OUTPUT){
            holder.mTxvBillType.setText(context.getString(R.string.label_output));
            holder.mTxvBillType.setTextColor(context.getResources().getColor(R.color.colorBillTypeOutput));
        } else {
            holder.mTxvBillType.setText(context.getString(R.string.label_transfer));
            holder.mTxvBillType.setTextColor(context.getResources().getColor(R.color.colorBillTypeTransfer));
        }
    }
    private void displayDescription(String description, HistoryViewHolder holder){
        TextView txvDescription = holder.mTxvDescription;
        if (description.equals("")){
            txvDescription.setText(R.string.label_no_description);
        } else {
            txvDescription.setText(description);
        }
    }

    private void displayDateAndAmount(Bill bill, HistoryViewHolder holder){
        String formattedAmount = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(bill.getAmount());
        String dateOfCreationDate = (String) DateUtils.getRelativeTimeSpanString(bill.getCreationDate());

        String dateAndAmount = dateOfCreationDate + " " + Character.toString((char)0x00B7) + " " + formattedAmount;
        holder.mTxvDateAmount.setText(dateAndAmount);
    }

    private void displayCategoryImage(Bill bill, HistoryViewHolder holder){
        Context context = holder.itemView.getContext();
        PrimaryCategory primaryCategory = bill.getSubcategory().getPrimaryCategory();

        int imageResource = Toolbox.getPrimaryCategoryIconResourceByName(context, primaryCategory);
        holder.mImvPrimaryCategory.setBackgroundResource(imageResource);
    }

    private void setupBillActionButtonsClickListeners(Bill bill, HistoryViewHolder holder) {
        holder.btnEdit.setOnClickListener(view -> showEditDialogForSubcategory(bill, getBankAccountOfBill(bill), holder.itemView.getContext()));
    }

    private void showHideBillActionButtons(HistoryViewHolder holder) {
        if (holder.mLlBillActionButtonsContainer.isShown()){
            hideBillActionButtons(holder);
        } else {
            showBillActionButtons(holder);
        }
    }

    private void showBillActionButtons(HistoryViewHolder holder){
        LinearLayout llBillActionButtonsContainer = holder.mLlBillActionButtonsContainer;
        llBillActionButtonsContainer.setVisibility(View.VISIBLE);

        TranslateAnimation billActionButtonsAnimation = new TranslateAnimation(0, 0, -llBillActionButtonsContainer.getHeight(),0);
        billActionButtonsAnimation.setDuration(200);
        billActionButtonsAnimation.setFillAfter(false);

        TranslateAnimation dividerAnimation = new TranslateAnimation(0,0, -llBillActionButtonsContainer.getHeight(), 0);
        dividerAnimation.setDuration(200);
        dividerAnimation.setFillAfter(false);

        llBillActionButtonsContainer.startAnimation(billActionButtonsAnimation);
        holder.mViewDivider.startAnimation(dividerAnimation);
    }

    private void hideBillActionButtons(HistoryViewHolder holder){
        LinearLayout llBillActionButtonsContainer = holder.mLlBillActionButtonsContainer;

        TranslateAnimation dividerAnimation = new TranslateAnimation(0,0, 0, -llBillActionButtonsContainer.getHeight());
        dividerAnimation.setDuration(200);
        dividerAnimation.setFillAfter(false);

        TranslateAnimation billActionButtonsAnimation = new TranslateAnimation(0,0, 0, -llBillActionButtonsContainer.getHeight());
        billActionButtonsAnimation.setDuration(300);
        billActionButtonsAnimation.setFillAfter(true);
        billActionButtonsAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                holder.mViewDivider.startAnimation(dividerAnimation);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                llBillActionButtonsContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        llBillActionButtonsContainer.startAnimation(billActionButtonsAnimation);
    }

    private void showEditDialogForSubcategory(Bill bill, BankAccount bankAccountOfBill, Context context){
        int bankAccountIndex = getIndexOfBankAccountInDatabase(bankAccountOfBill);
        int billPosition = getIndexOfBillInBankAccount(bill, bankAccountOfBill);

        Intent intent = new Intent(context, BillEditorActivity.class);
        intent.putExtra(BillEditorActivity.EXTRA_BILL_TO_EDIT, billPosition);
        intent.putExtra(BillEditorActivity.EXTRA_BILL_TO_EDIT_BANK_ACCOUNT, bankAccountIndex);
        context.startActivity(intent);
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
        Collections.sort(billsToFilter, (bill, t1) -> Long.compare(t1.getCreationDate(), bill.getCreationDate()));
    }

    private void filterBillsOldestFirst(ArrayList<Bill> billsToFilter){
        Collections.sort(billsToFilter, (bill, t1) -> Long.compare(bill.getCreationDate(), t1.getCreationDate()));
    }

    private void filterBillsHighestPriceFirst(ArrayList<Bill> billsToFilter){
        Collections.sort(billsToFilter, (bill, t1) -> Long.compare(t1.getAmount(), bill.getAmount()));
    }

    private void filterBillsLowestPriceFirst(ArrayList<Bill> billsToFilter){
        Collections.sort(billsToFilter, (bill, t1) -> Long.compare(bill.getAmount(), t1.getAmount()));
    }
}
