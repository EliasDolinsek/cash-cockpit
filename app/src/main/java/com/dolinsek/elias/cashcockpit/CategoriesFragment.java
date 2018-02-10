package com.dolinsek.elias.cashcockpit;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.dolinsek.elias.cashcockpit.model.Database;

import org.json.JSONException;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends Fragment{

    private RecyclerView mRvCategories;
    private Button mBtnCreateCategory, mBtnRestoreDefaultCategories;
    private FloatingActionButton mFbtnAdd;
    private PrimaryCategoryItemAdapter mPrimaryCategoryItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_categories, container, false);

        mRvCategories = (RecyclerView) inflatedView.findViewById(R.id.rv_fragment_categories);

        mPrimaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
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

                //Save Data
                Database.save(getContext());
                try {
                    Database.load(getContext());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setVisibilities();

                //Load categories
                mRvCategories.setAdapter((mPrimaryCategoryItemAdapter = new PrimaryCategoryItemAdapter()));
            }
        });

        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        setVisibilities();

        //Load bank accounts
        mPrimaryCategoryItemAdapter = new PrimaryCategoryItemAdapter();
        mRvCategories.setAdapter(mPrimaryCategoryItemAdapter);
    }

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

        //To save subcategory favored state
        Database.save(getContext());
    }
}
