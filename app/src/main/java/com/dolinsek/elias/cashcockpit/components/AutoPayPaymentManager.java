package com.dolinsek.elias.cashcockpit.components;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.CardView;

import com.dolinsek.elias.cashcockpit.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Elias Dolinsek on 12.05.2018 for cash-cockpit.
 */
public class AutoPayPaymentManager {

    private Context context;

    public AutoPayPaymentManager(Context context){
        this.context = context;
    }

    private Notification getAutoPayPaymentNotification(AutoPay autoPay){
        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.ic_notifications_24dp)
                .setContentTitle(autoPay.getName() + " has got added to the database");

        return notificationBuilder.build();
    }

    public ArrayList<AutoPay> getAutoPaysWherePaymentsAreRequired(){
        ArrayList<AutoPay> autoPaysWherePaymentsAreRequired = new ArrayList<>();
        for (AutoPay autoPay:Database.getAutoPays()){
            if (isPaymentForAutoPayRequired(autoPay)){
                autoPaysWherePaymentsAreRequired.add(autoPay);
            }
        }

        return autoPaysWherePaymentsAreRequired;
    }

    public void performPaymentsForAutoPays(ArrayList<AutoPay> autoPays){
        for (AutoPay autoPay:autoPays){
            long sizeOfRequiredPayments = getSizeOfRequiredPaymentsForAutoPays(autoPay);
            for(int i = 0; i<sizeOfRequiredPayments; i++){
                autoPay.addPayment(context);
            }
        }
    }

    private long getSizeOfRequiredPaymentsForAutoPays(AutoPay autoPay){
        int indexOfLastPayment = autoPay.getPayments().size() - 1;
        long lastPaymentOfAutoPay = autoPay.getPayments().get(indexOfLastPayment);
        long durationToLastPayment = getDurationOfLastPaymentToNow(lastPaymentOfAutoPay);

        return durationToLastPayment / getAutoPayTimeDifferenceBetweenPaymentsAsTimeStamp(autoPay);
    }

    private boolean isPaymentForAutoPayRequired(AutoPay autoPay){
        return getSizeOfRequiredPaymentsForAutoPays(autoPay) != 0;
    }

    private long getDurationOfLastPaymentToNow(long lastPaymentTimeStamp){
        return System.currentTimeMillis() - lastPaymentTimeStamp;
    }

    private long getAutoPayTimeDifferenceBetweenPaymentsAsTimeStamp(AutoPay autoPay){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0);

        if (autoPay.getType() == AutoPay.TYPE_WEEKLY){
            calendar.add(Calendar.WEEK_OF_MONTH, 1);
        } else if (autoPay.getType() == AutoPay.TYPE_MONTHLY) {
            calendar.add(Calendar.MONTH, 1);
        } else if (autoPay.getType() == AutoPay.TYPE_YEARLY){
            calendar.add(Calendar.YEAR, 1);
        } else {
            throw new IllegalArgumentException("Couldn't resolve " + autoPay.getType() + " as a valid auto-pay-type");
        }

        return calendar.getTimeInMillis();
    }

}
