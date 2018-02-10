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

import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;

import java.util.ArrayList;

/**
 * Created by elias on 20.01.2018.
 */

public class PrimaryCategoryItemAdapter extends RecyclerView.Adapter<PrimaryCategoryItemAdapter.PrimaryCategoryViewHolder>{

    private ArrayList<PrimaryCategory> primaryCategories;

    public PrimaryCategoryItemAdapter(){
        primaryCategories = Database.getPrimaryCategories();
    }

    @Override
    public PrimaryCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PrimaryCategoryViewHolder(inflater.inflate(R.layout.list_item_primary_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final PrimaryCategoryViewHolder holder, final int position) {
        PrimaryCategory primaryCategory = primaryCategories.get(position);

        if(primaryCategory.getGoal().getAmount() == 0){
            holder.mTxvCategoryGoalStatus.setText("0/0€");
        } else {
            long amount = 0;
            for(int i = 0; i<Database.getBankAccounts().size(); i++){
                for(int x = 0; x<Database.getBankAccounts().get(i).getBills().size(); x++){
                    for(int y = 0; y<primaryCategory.getSubcategories().size(); y++){
                        if(primaryCategory.getSubcategories().get(y).equals(Database.getBankAccounts().get(i).getBills().get(x).getSubcategory()))
                            amount += Database.getBankAccounts().get(i).getBills().get(x).getAmount();
                    }
                }
            }

            holder.mTxvCategoryGoalStatus.setText(((amount / 100) + "/" + (primaryCategory.getGoal().getAmount() / 100) + "€"));
            holder.mPgbCategoryGoalStatus.setProgress((int)(100.0 /(primaryCategory.getGoal().getAmount() / 100.0) * (amount / 100.0)));

            if(amount > primaryCategory.getGoal().getAmount()){
                holder.mTxvCategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                holder.mPgbCategoryGoalStatus.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else{
                holder.mTxvCategoryGoalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.colorAccent));
            }
        }

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), CategoryActivity.class);
                intent.putExtra(CategoryActivity.EXTRA_PRIMARY_CATEGORY_INDEX, position);
                holder.itemView.getContext().startActivity(intent);
            }
        });

        try{
            holder.mImvCategoryIcon.setBackgroundResource(holder.itemView.getContext().getResources().getIdentifier(primaryCategory.getIconName(), "drawable", holder.itemView.getContext().getPackageName()));
        } catch (Exception e) {
            holder.mImvCategoryIcon.setBackgroundResource(R.drawable.ic_default_category_image);
        }

        holder.mRvSubcategories.setAdapter(new SubcategoryItemAdapter(primaryCategory, false));
        holder.mRvSubcategories.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        holder.mTxvCategoryName.setText(primaryCategory.getName());
        //TODO Add icon
    }

    @Override
    public int getItemCount() {
        return primaryCategories.size();
    }

    public class PrimaryCategoryViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImvCategoryIcon;
        public TextView mTxvCategoryName, mTxvCategoryGoalStatus;
        public ProgressBar mPgbCategoryGoalStatus;
        public RecyclerView mRvSubcategories;
        public CardView mCardView;

        public PrimaryCategoryViewHolder(View itemView) {
            super(itemView);

            mCardView = (CardView) itemView.findViewById(R.id.cv_item_primary_category);
            mImvCategoryIcon = (ImageView) itemView.findViewById(R.id.imv_item_primary_category);
            mTxvCategoryName = (TextView) itemView.findViewById(R.id.txv_item_primary_category_name);
            mTxvCategoryGoalStatus = (TextView) itemView.findViewById(R.id.txv_item_primary_category_goal_status);
            mPgbCategoryGoalStatus = (ProgressBar) itemView.findViewById(R.id.pgb_item_primary_category_goal_status);
            mRvSubcategories = (RecyclerView) itemView.findViewById(R.id.rv_item_primary_categories_subcategory);
        }
    }
}
