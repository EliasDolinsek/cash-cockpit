package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private static final String EXTRA_SELECTED_ARRANGEMENT_FILTER = "selected_index_main_filter";
    private static final String EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER = "selected_index_bill_type_filter";

    private RecyclerView mRvHistory;
    private ArrayList<Bill> billsToDisplay;
    private NotEnoughDataFragment mFgmNotEnoughData;
    private int selectedArrangementFilter, selectedIndexBillTypeFilter;

    private ChipGroup chipGroupBillTypes, chipGroupArrangement;
    private Chip chipInput, chipOutput, chipTransfer, chipNewestFirst, chipOldestFirst, chipHighestFirst, chipLowestFirst;
    private int preivousCheckedArrangementChip;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_history, container, false);

        mRvHistory = inflatedView.findViewById(R.id.rv_history);
        mRvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        mFgmNotEnoughData = (NotEnoughDataFragment) getChildFragmentManager().findFragmentById(R.id.fgm_history_not_enough_data_for_history);

        chipGroupBillTypes = inflatedView.findViewById(R.id.chip_group_bill_types);
        chipGroupArrangement = inflatedView.findViewById(R.id.chip_group_chip_arrangement);

        chipInput = inflatedView.findViewById(R.id.chip_history_input);
        chipOutput = inflatedView.findViewById(R.id.chip_history_output);
        chipTransfer = inflatedView.findViewById(R.id.chip_history_transfer);

        chipNewestFirst = inflatedView.findViewById(R.id.chip_history_newest_first);
        chipOldestFirst = inflatedView.findViewById(R.id.chip_history_oldest_first);
        chipHighestFirst = inflatedView.findViewById(R.id.chip_history_highest_first);
        chipLowestFirst = inflatedView.findViewById(R.id.chip_history_lowest_first);

        billsToDisplay = Toolkit.getAllBills();
        if (savedInstanceState != null){
            setupSelections(savedInstanceState);
            setupChipsStates();
            applyBillsFilterOnBillsArrayList(selectedIndexBillTypeFilter);
        }

        setupChips();

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        reloadRecyclerView(selectedArrangementFilter);
        manageViews();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_ARRANGEMENT_FILTER, selectedArrangementFilter);
        outState.putInt(EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER, selectedIndexBillTypeFilter);
    }

    private void setupSelections(Bundle savedInstanceState){
        selectedIndexBillTypeFilter = savedInstanceState.getInt(EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER, 0);
        selectedArrangementFilter = savedInstanceState.getInt(EXTRA_SELECTED_ARRANGEMENT_FILTER, 0);
    }

    private void setupChips(){
        chipGroupBillTypes.setOnCheckedChangeListener((chipGroup, i) -> {
            switch (chipGroup.getCheckedChipId()){
                case R.id.chip_history_input: applyBillsFilterOnBillsArrayList(Bill.TYPE_INPUT); break;
                case R.id.chip_history_output: applyBillsFilterOnBillsArrayList(Bill.TYPE_OUTPUT); break;
                case R.id.chip_history_transfer: applyBillsFilterOnBillsArrayList(Bill.TYPE_TRANSFER); break;
                default: billsToDisplay = Toolkit.getAllBills(); break;
            }

            reloadRecyclerView(selectedArrangementFilter);
        });

        chipGroupArrangement.setOnCheckedChangeListener((chipGroup, i) -> {
            switch (chipGroup.getCheckedChipId()){
                case R.id.chip_history_newest_first: selectedArrangementFilter = 0; break;
                case R.id.chip_history_oldest_first: selectedArrangementFilter = 1; break;
                case R.id.chip_history_highest_first: selectedArrangementFilter = 2; break;
                case R.id.chip_history_lowest_first: selectedArrangementFilter = 3; break;
                default: ((Chip)chipGroup.getChildAt(preivousCheckedArrangementChip)).setChecked(true); break;
            }

            preivousCheckedArrangementChip = selectedArrangementFilter;
            reloadRecyclerView(selectedArrangementFilter);
        });
    }

    private void setupChipsStates(){
        switch (selectedIndexBillTypeFilter){
            case Bill.TYPE_INPUT: chipInput.setChecked(true); break;
            case Bill.TYPE_OUTPUT: chipOutput.setChecked(true); break;
            case Bill.TYPE_TRANSFER: chipTransfer.setChecked(true); break;
        }

        switch (selectedArrangementFilter){
            case 0: chipNewestFirst.setChecked(true); break;
            case 1: chipOldestFirst.setChecked(true); break;
            case 2: chipHighestFirst.setChecked(true); break;
            case 3: chipLowestFirst.setChecked(true); break;
        }
    }

    private void applyBillsFilterOnBillsArrayList(int selectedIndexBillTypeFilter){
        billsToDisplay = Toolkit.getAllBills();
        billsToDisplay = Toolkit.filterBillsByType(billsToDisplay, selectedIndexBillTypeFilter);
    }

    private void reloadRecyclerView(int selectedIndexMainFilter){
        switch (selectedIndexMainFilter){
            case HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST));
                break;
            case HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST));
                break;
            case HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST));
                break;
            case HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST));
                break;
                default: throw new IllegalArgumentException("Couldn't resolve " + selectedIndexMainFilter + " as a valid selection");
        }
    }

    private void manageViews(){
        if (getSizeOfBillsInDatabase() != 0){
            mFgmNotEnoughData.hide();
        } else {
            mFgmNotEnoughData.show();
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
