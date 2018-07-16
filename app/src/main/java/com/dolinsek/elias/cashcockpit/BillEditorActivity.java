package com.dolinsek.elias.cashcockpit;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.dolinsek.elias.cashcockpit.components.Database;

public class BillEditorActivity extends AppCompatActivity {

    public static final String EXTRA_BILL_TO_EDIT = "extra_bill";
    public static final String EXTRA_BILL_TO_EDIT_BANK_ACCOUNT = "extra_bill_bank_account";

    private OnSaveOrDeleteActionRequired onSaveOrDeleteActionRequired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_editor);

        CockpitFragment cockpitFragment = new CockpitFragment();
        cockpitFragment.setBillToEdit(Database.getBankAccounts().get(getIntent().getIntExtra(EXTRA_BILL_TO_EDIT_BANK_ACCOUNT, 0)).getBills().get(getIntent().getIntExtra(EXTRA_BILL_TO_EDIT, 0)));

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.ll_bill_editor, cockpitFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_delete_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (onSaveOrDeleteActionRequired != null){
            switch (item.getItemId()){
                case android.R.id.home: finish(); return true;
                case R.id.menu_save: onSaveOrDeleteActionRequired.onSaveRequested(); return true;
                case R.id.menu_delete: onSaveOrDeleteActionRequired.onDeleteRequested(); return true;
                default: return super.onOptionsItemSelected(item);
            }
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void setOnSaveOrDeleteActionRequired(OnSaveOrDeleteActionRequired onSaveOrDeleteActionRequired){
        this.onSaveOrDeleteActionRequired = onSaveOrDeleteActionRequired;
    }

    public static interface OnSaveOrDeleteActionRequired{
        public void onSaveRequested();
        public void onDeleteRequested();
    }
}
