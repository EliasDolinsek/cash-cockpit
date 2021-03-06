package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Toolbox;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by elias on 22.02.2018.
 */

public class HistoryItemAdapter extends RecyclerView.Adapter<HistoryItemAdapter.HistoryViewHolder> {

    public static final int FILTER_NEWEST_ITEM_FIRST = 0;
    public static final int FILTER_OLDEST_ITEM_FIRST = 1;
    public static final int FILTER_HIGHEST_PRICE_FIRST = 2;
    public static final int FILTER_LOWEST_PRICE_FIRST = 3;

    private static final int EDIT_TYPE_DESCRIPTION = 745;
    private static final int EDIT_TYPE_AMOUNT = 578;

    private ArrayList<Bill> billsToDisplay;
    private boolean allowToEditBill;
    private int filterType, expandedPosition = -1, editPosition = -1, editType;
    private RecyclerView recyclerView;

    public static HistoryItemAdapter getDefaultHistoryItemAdapter(RecyclerView recyclerView, ArrayList<Bill> billsToDisplay, int filterType){
        HistoryItemAdapter historyItemAdapter = new HistoryItemAdapter();
        historyItemAdapter.recyclerView = recyclerView;
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

        setupBillActionButtonsClickListeners(holder, bill);
        if(allowToEditBill && recyclerView != null){
            setupOnItemClickAction(holder, position);
        }

        if (editPosition == position && editType == EDIT_TYPE_DESCRIPTION){
            showKeyboardForEditInput(holder.mEdtEdit);
            setupEdtEditForDescriptionEdit(holder, position);
        } else if (editPosition == position && editType == EDIT_TYPE_AMOUNT){
            showKeyboardForEditInput(holder.mEdtEdit);
            setupEdtEditForAmountEdit(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return billsToDisplay.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{

        public View mViewDivider;
        public ImageView mImvPrimaryCategory;
        public LinearLayout mLlBillActionButtonsContainer, mLlBillEditElementsContainer;
        public TextView mTxvBillType, mTxvDescription, mTxvDateAmount;
        public Button btnEdit, btnDelete, btnDuplicate, btnEditAmount, btnEditDescription;
        private EditText mEdtEdit;

        public HistoryViewHolder(View itemView) {
            super(itemView);

            mViewDivider = itemView.findViewById(R.id.view_item_history_divider);
            mImvPrimaryCategory = itemView.findViewById(R.id.imv_item_history_category);

            mLlBillActionButtonsContainer = itemView.findViewById(R.id.ll_item_history_bill_action_buttons_container);
            mLlBillEditElementsContainer = itemView.findViewById(R.id.ll_item_history_bill_edit_elements_container);

            mTxvBillType = itemView.findViewById(R.id.txv_item_history_bill_type);
            mTxvDescription = itemView.findViewById(R.id.txv_item_history_description);
            mTxvDateAmount = itemView.findViewById(R.id.txv_item_history_date_amount);

            btnEdit = itemView.findViewById(R.id.btn_item_history_edit);
            btnDelete = itemView.findViewById(R.id.btn_item_history_delete);
            btnDuplicate = itemView.findViewById(R.id.btn_item_history_duplicate);
            btnEditAmount = itemView.findViewById(R.id.btn_item_history_edit_amount);
            btnEditDescription = itemView.findViewById(R.id.btn_item_history_edit_description);
            mEdtEdit = itemView.findViewById(R.id.edt_item_history_edit);
        }
    }

    public void setupStates(int expandedPosition, int editPosition, int editType){
        this.expandedPosition = expandedPosition;
        this.editPosition = editPosition;
        this.editType = editType;
    }

    public int getExpandedPosition() {
        return expandedPosition;
    }

    public int getEditPosition() {
        return editPosition;
    }

    public int getEditType() {
        return editType;
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
            holder.mTxvBillType.setText(context.getString(R.string.label_auto_pay));
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

    private void setupBillActionButtonsClickListeners(HistoryViewHolder holder, Bill bill) {
        holder.btnEdit.setOnClickListener(view -> showEditDialogForSubcategory(bill, getBankAccountOfBill(bill), holder.itemView.getContext()));
        holder.btnDelete.setOnClickListener(view -> showDeleteBillDialogFragment(holder, bill));
        holder.btnDuplicate.setOnClickListener(view -> duplicateBill(holder, bill));
        holder.btnEditAmount.setOnClickListener(view -> setupForAmountEdit());
        holder.btnEditDescription.setOnClickListener(view -> setupForDescriptionEdit());
    }

    private void setupOnItemClickAction(HistoryViewHolder holder, int position){
        final boolean isExpanded = position == expandedPosition;
        final boolean isEditModeActive = position == editPosition;

        holder.mLlBillActionButtonsContainer.setVisibility(isExpanded && !isEditModeActive ? View.VISIBLE : View.GONE);
        holder.mLlBillEditElementsContainer.setVisibility(isExpanded && isEditModeActive ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded || isEditModeActive);

        holder.itemView.setOnClickListener(v -> {
            int previousExpandedPosition = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;

            if (!isEditModeActive){
                editPosition = -1;
            }

            if (previousExpandedPosition != -1){
                notifyItemChanged(previousExpandedPosition);
            }

            if (expandedPosition != -1){
                notifyItemChanged(expandedPosition);
            }
        });
    }

    private void setupForDescriptionEdit(){
        editPosition = expandedPosition;
        editType = EDIT_TYPE_DESCRIPTION;
        expand();
    }

    private void setupForAmountEdit(){
        editPosition = expandedPosition;
        editType = EDIT_TYPE_AMOUNT;
        expand();
    }

    private void setupEdtEditForDescriptionEdit(HistoryViewHolder holder, int position){
        Bill currentBill = billsToDisplay.get(position);

        holder.mEdtEdit.setText(currentBill.getDescription());
        holder.mEdtEdit.setSelection(holder.mEdtEdit.length());
        holder.mEdtEdit.setOnEditorActionListener((textView, i, keyEvent) -> {
            String enteredDescription = holder.mEdtEdit.getText().toString();
            currentBill.setDescription(enteredDescription);

            expand();
            Database.save(holder.itemView.getContext());

            editPosition = -1;
            editType = 0;

            return true;
        });
    }

    private void setupEdtEditForAmountEdit(HistoryViewHolder holder, int position){
        Bill currentBill = billsToDisplay.get(position);
        Currency activeCurrency = Currency.getActiveCurrency(holder.itemView.getContext());
        String formattedBillAmount = activeCurrency.formatAmountToReadableStringWithCurrencySymbol(currentBill.getAmount());

        holder.mEdtEdit.setText(formattedBillAmount);
        holder.mEdtEdit.setSelection(holder.mEdtEdit.length());
        holder.mEdtEdit.addTextChangedListener(activeCurrency.getCurrencyTextWatcher(holder.mEdtEdit));
        holder.mEdtEdit.setOnEditorActionListener((v, actionId, event) -> {
            long enteredAmount = Toolkit.convertStringToLongAmount(holder.mEdtEdit.getText().toString());
            currentBill.setAmount(enteredAmount);

            expand();
            Database.save(holder.itemView.getContext());

            editPosition = -1;
            editType = 0;

            return true;
        });
    }

    private void expand(){
        notifyItemChanged(expandedPosition);
    }

    public void hide(){
        int previousExpandedPosition = expandedPosition;
        if (editPosition != -1){
            editPosition = -1;
            editType = 0;
        } else {
            expandedPosition = -1;
        }

        notifyItemChanged(previousExpandedPosition);
    }

    public boolean isHidingAnItemPossible(){
        return expandedPosition != -1;
    }

    private void showKeyboardForEditInput(View viewWhereKeyboardIsNeeded){
        InputMethodManager inputMethodManager = (InputMethodManager) viewWhereKeyboardIsNeeded.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        viewWhereKeyboardIsNeeded.requestFocus();
        inputMethodManager.showSoftInput(viewWhereKeyboardIsNeeded, 0);
    }

    private void showDeleteBillDialogFragment(HistoryViewHolder holder, Bill bill){
        Context context = holder.itemView.getContext();
        FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();

        DeleteBillDialogFragment deleteBillDialogFragment = new DeleteBillDialogFragment();
        deleteBillDialogFragment.setOnDialogPositiveClickListener((dialogInterface, i) -> {
            deleteBillAndRemoveItFromRecyclerView(holder, bill);
        });

        deleteBillDialogFragment.show(fragmentManager, "delete_bill");
    }

    private void deleteBillAndRemoveItFromRecyclerView(HistoryViewHolder holder, Bill bill){
        billsToDisplay.remove(bill);
        Toolkit.getBankAccountOfBill(bill).getBills().remove(bill);

        Database.save(holder.itemView.getContext());

        int position = holder.getAdapterPosition();
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());

        collapseAll();
    }

    private void duplicateBill(HistoryViewHolder holder, Bill bill){
        Bill duplication = (Bill)bill.clone();
        duplication.setCreationDate(System.currentTimeMillis());

        Toolkit.getBankAccountOfBill(bill).addBill(duplication);
        Database.save(holder.itemView.getContext());

        billsToDisplay.add(0, bill);

        notifyItemInserted(0);
        notifyItemRangeChanged(0, getItemCount());
        recyclerView.scrollToPosition(0);

        collapseAll();
    }

    private void showEditDialogForSubcategory(Bill bill, BankAccount bankAccountOfBill, Context context){
        int bankAccountIndex = getIndexOfBankAccountInDatabase(bankAccountOfBill);
        int billPosition = getIndexOfBillInBankAccount(bill, bankAccountOfBill);

        Intent intent = new Intent(context, BillActivity.class);
        intent.putExtra(BillActivity.EXTRA_BILL_TO_EDIT, billPosition);
        intent.putExtra(BillActivity.EXTRA_BILL_TO_EDIT_BANK_ACCOUNT, bankAccountIndex);
        context.startActivity(intent);
    }

    private void collapseAll(){
        editPosition = -1;
        expandedPosition = -1;
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
