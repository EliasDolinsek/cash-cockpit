package com.dolinsek.elias.cashcockpit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Currencies;
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

        //Converts the type of the AutoPay to a string
        String type = holder.itemView.getContext().getResources().getString(R.string.label_none);
        switch (autoPay.getType()){
            case AutoPay.TYPE_WEEKLY: type = holder.itemView.getContext().getResources().getString(R.string.label_weekly);
                break;
            case AutoPay.TYPE_MONTHLY: type = holder.itemView.getContext().getResources().getString(R.string.label_monthly);
                break;
            case AutoPay.TYPE_YEARLY: type = holder.itemView.getContext().getResources().getString(R.string.label_yearly);
                break;
        }

        //Displays data
        holder.mTxvName.setText(autoPay.getName());
        holder.mTxvDetails.setText(type + " " + Character.toString((char)0x00B7) + " " + Currencies.EURO.getCurrency().format(autoPay.getBill().getAmount()) + " " + Character.toString((char)0x00B7) + " " + autoPay.getBankAccount().getName());
    }

    @Override
    public int getItemCount() {
        return autoPays.size();
    }

    public class AutoPayItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvName, mTxvDetails;

        public AutoPayItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_auto_pays_name);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_auto_pays_details);
        }
    }
}
