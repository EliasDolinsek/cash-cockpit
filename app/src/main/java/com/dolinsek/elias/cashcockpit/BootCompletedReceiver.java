package com.dolinsek.elias.cashcockpit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;

import com.dolinsek.elias.cashcockpit.components.AutoPayPaymentManager;
import com.dolinsek.elias.cashcockpit.components.Database;

import org.json.JSONException;

import java.io.IOException;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
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
