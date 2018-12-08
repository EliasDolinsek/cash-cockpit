package com.dolinsek.elias.cashcockpit;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Database;

import org.json.JSONException;

import java.io.IOException;


public class AutoPaysFragment extends Fragment {

    private Button mBtnCreateAutoPay;
    private RecyclerView mRvAutoPays;
    private AutoPayItemAdapter mAutoPayItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_auto_pays, container, false);

        mBtnCreateAutoPay = inflatedView.findViewById(R.id.btn_auto_pays_create_autopay);
        mRvAutoPays = inflatedView.findViewById(R.id.rv_auto_pays);
        mRvAutoPays.setLayoutManager(new LinearLayoutManager(getContext()));

        mBtnCreateAutoPay.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AutoPayActivity.class);
            startActivity(intent);
        });
        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mRvAutoPays.setAdapter((mAutoPayItemAdapter = new AutoPayItemAdapter()));
    }

}
