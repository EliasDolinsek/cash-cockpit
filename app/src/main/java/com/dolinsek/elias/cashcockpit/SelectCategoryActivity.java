package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SubscriptionManager;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private PrimaryCategoryItemAdapter primaryCategoryItemAdapter;
    private ScrollView scrollView;
    private ImageView imvClose;
    private int selectedPrimaryCategoryIndex, selectedSubcategoryIndex;
    private Button mBtnCreateCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_select_category);
        mBtnCreateCategory = findViewById(R.id.btn_select_category_create_category);
        scrollView = findViewById(R.id.sv_select_category);
        imvClose = findViewById(R.id.imv_select_category_close);

        //Prevents ScrollView from scrolling down automatically
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));

        mBtnCreateCategory.setOnClickListener(view -> {
            Intent intentStartCategoryActivity = new Intent(getApplicationContext(), CategoryActivity.class);
            startActivityForResult(intentStartCategoryActivity, 0);
        });

        imvClose.setOnClickListener(view -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();

        setupRecyclerView();
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

    private void setupRecyclerView(){
        final SubcategoryItemAdapter.OnCategorySelectedListener onSubcategorySelectedListener = (primaryCategoryIndex, subcategoryIndex) -> {
            selectedPrimaryCategoryIndex = primaryCategoryIndex;
            selectedSubcategoryIndex = subcategoryIndex;
            setResultsAndFinishActivity();
        };

        final Intent intent = getIntent();
        setupPrimaryCategoryItemAdapter(intent, onSubcategorySelectedListener);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(primaryCategoryItemAdapter);
        mRecyclerView.setHasFixedSize(false);
    }

    private void setupViewsVisibilities(){
        if (Database.getPrimaryCategories().size() == 0){
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupPrimaryCategoryItemAdapter(Intent intent, SubcategoryItemAdapter.OnCategorySelectedListener onSubcategorySelectedListener){
        if (intent != null && intent.hasExtra(SELECTED_PRIMARY_CATEGORY_INDEX) && intent.hasExtra(SELECTED_SUBCATEGORY_INDEX)) {
            Subcategory selectedSubcategory = getSelectedSubcategoryFromIntentExtras();
            primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getSelectCategoryPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), selectedSubcategory, onSubcategorySelectedListener);
        } else {
            primaryCategoryItemAdapter = PrimaryCategoryItemAdapter.getSelectCategoryPrimaryCategoryItemAdapter(Database.getPrimaryCategories(), onSubcategorySelectedListener);
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
