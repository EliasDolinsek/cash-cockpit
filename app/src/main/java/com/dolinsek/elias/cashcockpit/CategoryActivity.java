package com.dolinsek.elias.cashcockpit;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.CategoriesSorter;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.dolinsek.elias.cashcockpit.components.Subcategory;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity {

    public static final String EXTRA_PRIMARY_CATEGORY_INDEX = "primaryCategoryIndex";
    public static final String EXTRA_SUBCATEGORY_TO_SHOW_INDEX = "subcategoryToShow";

    private TextInputLayout tilName;
    private TextInputEditText edtName;

    private Button mBtnSetGoal, mBtnAddSubcategory;
    private PrimaryCategory primaryCategory;
    private RecyclerView mRvSubcategories;
    private SubcategoryItemAdapter mSubcategoryItemAdapter;
    private boolean mEditMode, changesSaved = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        tilName = findViewById(R.id.til_category_name);
        edtName = findViewById(R.id.edt_category_name);

        mBtnSetGoal = findViewById(R.id.btn_category_set_goal);
        mBtnAddSubcategory = findViewById(R.id.btn_category_add_subcategory);

        mRvSubcategories = findViewById(R.id.rv_category_subcategories);
        mRvSubcategories.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        if (getIntent().hasExtra(EXTRA_PRIMARY_CATEGORY_INDEX)) {
            mEditMode = true;
            primaryCategory = Database.getPrimaryCategories().get(getIntent().getIntExtra(EXTRA_PRIMARY_CATEGORY_INDEX, 0));
            edtName.setText(primaryCategory.getName());
        } else {
            primaryCategory = new PrimaryCategory("", null);
        }

        setupSubcategoriesAdapter();
        setupButtons();
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

    private void createOrSaveCategoryIfPossible(){
        String enteredName = edtName.getText().toString();
        boolean nameAlreadyExists = doesPrimaryCategoryNameAlreadyExist(enteredName);

        if (enteredName.trim().equals("")) {
            Toolkit.displayPleaseCheckInputsToast(getApplicationContext());
        } else if (nameAlreadyExists && !mEditMode) {
            Toast.makeText(CategoryActivity.this, getString(R.string.toast_category_already_exists), Toast.LENGTH_SHORT).show();
        } else {
            changePrimaryCategoryName(enteredName);

            if (mEditMode) {
                Database.save(getApplicationContext());
            } else {
                Database.getPrimaryCategories().add(primaryCategory);
                Database.save(getApplicationContext());
            }

            changesSaved = true;
            finish();
        }
    }

    private void showDeleteCategoryDialog(){
        DeletePrimaryCategoryDialogFragment deletePrimaryCategoryDialogFragment = new DeletePrimaryCategoryDialogFragment();
        deletePrimaryCategoryDialogFragment.setAutoPaysToDelete(getAutoPays());
        deletePrimaryCategoryDialogFragment.setDialogClickListener(this::deletePrimaryCategory);
        deletePrimaryCategoryDialogFragment.show(getSupportFragmentManager(), "delete_category");
    }

    private void deletePrimaryCategory(){
        //Deletes AutoPay
        ArrayList<AutoPay> autoPaysToDelete = getAutoPays();
        for(int i = 0; i<autoPaysToDelete.size(); i++)
            Database.getAutoPays().remove(autoPaysToDelete.get(i));

        //Deletes associated bills
        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (int i = 0; i<bankAccount.getBills().size(); i++){
                if (bankAccount.getBills().get(i).getSubcategory().getPrimaryCategory().equals(primaryCategory)){
                    bankAccount.getBills().remove(i);
                }
            }
        }

        Database.getPrimaryCategories().remove(primaryCategory);
        Database.save(getApplicationContext());

        //Go back to MainActivity
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (getIntent().hasExtra(EXTRA_SUBCATEGORY_TO_SHOW_INDEX)){
            SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();

            int indexOfSubcategoryInPrimaryCategory = getIntent().getIntExtra(EXTRA_SUBCATEGORY_TO_SHOW_INDEX, 0);
            subcategoryEditorDialogFragment.setupForEditMode(primaryCategory, getSubcategoryInPrimaryCategoryOfIndex(primaryCategory, indexOfSubcategoryInPrimaryCategory));
            subcategoryEditorDialogFragment.setOnDismissListener(dialogInterface -> {
                CategoriesSorter.sortPrimaryCategoriesIfPreferenceIsChecked(getApplicationContext(), Database.getPrimaryCategories());
                setupSubcategoriesAdapter();

            });
            subcategoryEditorDialogFragment.show(getSupportFragmentManager(), "edit_subcategory");
        }
    }

    private Subcategory getSubcategoryInPrimaryCategoryOfIndex(PrimaryCategory primaryCategory, int index){
        return primaryCategory.getSubcategories().get(index);
    }

    private void setupSubcategoriesAdapter(){
        mRvSubcategories.setAdapter((mSubcategoryItemAdapter = SubcategoryItemAdapter.getNormalSubcategoryItemAdapter(primaryCategory, SubcategoryItemAdapter.ON_SUBCATEGORY_CLICK_ACTION_OPEN_EDITOR)));
    }

    private boolean doesPrimaryCategoryNameAlreadyExist(String nameOfPrimaryCategory){
        for (int i = 0; i < Database.getPrimaryCategories().size(); i++) {
            if (Database.getPrimaryCategories().get(i).getName().equals(nameOfPrimaryCategory)){
                return true;
            }
        }

        return false;
    }

    private void changePrimaryCategoryName(String newName){
        updateNewCategoryNameInComponents(primaryCategory.getName(), newName);
        primaryCategory.setName(newName);
    }

    private void updateNewCategoryNameInComponents(String oldName, String newName){
        for (Subcategory subcategory:primaryCategory.getSubcategories()){
            subcategory.setPrimaryCategoryName(newName);
        }

        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                if (bill.getPrimaryCategoryName().equals(oldName)){
                    bill.setPrimaryCategoryName(newName);
                }
            }
        }

        for (AutoPay autoPay:Database.getAutoPays()){
            if (autoPay.getBill().getPrimaryCategoryName().equals(oldName)){
                autoPay.getBill().setPrimaryCategoryName(newName);
            }
        }
    }

    private void setupButtons(){
        mBtnAddSubcategory.setOnClickListener(view -> {
            SubcategoryEditorDialogFragment subcategoryEditorDialogFragment = new SubcategoryEditorDialogFragment();
            subcategoryEditorDialogFragment.setupForCreateMode(primaryCategory);
            subcategoryEditorDialogFragment.setOnDismissListener(dialogInterface -> setupSubcategoriesAdapter());

            subcategoryEditorDialogFragment.show(getSupportFragmentManager(), "new_subcategory");
        });

        mBtnSetGoal.setOnClickListener(view -> {
            GoalDialogFragment goalDialogFragment = new GoalDialogFragment();
            goalDialogFragment.setPrimaryAccount(primaryCategory);
            goalDialogFragment.show(getSupportFragmentManager(), "goal");
        });

        Button btnCreateSave = findViewById(R.id.btn_category_create_save), btnCancelDelete = findViewById(R.id.btn_category_cance_delete);
        btnCreateSave.setOnClickListener(v -> createOrSaveCategoryIfPossible());

        if (mEditMode){
            btnCreateSave.setText(getString(R.string.btn_save));
            btnCancelDelete.setText(getString(R.string.btn_delete));
            btnCancelDelete.setOnClickListener(v -> showDeleteCategoryDialog());
        } else {
            btnCreateSave.setText(getString(R.string.btn_create));
            btnCancelDelete.setText(getString(R.string.btn_cancel));
            btnCancelDelete.setOnClickListener(v -> finish());
        }
    }
}