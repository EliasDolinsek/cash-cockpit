package com.dolinsek.elias.cashcockpit;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RemoteBackupService extends Service {

    private static final String BANK_ACCOUNTS_REFERENCE = "bankAccounts";
    private static final String AUTO_PAYS_REFERENCE = "autoPays";
    private static final String PRIMARY_CATEGORIES_REFERENCE = "primaryCategories";
    public static final int NOTIFICATION_ID = 12345;

    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userReference, bankAccountsReference, autoPaysReference, primaryCategoriesReference;
    private NotificationManagerCompat notificationManager;

    private ArrayList<BankAccount> bankAccounts;
    private ArrayList<AutoPay> autoPays;
    private ArrayList<PrimaryCategory> primaryCategories;

    @Override
    public void onCreate() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        userReference = firebaseDatabase.getReference().child(firebaseUser.getUid());
        bankAccountsReference = userReference.child(BANK_ACCOUNTS_REFERENCE);
        autoPaysReference = userReference.child(AUTO_PAYS_REFERENCE);
        primaryCategoriesReference = userReference.child(PRIMARY_CATEGORIES_REFERENCE);

        bankAccounts = new ArrayList<>();
        autoPays = new ArrayList<>();
        primaryCategories = new ArrayList<>();

        notificationManager = NotificationManagerCompat.from(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loadArrayListsWithDataFromDatabase();

        new Thread(() -> {
            displayUploadingNotification();

            deletePreviousBackupOnFirebase();
            createServerBackup(bankAccounts, autoPays, primaryCategories);

            displayFinishedUploadingNotification();
            stopSelf();
        }).start();

        return START_STICKY; //Restarts Services if the system stops it
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    private void displayUploadingNotification(){
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setProgress(100,0, true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.notification_title_cash_cockpit))
                .setContentText(getString(R.string.notification_text_uploading_data));

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void displayFinishedUploadingNotification(){
        Notification.Builder builder = new Notification.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(getString(R.string.notification_title_cash_cockpit))
                .setContentText(getString(R.string.notification_text_uploaded_data_successfully));

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void loadArrayListsWithDataFromDatabase(){
        bankAccounts.addAll(Database.getBankAccounts());
        autoPays.addAll(Database.getAutoPays());
        primaryCategories.addAll(Database.getPrimaryCategories());
    }
    public void createServerBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        try {
            bankAccountsReference.push().setValue(bankAccounts);
            autoPaysReference.push().setValue(autoPays);
            primaryCategoriesReference.push().setValue(primaryCategories);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deletePreviousBackupOnFirebase(){
        bankAccountsReference.push().setValue(null);
        autoPaysReference.push().setValue(null);
        primaryCategoriesReference.push().setValue(null);
    }
}
