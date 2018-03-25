package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolbox;

import java.util.ArrayList;
import java.util.Calendar;

public class PrimaryCategoryItemAdapter extends RecyclerView.Adapter<PrimaryCategoryItemAdapter.PrimaryCategoryViewHolder>{

    public static final int TYPE_NORMAL = 0, TYPE_GOAL_STATISTICS = 1, TYPE_SELECT_CATEGORY = 2;

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

    public static PrimaryCategoryItemAdapter getSelectCategoryPrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
        primaryCategoryItemAdapter.adapterType = TYPE_SELECT_CATEGORY;
        primaryCategoryItemAdapter.primaryCategoriesToDisplay = primaryCategoriesToDisplay;

        return primaryCategoryItemAdapter;
    }

    public static PrimaryCategoryItemAdapter getCategoriesStatisticsPrimaryCategoryItemAdapter(){
        return getNormalPrimaryCategoryAdapter(Database.getPrimaryCategories());
    }

    @Override
    public PrimaryCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PrimaryCategoryViewHolder(inflater.inflate(R.layout.list_item_primary_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final PrimaryCategoryViewHolder holder, final int position) {
        final PrimaryCategory primaryCategory = primaryCategoriesToDisplay.get(position);

        manageGoalViews(primaryCategory, holder);
        loadPrimaryCategoryIcon(primaryCategory, holder);

        SubcategoryItemAdapter subcategoryItemAdapter = createSubcategoriesItemAdapter(primaryCategory);
        setupSubcategoriesRecyclerView(subcategoryItemAdapter);

        holder.mRvSubcategories.setAdapter(subcategoryItemAdapter);
        holder.mRvSubcategories.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.mTxvCategoryName.setText(primaryCategory.getName());

        if(adapterType == TYPE_NORMAL){
            setupViewToStartCategoryActivityOnClick(holder.mCardView, position);
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

    public void setOnCategorySelectedListener(SubcategoryItemAdapter.OnCategorySelectedListener onCategorySelectedListener) {
        this.onCategorySelectedListener = onCategorySelectedListener;
    }

    public void setSelectedSubcategory(Subcategory selectedSubcategory){
        this.selectedSubcategory = selectedSubcategory;
    }

    private long getAmountOfUsedMoneyOfPresetTimestamp(PrimaryCategory primaryCategory){
        long usedMoney = 0;
        for (Bill bill:getBillsWithSameCreationDateAndCategory(primaryCategory, timeStampOfMonthToLoadStatistics)){
            usedMoney += bill.getAmount();
        }

        return usedMoney;
    }

    private ArrayList<Bill> getBillsWithSameCreationDateAndCategory(PrimaryCategory primaryCategory, long creationDateMonth){
        ArrayList<Bill> bills = getBillsWhatBelongToPrimaryCategory(primaryCategory);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(creationDateMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (Bill bill:bills){
            calendar.setTimeInMillis(bill.getCreationDate());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (year != currentYear || month != currentMonth){
                bills.remove(bill);
            }
        }

        return bills;
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

    private void setupSubcategoriesRecyclerView(SubcategoryItemAdapter subcategoryItemAdapter){
        subcategoryItemAdapter.setOnCategorySelectedListener(onCategorySelectedListener);
        subcategoryItemAdapter.setSelectedSubcategory(selectedSubcategory);
        subcategoryItemAdapter.setGoalStatisticsTime(timeStampOfMonthToLoadStatistics);
    }

    private SubcategoryItemAdapter createSubcategoriesItemAdapter(PrimaryCategory primaryCategoryWhatContainsSubcategories){
        switch (adapterType){
            case TYPE_NORMAL: return new SubcategoryItemAdapter(primaryCategoryWhatContainsSubcategories, false, SubcategoryItemAdapter.TYPE_NORMAl);
            case TYPE_GOAL_STATISTICS: return new SubcategoryItemAdapter(primaryCategoryWhatContainsSubcategories, false, SubcategoryItemAdapter.TYPE_GOAL_STATISTIC);
            case TYPE_SELECT_CATEGORY: return new SubcategoryItemAdapter(primaryCategoryWhatContainsSubcategories, false,SubcategoryItemAdapter.TYPE_SELECT_CATEGORY);
            default: throw new Resources.NotFoundException("Couldn't find following adapter-type: " + adapterType);
        }
    }
}
