package com.dolinsek.elias.cashcockpit;

import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.dolinsek.elias.cashcockpit.model.AutoPay;
import com.dolinsek.elias.cashcockpit.model.Database;
import com.dolinsek.elias.cashcockpit.model.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.model.Subcategory;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity implements DialogInterface.OnDismissListener, DeletePrimaryCategoryDialogFragment.DeletePrimaryCategoryListener {

    public static final String EXTRA_PRIMARY_CATEGORY_INDEX = "primaryCategoryIndex";

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

        //Don't show keyboard automatically
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
            mEditMode = true;
            primaryCategory = Database.getPrimaryCategories().get(getIntent().getIntExtra(EXTRA_PRIMARY_CATEGORY_INDEX, 0));
            mEdtCategoryName.setText(primaryCategory.getName());
        } else {
            primaryCategory = new PrimaryCategory("", null);
            mBtnCreate.setText(getResources().getString(R.string.btn_create));
            mBtnDelete.setVisibility(View.GONE);
        }

        mRvSubcategories.setAdapter((mSubcategoryItemAdapter = new SubcategoryItemAdapter(primaryCategory, true)));

        mBtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean nameAlreadyExists = false;
                for (int i = 0; i < Database.getPrimaryCategories().size(); i++) {
                    if (Database.getPrimaryCategories().get(i).getName().equals(mEdtCategoryName.getText().toString()))
                        nameAlreadyExists = true;
                }

                if (mEdtCategoryName.getText().toString().trim().equals("")) {
                    mTextInputLayout.setError(getResources().getString(R.string.label_enter_category_name));
                } else if (nameAlreadyExists && !mEditMode) {
                    mTextInputLayout.setError(getResources().getString(R.string.label_category_name_already_exists));
                } else {
                    //Set name
                    primaryCategory.setName(mEdtCategoryName.getText().toString());

                    if (mEditMode)
                        Database.save(getApplicationContext());
                    else {
                        //Add and save data
                        Database.getPrimaryCategories().add(primaryCategory);
                        Database.save(getApplicationContext());
                    }

                    changesSaved = true;
                    //Go back to MainActivity
                    finish();
                }
            }
        });

        mBtnAddSubcategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();
                subcategoryEditorDialogFragment.setPrimaryCategory(primaryCategory);
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
                deletePrimaryCategoryDialogFragment.setAutoPaysToDelete(getAutoPays());
                deletePrimaryCategoryDialogFragment.show(getSupportFragmentManager(), "delete_category");
            }
        });
    }

    /**
     * @return associated AutoPays
     */
    private ArrayList<AutoPay> getAutoPays(){
        ArrayList<AutoPay> autoPays = new ArrayList<>();
        for(int i = 0; i<Database.getAutoPays().size(); i++){
            for(int y = 0; y<primaryCategory.getSubcategories().size(); y++){
                if(Database.getAutoPays().get(i).getBill().getSubcategory().equals(primaryCategory.getSubcategories().get(y)))
                    autoPays.add(Database.getAutoPays().get(i));
            }
        }

        return autoPays;
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
    public void onDismiss(DialogInterface dialogInterface) {
        mRvSubcategories.setAdapter((mSubcategoryItemAdapter = new SubcategoryItemAdapter(primaryCategory, true)));
    }

    @Override
    public void onDialogPositiveClick() {
        //Delete AutoPay
        ArrayList<AutoPay> autoPaysToDelete = getAutoPays();
        for(int i = 0; i<autoPaysToDelete.size(); i++)
            Database.getAutoPays().remove(autoPaysToDelete.get(i));

        Database.getPrimaryCategories().remove(primaryCategory);
        Database.save(getApplicationContext());

        //Go back to MainActivity
        finish();
    }

    private void discardChanges() {
        try {
            Database.load(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}