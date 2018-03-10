package com.dolinsek.elias.cashcockpit;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dolinsek.elias.cashcockpit.components.Database;

import org.json.JSONException;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment{

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
     * FloatingActionButton to create a new category
     */
    private FloatingActionButton mFbtnAdd;

    /**
     * Adapter what displays primary categories
     */
    private PrimaryCategoryItemAdapter mPrimaryCategoryItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_categories, container, false);

        mRvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_fragment_categories);

        mPrimaryCategoryItemAdapter = new PrimaryCategoryItemAdapter(Database.getPrimaryCategories(), PrimaryCategoryItemAdapter.TYPE_NORMAL);
        mRvCategories.setAdapter(mPrimaryCategoryItemAdapter);

        mRvCategories.setLayoutManager(new LinearLayoutManager(inflatedView.getContext()));

        mBtnCreateCategory = (Button) inflatedView.findViewById(R.id.btn_fragment_categories_create);
        mBtnRestoreDefaultCategories = (Button) inflatedView.findViewById(R.id.btn_fragment_categories_restore_default_categories);
        mFbtnAdd = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_fragment_categories);

        mFbtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start CategoryActivity
                Intent intent = new Intent(container.getContext(), CategoryActivity.class);
                startActivity(intent);
            }
        });

        mBtnCreateCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start CategoryActivity
                Intent intent = new Intent(container.getContext(), CategoryActivity.class);
                startActivity(intent);
            }
        });

        mBtnRestoreDefaultCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Add default categories to categories
                for(int i = 0; i<Database.getDefaultPrimaryCategories().size(); i++){
                    Database.getPrimaryCategories().add(Database.getDefaultPrimaryCategories().get(i));
                }

                try {
                    //Save data
                    Database.save(getContext());

                    //Reload data
                    Database.load(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Reset Button-visibilities
                setVisibilities();

                //Load categories
                mRvCategories.setAdapter((mPrimaryCategoryItemAdapter = new PrimaryCategoryItemAdapter(Database.getPrimaryCategories(), PrimaryCategoryItemAdapter.TYPE_NORMAL)));
            }
        });

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Reset Button-visibilities
        setVisibilities();

        //Load bank accounts
        mPrimaryCategoryItemAdapter = new PrimaryCategoryItemAdapter(Database.getPrimaryCategories(), PrimaryCategoryItemAdapter.TYPE_NORMAL);
        mRvCategories.setAdapter(mPrimaryCategoryItemAdapter);
    }

    /**
     * Displays all Buttons and hides FloatingActionButton when there are zero categories,
     * hides all Buttons and shows FloatingActionButton when there are more than zero categories
     */
    private void setVisibilities(){
        if(Database.getPrimaryCategories().size() != 0){
            mBtnCreateCategory.setVisibility(View.GONE);
            mBtnRestoreDefaultCategories.setVisibility(View.GONE);
            mFbtnAdd.setVisibility(View.VISIBLE);
        } else{
            mBtnCreateCategory.setVisibility(View.VISIBLE);
            mBtnRestoreDefaultCategories.setVisibility(View.VISIBLE);
            mFbtnAdd.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //Saves data to save categories-favored states
        Database.save(getContext());
    }
}
