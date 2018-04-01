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
import java.util.ArrayList;


public class AutoPaysFragment extends Fragment {

    private Button mBtnCreateAutoPay;
    private FloatingActionButton mFbtnAdd;
    private RecyclerView mRvAutoPays;
    private AutoPayItemAdapter mAutoPayItemAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_auto_pays, container, false);

        mBtnCreateAutoPay = (Button) inflatedView.findViewById(R.id.btn_auto_pays_create_autopay);
        mFbtnAdd = (FloatingActionButton) inflatedView.findViewById(R.id.fbtn_auto_pays_add);
        mRvAutoPays = (RecyclerView) inflatedView.findViewById(R.id.rv_auto_pays);
        mRvAutoPays.setLayoutManager(new LinearLayoutManager(getContext()));

        mFbtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start AutoPayActivity
                Intent intent = new Intent(getContext(), AutoPayActivity.class);
                startActivity(intent);
            }
        });

        mBtnCreateAutoPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Start AutoPayActivity
                Intent intent = new Intent(getContext(), AutoPayActivity.class);
                startActivity(intent);
            }
        });
        return inflatedView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(Database.getAutoPays().size() != 0){
            mBtnCreateAutoPay.setVisibility(View.GONE);
            mFbtnAdd.setVisibility(View.VISIBLE);
        } else {
            mFbtnAdd.setVisibility(View.GONE);
            mBtnCreateAutoPay.setVisibility(View.VISIBLE);
        }

        ArrayList<AutoPay> autoPays = Database.getAutoPays();
        mRvAutoPays.setAdapter((mAutoPayItemAdapter = new AutoPayItemAdapter(autoPays)));
    }

}
