package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private static final String EXTRA_SELECTED_ARRANGEMENT_FILTER = "selected_index_main_filter";
    private static final String EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER = "selected_index_bill_type_filter";
    private static final String EXTRA_FILTERS_VISIBLE = "filters_shown";
    private static final String EXTRA_ITEM_ADAPTER_EXPANDED_ITEM_INDEX = "expanded_item_index";
    private static final String EXTRA_ITEM_ADAPTER_EDIT_ITEM_INDEX = "edit_item_index";
    private static final String EXTRA_ITEM_ADAPTER_EDIT_TYPE = "edit_type";

    private RecyclerView mRvHistory;
    private ArrayList<Bill> billsToDisplay;
    private NotEnoughDataFragment mFgmNotEnoughData;
    private int selectedArrangementFilter, selectedIndexBillTypeFilter;
    private Button btnFilters;
    private LinearLayout llRoot;

    private HorizontalScrollView svBillTypes, svArrangement;
    private ChipGroup chipGroupBillTypes, chipGroupArrangement;
    private Chip chipInput, chipOutput, chipTransfer, chipNewestFirst, chipOldestFirst, chipHighestFirst, chipLowestFirst;
    private int previousCheckedArrangementChip;
    private boolean filtersVisible;
    private HistoryItemAdapter historyItemAdapter;

    private Bundle savedInstanceState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_history, container, false);
        displayHowManyBillsAreInDatabase(inflatedView);
        this.savedInstanceState = savedInstanceState;

        mRvHistory = inflatedView.findViewById(R.id.rv_history);
        mRvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        mRvHistory.setItemAnimator(new DefaultItemAnimator(){
            @Override public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) { return true; }
            @Override public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) { return true; }
        });

        mFgmNotEnoughData = (NotEnoughDataFragment) getChildFragmentManager().findFragmentById(R.id.fgm_history_not_enough_data_for_history);
        btnFilters = inflatedView.findViewById(R.id.btn_history_filters);
        llRoot = inflatedView.findViewById(R.id.ll_history_root);

        chipGroupBillTypes = inflatedView.findViewById(R.id.chip_group_bill_types);
        chipGroupArrangement = inflatedView.findViewById(R.id.chip_group_chip_arrangement);

        chipInput = inflatedView.findViewById(R.id.chip_history_input);
        chipOutput = inflatedView.findViewById(R.id.chip_history_output);
        chipTransfer = inflatedView.findViewById(R.id.chip_history_transfer);

        chipNewestFirst = inflatedView.findViewById(R.id.chip_history_newest_first);
        chipOldestFirst = inflatedView.findViewById(R.id.chip_history_oldest_first);
        chipHighestFirst = inflatedView.findViewById(R.id.chip_history_highest_first);
        chipLowestFirst = inflatedView.findViewById(R.id.chip_history_lowest_first);

        svBillTypes = inflatedView.findViewById(R.id.sv_history_bill_types);
        svArrangement = inflatedView.findViewById(R.id.sv_history_arrangement);

        billsToDisplay = Toolkit.getAllBills();
        if (savedInstanceState != null){
            setupFiltersFromSavedInstanceState(savedInstanceState);
            applyBillsFilterOnBillsArrayList(selectedIndexBillTypeFilter);
            setupChipsStates();

        }

        setupChips();

        btnFilters.setOnClickListener(view -> {
            if (filtersVisible){
                hideFilters();
            } else {
                showFilters();
            }
        });

        reloadRecyclerView(selectedArrangementFilter);
        manageViews();

        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupRvHistoryStatesFromSavedInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_SELECTED_ARRANGEMENT_FILTER, selectedArrangementFilter);
        outState.putInt(EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER, selectedIndexBillTypeFilter);
        outState.putBoolean(EXTRA_FILTERS_VISIBLE, filtersVisible);

        if (historyItemAdapter != null){
            outState.putInt(EXTRA_ITEM_ADAPTER_EXPANDED_ITEM_INDEX, historyItemAdapter.getExpandedPosition());
            outState.putInt(EXTRA_ITEM_ADAPTER_EDIT_ITEM_INDEX, historyItemAdapter.getEditPosition());
            outState.putInt(EXTRA_ITEM_ADAPTER_EDIT_TYPE, historyItemAdapter.getEditType());
        }
    }

    private void setupFiltersFromSavedInstanceState(Bundle savedInstanceState){
        setupFiltersVisibilityStateFromSavedInstanceState(savedInstanceState);
        setupSelections(savedInstanceState);
    }

    private void setupFiltersVisibilityStateFromSavedInstanceState(Bundle savedInstanceState){
        if (savedInstanceState != null){
            if (savedInstanceState.getBoolean(EXTRA_FILTERS_VISIBLE)) {
                showFilters();
            } else {
                hideFilters();
            }
        }
    }

    private void showFilters(){
        svBillTypes.setVisibility(View.VISIBLE);
        svArrangement.setVisibility(View.VISIBLE);

        btnFilters.setText(R.string.btn_hide_filters);
        filtersVisible = true;
        TransitionManager.beginDelayedTransition(llRoot);
    }

    private void hideFilters(){
        svBillTypes.setVisibility(View.GONE);
        svArrangement.setVisibility(View.GONE);

        btnFilters.setText(R.string.btn_show_filters);
        filtersVisible = false;
        TransitionManager.beginDelayedTransition(llRoot);
    }

    private void setupRvHistoryStatesFromSavedInstanceState(Bundle savedInstanceState){
        if (savedInstanceState != null && historyItemAdapter != null){
            int expandedPosition = savedInstanceState.getInt(EXTRA_ITEM_ADAPTER_EXPANDED_ITEM_INDEX);
            int editPosition = savedInstanceState.getInt(EXTRA_ITEM_ADAPTER_EDIT_ITEM_INDEX);
            int editType = savedInstanceState.getInt(EXTRA_ITEM_ADAPTER_EDIT_TYPE);

            historyItemAdapter.setupStates(expandedPosition, editPosition, editType);
        }
    }

    public void hideExpandedHistoryItemAdapterItem(){
        if (historyItemAdapter != null){
            historyItemAdapter.hide();
        }
    }

    public boolean isHidingExpandedHistoryItemAdapterItemPossible(){
        if (historyItemAdapter != null){
            return historyItemAdapter.isHidingAnItemPossible();
        } else {
            return false;
        }
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
                default: ((Chip)chipGroup.getChildAt(previousCheckedArrangementChip)).setChecked(true); break;
            }

            previousCheckedArrangementChip = selectedArrangementFilter;
            reloadRecyclerView(selectedArrangementFilter);
        });
    }

    private void setupChipsStates(){
        switch (selectedIndexBillTypeFilter){
            case Bill.TYPE_INPUT: chipInput.setChecked(true); break;
            case Bill.TYPE_OUTPUT: chipOutput.setChecked(true); break;
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
            case HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST: mRvHistory.setAdapter((historyItemAdapter = HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST)));
                break;
            case HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST: mRvHistory.setAdapter((historyItemAdapter = HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST)));
                break;
            case HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST: mRvHistory.setAdapter((historyItemAdapter = HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST)));
                break;
            case HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST: mRvHistory.setAdapter((historyItemAdapter = HistoryItemAdapter.getDefaultHistoryItemAdapter(mRvHistory, billsToDisplay, HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST)));
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

    private void displayHowManyBillsAreInDatabase(View inflatedView){
        int billsInDatabase = Toolkit.getAllBills().size();
        ((TextView)inflatedView.findViewById(R.id.txv_history_bills_in_database)).setText(getString(R.string.label_bills_in_database, billsInDatabase));
    }
}
