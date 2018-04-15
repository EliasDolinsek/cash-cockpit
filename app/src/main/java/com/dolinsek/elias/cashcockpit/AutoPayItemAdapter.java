package com.dolinsek.elias.cashcockpit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Bill;
import com.dolinsek.elias.cashcockpit.components.Currency;
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
    public void onBindViewHolder(final AutoPayItemViewHolder holder, final int position) {
        AutoPay autoPay = autoPays.get(position);
        Context context = holder.itemView.getContext();

        holder.mTxvName.setText(autoPay.getName());

        String autoPayTypeAsString = getAutoPayTypeAsString(autoPay.getType(), context);
        holder.mTxvAutoPayType.setText(autoPayTypeAsString);

        String billTypeAsString = getBillTypeAsString(autoPay.getBill().getType(), context);
        holder.mTxvAutoPayBillType.setText(billTypeAsString);
        setTxvBackgroundDependingAutoPayBillType(autoPay.getBill().getType(), holder.mTxvAutoPayBillType);

        String formattedAmount = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(autoPay.getBill().getAmount());
        String details = formattedAmount + " " + Character.toString((char)0x00B7) + " " + autoPay.getBankAccount().getName();
        holder.mTxvDetails.setText(details);

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), AutoPayActivity.class);
                intent.putExtra(AutoPayActivity.EXTRA_AUTO_PAY_INDEX, position);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return autoPays.size();
    }

    public class AutoPayItemViewHolder extends RecyclerView.ViewHolder{

        public TextView mTxvName, mTxvAutoPayType, mTxvAutoPayBillType, mTxvDetails;
        public CardView mCardView;

        public AutoPayItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_name);
            mTxvAutoPayType = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_type);
            mTxvAutoPayBillType = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_bill_type);

            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_details);
            mCardView = (CardView) itemView.findViewById(R.id.cv_item_auto_pay);
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

    private String getBillTypeAsString(int billType, Context context){
        switch (billType){
            case Bill.TYPE_INPUT: return context.getString(R.string.label_input);
            case Bill.TYPE_OUTPUT: return context.getString(R.string.label_output);
            case Bill.TYPE_TRANSFER: return context.getString(R.string.label_transfer);
        }

        throw new IllegalArgumentException("Couldn't resolve " + billType + " as a Bill-Type");
    }

    private void setTxvBackgroundDependingAutoPayBillType(int autoPayBillType, TextView textView){
        switch (autoPayBillType){
            case Bill.TYPE_INPUT: textView.setBackgroundResource(R.drawable.border_green); return;
            case Bill.TYPE_OUTPUT: textView.setBackgroundResource(R.drawable.border_red); return;
            case Bill.TYPE_TRANSFER: textView.setBackgroundResource(R.drawable.border_orange); return;
        }
    }
}
