package com.dolinsek.elias.cashcockpit.components;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.R;
import com.dolinsek.elias.cashcockpit.RemoteBackupDownloadService;
import com.dolinsek.elias.cashcockpit.RemoteBackupService;
import com.dolinsek.elias.cashcockpit.StartActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elias Dolinsek on 06.08.2018 for cash-cockpit.
 */
public class BackupHelper {

    private static final String DATABASE_FILE_NAME = "database.json";
    private static final String DATABASE_BACKUP_FILE_NAME = "databaseBackup.json";

    public static final String BACKUPS_REFERENCE = "backups";
    public static final String BANK_ACCOUNTS_REFERENCE = "bankAccounts";
    public static final String AUTO_PAYS_REFERENCE = "autoPays";
    public static final String PRIMARY_CATEGORIES_REFERENCE = "primaryCategories";

    public static final String BACKUP_LOCATION_LOCAL = "1";
    public static final String BACKUP_LOCATION_SERVER = "2";

    private Context context;
    private RemoteBackupService remoteBackupService;
    private OnCompleteListener onCompleteListener;

    public BackupHelper(Context context) throws IllegalStateException{
        this.context = context;
    }

    public void createBackup(){
        String backupLocation = getBackupLocation(context);
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
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                RemoteBackupService.LocalBinder binder = (RemoteBackupService.LocalBinder) service;
                remoteBackupService = binder.getService();
                new Thread(() -> {
                    while (true){
                        if (remoteBackupService.hasFinished()){
                            onCompleteListener.onComplete(true);

                            context.unbindService(this);
                            context.stopService(intent);

                            return;
                        }
                    }
                }).start();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                onCompleteListener.onComplete(false);
            }
        };

        context.startService(intent);
        context.bindService(intent, serviceConnection, 0);
    }

    public void overrideDataWithBackup(){
        String backupLocation = getBackupLocation(context);
        if (backupLocation.equals(BACKUP_LOCATION_LOCAL)){
            overrideDataWithLocalBackup();
        } else {
            overrideDataWithServerBackup();
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

    private void overrideDataWithServerBackup(){
        Intent intent = new Intent(context, RemoteBackupDownloadService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                RemoteBackupDownloadService remoteBackupDownloadService = ((RemoteBackupDownloadService.LocalBinder)iBinder).getService();
                new Thread(() -> {
                    while (true){
                        if (remoteBackupDownloadService.hasFinished()){
                            replaceDatabaseDataWithNewArrayLists(remoteBackupDownloadService.getBankAccounts(),
                                    remoteBackupDownloadService.getAutoPays(),
                                    remoteBackupDownloadService.getPrimaryCategories());
                            Database.save(context);

                            context.unbindService(this);
                            context.stopService(intent);
                            onCompleteListener.onComplete(true);
                            return;
                        }
                    }
                }).start();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                onCompleteListener.onComplete(false);
            }
        };

        context.startService(intent);
        context.bindService(intent, serviceConnection, 0);
    }

    private void replaceDatabaseDataWithNewArrayLists(ArrayList<BankAccount> bankAccounts, ArrayList<AutoPay> autoPays, ArrayList<PrimaryCategory> primaryCategories){
        Database.setBankAccounts(bankAccounts);
        Database.setAutoPays(autoPays);
        Database.setPrimaryCategories(primaryCategories);
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

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public static String getBackupLocation(Context context){
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context).getString("preference_backup_location", BACKUP_LOCATION_LOCAL);
    }

    public static interface OnCompleteListener {
        public void onComplete(boolean successfully);
    }
}
