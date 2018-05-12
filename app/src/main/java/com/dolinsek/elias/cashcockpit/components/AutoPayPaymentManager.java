package com.dolinsek.elias.cashcockpit.components;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import com.dolinsek.elias.cashcockpit.R;

import java.util.ArrayList;

/**
 * Created by Elias Dolinsek on 12.05.2018 for cash-cockpit.
 */
public class AutoPayPaymentManager {

    private Context context;

    public AutoPayPaymentManager(Context context){
        this.context = context;
    }

    public ArrayList<AutoPay> manageAutoPaysPayments(){
        ArrayList<AutoPay> autoPaysWherePaymentsAreRequired = Database.Toolkit.getAutoPaysWherePaymentsAreRequired();
        if (autoPaysWherePaymentsAreRequired.size() != 0){
            Database.Toolkit.manageAllPayments();
        }

        return autoPaysWherePaymentsAreRequired;
    }

    public void manageAutoPayPaymentsAndDisplayNotifications(){
        ArrayList<AutoPay> autoPaysWherePaymentsWereRequired = manageAutoPaysPayments();
        for (AutoPay autoPay:autoPaysWherePaymentsWereRequired){
            Notification notification = getAutoPayPaymentNotification(autoPay);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(123, notification); //TODO
        }
    }

    private Notification getAutoPayPaymentNotification(AutoPay autoPay){
        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.ic_notifications_24dp)
                .setContentTitle("Yay! It works! (" + autoPay.getName() + ")")
                .setContentText("TODO change texts!");

        return notificationBuilder.build();
    }
}
