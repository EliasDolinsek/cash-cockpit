package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

/**
 * Adapter for primary accounts
 * Created by elias on 20.01.2018.
 */

public class PrimaryCategoryItemAdapter extends RecyclerView.Adapter<PrimaryCategoryItemAdapter.PrimaryCategoryViewHolder>{

    public static int TYPE_NORMAL = 0, TYPE_GOAL_STATISTICS = 1, TYPE_SELECT_CATEGORY = 2;

    private int type;
    /**
     * Contains all primary categories
     */
    private ArrayList<PrimaryCategory> primaryCategories;

    private SubcategoryItemAdapter.OnCategorySelectedListener onCategorySelectedListener;
    private Subcategory selectedSubcategory;
    private long goalStatisticsTime = System.currentTimeMillis();

    /**
     * Creates a new adapter and sets categories from the database
     */
    public PrimaryCategoryItemAdapter(ArrayList<PrimaryCategory> primaryCategories, int type){
        this.type = type;

        if (type == TYPE_GOAL_STATISTICS){
            ArrayList<PrimaryCategory> editedPrimaryCategories = new ArrayList<>();
            for (PrimaryCategory primaryCategory:primaryCategories){
                if (primaryCategory.getGoal().getAmount() != 0){
                    editedPrimaryCategories.add(primaryCategory);
                }
            }

            this.primaryCategories = editedPrimaryCategories;
        } else {
            this.primaryCategories = primaryCategories;
        }
    }

    @Override
    public PrimaryCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PrimaryCategoryViewHolder(inflater.inflate(R.layout.list_item_primary_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final PrimaryCategoryViewHolder holder, final int position) {
        final PrimaryCategory primaryCategory = primaryCategories.get(position);

        //Displays the amount of the goal for the primary category if it's set and zero if not
        if(primaryCategory.getGoal().getAmount() == 0){
            holder.mTxvCategoryGoalStatus.setVisibility(View.INVISIBLE);
        } else {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(goalStatisticsTime);
            int thisMonth = calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH);

            //Reads how much bills have the this as primary category, reads their amount and adds it in the amount variable
            long amount = 0;
            for (Bill bill: Toolbox.getBills(goalStatisticsTime, Toolbox.TYPE_MONTH)){
                for (Subcategory subcategory:primaryCategory.getSubcategories()){
                    if (subcategory.getGoal().getAmount() != 0 && bill.getSubcategory().equals(subcategory)){
                        if (bill.getType() == Bill.TYPE_INPUT){
                            amount -= bill.getAmount();
                        } else {
                            amount += bill.getAmount();
                        }
                    }
                }
            }

            //Displays informations
            holder.mTxvCategoryGoalStatus.setText((Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(amount)));
            holder.mTxvGoalStatusAmount.setText(" (" + Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(primaryCategory.getGoal().getAmount()) + ")");
            holder.mPgbCategoryGoalStatus.setProgress((int)(100.0 /(primaryCategory.getGoal().getAmount() / 100.0) * (amount / 100.0)));

            //Enables a ProgressBar if there is a goal
            if(amount > primaryCategory.getGoal().getAmount()){
                holder.mTxvCategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                holder.mPgbCategoryGoalStatus.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else {
                //Colors the text to read if the user has exceeded the specified goal
                holder.mTxvCategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorAccent));
            }
        }

        if(type == TYPE_NORMAL){
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Starts CategoryActivity
                    Intent intent = new Intent(holder.itemView.getContext(), CategoryActivity.class);
                    intent.putExtra(CategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, position);
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }

        //Loads the image for this category
        try{
            holder.mImvCategoryIcon.setBackgroundResource(holder.itemView.getContext().getResources().getIdentifier(primaryCategory.getIconName(), "drawable", holder.itemView.getContext().getPackageName()));
        } catch (Exception e) {
            holder.mImvCategoryIcon.setBackgroundResource(R.drawable.ic_default_category_image);
        }

        //Sets up the RecyclerView
        SubcategoryItemAdapter subcategoryItemAdapter;
        if(type == TYPE_NORMAL){
            subcategoryItemAdapter = new SubcategoryItemAdapter(primaryCategory, false, SubcategoryItemAdapter.TYPE_NORMAl);
        } else if (type == TYPE_GOAL_STATISTICS){
            subcategoryItemAdapter = new SubcategoryItemAdapter(primaryCategory, false, SubcategoryItemAdapter.TYPE_GOAL_STATISTIC);
        } else {
            subcategoryItemAdapter = new SubcategoryItemAdapter(primaryCategory, false,SubcategoryItemAdapter.TYPE_SELECT_CATEGORY);
        }

        subcategoryItemAdapter.setOnCategorySelectedListener(onCategorySelectedListener);
        subcategoryItemAdapter.setSelectedSubcategory(selectedSubcategory);
        subcategoryItemAdapter.setGoalStatisticsTime(goalStatisticsTime);

        holder.mRvSubcategories.setAdapter(subcategoryItemAdapter);

        holder.mRvSubcategories.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        //Displays informations
        holder.mTxvCategoryName.setText(primaryCategory.getName());
    }

    @Override
    public int getItemCount() {
        return primaryCategories.size();
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

    public void setGoalStatisticsTime(long goalStatisticsTime){
        this.goalStatisticsTime = goalStatisticsTime;
    }
}
