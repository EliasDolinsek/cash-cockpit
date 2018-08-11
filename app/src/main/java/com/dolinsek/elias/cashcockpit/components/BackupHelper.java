package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Elias Dolinsek on 06.08.2018 for cash-cockpit.
 */
public class BackupHelper {

    private static final String DATABASE_FILE_NAME = "database.json";
    private static final String DATABASE_BACKUP_FILE_NAME = "databaseBackup.json";

    private static final String BANK_ACCOUNTS_REFERENCE = "bankAccounts";
    private static final String AUTO_PAYS_REFERENCE = "autoPays";
    private static final String PRIMARY_CATEGORIES_REFERENCE = "primaryCategories";

    public static final String BACKUP_LOCATION_LOCAL = "1";
    public static final String BACKUP_LOCATION_SERVER = "2";

    private Context context;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userReference;

    private OnCompleteListener onCompleteListener;

    public BackupHelper(Context context) throws IllegalStateException{
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null){
            firebaseDatabase = FirebaseDatabase.getInstance();
            userReference = firebaseDatabase.getReference().child(firebaseUser.getUid());
        } else {
            throw new IllegalStateException("FirebaseUser is null!");
        }
    }

    public void createBackup(){
        createBackup(Database.getBankAccounts(), Database.getAutoPays(), Database.getPrimaryCategories());
    }

    public void createBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        String backupLocation = getBackupLocation();
        switch (backupLocation){
            case BACKUP_LOCATION_LOCAL: createLocalBackup(bankAccounts, autoPays, primaryCategories); return;
            case BACKUP_LOCATION_SERVER: createServerBackup(bankAccounts, autoPays, primaryCategories); return;
            default: throw new IllegalArgumentException("Couldn't resolve backup location!");
        }
    }

    public void createLocalBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        try {
            String dataFromDatabaseFile = getDataFromFile(DATABASE_FILE_NAME);
            writeDataToFile(dataFromDatabaseFile, DATABASE_BACKUP_FILE_NAME);
            onCompleteListener.onComplete(true);
        } catch (IOException e) {
            onCompleteListener.onComplete(false);
            e.printStackTrace();
        }
    }


    public void createServerBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        try {
            DatabaseReference bankAccountsReference = userReference.child(BANK_ACCOUNTS_REFERENCE);
            DatabaseReference autoPaysReference = userReference.child(AUTO_PAYS_REFERENCE);
            DatabaseReference primaryCategoriesReference = userReference.child(PRIMARY_CATEGORIES_REFERENCE);

            bankAccountsReference.setValue(bankAccounts);

            onCompleteListener.onComplete(true);
        } catch (Exception e){
            onCompleteListener.onComplete(false);
            e.printStackTrace();
        }
    }

    public void overrideDataWithBackup(){
        String backupLocation = getBackupLocation();
        if (backupLocation.equals(BACKUP_LOCATION_LOCAL)){
            overrideDataWithLocalBackup();
        } else {
            //TODO implement
        }
    }

    public void overrideDataWithLocalBackup(){
        try {
            String dataFromBackup = getDataFromFile(DATABASE_BACKUP_FILE_NAME);
            writeDataToFile(dataFromBackup, DATABASE_FILE_NAME);
            onCompleteListener.onComplete(true);
        } catch (IOException e) {
            onCompleteListener.onComplete(false);
            e.printStackTrace();
        }
    }

    private void writeDataToFile(String dataAsString, String fileName) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

        outputStreamWriter.write(dataAsString);

        outputStreamWriter.close();
        fileOutputStream.close();
    }

    private String getDataFromFile(String fileName) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(context.openFileInput(fileName));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String currentLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((currentLine = bufferedReader.readLine()) != null){
            stringBuilder.append(currentLine);
        }

        return stringBuilder.toString();
    }
    
    private String getBackupLocation(){
        return PreferenceManager.getDefaultSharedPreferences(context).getString("preference_backup_location", BACKUP_LOCATION_LOCAL);
    }

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public static interface OnCompleteListener {
        public void onComplete(boolean successfully);
    }
}
