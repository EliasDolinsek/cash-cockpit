package com.dolinsek.elias.cashcockpit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;

/**
 * Created by elias on 20.01.2018.
 */

public class SubcategoryItemAdapter extends RecyclerView.Adapter<SubcategoryItemAdapter.SubcategoryItemViewHolder>{

    public static final int TYPE_NORMAl = 0;
    public static final int TYPE_GOAL_STATISTIC = 1;
    /**
     * PrimaryCategory what the Subcategory belongs to
     */
    private PrimaryCategory primaryCategory;

    /**
     * If a click shows SubcategoryEditorDialogFragment
     */
    private boolean allowDirectEdit, showFavoredIcon;

    private int type;

    public SubcategoryItemAdapter(PrimaryCategory primaryCategory, boolean allowDirectEdit, int type){
        this.primaryCategory = primaryCategory;
        this.allowDirectEdit = allowDirectEdit;
        this.showFavoredIcon = showFavoredIcon;
        this.type = type;
    }

    @Override
    public SubcategoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new SubcategoryItemViewHolder(inflater.inflate(R.layout.list_item_subcategory, parent, false));
    }

    @Override
    public void onBindViewHolder(final SubcategoryItemViewHolder holder, final int position) {
        final Subcategory subcategory;

        if(type == TYPE_NORMAl){
            subcategory = primaryCategory.getSubcategories().get(position);
        } else {
            ArrayList<Subcategory> subcategories = new ArrayList<>();
            for(Subcategory currentSubcategory:primaryCategory.getSubcategories()){
                if(currentSubcategory.getGoal().getAmount() != 0){
                    subcategories.add(currentSubcategory);
                }
            }

            subcategory = subcategories.get(position);
        }

        //Sets name of the subcategory
        holder.mTxvSubcategoryName.setText(subcategory.getName());

        //Displays a specified icon when the subcategory is favored and another when not
        if(showFavoredIcon){
            if(subcategory.isFavoured())
                holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_favorite);
            else
                holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_not_favorite);
        } else {
            holder.mImvSubcategoryFavored.setVisibility(View.GONE);
        }

        //Displays the goal-amount
        if(subcategory.getGoal().getAmount() != 0) {
            //Reads how much bills have this as subcategory and adds its amount into the variable amount
            long amount = 0;
            for(BankAccount bankAccount: Database.getBankAccounts()){
                for(Bill bill:bankAccount.getBills()){
                    for(int i = 0; i<primaryCategory.getSubcategories().size(); i++){
                        if(bill.getSubcategory() == primaryCategory.getSubcategories().get(i)){
                            amount += bill.getAmount();
                        }
                    }
                }
            }

            //Displays informations
            holder.mTxvSubcategoryGoalStatus.setText(Currency.Factory.getActiveCurrency(holder.itemView.getContext()).formatAmountToString(amount));
            holder.mTxvSubcategoryGoalStatusAmount.setText(" (" + Currency.Factory.getActiveCurrency(holder.itemView.getContext()).formatAmountToString(subcategory.getGoal().getAmount()) + ")");
            holder.mPgbSubcategoryGoalStatus.setProgress((int)(100 / (double)(subcategory.getGoal().getAmount() / 100) * (double)(amount / 100)));

            //Enables a ProgressBar if there is a goal
            if(amount > subcategory.getGoal().getAmount()){
                holder.mTxvSubcategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                holder.mPgbSubcategoryGoalStatus.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else{
                //Colors text color red if the user has exceeded the amount of the goal
                holder.mTxvSubcategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorAccent));
            }
        }

        holder.mImvSubcategoryFavored.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Changes the favored state of the subcategory
                    if(subcategory.isFavoured())
                        holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_not_favorite);
                    else
                        holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_favorite);

                    subcategory.setFavoured(!subcategory.isFavoured());
                    CategoriesSorter.sortSubcategories(primaryCategory.getSubcategories());
                }
        });


        //Displays SubcategoryEditorDialogFragment if it's allowed
        if(allowDirectEdit){
            holder.mLlMaster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();
                    subcategoryEditorDialogFragment.setPrimaryCategory(primaryCategory, position);
                    subcategoryEditorDialogFragment.show(((AppCompatActivity)holder.itemView.getContext()).getSupportFragmentManager(), "edit_subcategory");
                }
            });
        } else {
            holder.mLlMaster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Gets the index of the primary category in the Database
                    int primaryCategoryIndex = 0;
                    for(int i = 0; i<Database.getPrimaryCategories().size(); i++){
                        if(primaryCategory.equals(Database.getPrimaryCategories().get(i))){
                            primaryCategoryIndex = i;
                            System.out.println(i);
                        }
                    }

                    //Starts CategoryActivity
                    Intent intent = new Intent(holder.itemView.getContext(), CategoryActivity.class);
                    intent.putExtra(CategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, primaryCategoryIndex);
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(type == TYPE_GOAL_STATISTIC){
            int itemCount = 0;
            for(int i = 0; i<primaryCategory.getSubcategories().size(); i++){
                if(primaryCategory.getSubcategories().get(i).getGoal().getAmount() != 0){
                    itemCount++;
                }
            }
            return itemCount;
        }

        return primaryCategory.getSubcategories().size();
    }

    public class SubcategoryItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvSubcategoryName, mTxvSubcategoryGoalStatus, mTxvSubcategoryGoalStatusAmount;
        public ProgressBar mPgbSubcategoryGoalStatus;
        public ImageView mImvSubcategoryFavored;
        public LinearLayout mLlMaster;

        public SubcategoryItemViewHolder(View itemView) {
            super(itemView);

            mTxvSubcategoryName = (TextView) itemView.findViewById(R.id.txv_item_subcategory_name);
            mTxvSubcategoryGoalStatus = (TextView) itemView.findViewById(R.id.txv_item_subcategory_goal_status);
            mTxvSubcategoryGoalStatusAmount = (TextView) itemView.findViewById(R.id.txv_item_subcategory_goal_status_amount);
            mPgbSubcategoryGoalStatus = (ProgressBar) itemView.findViewById(R.id.pgb_item_subcategory_goal_status);
            mImvSubcategoryFavored = (ImageView) itemView.findViewById(R.id.imv_item_subcategory_favored);

            mLlMaster = (LinearLayout) itemView.findViewById(R.id.ll_item_subcategory_master);
        }
    }
}
