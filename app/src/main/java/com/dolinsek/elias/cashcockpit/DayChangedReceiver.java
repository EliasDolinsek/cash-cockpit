package com.dolinsek.elias.cashcockpit;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.Database;

import java.util.ArrayList;

public class DayChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
            ArrayList<AutoPay> autoPaysWherePaymentsAreRequired = new ArrayList<>();
            if (autoPaysWherePaymentsAreRequired.size() != 0){
                Database.Toolkit.manageAllPayments();
            }

            for (AutoPay autoPay:autoPaysWherePaymentsAreRequired){
                Notification notification = getAutoPayPaymentNotification(autoPay, context);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(123, notification); //TODO
            }
        }
    }

    private Notification getAutoPayPaymentNotification(AutoPay autoPay, Context context){
        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.ic_notifications_24dp)
        .setContentTitle("Yay! It works! (" + autoPay.getName() + ")")
        .setContentText("TODO change texts!");

        return notificationBuilder.build();
    }
}
