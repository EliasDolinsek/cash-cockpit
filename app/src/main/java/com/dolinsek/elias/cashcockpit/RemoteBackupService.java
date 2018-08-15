package com.dolinsek.elias.cashcockpit;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BackupHelper;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.Database;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RemoteBackupService extends Service {

    public static final String BANK_ACCOUNTS_REFERENCE = "bankAccounts";
    public static final String AUTO_PAYS_REFERENCE = "autoPays";
    public static final String PRIMARY_CATEGORIES_REFERENCE = "primaryCategories";

    private IBinder localBinder = new LocalBinder();
    private boolean hasFinished;

    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userReference, bankAccountsReference, autoPaysReference, primaryCategoriesReference;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        hasFinished = false;
        createServerBackupAndDeletePreviousBackup();
        hasFinished = true;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    private void createServerBackupAndDeletePreviousBackup(){
        loadArrayListsWithDataFromDatabase();
        new Thread(() -> {
            deletePreviousBackupOnFirebase();
            createServerBackup(bankAccounts, autoPays, primaryCategories);
        }).start();
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
        userReference.removeValue();
    }

    public boolean hasFinished() {
        return hasFinished;
    }

    public class LocalBinder extends Binder {
        public RemoteBackupService getService(){
            return RemoteBackupService.this;
        }
    }
}
