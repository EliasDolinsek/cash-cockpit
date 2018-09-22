package com.dolinsek.elias.cashcockpit.components;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.R;
import com.dolinsek.elias.cashcockpit.StartActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by elias on 16.03.2018.
 */

public class Toolbox {

    public static final int TYPE_YEAR = 876;
    public static final int TYPE_MONTH = 298;
    public static final int TYPE_DAY = 323;

    private static final int PENDING_INTENT_ID = 978;

    public static ArrayList<Bill> getBills(long creationTime, int type){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(creationTime);

        int year = 0, month = 0, day = 0;
        switch (type){
            case TYPE_DAY: year = calendar.get(Calendar.YEAR); month = calendar.get(Calendar.MONTH); day = calendar.get(Calendar.DAY_OF_MONTH);
                break;
            case TYPE_MONTH: year = calendar.get(Calendar.YEAR); month = calendar.get(Calendar.MONTH);
                break;
            case TYPE_YEAR: year = calendar.get(Calendar.YEAR);
                break;
        }

        ArrayList<Bill> bills = new ArrayList<>();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            for (Bill bill:bankAccount.getBills()){
                calendar.setTimeInMillis(bill.getCreationDate());

                switch (type){
                    case TYPE_DAY: {
                        if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH) && day == calendar.get(Calendar.DAY_OF_MONTH)){
                            bills.add(bill);
                        }
                    }
                        break;
                    case TYPE_MONTH:{
                        if (year == calendar.get(Calendar.YEAR) && month == calendar.get(Calendar.MONTH)){
                            bills.add(bill);
                        }
                    }
                        break;
                    case TYPE_YEAR:{
                        if (year == calendar.get(Calendar.YEAR)){
                            bills.add(bill);
                        }
                    }
                        break;
                }
            }
        }

        return bills;
    }

    public static boolean connectedToInternet(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public static void restartCashCockpit(Context context){
        Intent intent = new Intent(context, StartActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);

        System.exit(0);
    }

    public static int getPrimaryCategoryIconResourceByName(Context context, PrimaryCategory primaryCategory){
        String primaryCategoryIconName = primaryCategory.getIconName();
        String packageName = context.getPackageName();

        return context.getResources().getIdentifier(primaryCategoryIconName, "drawable", packageName);
    }

    public static void showSingInRequiredToUseFeatureToast(Context context){
        Toast.makeText(context, R.string.toast_sing_in_required_to_use_feature, Toast.LENGTH_SHORT).show();
    }

}
