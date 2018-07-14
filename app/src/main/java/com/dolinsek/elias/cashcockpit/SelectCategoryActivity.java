package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

public class SelectCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_PRIMARY_CATEGORY_INDEX = "extra_primary_category";
    public static final String EXTRA_SUBCATEGORY_INDEX = "extra_subcategory";
    public static final String SELECTED_PRIMARY_CATEGORY_INDEX = "selected_private_category";
    public static final String SELECTED_SUBCATEGORY_INDEX = "selected_subcategory";

    private RecyclerView mRecyclerView;
    private NotEnoughDataFragment mFgmNoCategoriesFound;
    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter;
    private int selectedPrimaryCategoryIndex, selectedSubcategoryIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_select_category);
        mFgmNoCategoriesFound = (NotEnoughDataFragment) getSupportFragmentManager().findFragmentById(R.id.fgm_select_category_no_categories_found);

        SubcategoryItemAdapter.OnCategorySelectedListener onSubcategorySelectedListener = new SubcategoryItemAdapter.OnCategorySelectedListener() {
            @Override
            public void onSelected(int primaryCategoryIndex, int subcategoryIndex) {
                selectedPrimaryCategoryIndex = primaryCategoryIndex;
                selectedSubcategoryIndex = subcategoryIndex;
                setResultsAndFinishActivity();
            }
        };

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(SELECTED_PRIMARY_CATEGORY_INDEX) && intent.hasExtra(SELECTED_SUBCATEGORY_INDEX)) {
            Subcategory selectedSubcategory = getSelectedSubcategoryFromIntentExtras();
            primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getSelectCategoryPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), selectedSubcategory, onSubcategorySelectedListener);
        } else {
            primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getSelectCategoryPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), onSubcategorySelectedListener);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(primaryCategoryItemAdapter);
        mRecyclerView.setHasFixedSize(false);

        setupViewsVisibilities();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewsVisibilities(){
        if (Database.getPrimaryCategories().size() == 0){
            mFgmNoCategoriesFound.show();
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mFgmNoCategoriesFound.hide();
        }
    }

    private void setResultsAndFinishActivity(){
        Intent resultIntent = new Intent();

        resultIntent.putExtra(EXTRA_PRIMARY_CATEGORY_INDEX, selectedPrimaryCategoryIndex);
        resultIntent.putExtra(EXTRA_SUBCATEGORY_INDEX, selectedSubcategoryIndex);
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    private Subcategory getSelectedSubcategoryFromIntentExtras(){
        Intent intent = getIntent();

        int primaryCategoryIndexInDatabase = intent.getIntExtra(SELECTED_PRIMARY_CATEGORY_INDEX, 0);
        int subcategoryIndexInPrimaryCategory = intent.getIntExtra(SELECTED_SUBCATEGORY_INDEX, 0);

        return Database.getPrimaryCategories().get(primaryCategoryIndexInDatabase).getSubcategories().get(subcategoryIndexInPrimaryCategory);
    }
}
