package com.dolinsek.elias.cashcockpit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;

/**
 * Created by elias on 02.02.2018.
 */

public class AutoPayItemAdapter extends RecyclerView.Adapter<AutoPayItemAdapter.AutoPayItemViewHolder> {

    private ArrayList<AutoPay> autoPays;

    public AutoPayItemAdapter(){
        autoPays = Database.getAutoPays();
    }

    @Override
    public AutoPayItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new AutoPayItemViewHolder(layoutInflater.inflate(R.layout.list_item_auto_pay, parent, false));
    }

    @Override
    public void onBindViewHolder(AutoPayItemViewHolder holder, int position) {
        AutoPay autoPay = autoPays.get(position);

        holder.mTxvName.setText(autoPay.getName());
        holder.mTxvBankAccount.setText(autoPay.getBankAccount().getName());
        holder.mTxvCategory.setText(autoPay.getBill().getSubcategory().getName());
    }

    @Override
    public int getItemCount() {
        return autoPays.size();
    }

    public class AutoPayItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvName, mTxvBankAccount, mTxvCategory;

        public AutoPayItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_auto_pays_name);
            mTxvBankAccount = (TextView) itemView.findViewById(R.id.txv_item_auto_pays_bank_account);
            mTxvCategory = (TextView) itemView.findViewById(R.id.txv_item_auto_pays_category);
        }
    }
}
