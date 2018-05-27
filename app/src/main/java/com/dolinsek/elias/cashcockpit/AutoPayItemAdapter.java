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
        String formattedAmount = Currency.getActiveCurrency(holder.itemView.getContext()).formatAmountToReadableStringWithCurrencySymbol(autoPay.getBill().getAmount());
        String details = formattedAmount + " " + Character.toString((char)0x00B7) + " " + autoPay.getBankAccount().getName() + " " + Character.toString((char)0x00B7) + " " + autoPayTypeAsString;

        holder.mTxvName.setText(autoPay.getName());
        holder.mTxvDetails.setText(details);
        setupImageViewDependingOnBillType(autoPay.getBill(), holder.mImvBillType);

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
        public ImageView mImvBillType;
        public CardView mCardView;

        public AutoPayItemViewHolder(View itemView) {
            super(itemView);

            mTxvName = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_name);
            mTxvDetails = (TextView) itemView.findViewById(R.id.txv_item_auto_pay_details);
            mImvBillType = (ImageView) itemView.findViewById(R.id.imv_item_auto_pay_bill_type);
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

    private void setupImageViewDependingOnBillType(Bill bill, ImageView imageView){
        Context context = imageView.getContext();
        switch (bill.getType()){
            case Bill.TYPE_INPUT: imageView.setImageDrawable(context.getDrawable(R.drawable.ic_bill_type_input));
                break;
            case Bill.TYPE_OUTPUT: imageView.setImageDrawable(context.getDrawable(R.drawable.ic_bill_type_output));
                break;
            case Bill.TYPE_TRANSFER: imageView.setImageDrawable(context.getDrawable(R.drawable.ic_bill_type_transfer));
                break;
            default: throw new IllegalArgumentException("Couldn't resolve " + bill.getType() + " as a valid bill-type");
        }
    }
}
