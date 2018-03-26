package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

public class SelectCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_PRIMARY_CATEGORY_INDEX = "extra_primary_category";
    public static final String EXTRA_SUBCATEGORY_INDEX = "extra_subcategory";
    public static final String SELECTED_PRIMARY_CATEGORY_INDEX = "selected_private_category";
    public static final String SELECTED_SUBCATEGORY_INDEX = "selected_subcategory";

    private RecyclerView mRecyclerView;
    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter;
    private int selectedPrimaryCategoryIndex, selectedSubcategoryIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_select_category);

        Intent intent = getIntent();
        SubcategoryItemAdapter.OnCategorySelectedListener onSubcategorySelectedListener = new SubcategoryItemAdapter.OnCategorySelectedListener() {
            @Override
            public void onSelected(int primaryCategoryIndex, int subcategoryIndex) {
                selectedPrimaryCategoryIndex = primaryCategoryIndex;
                selectedSubcategoryIndex = subcategoryIndex;

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_PRIMARY_CATEGORY_INDEX, selectedPrimaryCategoryIndex);
                resultIntent.putExtra(EXTRA_SUBCATEGORY_INDEX, selectedSubcategoryIndex);
                setResult(RESULT_OK, resultIntent);

                finish();
            }
        };

        if (intent != null && intent.hasExtra(SELECTED_PRIMARY_CATEGORY_INDEX) && intent.hasExtra(SELECTED_SUBCATEGORY_INDEX)) {
            primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getSelectCategoryPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), Database.getPrimaryCategories().get(intent.getIntExtra(SELECTED_PRIMARY_CATEGORY_INDEX, 0)).getSubcategories().get(intent.getIntExtra(SELECTED_SUBCATEGORY_INDEX, 0)), onSubcategorySelectedListener);
        } else {
            primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getSelectCategoryPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), onSubcategorySelectedListener);
        }

        mRecyclerView.setAdapter(primaryCategoryItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setHasFixedSize(false);
    }
}
