package com.dolinsek.elias.cashcockpit.components;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.R;
import com.dolinsek.elias.cashcockpit.RemoteBackupService;
import com.dolinsek.elias.cashcockpit.StartActivity;
import com.firebase.ui.auth.AuthUI;
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

    public static final String BACKUP_LOCATION_LOCAL = "1";
    public static final String BACKUP_LOCATION_SERVER = "2";

    private Context context;

    private OnCompleteListener onCompleteListener;

    public BackupHelper(Context context) throws IllegalStateException{
        this.context = context;
    }

    public void createBackup(){
        createBackup(Database.getBankAccounts(), Database.getAutoPays(), Database.getPrimaryCategories());
    }

    public void createBackup(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        String backupLocation = getBackupLocation();
        switch (backupLocation){
            case BACKUP_LOCATION_LOCAL: createLocalBackup(); return;
            case BACKUP_LOCATION_SERVER: createServerBackup(); return;
            default: throw new IllegalArgumentException("Couldn't resolve backup location!");
        }
    }

    public void createLocalBackup(){
        try {
            String dataFromDatabaseFile = getDataFromFile(DATABASE_FILE_NAME);
            writeDataToFile(dataFromDatabaseFile, DATABASE_BACKUP_FILE_NAME);
            onCompleteListener.onComplete(true);
        } catch (IOException e) {
            onCompleteListener.onComplete(false);
            e.printStackTrace();
        }
    }

    private void createServerBackup(){
        Intent intent = new Intent(context, RemoteBackupService.class);
        context.startService(intent);

        Toast.makeText(context, R.string.toast_started_uploading_data_to_servers, Toast.LENGTH_SHORT).show();
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
