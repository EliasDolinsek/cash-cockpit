package com.dolinsek.elias.cashcockpit;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dolinsek.elias.cashcockpit.components.CategoriesSorter;
import com.dolinsek.elias.cashcockpit.components.Database;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment implements DialogInterface.OnDismissListener{

    /**
     * Displays a list of all categories
     */
    private RecyclerView mRvCategories;

    /**
     * Button to create a new category
     */
    private Button mBtnCreateCategory;

    /**
     * Button to restore all default categories
     */
    private Button mBtnRestoreDefaultCategories;

    /**
     * Adapter what displays primary categories
     */
    private PrimaryCategoryItemAdapter mPrimaryCategoryItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_categories, container, false);

        mRvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_fragment_categories);

        mPrimaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getNormalPrimaryCategoryAdapter(Database.getPrimaryCategories());
        mRvCategories.setAdapter(mPrimaryCategoryItemAdapter);
        mRvCategories.setLayoutManager(new LinearLayoutManager(inflatedView.getContext()));
        mRvCategories.setItemAnimator(new DefaultItemAnimator(){
            @Override public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) { return true; }
            @Override public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) { return true; }
        });

        mBtnCreateCategory = inflatedView.findViewById(R.id.btn_fragment_categories_create);
        mBtnRestoreDefaultCategories = inflatedView.findViewById(R.id.btn_fragment_categories_restore_default_categories);

        mBtnCreateCategory.setOnClickListener(view -> {
            Intent intent = new Intent(container.getContext(), CategoryActivity.class);
            startActivity(intent);
        });

        mBtnRestoreDefaultCategories.setOnClickListener(view -> {
            restoreDefaultCategories();
        });

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setVisibilities();

        setupPrimaryCategoryItemAdapter();
        mRvCategories.setAdapter(mPrimaryCategoryItemAdapter);
    }

    private void setVisibilities(){
        if(Database.getPrimaryCategories().size() != 0){
            mBtnRestoreDefaultCategories.setVisibility(View.GONE);
        } else{
            mBtnRestoreDefaultCategories.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CategoriesSorter.sortPrimaryCategoriesIfPreferenceIsChecked(getActivity(), Database.getPrimaryCategories());
        Database.save(getContext());
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        setupPrimaryCategoryItemAdapter();
        mRvCategories.setAdapter(mPrimaryCategoryItemAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        //Saves data if user changed something in SubcategoryEditorDialogFragment
        Database.save(getContext());
    }

    private void setupPrimaryCategoryItemAdapter(){
        mPrimaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getNormalPrimaryCategoryAdapter(Database.getPrimaryCategories());
    }

    private void restoreDefaultCategories(){
        addDefaultPrimaryCategoriesToPrimaryCategories();

        try {
            Database.save(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setVisibilities();
        mRvCategories.setAdapter((mPrimaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getNormalPrimaryCategoryAdapter(Database.getPrimaryCategories())));
    }

    private void addDefaultPrimaryCategoriesToPrimaryCategories(){
        Database.getPrimaryCategories().addAll(Database.getDefaultPrimaryCategories());
    }
}
