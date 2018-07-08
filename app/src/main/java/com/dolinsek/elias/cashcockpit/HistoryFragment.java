package com.dolinsek.elias.cashcockpit;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private static final String EXTRA_SELECTED_INDEX_MAIN_FILTER = "selected_index_main_filter";
    private static final String EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER = "selected_index_bill_type_filter";

    private RecyclerView mRvHistory;
    private TextView mTxvSelectedFilters;
    private ArrayList<Bill> billsToDisplay;
    private Button mBtnShowFilters;
    private HistoryFragmentFiltersDialogFragment historyFragmentFiltersDialogFragment;
    private NotEnoughDataFragment mFgmNotEnoughData;
    private int selectedIndexMainFilter, selectedIndexBillTypeFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_history, container, false);

        mRvHistory = inflatedView.findViewById(R.id.rv_history);
        mRvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        mFgmNotEnoughData = (NotEnoughDataFragment) getChildFragmentManager().findFragmentById(R.id.fgm_history_not_enough_data_for_history);
        mTxvSelectedFilters = inflatedView.findViewById(R.id.txv_history_selected_filters);
        mBtnShowFilters = inflatedView.findViewById(R.id.btn_history_show_filters);

        billsToDisplay = Database.Toolkit.getAllBillsInDatabase();
        createHistoryFragmentFiltersDialogFragment();
        setupBillsToDisplayFromSavedInstanceState(savedInstanceState);

        mBtnShowFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                historyFragmentFiltersDialogFragment.setFiltersSelection(selectedIndexMainFilter, selectedIndexBillTypeFilter);
                historyFragmentFiltersDialogFragment.show(getActivity().getFragmentManager(), "select_filter");
            }
        });
        return inflatedView;
    }

    @Override
    public void onStart() {
        super.onStart();

        setupHistoryFiltersDialogFragment();
        reloadRecyclerView(selectedIndexMainFilter);
        manageViews();
        displayHowManyBillsAreInDatabaseOnSelectedFilterTxv();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_INDEX_MAIN_FILTER, selectedIndexMainFilter);
        outState.putInt(EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER, selectedIndexBillTypeFilter);
    }

    private void displayHowManyBillsAreInDatabaseOnSelectedFilterTxv(){
        int billsInDatabase = Database.Toolkit.getAllBillsInDatabase().size();
        mTxvSelectedFilters.setText(getString(R.string.label_bills_in_database, billsInDatabase));
    }

    private void createHistoryFragmentFiltersDialogFragment(){
        HistoryFragmentFiltersDialogFragment historyFragmentFiltersDialogFragment = (HistoryFragmentFiltersDialogFragment) getActivity().getFragmentManager().findFragmentByTag("select_filter");
        if (historyFragmentFiltersDialogFragment != null){
            this.historyFragmentFiltersDialogFragment = historyFragmentFiltersDialogFragment;
        } else {
            this.historyFragmentFiltersDialogFragment = new HistoryFragmentFiltersDialogFragment();
        }
    }

    private void setupBillsToDisplayFromSavedInstanceState(Bundle savedInstanceState){
        if (savedInstanceState != null){
            selectedIndexMainFilter = savedInstanceState.getInt(EXTRA_SELECTED_INDEX_MAIN_FILTER, 0);
            selectedIndexBillTypeFilter = savedInstanceState.getInt(EXTRA_SELECTED_INDEX_BILL_TYPE_FILTER, 0);

            filterBillsToDisplayDependingOnSelectedBillTypeFilter(selectedIndexBillTypeFilter);
            reloadRecyclerView(selectedIndexMainFilter);
            displaySelectedFilters();
        }
    }

    private void setupHistoryFiltersDialogFragment(){
        historyFragmentFiltersDialogFragment.setOnFilterSelectedListener(new OnFilterSelectedListener() {
            @Override
            public void onFilterSelected(int selectedIndexInMainFilter, int selectedIndexInBillTypeFilter) {
                selectedIndexMainFilter = selectedIndexInMainFilter;
                selectedIndexBillTypeFilter = selectedIndexInBillTypeFilter;

                billsToDisplay = Database.Toolkit.getAllBillsInDatabase();
                filterBillsToDisplayDependingOnSelectedBillTypeFilter(selectedIndexInBillTypeFilter);

                reloadRecyclerView(selectedIndexMainFilter);
                displaySelectedFilters();
            }
        });
    }

    private void displaySelectedFilters(){
        String selectedBillTypeInFilterAsString = getSelectedBillTypeInFilterAsString(historyFragmentFiltersDialogFragment);
        String selectedMainFilterSelection = getSelectionOfMainFilterAsString();
        String activeFilters = selectedBillTypeInFilterAsString + " " + Character.toString((char)0x00B7) + " " + selectedMainFilterSelection;

        mTxvSelectedFilters.setText(activeFilters);
    }

    private String getSelectedBillTypeInFilterAsString(HistoryFragmentFiltersDialogFragment historyFragmentFiltersDialogFragment){
        String[] billTypeSelections = historyFragmentFiltersDialogFragment.getBillsTypesAsStringIncludingAll();
        return billTypeSelections[selectedIndexBillTypeFilter];
    }

    private void filterBillsToDisplayDependingOnSelectedBillTypeFilter(int selectedIndexBillTypeFilter){
        if (selectedIndexBillTypeFilter != 0){
            billsToDisplay = Database.Toolkit.filterBillsOfBillType(billsToDisplay, selectedIndexBillTypeFilter - 1);
        }
    }

    private String getSelectionOfMainFilterAsString(){
        String[] mainFilterSelections = getResources().getStringArray(R.array.filters_array);
        return mainFilterSelections[selectedIndexMainFilter];
    }

    private void reloadRecyclerView(int selectedIndexMainFilter){
        switch (selectedIndexMainFilter){
            case HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST));
                break;
            case HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST));
                break;
            case HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST));
                break;
            case HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST: mRvHistory.setAdapter(HistoryItemAdapter.getDefaultHistoryItemAdapter(billsToDisplay, HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST));
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

    public static class HistoryFragmentFiltersDialogFragment extends DialogFragment {

        private Spinner mSpnFilterMain, mSpnFilterBillTypes;
        private int selectedIndexMainFilter, selectedIndexBillTypeFilter;

        private OnFilterSelectedListener onFilterSelectedListener;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
            View inflatedView = layoutInflater.inflate(R.layout.dialog_histroy_filters, null);

            mSpnFilterMain = inflatedView.findViewById(R.id.spn_dialog_history_filter_main);
            mSpnFilterBillTypes = inflatedView.findViewById(R.id.spn_dialog_history_filter_bill_type);

            setupSpinnerMain();
            setupSpinnerBillTypes();

            mSpnFilterMain.setSelection(selectedIndexMainFilter);
            mSpnFilterBillTypes.setSelection(selectedIndexBillTypeFilter);

            builder.setTitle(R.string.label_filters);
            builder.setPositiveButton(R.string.label_close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismiss();
                }
            });

            builder.setView(inflatedView);
            return builder.create();
        }

        public void setFiltersSelection(int selectedIndexMainFilter, int selectedIndexBillTypeFilter){
            this.selectedIndexMainFilter = selectedIndexMainFilter;
            this.selectedIndexBillTypeFilter = selectedIndexBillTypeFilter;
        }

        private void setupSpinnerMain(){
            ArrayAdapter<String> filterItems = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.filters_array));
            filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mSpnFilterMain.setAdapter(filterItems);
            mSpnFilterMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    selectedIndexMainFilter = i;
                    notifyThatSelectionHasChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        private void setupSpinnerBillTypes(){
            ArrayAdapter<String> filterItems = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, getBillsTypesAsStringIncludingAll());
            filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mSpnFilterBillTypes.setAdapter(filterItems);
            mSpnFilterBillTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    selectedIndexBillTypeFilter = index;
                    notifyThatSelectionHasChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

        public String[] getBillsTypesAsStringIncludingAll(){
            String[] billTypes = getResources().getStringArray(R.array.bill_types_array);
            String[] billTypesIncludingAll = new String[billTypes.length + 1];

            for (int i = 0; i<billTypesIncludingAll.length; i++){
                if (i == 0){
                    billTypesIncludingAll[i] = getString(R.string.label_all_bill_types);
                } else {
                    billTypesIncludingAll[i] = billTypes[i - 1];
                }
            }

            return billTypesIncludingAll;
        }

        private void notifyThatSelectionHasChanged(){
            if (onFilterSelectedListener != null){
                onFilterSelectedListener.onFilterSelected(selectedIndexMainFilter, selectedIndexBillTypeFilter);
            }
        }

        public void setOnFilterSelectedListener(OnFilterSelectedListener onFilterSelectedListener){
            this.onFilterSelectedListener = onFilterSelectedListener;
        }
    }

    public static interface OnFilterSelectedListener{
        public void onFilterSelected(int selectedIndexInMainFilter, int selectedIndexInBillTypeFilter);
    }
}
