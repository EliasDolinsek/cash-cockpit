package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    Context context;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    public BackupHelper(Context context) throws IllegalStateException{
        this.context = context;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null){
            throw new IllegalStateException("FirebaseUser is null!");
        }
    }

    public void createBackup(){
        createBackup(Database.getBankAccounts(), Database.getAutoPays(), Database.getPrimaryCategories());
    }

    public void createBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        String backupLocation = PreferenceManager.getDefaultSharedPreferences(context).getString("preference_backup_location", "1");
        switch (backupLocation){
            case "1": createLocalBackup(bankAccounts, autoPays, primaryCategories); return;
            case "2": createServerBackup(bankAccounts, autoPays, primaryCategories); return;
            default: throw new IllegalArgumentException("Couldn't resolve backup location!");
        }
    }

    public void createLocalBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        try {
            String dataFromDatabaseFile = getDataFromDatabaseFile();
            writeDataToDatabaseBackup(dataFromDatabaseFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDataFromDatabaseFile() throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(context.openFileInput(DATABASE_FILE_NAME));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String currentLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((currentLine = bufferedReader.readLine()) != null){
            stringBuilder.append(currentLine);
        }

        return stringBuilder.toString();
    }

    private void writeDataToDatabaseBackup(String dataAsString) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(DATABASE_BACKUP_FILE_NAME, Context.MODE_PRIVATE);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

        outputStreamWriter.write(dataAsString);

        outputStreamWriter.close();
        fileOutputStream.close();
    }

    public void createServerBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child(firebaseUser.getUid());

        DatabaseReference bankAccountsReference = databaseReference.child(BANK_ACCOUNTS_REFERENCE);
        DatabaseReference autoPaysReference = databaseReference.child(AUTO_PAYS_REFERENCE);
        DatabaseReference primaryCategoriesReference = databaseReference.child(PRIMARY_CATEGORIES_REFERENCE);

        autoPaysReference.push().setValue(autoPays);
    }
}
