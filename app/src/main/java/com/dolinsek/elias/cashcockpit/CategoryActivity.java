package com.dolinsek.elias.cashcockpit;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.CategoriesSorter;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity implements DeletePrimaryCategoryDialogFragment.DeletePrimaryCategoryListener {

    public static final String EXTRA_PRIMARY_CATEGORY_INDEX = "primaryCategoryIndex";
    public static final String EXTRA_SUBCATEGORY_TO_SHOW_INDEX = "subcategoryToShow";

    private Button mBtnCreate, mBtnDelete, mBtnSetGoal, mBtnAddSubcategory;
    private EditText mEdtCategoryName;
    private TextInputLayout mTextInputLayout;

    private PrimaryCategory primaryCategory;

    private RecyclerView mRvSubcategories;
    private SubcategoryItemAdapter mSubcategoryItemAdapter;
    private boolean mEditMode, changesSaved = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mBtnCreate = (Button) findViewById(R.id.btn_category_create);
        mBtnDelete = (Button) findViewById(R.id.btn_category_delete);

        mBtnSetGoal = (Button) findViewById(R.id.btn_category_set_goal);
        mBtnAddSubcategory = (Button) findViewById(R.id.btn_category_add_subcategory);

        mEdtCategoryName = (EditText) findViewById(R.id.edt_category_name);
        mTextInputLayout = (TextInputLayout) findViewById(R.id.til_category_name);

        mRvSubcategories = (RecyclerView) findViewById(R.id.rv_category_subcategories);
        mRvSubcategories.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if (getIntent().hasExtra(EXTRA_PRIMARY_CATEGORY_INDEX)) {
            setupForEditMode();
        } else {
            setupForNormalMode();
        }

        setupRecyclerViewAdapter();

        mBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameOfPrimaryCategory = mEdtCategoryName.getText().toString();
                boolean nameAlreadyExists = doesNameForPrimaryCategoryAlreadyExist(nameOfPrimaryCategory);

                if (mEdtCategoryName.getText().toString().trim().equals("")) {
                    mTextInputLayout.setError(getResources().getString(R.string.label_enter_category_name));
                } else if (nameAlreadyExists && !mEditMode) {
                    mTextInputLayout.setError(getResources().getString(R.string.label_category_name_already_exists));
                } else {
                    primaryCategory.setName(mEdtCategoryName.getText().toString());

                    if (!mEditMode) {
                        Database.getPrimaryCategories().add(primaryCategory);
                    }

                    Database.save(getApplicationContext());
                    changesSaved = true;
                    finish();
                }
            }
        });

        mBtnAddSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();
                subcategoryEditorDialogFragment.setupForCreateMode(primaryCategory);
                subcategoryEditorDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        setupRecyclerViewAdapter();
                    }
                });
                subcategoryEditorDialogFragment.show(getSupportFragmentManager(), "new_subcategory");
            }
        });

        mBtnSetGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoalDialogFragment goalDialogFragment = new GoalDialogFragment();
                goalDialogFragment.setPrimaryAccount(primaryCategory);
                goalDialogFragment.show(getSupportFragmentManager(), "goal");
            }
        });

        mBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeletePrimaryCategoryDialogFragment deletePrimaryCategoryDialogFragment = new DeletePrimaryCategoryDialogFragment();
                deletePrimaryCategoryDialogFragment.setAutoPaysToDelete(getAutoPaysOfPrimaryCategory());
                deletePrimaryCategoryDialogFragment.show(getSupportFragmentManager(), "delete_category");
            }
        });
    }

    private ArrayList<AutoPay> getAutoPaysOfPrimaryCategory(){
        ArrayList<AutoPay> autoPaysOfPrimaryCategory = new ArrayList<>();
        for (AutoPay autoPay:Database.getAutoPays()){
            if (autoPay.getBill().getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                autoPaysOfPrimaryCategory.add(autoPay);
            }
        }

        return autoPaysOfPrimaryCategory;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            discardChanges();
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        discardChanges();
    }

    @Override
    public void onDialogPositiveClick() {
        deleteAutoPaysOfPrimaryCategory();
        deleteBillsOfPrimaryCategory();

        Database.getPrimaryCategories().remove(primaryCategory);
        Database.save(getApplicationContext());

        finish();
    }

    private void deleteAutoPaysOfPrimaryCategory(){
        Database.getAutoPays().removeAll(getAutoPaysOfPrimaryCategory());
    }

    private void deleteBillsOfPrimaryCategory(){
        for (BankAccount bankAccount:Database.getBankAccounts()){
            ArrayList<Bill> billsToDeleteOfCurrentBankAccount = new ArrayList<>();
            for (Bill bill:bankAccount.getBills()){
                if (bill.getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                    billsToDeleteOfCurrentBankAccount.add(bill);
                }
            }

            bankAccount.getBills().removeAll(billsToDeleteOfCurrentBankAccount);
        }
    }

    private void discardChanges() {
        try {
            Database.load(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().hasExtra(EXTRA_SUBCATEGORY_TO_SHOW_INDEX)){
            SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();
            int indexOfSubcategoryInPrimaryCategory = getIntent().getIntExtra(EXTRA_SUBCATEGORY_TO_SHOW_INDEX, 0);

            subcategoryEditorDialogFragment.setupForEditMode(primaryCategory, getSubcategoryInPrimaryCategoryOfIndex(primaryCategory, indexOfSubcategoryInPrimaryCategory));
            subcategoryEditorDialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    CategoriesSorter.sortPrimaryCategories(Database.getPrimaryCategories());
                    setupRecyclerViewAdapter();
                }
            });
            subcategoryEditorDialogFragment.show(getSupportFragmentManager(), "edit_subcategory");
        }
    }

    private Subcategory getSubcategoryInPrimaryCategoryOfIndex(PrimaryCategory primaryCategory, int index){
        return primaryCategory.getSubcategories().get(index);
    }

    private void setupForEditMode(){
        int indexOfPrimaryCategoryInDatabase = getIntent().getIntExtra(EXTRA_PRIMARY_CATEGORY_INDEX, 0);
        primaryCategory = Database.getPrimaryCategories().get(indexOfPrimaryCategoryInDatabase);
        mEdtCategoryName.setText(primaryCategory.getName());

        mEditMode = true;
    }

    private void setupForNormalMode(){
        primaryCategory = new PrimaryCategory("", null);
        mBtnCreate.setText(getResources().getString(R.string.btn_create));
        mBtnDelete.setVisibility(View.GONE);

        mEditMode = false;
    }

    private boolean doesNameForPrimaryCategoryAlreadyExist(String nameOfPrimaryCategory){
        for (PrimaryCategory primaryCategory:Database.getPrimaryCategories()){
            if (primaryCategory.getName().equals(nameOfPrimaryCategory)){
                return true;
            }
        }

        return false;
    }

    private void setupRecyclerViewAdapter(){
        mSubcategoryItemAdapter = SubcategoryItemAdapter.getNormalSubcategoryItemAdapter(primaryCategory, SubcategoryItemAdapter.ON_SUBCATEGORY_CLICK_ACTION_OPEN_EDITOR);
        mRvSubcategories.setAdapter(mSubcategoryItemAdapter);
    }
}