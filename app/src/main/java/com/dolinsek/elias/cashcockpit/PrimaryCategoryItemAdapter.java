package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Picture;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Category;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolbox;

import java.util.ArrayList;
import java.util.Calendar;

public class PrimaryCategoryItemAdapter extends RecyclerView.Adapter<PrimaryCategoryItemAdapter.PrimaryCategoryViewHolder>{

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_GOAL_STATISTICS = 1;
    private static final int TYPE_SELECT_CATEGORY = 2;
    private static final int TYPE_CATEGORIES_STATISTICS = 3;

    private int adapterType;
    private ArrayList<PrimaryCategory> primaryCategoriesToDisplay;

    private SubcategoryItemAdapter.OnCategorySelectedListener onCategorySelectedListener;
    private Subcategory selectedSubcategory;
    private long timeStampOfMonthToLoadStatistics;

    private PrimaryCategoryItemAdapter(){

    }

    public static PrimaryCategoryItemAdapter getNormalPrimaryCategoryAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
        primaryCategoryItemAdapter.adapterType = TYPE_NORMAL;
        primaryCategoryItemAdapter.primaryCategoriesToDisplay = primaryCategoriesToDisplay;

        return primaryCategoryItemAdapter;
    }

    public static PrimaryCategoryItemAdapter getGoalsStatisticsPrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay, long timeStampOfMonthToLoadStatistics){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
        primaryCategoryItemAdapter.adapterType = TYPE_GOAL_STATISTICS;
        primaryCategoryItemAdapter.timeStampOfMonthToLoadStatistics = timeStampOfMonthToLoadStatistics;
        primaryCategoryItemAdapter.primaryCategoriesToDisplay = filterPrimaryCategoriesWithGoals(primaryCategoriesToDisplay);

        return primaryCategoryItemAdapter;
    }

    public static PrimaryCategoryItemAdapter getSelectCategoryPrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay, SubcategoryItemAdapter.OnCategorySelectedListener onCategorySelectedListener){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
        primaryCategoryItemAdapter.adapterType = TYPE_SELECT_CATEGORY;
        primaryCategoryItemAdapter.primaryCategoriesToDisplay = filterPrimaryCategoriesWithSubcategories(primaryCategoriesToDisplay);
        primaryCategoryItemAdapter.onCategorySelectedListener = onCategorySelectedListener;

        return primaryCategoryItemAdapter;
    }

    public static PrimaryCategoryItemAdapter getSelectCategoryPrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay, Subcategory selectedSubcategory, SubcategoryItemAdapter.OnCategorySelectedListener onCategorySelectedListener){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = getSelectCategoryPrimaryCategoryItemAdapter(primaryCategoriesToDisplay, onCategorySelectedListener);
        primaryCategoryItemAdapter.selectedSubcategory = selectedSubcategory;

        return primaryCategoryItemAdapter;
    }

    public static PrimaryCategoryItemAdapter getCategoriesStatisticsPrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay, long timeStampOfMonthToLoadStatistics){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
        primaryCategoryItemAdapter.adapterType = TYPE_CATEGORIES_STATISTICS;
        primaryCategoryItemAdapter.primaryCategoriesToDisplay = primaryCategoriesToDisplay;
        primaryCategoryItemAdapter.timeStampOfMonthToLoadStatistics = timeStampOfMonthToLoadStatistics;

        return primaryCategoryItemAdapter;
    }

    @Override
    public PrimaryCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PrimaryCategoryViewHolder(inflater.inflate(R.layout.list_item_primary_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final PrimaryCategoryViewHolder holder, final int position) {
        final PrimaryCategory primaryCategory = primaryCategoriesToDisplay.get(position);

        loadPrimaryCategoryIcon(primaryCategory, holder);

        SubcategoryItemAdapter subcategoryItemAdapter = createSubcategoriesItemAdapter(primaryCategory);

        holder.mRvSubcategories.setAdapter(subcategoryItemAdapter);
        holder.mRvSubcategories.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.mTxvCategoryName.setText(primaryCategory.getName());

        if(adapterType == TYPE_NORMAL){
            setupViewToStartCategoryActivityOnClick(holder.mCardView, position);
            manageGoalViews(primaryCategory, holder);
        } else if (adapterType == TYPE_GOAL_STATISTICS){
            hideItemIfPrimaryCategoryHasNoSubcategories(primaryCategory, holder);
            manageGoalViews(primaryCategory, holder);
        } else if (adapterType == TYPE_SELECT_CATEGORY){
            manageGoalViews(primaryCategory, holder);
        } else if (adapterType == TYPE_CATEGORIES_STATISTICS){
            loadPrimaryCategoryStatisticInGoalViews(primaryCategory, holder, timeStampOfMonthToLoadStatistics);
        }
    }

    @Override
    public int getItemCount() {
        return primaryCategoriesToDisplay.size();
    }

    public class PrimaryCategoryViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImvCategoryIcon;
        public TextView mTxvCategoryName, mTxvCategoryGoalStatus, mTxvGoalStatusAmount;
        public ProgressBar mPgbCategoryGoalStatus;
        public RecyclerView mRvSubcategories;
        public CardView mCardView;

        public PrimaryCategoryViewHolder(View itemView) {
            super(itemView);

            mCardView = (CardView) itemView.findViewById(R.id.cv_item_primary_category);
            mImvCategoryIcon = (ImageView) itemView.findViewById(R.id.imv_item_primary_category);
            mTxvCategoryName = (TextView) itemView.findViewById(R.id.txv_item_primary_category_name);
            mTxvCategoryGoalStatus = (TextView) itemView.findViewById(R.id.txv_item_primary_category_goal_status);
            mTxvGoalStatusAmount = (TextView) itemView.findViewById(R.id.txv_item_primary_category_goal_status_amount);
            mPgbCategoryGoalStatus = (ProgressBar) itemView.findViewById(R.id.pgb_item_primary_category_goal_status);
            mRvSubcategories = (RecyclerView) itemView.findViewById(R.id.rv_item_primary_categories_subcategory);
        }
    }

    private long getAmountOfUsedMoneyOfPresetTimestamp(PrimaryCategory primaryCategory){
        ArrayList<Bill> bills = getBillsWithSameCreationDateAndCategory(primaryCategory, timeStampOfMonthToLoadStatistics);
        long usedMoney = 0;

        for (Bill bill:bills){
            usedMoney += bill.getAmount();
        }

        return usedMoney;
    }

    private ArrayList<Bill> getBillsWithSameCreationDateAndCategory(PrimaryCategory primaryCategory, long creationDateMonth){
        ArrayList<Bill> bills = getBillsWhatBelongToPrimaryCategory(primaryCategory);
        ArrayList<Bill> filteredBills = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(creationDateMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (Bill bill:bills){
            calendar.setTimeInMillis(bill.getCreationDate());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (year == currentYear && month == currentMonth){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private ArrayList<Bill> getBillsWhatBelongToPrimaryCategory(PrimaryCategory primaryCategory){
        ArrayList<Bill> bills = new ArrayList<>();

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                if (bill.getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                    bills.add(bill);
                }
            }
        }

        return bills;
    }

    private void displayGoalInformations(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder primaryCategoryViewHolder){
        long usedMoney = getAmountOfUsedMoneyOfPresetTimestamp(primaryCategory);
        long goalAmount = primaryCategory.getGoal().getAmount();
        Context context = primaryCategoryViewHolder.itemView.getContext();

        int percentOfUsedAmount = (int)(100 / (double) goalAmount * (double) usedMoney);
        String formattedUsedMoney = formatToReadableAmountUsingActiveCurrency(usedMoney, context);
        String formattedGoalAmount = formatToReadableAmountUsingActiveCurrency(goalAmount, context);

        primaryCategoryViewHolder.mTxvCategoryGoalStatus.setText(formattedUsedMoney);
        primaryCategoryViewHolder.mTxvGoalStatusAmount.setText("(" + formattedGoalAmount + ")");
        primaryCategoryViewHolder.mPgbCategoryGoalStatus.setProgress(percentOfUsedAmount);

        manageGoalStatusTxvColor(primaryCategory, primaryCategoryViewHolder);
    }

    private String formatToReadableAmountUsingActiveCurrency(long amount, Context context){
        Currency activeCurrency = Currency.getActiveCurrency(context);
        return activeCurrency.formatAmountToReadableStringWithCurrencySymbol(amount);
    }

    private void loadPrimaryCategoryIcon(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder primaryCategoryViewHolder){
        try{
            Context context = primaryCategoryViewHolder.itemView.getContext();
            String primaryCategoryIconName = primaryCategory.getIconName();
            String packageName = context.getPackageName();

            int resource = context.getResources().getIdentifier(primaryCategoryIconName, "drawable", packageName);
            primaryCategoryViewHolder.mImvCategoryIcon.setBackgroundResource(resource);
        } catch (Exception e) {
            loadDefaultPrimaryCategoryIcon(primaryCategoryViewHolder);
        }
    }

    private void loadDefaultPrimaryCategoryIcon(PrimaryCategoryViewHolder primaryCategoryViewHolder){
        primaryCategoryViewHolder.mImvCategoryIcon.setBackgroundResource(R.drawable.ic_default_category_image);
    }

    private void manageGoalStatusTxvColor(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder primaryCategoryViewHolder){
        long primaryCategoryGoalAmount = primaryCategory.getGoal().getAmount();
        long usedMoney = getAmountOfUsedMoneyOfPresetTimestamp(primaryCategory);

        Context context = primaryCategoryViewHolder.itemView.getContext();
        if (usedMoney > primaryCategoryGoalAmount){
            int redColor = context.getResources().getColor(android.R.color.holo_red_dark);
            primaryCategoryViewHolder.mTxvCategoryGoalStatus.setTextColor(redColor);
        } else {
            int colorAccent = context.getResources().getColor(R.color.colorAccent);
            primaryCategoryViewHolder.mTxvCategoryGoalStatus.setTextColor(colorAccent);
        }
    }

    private void manageGoalViews(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder primaryCategoryViewHolder){
        long primaryCategoryGoalAmount = primaryCategory.getGoal().getAmount();

        if(primaryCategoryGoalAmount == 0){
            primaryCategoryViewHolder.mTxvCategoryGoalStatus.setVisibility(View.INVISIBLE);
        } else {
            displayGoalInformations(primaryCategory, primaryCategoryViewHolder);
        }
    }

    private void setupViewToStartCategoryActivityOnClick(View view, final int primaryCategoryIndexInDatabase){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();

                Intent intent = new Intent(context, CategoryActivity.class);
                intent.putExtra(CategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, primaryCategoryIndexInDatabase);
                context.startActivity(intent);
            }
        });
    }

    private static ArrayList<PrimaryCategory> filterPrimaryCategoriesWithGoals(ArrayList<PrimaryCategory> primaryCategories){
        ArrayList<PrimaryCategory> primaryCategoriesWithGoals = new ArrayList<>();

        for (PrimaryCategory primaryCategory:primaryCategories){
            if (primaryCategory.getGoal().getAmount() != 0){
                primaryCategoriesWithGoals.add(primaryCategory);
            }
        }

        return primaryCategoriesWithGoals;
    }

    private static ArrayList<PrimaryCategory> filterPrimaryCategoriesWithSubcategories(ArrayList<PrimaryCategory> primaryCategories){
        ArrayList<PrimaryCategory> primaryCategoriesToReturn = new ArrayList<>();
        for (PrimaryCategory primaryCategory:primaryCategories){
            if (primaryCategory.getSubcategories().size() != 0){
                primaryCategoriesToReturn.add(primaryCategory);
            }
        }

        return primaryCategoriesToReturn;
    }

    private SubcategoryItemAdapter createSubcategoriesItemAdapter(PrimaryCategory primaryCategoryWhatContainsSubcategories){
        switch (adapterType){
            case TYPE_NORMAL: return SubcategoryItemAdapter.getNormalSubcategoryItemAdapter(primaryCategoryWhatContainsSubcategories, SubcategoryItemAdapter.ON_SUBCATEGORY_CLICK_ACTION_OPEN_EDITOR);
            case TYPE_GOAL_STATISTICS: return SubcategoryItemAdapter.getGoalsStatisticsSubcategoryItemAdapter(primaryCategoryWhatContainsSubcategories, timeStampOfMonthToLoadStatistics);
            case TYPE_CATEGORIES_STATISTICS: return SubcategoryItemAdapter.getCategoriesStatisticsItemAdapter(primaryCategoryWhatContainsSubcategories, timeStampOfMonthToLoadStatistics);
            case TYPE_SELECT_CATEGORY: {
                if (selectedSubcategory != null){
                    return SubcategoryItemAdapter.getSelectCategoryItemAdapter(primaryCategoryWhatContainsSubcategories, onCategorySelectedListener, selectedSubcategory);
                } else {
                    return SubcategoryItemAdapter.getSelectCategoryItemAdapter(primaryCategoryWhatContainsSubcategories, onCategorySelectedListener);
                }
            }
            default: throw new Resources.NotFoundException("Couldn't find following adapter-type: " + adapterType);
        }
    }

    private void hideItemIfPrimaryCategoryHasNoSubcategories(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder holder){
        if (primaryCategory.getSubcategories().size() == 0){
            holder.mCardView.setVisibility(View.GONE);
        }
    }

    private void loadPrimaryCategoryStatisticInGoalViews(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder holder, long timeStampOfMonthToLoadStatistics){
        ArrayList<Bill> bills = getBillsOfPrimaryCategoryFromDatabase(primaryCategory);
        ArrayList<Bill> filteredBills = filterBillsOfMonth(bills, timeStampOfMonthToLoadStatistics);
        long totalAmountOfBillsOfCategory = getTotalAmountOfBills(filteredBills);

        ArrayList<Bill> allBillsOfMonth = getAllBillsOfMonthFromDatabase(timeStampOfMonthToLoadStatistics);
        long totalAmountOfAllBillsOfMonth = getTotalAmountOfBills(allBillsOfMonth);
        int usageOfPrimaryCategoryOfMonthInPercent = (int)Math.round((100 / (double)totalAmountOfAllBillsOfMonth * (double) totalAmountOfBillsOfCategory));

        String formattedTotalAmountOfBills = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(totalAmountOfBillsOfCategory);
        holder.mTxvCategoryGoalStatus.setText(formattedTotalAmountOfBills);
        holder.mTxvGoalStatusAmount.setText("(" + usageOfPrimaryCategoryOfMonthInPercent + "%)");
        holder.mPgbCategoryGoalStatus.setProgress(usageOfPrimaryCategoryOfMonthInPercent);
    }

    private long getTotalAmountOfBills(ArrayList<Bill> bills){
        long totalAmount = 0;
        for (Bill bill:bills){
            if (bill.getType() == Bill.TYPE_INPUT){
                totalAmount -= bill.getAmount();
            } else {
                totalAmount += bill.getAmount();
            }
        }

        return totalAmount;
    }

    private ArrayList<Bill> filterBillsOfMonth(ArrayList<Bill> billsToFilter, long timeStampOfMonth){
        ArrayList<Bill> billsToReturn = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (Bill bill:billsToFilter){
            calendar.setTimeInMillis(bill.getCreationDate());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (year == currentYear && month == currentMonth){
                billsToReturn.add(bill);
            }
        }

        return billsToReturn;
    }

    private ArrayList<Bill> getBillsOfPrimaryCategoryFromDatabase(PrimaryCategory primaryCategory){
        ArrayList<Bill> bills = new ArrayList<>();

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                if (bill.getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                    bills.add(bill);
                }
            }
        }

        return bills;
    }

    private ArrayList<Bill> getAllBillsOfMonthFromDatabase(long timeStampOfMonth){
        ArrayList<Bill> allBillsInDatabase = new ArrayList<>();

        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            allBillsInDatabase.addAll(getBillsWhatBelongToPrimaryCategory(primaryCategory));
        }

        return filterBillsOfMonth(allBillsInDatabase, timeStampOfMonth);
    }
}
