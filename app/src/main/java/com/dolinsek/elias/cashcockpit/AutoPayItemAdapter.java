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
import com.dolinsek.elias.cashcockpit.components.Currency;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;

/**
 * Created by elias on 02.02.2018.
 */

public class AutoPayItemAdapter extends RecyclerView.Adapter<AutoPayItemAdapter.AutoPayItemViewHolder> {

    private ArrayList<AutoPay> autoPays;

    public AutoPayItemAdapter(ArrayList<AutoPay> autoPaysToDisplay){
        autoPays = autoPaysToDisplay;
    }

    @Override
    public AutoPayItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new AutoPayItemViewHolder(layoutInflater.inflate(R.layout.list_item_auto_pay, parent, false));
    }

    @Override
    public void onBindViewHolder(final AutoPayItemViewHolder holder, final int position) {
        AutoPay autoPay = autoPays.get(position);
        displayAutoPayDetails(autoPay, holder);

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

        public TextView mTxvName, mTxvDetails;
        public CardView mCardView;

        public AutoPayItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_auto_pays_name);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_auto_pays_details);
            mCardView = (CardView) itemView.findViewById(R.id.cv_item_auto_pay);
        }
    }

    private String getAutoPayTypeAsSting(AutoPay autoPay, Context context){
        switch (autoPay.getType()){
            case AutoPay.TYPE_WEEKLY: return context.getResources().getString(R.string.label_weekly);
            case AutoPay.TYPE_MONTHLY: return  context.getString(R.string.label_monthly);
            case AutoPay.TYPE_YEARLY: return context.getResources().getString(R.string.label_yearly);
        }

        throw new IllegalArgumentException("Couldn't resolve " + autoPay.getType() + " as a auto-pay-type");
    }

    private void displayAutoPayDetails(AutoPay autoPay, AutoPayItemViewHolder holder){
        Context context = holder.itemView.getContext();

        String centerDot = Character.toString((char)0x00B7);
        String formattedType = getAutoPayTypeAsSting(autoPay, context);
        String formattedAmount = Currency.getActiveCurrency(context).formatAmountToReadableStringWithCurrencySymbol(autoPay.getBill().getAmount());
        String detailsToDisplay = formattedType + " " + centerDot + " " + formattedAmount + " " + centerDot + " " + autoPay.getBankAccount().getName();

        holder.mTxvName.setText(autoPay.getName());
        holder.mTxvDetails.setText(detailsToDisplay);
    }
}
