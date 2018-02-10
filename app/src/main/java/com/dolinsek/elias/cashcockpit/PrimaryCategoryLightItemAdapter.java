package com.dolinsek.elias.cashcockpit;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

import java.util.ArrayList;

/**
 * Created by elias on 06.02.2018.
 */

public class PrimaryCategoryLightItemAdapter extends RecyclerView.Adapter<PrimaryCategoryLightItemAdapter.PrimaryCategoryLightItemViewHolder> {

    private ArrayList<PrimaryCategory> primaryCategories;
    private SubcategorySelectionListener subcategorySelectionListener;

    public PrimaryCategoryLightItemAdapter(SubcategorySelectionListener subcategorySelectionListener){
        primaryCategories = Database.getPrimaryCategories();
        this.subcategorySelectionListener = subcategorySelectionListener;
    }

    @Override
    public PrimaryCategoryLightItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new PrimaryCategoryLightItemViewHolder(layoutInflater.inflate(R.layout.list_item_primary_category_light, parent, false));
    }

    @Override
    public void onBindViewHolder(PrimaryCategoryLightItemViewHolder holder, int position) {
        PrimaryCategory primaryCategory = primaryCategories.get(position);

        try{
            holder.mImvPrimaryCategoryIcon.setBackgroundResource(holder.itemView.getContext().getResources().getIdentifier(primaryCategory.getIconName(), "drawable", holder.itemView.getContext().getPackageName()));
        } catch (Exception e) {
            holder.mImvPrimaryCategoryIcon.setBackgroundResource(R.drawable.ic_default_category_image);
        }

        holder.mTxvPrimaryCategoryName.setText(primaryCategory.getName());
        holder.mRvPrimaryCategorySubcategories.setAdapter(new SubcategoryLightItemAdapter(primaryCategory));
        holder.mRvPrimaryCategorySubcategories.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
    }

    @Override
    public int getItemCount() {
        return primaryCategories.size();
    }

    class PrimaryCategoryLightItemViewHolder extends RecyclerView.ViewHolder{

        public ImageView mImvPrimaryCategoryIcon;
        public TextView mTxvPrimaryCategoryName;
        public RecyclerView mRvPrimaryCategorySubcategories;

        public PrimaryCategoryLightItemViewHolder(View itemView) {
            super(itemView);

            mImvPrimaryCategoryIcon = (ImageView) itemView.findViewById(R.id.imv_item_primary_category_light);
            mTxvPrimaryCategoryName = (TextView) itemView.findViewById(R.id.txv_item_primary_category_light);
            mRvPrimaryCategorySubcategories = (RecyclerView) itemView.findViewById(R.id.rv_item_primary_categories_light_subcategories);
        }
    }

    private class SubcategoryLightItemAdapter extends RecyclerView.Adapter<SubcategoryLightItemAdapter.SubcategoryLightItemViewHolder>{

        private PrimaryCategory primaryCategory;

        public SubcategoryLightItemAdapter(PrimaryCategory primaryCategory){
            this.primaryCategory = primaryCategory;
        }

        @Override
        public SubcategoryLightItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            return new SubcategoryLightItemViewHolder(layoutInflater.inflate(R.layout.list_item_subcategory_light, parent, false));
        }

        @Override
        public void onBindViewHolder(SubcategoryLightItemViewHolder holder, final int position) {
            holder.mTxvSubcategoryName.setText(primaryCategory.getSubcategories().get(position).getName());
            holder.mBtnSelectSubcategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    subcategorySelectionListener.onSubcategorySelected(primaryCategory.getSubcategories().get(position));
                }
            });
        }

        @Override
        public int getItemCount() {
            return primaryCategory.getSubcategories().size();
        }


        class SubcategoryLightItemViewHolder extends RecyclerView.ViewHolder{

            public TextView mTxvSubcategoryName;
            public Button mBtnSelectSubcategory;

            public SubcategoryLightItemViewHolder(View itemView) {
                super(itemView);

                mTxvSubcategoryName = (TextView) itemView.findViewById(R.id.txv_item_subcategory_light_name);
                mBtnSelectSubcategory = (Button) itemView.findViewById(R.id.btn_item_subcategory_light_select);
            }
        }
    }

    public interface SubcategorySelectionListener{
        public void onSubcategorySelected(Subcategory subcategory);
    }
}
