package com.dolinsek.elias.cashcockpit;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.CategoriesSorter;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolbox;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by elias on 20.01.2018.
 */

public class SubcategoryItemAdapter extends RecyclerView.Adapter<SubcategoryItemAdapter.SubcategoryItemViewHolder>{

    private static final int TYPE_NORMAl = 0;
    private static final int TYPE_GOAL_STATISTIC = 1;
    private static final int TYPE_SELECT_CATEGORY = 2;
    private static final int TYPE_CATEGORIES_STATISTICS = 3;

    public static final int ON_SUBCATEGORY_CLICK_ACTION_OPEN_EDITOR = 3;
    public static final int ON_SUBCATEGORY_CLICK_ACTION_OPEN_CATEGORY_ACTIVITY_FIRST = 4;

    private PrimaryCategory primaryCategoryOfSubcategories;
    private ArrayList<Subcategory> subcategories;
    private ArrayList<Bill> billsUsedForPrimaryCategoryStatistic;
    private OnCategorySelectedListener onCategorySelectedListener;
    private Subcategory selectedSubcategory;
    private long timeStampOfMonthToLoadStatistics;
    private int adapterType, onSubcategoryClickAction;

    public static SubcategoryItemAdapter getNormalSubcategoryItemAdapter(PrimaryCategory primaryCategoryOfSubcategories, int onSubcategoryItemClickAction){
        SubcategoryItemAdapter subcategoryItemAdapter = new SubcategoryItemAdapter();
        subcategoryItemAdapter.primaryCategoryOfSubcategories = primaryCategoryOfSubcategories;
        subcategoryItemAdapter.subcategories = primaryCategoryOfSubcategories.getSubcategories();
        subcategoryItemAdapter.adapterType = TYPE_NORMAl;
        subcategoryItemAdapter.onSubcategoryClickAction = onSubcategoryItemClickAction;

        return subcategoryItemAdapter;
    }

    public static SubcategoryItemAdapter getGoalsStatisticsSubcategoryItemAdapter(PrimaryCategory primaryCategoryOfSubcategories, long timeStampOfMonthToLoadStatistics){
        SubcategoryItemAdapter subcategoryItemAdapter = new SubcategoryItemAdapter();
        subcategoryItemAdapter.primaryCategoryOfSubcategories = primaryCategoryOfSubcategories;
        subcategoryItemAdapter.timeStampOfMonthToLoadStatistics = timeStampOfMonthToLoadStatistics;
        subcategoryItemAdapter.subcategories = removeSubcategoriesWhatHaveNoGoal(primaryCategoryOfSubcategories.getSubcategories());
        subcategoryItemAdapter.adapterType = TYPE_GOAL_STATISTIC;

        return subcategoryItemAdapter;
    }

    public static SubcategoryItemAdapter getSelectCategoryItemAdapter(PrimaryCategory primaryCategoryOfSubcategories, OnCategorySelectedListener onCategorySelectedListener){
        SubcategoryItemAdapter subcategoryItemAdapter = new SubcategoryItemAdapter();
        subcategoryItemAdapter.primaryCategoryOfSubcategories = primaryCategoryOfSubcategories;
        subcategoryItemAdapter.onCategorySelectedListener = onCategorySelectedListener;
        subcategoryItemAdapter.timeStampOfMonthToLoadStatistics = System.currentTimeMillis();
        subcategoryItemAdapter.subcategories = primaryCategoryOfSubcategories.getSubcategories();
        subcategoryItemAdapter.adapterType = TYPE_SELECT_CATEGORY;

        return subcategoryItemAdapter;
    }

    public static SubcategoryItemAdapter getSelectCategoryItemAdapter(PrimaryCategory primaryCategoryOfSubcategories, OnCategorySelectedListener onCategorySelectedListener, Subcategory selectedSubcategory){
        SubcategoryItemAdapter subcategoryItemAdapter = getSelectCategoryItemAdapter(primaryCategoryOfSubcategories, onCategorySelectedListener);
        subcategoryItemAdapter.selectedSubcategory = selectedSubcategory;

        return subcategoryItemAdapter;
    }

    public static SubcategoryItemAdapter getCategoriesStatisticsItemAdapter(PrimaryCategory primaryCategoryOfSubcategories, ArrayList<Bill> billsUsedForPrimaryCategoryStatistic, long timeStampOfMonthToLoadStatistics){
        SubcategoryItemAdapter subcategoryItemAdapter = new SubcategoryItemAdapter();
        subcategoryItemAdapter.primaryCategoryOfSubcategories = primaryCategoryOfSubcategories;
        subcategoryItemAdapter.subcategories = primaryCategoryOfSubcategories.getSubcategories();
        subcategoryItemAdapter.timeStampOfMonthToLoadStatistics = timeStampOfMonthToLoadStatistics;
        subcategoryItemAdapter.billsUsedForPrimaryCategoryStatistic = billsUsedForPrimaryCategoryStatistic;
        subcategoryItemAdapter.adapterType = TYPE_CATEGORIES_STATISTICS;

        return subcategoryItemAdapter;
    }

    @Override
    public SubcategoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new SubcategoryItemViewHolder(inflater.inflate(R.layout.list_item_subcategory, parent, false));
    }

    @Override
    public void onBindViewHolder(final SubcategoryItemViewHolder holder, final int position) {
        final Subcategory subcategory = subcategories.get(position);

        if (adapterType == TYPE_NORMAl){
            setupForNormalAdapterType(subcategory, holder);
            loadGoalStatistics(holder, subcategory);
            setupItemViewToShowSubcategoryEditor(holder, subcategory);
        } else if (adapterType == TYPE_SELECT_CATEGORY){
            setupForSelectCategoryAdapterType(subcategory, holder);
            loadGoalStatistics(holder, subcategory);
            markSelectedSubcategory(subcategory, holder);
        } else if (adapterType == TYPE_GOAL_STATISTIC){
            setupForGoalsStatisticsAdapterType(holder);
            loadGoalStatistics(holder, subcategory);
        } else if (adapterType == TYPE_CATEGORIES_STATISTICS){
            setupForCategoriesStatistics(holder);
            loadSubcategoryStatistic(subcategory, holder);
        } else {
            throw new IllegalStateException("Couldn't resolve " + adapterType + " as adapter-type");
        }

        holder.mTxvSubcategoryName.setText(subcategory.getName());
        holder.mImvSubcategoryFavored.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    subcategory.setFavoured(!subcategory.isFavoured());
                    setFavoredIcon(subcategory.isFavoured(), holder);
                    CategoriesSorter.sortSubcategories(primaryCategoryOfSubcategories.getSubcategories());
                }
        });
    }

    @Override
    public int getItemCount() {
        return subcategories.size();
    }

    public class SubcategoryItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvSubcategoryName, mTxvSubcategoryGoalStatus, mTxvSubcategoryGoalStatusAmount;
        public ProgressBar mPgbSubcategoryGoalStatus;
        public ImageView mImvSubcategoryFavored;
        public LinearLayout mLlMaster, mLlHorizontalContents;
        public Button mBtnSelectCategory;

        public SubcategoryItemViewHolder(View itemView) {
            super(itemView);

            mTxvSubcategoryName = (TextView) itemView.findViewById(R.id.txv_item_subcategory_name);
            mTxvSubcategoryGoalStatus = (TextView) itemView.findViewById(R.id.txv_item_subcategory_goal_status);
            mTxvSubcategoryGoalStatusAmount = (TextView) itemView.findViewById(R.id.txv_item_subcategory_goal_status_amount);
            mPgbSubcategoryGoalStatus = (ProgressBar) itemView.findViewById(R.id.pgb_item_subcategory_goal_status);
            mImvSubcategoryFavored = (ImageView) itemView.findViewById(R.id.imv_item_subcategory_favored);
            mBtnSelectCategory = (Button) itemView.findViewById(R.id.btn_item_subcategory_select);
            mLlHorizontalContents = itemView.findViewById(R.id.ll_item_subcategory_horizontal_contents);

            mLlMaster = (LinearLayout) itemView.findViewById(R.id.ll_item_subcategory_master);
        }
    }

    interface OnCategorySelectedListener{

        void onSelected(int primaryCategoryIndex, int subcategoryIndex);
    }

    private void loadGoalStatistics(SubcategoryItemViewHolder holder, Subcategory subcategory){
        if(subcategory.getGoal().getAmount() != 0) {
            ArrayList<Bill> billsOfMonth = getBillsOfSubcategoryAndMonth(subcategory, timeStampOfMonthToLoadStatistics);
            int percentOfUsedGoalAmount = getPercentOfUsedGoalAmount(subcategory, billsOfMonth);

            long usedGoalAmount = getTotalAmountOfBills(billsOfMonth);
            long subcategoryGoalAmount = subcategory.getGoal().getAmount();

            String formattedUsedGoalAmount = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(subcategoryGoalAmount);
            String formattedGoalAmount = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(usedGoalAmount);

            holder.mPgbSubcategoryGoalStatus.setProgress(percentOfUsedGoalAmount);
            holder.mTxvSubcategoryGoalStatus.setText(formattedGoalAmount);
            holder.mTxvSubcategoryGoalStatusAmount.setText(formattedUsedGoalAmount);

            holder.mTxvSubcategoryGoalStatusAmount.setVisibility(View.VISIBLE);
        } else {
            holder.mTxvSubcategoryGoalStatusAmount.setVisibility(View.GONE);
        }
    }

    private void loadSubcategoryStatistic(Subcategory subcategory, SubcategoryItemViewHolder holder){
        ArrayList<Bill> billsOfSubcategory = filterBillsOfSubcategory(billsUsedForPrimaryCategoryStatistic, subcategory);
        long totalAmountOfBillsOfPrimaryCategory = getTotalAmountOfBills(billsUsedForPrimaryCategoryStatistic);
        long totalAmountOfBillsOfSubcategory = getTotalAmountOfBills(billsOfSubcategory);

        int usageOfSubcategoryOfMonthInPercent = (int) Math.round(100 / (double)totalAmountOfBillsOfPrimaryCategory * (double)totalAmountOfBillsOfSubcategory);
        String formattedTotalAmountOfBillsOfSubcategory = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(totalAmountOfBillsOfSubcategory);

        holder.mTxvSubcategoryGoalStatus.setText(formattedTotalAmountOfBillsOfSubcategory);
        holder.mTxvSubcategoryGoalStatusAmount.setText(usageOfSubcategoryOfMonthInPercent + "%");
        holder.mPgbSubcategoryGoalStatus.setProgress(usageOfSubcategoryOfMonthInPercent);
    }

    private ArrayList<Bill> getBillsOfSubcategoryAndMonth(Subcategory subcategory, long timeStampOfMonth){
        ArrayList<Bill> billsToReturn = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                calendar.setTimeInMillis(bill.getCreationDate());

                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH);

                if (bill.getSubcategory().equals(subcategory) && currentYear == year && currentMonth == month){
                    billsToReturn.add(bill);
                }
            }
        }

        return billsToReturn;
    }

    private void setupForNormalAdapterType(Subcategory subcategory, SubcategoryItemViewHolder holder){
        holder.mBtnSelectCategory.setVisibility(View.GONE);
        holder.mTxvSubcategoryGoalStatus.setVisibility(View.GONE);
        setFavoredIcon(subcategory.isFavoured(), holder);
        setTwentyTwoDpMarginTopToView(holder.mPgbSubcategoryGoalStatus);
        setTwentyTwoDpMarginTopToView(holder.mLlHorizontalContents);
    }

    private void setupForGoalsStatisticsAdapterType(SubcategoryItemViewHolder holder){
        holder.mBtnSelectCategory.setVisibility(View.GONE);
        holder.mImvSubcategoryFavored.setVisibility(View.GONE);
        setTwentyTwoDpMarginTopToView(holder.mPgbSubcategoryGoalStatus);
        setTwentyTwoDpMarginTopToView(holder.mLlHorizontalContents);
    }

    private void setupForCategoriesStatistics(SubcategoryItemViewHolder holder){
        holder.mBtnSelectCategory.setVisibility(View.GONE);
        holder.mImvSubcategoryFavored.setVisibility(View.GONE);
        setTwentyTwoDpMarginTopToView(holder.mPgbSubcategoryGoalStatus);
        setTwentyTwoDpMarginTopToView(holder.mLlHorizontalContents);
    }

    private void setupForSelectCategoryAdapterType(final Subcategory subcategory, SubcategoryItemViewHolder holder){
        holder.mTxvSubcategoryGoalStatus.setVisibility(View.GONE);
        holder.mTxvSubcategoryGoalStatusAmount.setVisibility(View.GONE);
        setFavoredIcon(subcategory.isFavoured(), holder);

        holder.mBtnSelectCategory.setOnClickListener(view -> {
            int primaryCategoryIndexInDatabase = getIndexOfPrimaryCategoryInDatabase(subcategory.getPrimaryCategory());
            int subcategoryIndexInDatabase = getIndexOfSubcategoryInPrimaryCategory(subcategory);

            onCategorySelectedListener.onSelected(primaryCategoryIndexInDatabase, subcategoryIndexInDatabase);
        });
    }

    private void setTwentyTwoDpMarginTopToView(View view){
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(0,22,0,0);
        view.setLayoutParams(params);
    }

    private static ArrayList<Subcategory> removeSubcategoriesWhatHaveNoGoal(ArrayList<Subcategory> subcategories){
        ArrayList<Subcategory> subcategoriesToReturn = new ArrayList<>();
        for (Subcategory subcategory:subcategories){
            if (subcategory.getGoal().getAmount() != 0){
                subcategoriesToReturn.add(subcategory);
            }
        }

        return subcategoriesToReturn;
    }

    private void setFavoredIcon(boolean isSubcategoryFavored, SubcategoryItemViewHolder holder){
        if (isSubcategoryFavored){
            holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_favorite);
        } else {
            holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_not_favorite);
        }
    }

    private void markSelectedSubcategory(Subcategory subcategory, SubcategoryItemViewHolder holder){
        if (subcategory != null && subcategory.equals(selectedSubcategory)){
            holder.mTxvSubcategoryName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
            holder.mBtnSelectCategory.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorPrimary));
        }
    }

    private void showSubcategoryEditor(Subcategory subcategory, SubcategoryItemViewHolder holder){
        SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();
        subcategoryEditorDialogFragment.setupForEditMode(primaryCategoryOfSubcategories, subcategory);
        subcategoryEditorDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                refreshListOfItems();
            }
        });

        subcategoryEditorDialogFragment.show(((AppCompatActivity)holder.itemView.getContext()).getSupportFragmentManager(), "edit_subcategory");
    }

    private int getIndexOfSubcategoryInPrimaryCategory(Subcategory subcategory){
        for (int i = 0; i<Database.getPrimaryCategories().size(); i++){
            for (int y = 0; y<Database.getPrimaryCategories().get(i).getSubcategories().size(); y++){
                if (Database.getPrimaryCategories().get(i).getSubcategories().get(y).equals(subcategory)){
                    return y;
                }
            }
        }

        throw new IllegalStateException("Couldn't find subcategory in database!");
    }


    private void showCategoryActivityAndSubcategoryEditor(Subcategory selectedSubcategory, SubcategoryItemViewHolder holder){
        int indexOfSubcategoryPrimaryCategory = getIndexOfSubcategoryInPrimaryCategory(selectedSubcategory);
        int indexOfPrimaryCategoryInDatabase = getIndexOfPrimaryCategoryInDatabase(selectedSubcategory.getPrimaryCategory());

        Intent intent = new Intent(holder.itemView.getContext(), CategoryActivity.class);
        intent.putExtra(CategoryActivity.EXTRA_SUBCATEGORY_TO_SHOW_INDEX, indexOfSubcategoryPrimaryCategory);
        intent.putExtra(CategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, indexOfPrimaryCategoryInDatabase);

        holder.itemView.getContext().startActivity(intent);
    }

    private ArrayList<Bill> filterBillsOfSubcategory(ArrayList<Bill> bills, Subcategory subcategory){
        ArrayList<Bill> filteredBills = new ArrayList<>();
        for (Bill bill:bills){
            if (bill.getSubcategory().equals(subcategory)){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private int getPercentOfUsedGoalAmount(Subcategory subcategory, ArrayList<Bill> bills){
        long subcategoryGoalAmount = subcategory.getGoal().getAmount();
        long billsAmount = getTotalAmountOfBills(bills);

        return (int)(100 / (double)subcategoryGoalAmount * (double)billsAmount);
    }

    private long getTotalAmountOfBills(ArrayList<Bill> bills){
        long billsTotalAmount = 0;
        for (Bill bill:bills){
            billsTotalAmount += bill.getAmount();
        }

        return billsTotalAmount;
    }

    private int getIndexOfPrimaryCategoryInDatabase(PrimaryCategory primaryCategory){
        for (int i = 0; i<Database.getPrimaryCategories().size(); i++){
            if (primaryCategory.equals(Database.getPrimaryCategories().get(i))){
                return i;
            }
        }

        throw new IllegalStateException("Couldn't find primary category in database!");
    }

    private void refreshListOfItems(){
        int indexOfPrimaryCategoryInDatabase = getIndexOfPrimaryCategoryInDatabase(primaryCategoryOfSubcategories);
        subcategories = Database.getPrimaryCategories().get(indexOfPrimaryCategoryInDatabase).getSubcategories();

        notifyDataSetChanged();
    }

    private void setupItemViewToShowSubcategoryEditor(final SubcategoryItemViewHolder holder, final Subcategory subcategory){
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onSubcategoryClickAction == ON_SUBCATEGORY_CLICK_ACTION_OPEN_EDITOR){
                    showSubcategoryEditor(subcategory, holder);
                } else if (onSubcategoryClickAction == ON_SUBCATEGORY_CLICK_ACTION_OPEN_CATEGORY_ACTIVITY_FIRST){
                    showCategoryActivityAndSubcategoryEditor(subcategory, holder);
                } else {
                    throw new InvalidParameterException("Couldn't resolve " + onSubcategoryClickAction + " as a subcategory-click-action!");
                }
            }
        });
    }
}
