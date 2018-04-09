package com.dolinsek.elias.cashcockpit;


import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.text.DateFormat;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private static final String EXTRA_SELECTED_FILTER = "extra_selected_filter";

    private RecyclerView mRvHistory;
    private Spinner mSpnFilterMain, mSpnFilterBillTypes;
    private TextView mTxvNoDataForHistory;

    private ArrayList<Bill> billsToDisplay;
    private int selectedMainFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_history, container, false);

        mRvHistory = inflatedView.findViewById(R.id.rv_history);
        mRvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        mTxvNoDataForHistory = inflatedView.findViewById(R.id.txv_history_no_data_for_history);
        mSpnFilterMain = inflatedView.findViewById(R.id.spn_history_filter_main);
        mSpnFilterBillTypes = inflatedView.findViewById(R.id.spn_history_filter_bill_type);

        billsToDisplay = getAllBillsInDatabase();

        setupSpinnerMain();
        setupSpinnerBillTypes();

        if(savedInstanceState != null){
            mSpnFilterMain.setSelection(savedInstanceState.getInt(EXTRA_SELECTED_FILTER, 0));
        }

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(getAllBillsInDatabase(), HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST));
        manageViews();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_FILTER, mSpnFilterMain.getSelectedItemPosition());
    }

    private void resetFilters(){
        mSpnFilterMain.setSelection(0);
        mSpnFilterBillTypes.setSelection(0);

        reloadRecyclerView(selectedMainFilter);
    }

    private ArrayList<Bill> getAllBillsInDatabase(){
        ArrayList<Bill> allBills = new ArrayList<>();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            allBills.addAll(bankAccount.getBills());
        }

        return allBills;
    }

    private void reloadRecyclerView(int selectedItemOfSpinner){
        switch (selectedItemOfSpinner){
            case 0: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST));
                break;
            case 1: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST));
                break;
            case 2: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST));
                break;
            default: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST));
        }
    }

    private void setupSpinnerMain(){
        ArrayAdapter<String> filterItems = new ArrayAdapter<String>(getContext(), R.layout.costum_spinner_layout, getResources().getStringArray(R.array.filters_array));
        filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpnFilterMain.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        mSpnFilterMain.setAdapter(filterItems);

        mSpnFilterMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMainFilter = i;
                reloadRecyclerView(selectedMainFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupSpinnerBillTypes(){
        ArrayAdapter<String> filterItems = new ArrayAdapter<>(getContext(), R.layout.costum_spinner_layout, getBillsTypesAsStringIncludingAll());
        filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpnFilterBillTypes.getBackground().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        mSpnFilterBillTypes.setAdapter(filterItems);
        mSpnFilterBillTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                billsToDisplay = getAllBillsInDatabase();

                if (index == 1){
                    billsToDisplay = filterBillsBillType(billsToDisplay, Bill.TYPE_INPUT);
                } else if (index == 2){
                    billsToDisplay = filterBillsBillType(billsToDisplay, Bill.TYPE_OUTPUT);
                } else if (index == 3){
                    billsToDisplay = filterBillsBillType(billsToDisplay, Bill.TYPE_TRANSFER);
                }

                reloadRecyclerView(selectedMainFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private ArrayList<Bill> filterBillsBillType(ArrayList<Bill> billsToFilter, int billTypeToFilter){
        ArrayList<Bill> filteredBills = new ArrayList<>();

        for (Bill bill:billsToFilter){
            if (bill.getType() == billTypeToFilter){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    private String[] getBillsTypesAsStringIncludingAll(){
        String[] billTypes = getResources().getStringArray(R.array.bill_types_array);
        String[] billTypesIncludingAll = new String[billTypes.length + 1];

        for (int i = 0; i<billTypesIncludingAll.length; i++){
            if (i == 0){
                billTypesIncludingAll[i] = getString(R.string.label_all_bills);
            } else {
                billTypesIncludingAll[i] = billTypes[i - 1];
            }
        }

        return billTypesIncludingAll;
    }
    private void manageViews(){
        if (getSizeOfBillsInDatabase() != 0){
            mTxvNoDataForHistory.setVisibility(View.GONE);
        } else {
            mTxvNoDataForHistory.setVisibility(View.VISIBLE);
        }
    }

    private int getSizeOfBillsInDatabase(){
        int size = 0;
        for (BankAccount bankAccount:Database.getBankAccounts()){
            size += bankAccount.getBills().size();
        }

        return size;
    }
}
