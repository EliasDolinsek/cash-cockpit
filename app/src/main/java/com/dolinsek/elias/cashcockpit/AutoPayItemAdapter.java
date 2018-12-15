package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.Toolkit;

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
    public void onBindViewHolder(final AutoPayItemViewHolder holder, final int position) {
        AutoPay autoPay = autoPays.get(position);
        Context context = holder.itemView.getContext();

        String autoPayTypeAsString = getAutoPayTypeAsString(autoPay.getType(), context);
        String formattedAmount = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithoutCentsWithCurrencySymbol(autoPay.getBill().getAmount());
        String details = context.getResources().getString(R.string.label_item_auto_pays_details, autoPayTypeAsString, formattedAmount, autoPay.getBankAccountName());

        holder.mTxvName.setText(autoPay.getName());
        holder.mTxvDetails.setText(details);
        setupBillTypeIndicator(holder, autoPay);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(holder.itemView.getContext(), AutoPayActivity.class);
            intent.putExtra(AutoPayActivity.EXTRA_AUTO_PAY_INDEX, position);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return autoPays.size();
    }

    public class AutoPayItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvName, mTxvDetails;

        public AutoPayItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_name);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_details);
        }
    }

    private String getAutoPayTypeAsString(int autoPayType, Context context){
        switch (autoPayType){
            case AutoPay.TYPE_WEEKLY: return context.getString(R.string.label_weekly);
            case AutoPay.TYPE_MONTHLY: return context.getString(R.string.label_monthly);
            case AutoPay.TYPE_YEARLY: return context.getString(R.string.label_yearly);
        }

        throw new IllegalArgumentException("Couldn't resolve " + autoPayType + " as an AutoPay-Type");
    }

    private void setupBillTypeIndicator(AutoPayItemViewHolder holder, AutoPay autoPay){
        Context context = holder.itemView.getContext();
        TextView txvBillTypeIndicator = holder.itemView.findViewById(R.id.txv_item_auto_pay_bill_type);

        switch (autoPay.getBill().getType()){
            case Bill.TYPE_INPUT: {
                txvBillTypeIndicator.setTextColor(context.getResources().getColor(R.color.colorBillTypeInput));
                txvBillTypeIndicator.setText(context.getString(R.string.label_input));
            } break;
            case Bill.TYPE_OUTPUT: {
                txvBillTypeIndicator.setTextColor(context.getResources().getColor(R.color.colorBillTypeOutput));
                txvBillTypeIndicator.setText(context.getString(R.string.label_output));
            } break;
        }


    }
}
