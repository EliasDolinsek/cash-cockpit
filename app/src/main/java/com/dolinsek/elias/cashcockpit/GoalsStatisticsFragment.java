package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class GoalsStatisticsFragment extends Fragment {

    private RecyclerView mRvCategories;
    private ArrayList<Subcategory> subcategoriesToRestore = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_goals_statistics, container, false);

        mRvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_goals_statistics_categories);
        mRvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvCategories.setAdapter(new PrimaryCategoryItemAdapter(getCategories(), PrimaryCategoryItemAdapter.TYPE_GOAL_STATISTICS));

        return inflatedView;
    }

    private ArrayList<PrimaryCategory> getCategories(){
        ArrayList<PrimaryCategory> primaryCategories = new ArrayList<>();

        for(PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            if(primaryCategory.getGoal().getAmount() != 0){
                primaryCategories.add(primaryCategory);
            }
        }

        return primaryCategories;
    }
}
