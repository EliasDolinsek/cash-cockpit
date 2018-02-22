package com.dolinsek.elias.cashcockpit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_history, container, false);

        mRvHistory = inflatedView.findViewById(R.id.rv_history);
        mRvHistory.setAdapter(new HistoryItemAdapter());
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

        return inflatedView;
    }

}
