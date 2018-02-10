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
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

/**
 * Created by elias on 20.01.2018.
 */

public class SubcategoryItemAdapter extends RecyclerView.Adapter<SubcategoryItemAdapter.SubcategoryItemViewHolder>{

    private PrimaryCategory primaryCategory;
    private boolean allowDirectEdit;

    public SubcategoryItemAdapter(PrimaryCategory primaryCategory, boolean allowDirectEdit){
        this.primaryCategory = primaryCategory;
        this.allowDirectEdit = allowDirectEdit;
    }

    @Override
    public SubcategoryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new SubcategoryItemViewHolder(inflater.inflate(R.layout.list_item_subcategory, parent, false));
    }

    @Override
    public void onBindViewHolder(final SubcategoryItemViewHolder holder, final int position) {
        final Subcategory subcategory = primaryCategory.getSubcategories().get(position);

        holder.mTxvSubcategoryName.setText(subcategory.getName());

        if(subcategory.isFavoured())
            holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_favorite);
        else
            holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_not_favorite);

        if(subcategory.getGoal().getAmount() == 0)
            holder.mTxvSubcategoryGoalStatus.setText("0/0€");
        else {
            long amount = 0;
            for(BankAccount bankAccount: Database.getBankAccounts()){
                for(Bill bill:bankAccount.getBills()){
                    for(int i = 0; i<primaryCategory.getSubcategories().size(); i++){
                        if(bill.getSubcategory() == primaryCategory.getSubcategories().get(i)){
                            amount += bill.getAmount();
                        }
                    }
                }}

            holder.mTxvSubcategoryGoalStatus.setText((amount + "/" + (subcategory.getGoal().getAmount() / 100) + "€"));
            holder.mPgbSubcategoryGoalStatus.setProgress((int)(100 / (double)(subcategory.getGoal().getAmount() / 100) * (double)(amount / 100)));

            if(amount > subcategory.getGoal().getAmount()){
                holder.mTxvSubcategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                holder.mPgbSubcategoryGoalStatus.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else{
                holder.mTxvSubcategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorAccent));
            }
        }

        holder.mImvSubcategoryFavored.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(subcategory.isFavoured())
                        holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_not_favorite);
                    else
                        holder.mImvSubcategoryFavored.setImageResource(R.drawable.ic_favorite);

                    subcategory.setFavoured(!subcategory.isFavoured());
                }
        });


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
                    int primaryCategoryIndex = 0;
                    for(int i = 0; i<Database.getPrimaryCategories().size(); i++){
                        if(primaryCategory.equals(Database.getPrimaryCategories()))
                            primaryCategoryIndex = 0;
                    }

                    Intent intent = new Intent(holder.itemView.getContext(), CategoryActivity.class);
                    intent.putExtra(CategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, primaryCategoryIndex);
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return primaryCategory.getSubcategories().size();
    }

    public class SubcategoryItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvSubcategoryName, mTxvSubcategoryGoalStatus;
        public ProgressBar mPgbSubcategoryGoalStatus;
        public ImageView mImvSubcategoryFavored;
        public LinearLayout mLlMaster;

        public SubcategoryItemViewHolder(View itemView) {
            super(itemView);

            mTxvSubcategoryName = (TextView) itemView.findViewById(R.id.txv_item_subcategory_name);
            mTxvSubcategoryGoalStatus = (TextView) itemView.findViewById(R.id.txv_item_subcategory_goal_status);
            mPgbSubcategoryGoalStatus = (ProgressBar) itemView.findViewById(R.id.pgb_item_subcategory_goal_status);
            mImvSubcategoryFavored = (ImageView) itemView.findViewById(R.id.imv_item_subcategory_favored);

            mLlMaster = (LinearLayout) itemView.findViewById(R.id.ll_item_subcategory_master);
        }
    }
}
