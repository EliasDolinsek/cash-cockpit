package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
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
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolbox;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;
import java.util.Calendar;

public class PrimaryCategoryItemAdapter extends RecyclerView.Adapter<PrimaryCategoryItemAdapter.PrimaryCategoryViewHolder>{

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_GOAL_STATISTICS = 1;
    private static final int TYPE_SELECT_CATEGORY = 2;
    private static final int TYPE_CATEGORIES_STATISTICS = 3;

    private int adapterType;
    private ArrayList<PrimaryCategory> primaryCategoriesToDisplay;
    private ArrayList<Bill> billsToUseForPrimaryCategoryStatisticUsage;

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
        primaryCategoryItemAdapter.primaryCategoriesToDisplay = primaryCategoriesToDisplay;
        primaryCategoryItemAdapter.onCategorySelectedListener = onCategorySelectedListener;
        primaryCategoryItemAdapter.timeStampOfMonthToLoadStatistics = System.currentTimeMillis();

        return primaryCategoryItemAdapter;
    }

    public static PrimaryCategoryItemAdapter getSelectCategoryPrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay, Subcategory selectedSubcategory, SubcategoryItemAdapter.OnCategorySelectedListener onCategorySelectedListener){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = getSelectCategoryPrimaryCategoryItemAdapter(primaryCategoriesToDisplay, onCategorySelectedListener);
        primaryCategoryItemAdapter.selectedSubcategory = selectedSubcategory;

        return primaryCategoryItemAdapter;
    }

    public static PrimaryCategoryItemAdapter getCategoriesStatisticsPrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategoriesToDisplay, ArrayList<Bill> billsToUseForPrimaryCategoryStatisticUsage, long timeStampOfMonthToLoadStatistics){
        PrimaryCategoryItemAdapter primaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
        primaryCategoryItemAdapter.adapterType = TYPE_CATEGORIES_STATISTICS;
        primaryCategoryItemAdapter.primaryCategoriesToDisplay = primaryCategoriesToDisplay;
        primaryCategoryItemAdapter.timeStampOfMonthToLoadStatistics = timeStampOfMonthToLoadStatistics;
        primaryCategoryItemAdapter.billsToUseForPrimaryCategoryStatisticUsage = billsToUseForPrimaryCategoryStatisticUsage;

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
        setupOnCategoryClickListener(holder);
        setupButtonClickListeners(holder);

        SubcategoryItemAdapter subcategoryItemAdapter = createSubcategoriesItemAdapter(primaryCategory);

        holder.mRvSubcategories.setAdapter(subcategoryItemAdapter);
        holder.mRvSubcategories.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.mTxvCategoryName.setText(primaryCategory.getName());

        if(adapterType == TYPE_NORMAL){
            manageGoalViews(primaryCategory, holder);
        } else if (adapterType == TYPE_GOAL_STATISTICS){
            manageGoalViews(primaryCategory, holder);
            hideItemIfPrimaryCategoryHasNoSubcategories(primaryCategory, holder);
        } else if (adapterType == TYPE_SELECT_CATEGORY){
            manageGoalViews(primaryCategory, holder);
            manageViewsIfPrimaryCategoryHasNoSubcategories(primaryCategory, holder);
        } else if (adapterType == TYPE_CATEGORIES_STATISTICS){
            loadPrimaryCategoryStatisticInGoalViews(primaryCategory, holder);
            hideSubcategoriesRecyclerView(holder);
        }
    }

    @Override
    public int getItemCount() {
        return primaryCategoriesToDisplay.size();
    }

    public class PrimaryCategoryViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImvCategoryIcon;
        public TextView mTxvCategoryName, mTxvGoal;
        public ProgressBar mPgbCategoryGoalStatus;
        public RecyclerView mRvSubcategories;
        public LinearLayout mLlActionButtonsContainer;
        public Button btnEdit, btnDelete, btnAddSubcategory;

        public PrimaryCategoryViewHolder(View itemView) {
            super(itemView);

            mImvCategoryIcon = (ImageView) itemView.findViewById(R.id.imv_item_primary_category);
            mTxvCategoryName = (TextView) itemView.findViewById(R.id.txv_item_primary_category_name);
            mPgbCategoryGoalStatus = (ProgressBar) itemView.findViewById(R.id.pgb_item_primary_category_goal_status);
            mRvSubcategories = (RecyclerView) itemView.findViewById(R.id.rv_item_primary_categories_subcategory);
            mLlActionButtonsContainer = itemView.findViewById(R.id.ll_primary_category_action_buttons_container);
            mTxvGoal = itemView.findViewById(R.id.txv_item_primary_category_goal);

            btnEdit = itemView.findViewById(R.id.btn_item_primary_category_edit);
            btnDelete = itemView.findViewById(R.id.btn_item_primary_category_delete);
            btnAddSubcategory = itemView.findViewById(R.id.btn_item_primary_category_add_subcategory);
        }
    }

    private void setupButtonClickListeners(PrimaryCategoryViewHolder holder) {
        Context context = holder.itemView.getContext();
        FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();

        holder.btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(context, CategoryActivity.class);

            intent.putExtra(CategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, holder.getAdapterPosition());
            context.startActivity(intent);
        });


        holder.btnDelete.setOnClickListener(view -> {
            DeletePrimaryCategoryDialogFragment deletePrimaryCategoryDialogFragment = new DeletePrimaryCategoryDialogFragment();
            deletePrimaryCategoryDialogFragment.setDialogClickListener(() -> {
                deletePrimaryCategory(holder);
            });

            deletePrimaryCategoryDialogFragment.show(fragmentManager, "delete_primary_category");
        });

        holder.btnAddSubcategory.setOnClickListener(view -> showSubcategoryEditorDialogFragmentToAddSubcategory(holder, fragmentManager));
    }

    private void showSubcategoryEditorDialogFragmentToAddSubcategory(PrimaryCategoryViewHolder holder, FragmentManager fragmentManager) {
        PrimaryCategory primaryCategory = primaryCategoriesToDisplay.get(holder.getAdapterPosition());
        int previousSubcategoriesSize = primaryCategory.getSubcategories().size();

        SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();
        subcategoryEditorDialogFragment.setupForCreateMode(primaryCategoriesToDisplay.get(holder.getAdapterPosition()));
        subcategoryEditorDialogFragment.setOnDismissListener(dialogInterface -> {
            int currentSubcategoriesSize = primaryCategory.getSubcategories().size();
            if (previousSubcategoriesSize != currentSubcategoriesSize){
                holder.mRvSubcategories.getAdapter().notifyItemInserted(currentSubcategoriesSize);
                Database.save(holder.itemView.getContext());
            }
        });

        subcategoryEditorDialogFragment.show(fragmentManager, "add_subcategory");
    }

    private void deletePrimaryCategory(PrimaryCategoryViewHolder holder){
        int position = holder.getAdapterPosition();
        PrimaryCategory primaryCategoryToDelete = primaryCategoriesToDisplay.get(position);

        Database.getPrimaryCategories().remove(primaryCategoryToDelete);
        primaryCategoriesToDisplay.remove(primaryCategoryToDelete);
        Database.save(holder.itemView.getContext());

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    private void hideSubcategoriesRecyclerView(PrimaryCategoryViewHolder holder){
        holder.mRvSubcategories.setVisibility(View.GONE);
    }

    private long getAmountOfUsedMoneyOfTimestamp(PrimaryCategory primaryCategory, long timeStampOfMonth){
        ArrayList<Bill> bills = Toolkit.filterBillsByCategory(Toolkit.getBillsByMonth(timeStampOfMonth), primaryCategory);
        return Toolkit.getBillsAmount(bills);
    }

    private void displayGoalInformation(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder primaryCategoryViewHolder){
        long usedMoney = getAmountOfUsedMoneyOfTimestamp(primaryCategory, timeStampOfMonthToLoadStatistics);
        long goalAmount = primaryCategory.getGoal().getAmount();
        Context context = primaryCategoryViewHolder.itemView.getContext();

        Currency activeCurrency = Currency.getActiveCurrency(context);
        String formattedUsedMoney = activeCurrency.formatAmountToReadableStringWithoutCents(Math.abs(usedMoney));
        String formattedGoalAmount = activeCurrency.formatAmountToReadableStringWithoutCentsWithCurrencySymbol(goalAmount);

        if (usedMoney < 0){
            int percentOfUsedAmount = (int)(100 / (double) goalAmount * (double) Math.abs(usedMoney));
            primaryCategoryViewHolder.mPgbCategoryGoalStatus.setProgress(percentOfUsedAmount);
            primaryCategoryViewHolder.mTxvGoal.setText(formattedUsedMoney + "/" + formattedGoalAmount);
        } else {
            primaryCategoryViewHolder.mTxvGoal.setText("0/" + formattedGoalAmount);
        }

        System.out.println(primaryCategoryViewHolder.mTxvGoal.getVisibility());
    }

    private void loadPrimaryCategoryIcon(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder primaryCategoryViewHolder){
        try{
            Context context = primaryCategoryViewHolder.itemView.getContext();
            int resource = Toolbox.getPrimaryCategoryIconResourceByName(context, primaryCategory);
            primaryCategoryViewHolder.mImvCategoryIcon.setBackgroundResource(resource);
        } catch (Exception e) {
            loadDefaultPrimaryCategoryIcon(primaryCategoryViewHolder);
        }
    }

    private void loadDefaultPrimaryCategoryIcon(PrimaryCategoryViewHolder primaryCategoryViewHolder){
        primaryCategoryViewHolder.mImvCategoryIcon.setBackgroundResource(R.drawable.ic_default_category_image);
    }

    private void manageGoalViews(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder primaryCategoryViewHolder){
        if(primaryCategory.getGoal().getAmount() != 0){
            displayGoalInformation(primaryCategory, primaryCategoryViewHolder);
        }
    }

    private void setupOnCategoryClickListener(PrimaryCategoryViewHolder holder){
        holder.itemView.setOnClickListener(view -> {
            if (holder.mRvSubcategories.getVisibility() == View.VISIBLE){
                collapse(holder);
            } else {
                expand(holder);
            }
        });
    }

    private void expand(PrimaryCategoryViewHolder holder){
        if (adapterType == TYPE_NORMAL){
            holder.mLlActionButtonsContainer.setVisibility(View.VISIBLE);
        }

        holder.mRvSubcategories.setVisibility(View.VISIBLE);

        TransitionManager.beginDelayedTransition(holder.itemView.findViewById(R.id.cv_item_primary_category_root));
    }

    private void collapse(PrimaryCategoryViewHolder holder){
        holder.mLlActionButtonsContainer.setVisibility(View.GONE);
        holder.mRvSubcategories.setVisibility(View.GONE);

        notifyItemChanged(holder.getAdapterPosition());
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

    private SubcategoryItemAdapter createSubcategoriesItemAdapter(PrimaryCategory primaryCategoryWhatContainsSubcategories){
        switch (adapterType){
            case TYPE_NORMAL: return SubcategoryItemAdapter.getNormalSubcategoryItemAdapter(primaryCategoryWhatContainsSubcategories, SubcategoryItemAdapter.ON_SUBCATEGORY_CLICK_ACTION_OPEN_EDITOR);
            case TYPE_GOAL_STATISTICS: return SubcategoryItemAdapter.getGoalsStatisticsSubcategoryItemAdapter(primaryCategoryWhatContainsSubcategories, timeStampOfMonthToLoadStatistics);
            case TYPE_CATEGORIES_STATISTICS: return SubcategoryItemAdapter.getCategoriesStatisticsItemAdapter(primaryCategoryWhatContainsSubcategories, billsToUseForPrimaryCategoryStatisticUsage, timeStampOfMonthToLoadStatistics);
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
            holder.itemView.setVisibility(View.GONE);
        }
    }

    private void loadPrimaryCategoryStatisticInGoalViews(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder holder){
        ArrayList<Bill> bills = filterBillsOfPrimaryCategory(billsToUseForPrimaryCategoryStatisticUsage, primaryCategory);
        ArrayList<Bill> filteredBills = filterBillsOfMonth(bills, timeStampOfMonthToLoadStatistics);
        long totalAmountOfBillsOfCategory = getTotalAmountOfBills(filteredBills);

        long totalAmountOfAllBillsOfMonth = Toolkit.getBillsTotalAmount(Toolkit.filterBillsByMonth(billsToUseForPrimaryCategoryStatisticUsage, timeStampOfMonthToLoadStatistics));
        int usageOfPrimaryCategoryOfMonthInPercent = (int)Math.round(100 / (double)totalAmountOfAllBillsOfMonth * (double) totalAmountOfBillsOfCategory);

        String formattedTotalAmountOfBills = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(totalAmountOfBillsOfCategory);
        holder.mTxvGoal.setText(formattedTotalAmountOfBills + "/" + usageOfPrimaryCategoryOfMonthInPercent + "%");
        holder.mPgbCategoryGoalStatus.setProgress(usageOfPrimaryCategoryOfMonthInPercent);
    }

    private void manageViewsIfPrimaryCategoryHasNoSubcategories(PrimaryCategory primaryCategory, PrimaryCategoryViewHolder holder){
        if (primaryCategory.getSubcategories().size() == 0){
            holder.mTxvCategoryName.setEnabled(false);
        }
    }

    private long getTotalAmountOfBills(ArrayList<Bill> bills){
        long totalAmount = 0;
        for (Bill bill:bills){
            totalAmount += bill.getAmount();
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

    private ArrayList<Bill> filterBillsOfPrimaryCategory(ArrayList<Bill> billsToFilter, PrimaryCategory primaryCategory){
        ArrayList<Bill> billsOfPrimaryCategory = new ArrayList<>();
        for (Bill bill:billsToFilter){
            if (bill.getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                billsOfPrimaryCategory.add(bill);
            }
        }

        return billsOfPrimaryCategory;
    }
}
