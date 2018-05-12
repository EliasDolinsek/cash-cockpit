package com.dolinsek.elias.cashcockpit;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.AutoPayPaymentManager;
import com.dolinsek.elias.cashcockpit.components.Database;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class DayChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_DATE_CHANGED)){
            if (!Database.isLoaded()){
                initDatabase(context);
            }

            AutoPayPaymentManager autoPayPaymentManager = new AutoPayPaymentManager(context);
            autoPayPaymentManager.manageAutoPayPaymentsAndDisplayNotifications();
        }
    }

    private void initDatabase(Context context){
        try {
            Database.load(context);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
