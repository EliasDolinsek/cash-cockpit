package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private RecyclerView mRvHistory;
    private Spinner mSpnFilter;
    private TextView mTxvNoDataForHistory;
    private HistoryItemAdapter mHistoryItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_history, container, false);

        mRvHistory = inflatedView.findViewById(R.id.rv_history);
        mRvHistory.setAdapter((mHistoryItemAdapter = new HistoryItemAdapter(HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST, null)));
        mRvHistory.setLayoutManager(new LinearLayoutManager(getContext()));

        mTxvNoDataForHistory = inflatedView.findViewById(R.id.txv_history_no_data_for_history);

        //Gets size of bills
        int bills = 0;
        for(BankAccount bankAccount: Database.getBankAccounts()){
            bills += bankAccount.getBills().size();
        }

        if(bills != 0)
            mTxvNoDataForHistory.setVisibility(View.GONE);

        mSpnFilter = inflatedView.findViewById(R.id.spn_history_filter);
        ArrayAdapter<String> filterItems = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.filters_array));
        filterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnFilter.setAdapter(filterItems);
        mSpnFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0: mRvHistory.setAdapter((mHistoryItemAdapter = new HistoryItemAdapter(HistoryItemAdapter.FILTER_NEWEST_ITEM_FIRST, null)));
                        break;
                    case 1: mRvHistory.setAdapter((mHistoryItemAdapter = new HistoryItemAdapter(HistoryItemAdapter.FILTER_OLDEST_ITEM_FIRST, null)));
                        break;
                    case 2: mRvHistory.setAdapter((mHistoryItemAdapter = new HistoryItemAdapter(HistoryItemAdapter.FILTER_LOWEST_PRICE_FIRST, null)));
                        break;
                    default: mRvHistory.setAdapter((mHistoryItemAdapter = new HistoryItemAdapter(HistoryItemAdapter.FILTER_HIGHEST_PRICE_FIRST, null)));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return inflatedView;
    }

}
