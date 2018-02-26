package com.dolinsek.elias.cashcockpit;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dolinsek.elias.cashcockpit.components.Database;

public class BillEditorActivity extends AppCompatActivity {

    public static final String EXTRA_BILL_TO_EDIT = "extra_bill";
    public static final String EXTRA_BILL_TO_EDIT_BANK_ACCOUNT = "extra_bill_bank_account";

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
}
