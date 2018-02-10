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

import com.dolinsek.elias.cashcockpit.model.AutoPay;
import com.dolinsek.elias.cashcockpit.model.Bill;
import com.dolinsek.elias.cashcockpit.model.Database;

import org.json.JSONException;

import java.io.IOException;


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
                Database.getAutoPays().add(new AutoPay(new Bill(10000, "description", Database.getPrimaryCategories().get(0).getSubcategories().get(0)), 0, "Name", Database.getBankAccounts().get(0))); //TODO delte
                Database.save(getContext());
                try {
                    Database.load(getContext());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

        mRvAutoPays.setAdapter((mAutoPayItemAdapter = new AutoPayItemAdapter()));
    }

}
