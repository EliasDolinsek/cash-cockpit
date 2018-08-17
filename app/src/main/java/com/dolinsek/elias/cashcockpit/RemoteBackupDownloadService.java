package com.dolinsek.elias.cashcockpit;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.dolinsek.elias.cashcockpit.components.AutoPay;
import com.dolinsek.elias.cashcockpit.components.BackupHelper;
import com.dolinsek.elias.cashcockpit.components.BankAccount;
import com.dolinsek.elias.cashcockpit.components.PrimaryCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RemoteBackupDownloadService extends Service {

    private IBinder localBinder = new LocalBinder();

    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference bankAccountsReference, autoPaysReference, primaryCategoriesReference;

    private ArrayList<BankAccount> bankAccounts;
    private ArrayList<AutoPay> autoPays;
    private ArrayList<PrimaryCategory> primaryCategories;

    private boolean hasFinishedBankAccounts, hasFinishedAutoPays, hasFinishedPrimaryCategories;

    @Override
    public void onCreate() {
        super.onCreate();

        bankAccounts = new ArrayList<>();
        autoPays = new ArrayList<>();
        primaryCategories = new ArrayList<>();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        String firebaseUserUid = firebaseUser.getUid();
        DatabaseReference backupsReference = firebaseDatabase.getReference().child(BackupHelper.BACKUPS_REFERENCE);

        bankAccountsReference = backupsReference.child(BackupHelper.BANK_ACCOUNTS_REFERENCE).child(firebaseUserUid);
        autoPaysReference = backupsReference.child(BackupHelper.AUTO_PAYS_REFERENCE).child(firebaseUserUid);
        primaryCategoriesReference = backupsReference.child(BackupHelper.PRIMARY_CATEGORIES_REFERENCE).child(firebaseUserUid);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loadArraysWithDataFromFirebase();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    private void loadArraysWithDataFromFirebase(){
        loadBankAccountsArrayWithDataFromFirebase();
        loadAutoPaysArrayWithDataFromFirebase();
        loadPrimaryCategoriesWithDataFromFirebase();
    }

    private void loadBankAccountsArrayWithDataFromFirebase(){
        bankAccountsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hasFinishedBankAccounts = false;
                for (DataSnapshot child:dataSnapshot.getChildren()){
                    bankAccounts.add(child.getValue(BankAccount.class));
                }
                hasFinishedBankAccounts = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadAutoPaysArrayWithDataFromFirebase(){
        autoPaysReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hasFinishedAutoPays = false;
                for (DataSnapshot child:dataSnapshot.getChildren()){
                    autoPays.add(child.getValue(AutoPay.class));
                }
                hasFinishedAutoPays = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPrimaryCategoriesWithDataFromFirebase(){
        primaryCategoriesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                hasFinishedPrimaryCategories = false;
                for (DataSnapshot child:dataSnapshot.getChildren()){
                    primaryCategories.add(child.getValue(PrimaryCategory.class));
                }

                hasFinishedPrimaryCategories = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public ArrayList<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public ArrayList<AutoPay> getAutoPays() {
        return autoPays;
    }

    public ArrayList<PrimaryCategory> getPrimaryCategories() {
        return primaryCategories;
    }

    public boolean hasFinished() {
        return hasFinishedBankAccounts && hasFinishedAutoPays && hasFinishedPrimaryCategories;
    }

    public class LocalBinder extends Binder{
        public RemoteBackupDownloadService getService(){
            return RemoteBackupDownloadService.this;
        }
    }
}
