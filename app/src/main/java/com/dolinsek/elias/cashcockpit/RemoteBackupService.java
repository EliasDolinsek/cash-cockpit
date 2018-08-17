package com.dolinsek.elias.cashcockpit;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

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


    private IBinder localBinder = new LocalBinder();
    private boolean hasFinished;

    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference bankAccountsReference, autoPaysReference, primaryCategoriesReference;

    private ArrayList<BankAccount> bankAccounts;
    private ArrayList<AutoPay> autoPays;
    private ArrayList<PrimaryCategory> primaryCategories;

    @Override
    public void onCreate() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        String firebaseUserUid = firebaseUser.getUid();
        DatabaseReference backupsReference = firebaseDatabase.getReference().child(BackupHelper.BACKUPS_REFERENCE);

        bankAccountsReference = backupsReference.child(BackupHelper.BANK_ACCOUNTS_REFERENCE).child(firebaseUserUid);
        autoPaysReference = backupsReference.child(BackupHelper.AUTO_PAYS_REFERENCE).child(firebaseUserUid);
        primaryCategoriesReference = backupsReference.child(BackupHelper.PRIMARY_CATEGORIES_REFERENCE).child(firebaseUserUid);

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
            pushBankAccounts(bankAccounts);
            pushAutoPays(autoPays);
            pushPrimaryCategories(primaryCategories);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void pushBankAccounts(ArrayList<BankAccount> bankAccounts){
        for (BankAccount bankAccount:bankAccounts){
            bankAccountsReference.push().setValue(bankAccount);
        }
    }

    private void pushAutoPays(ArrayList<AutoPay> autoPays){
        for (AutoPay autoPay:autoPays){
            autoPaysReference.push().setValue(autoPay);
        }
    }

    private void pushPrimaryCategories(ArrayList<PrimaryCategory> primaryCategories){
        for (PrimaryCategory primaryCategory:primaryCategories){
            primaryCategoriesReference.push().setValue(primaryCategory);
        }
    }

    private void deletePreviousBackupOnFirebase(){
        bankAccountsReference.removeValue();
        autoPaysReference.removeValue();
        primaryCategoriesReference.removeValue();
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
