package com.dolinsek.elias.cashcockpit.model;

import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by elias on 10.01.2018.
 */

public class Database {

    private static ArrayList<BankAccount> bankAccounts = new ArrayList<>();
    private static ArrayList<PrimaryCategory> primaryCategories = new ArrayList<>();
    private static ArrayList<PrimaryCategory> defaultPrimaryCategories = new ArrayList<>();
    private static ArrayList<AutoPay> autoPays = new ArrayList<>();
    private static DataHelper dataHelper;

    public static ArrayList<BankAccount> getBankAccounts() {
        return bankAccounts;
    }

    public static ArrayList<PrimaryCategory> getPrimaryCategories() {
        return primaryCategories;
    }

    public static ArrayList<AutoPay> getAutoPays() {
        return autoPays;
    }

    public static ArrayList<PrimaryCategory> getDefaultPrimaryCategories() {
        return defaultPrimaryCategories;
    }

    public static void load(Context context) throws IOException, JSONException {
        dataHelper = new DataHelper(context);
        primaryCategories = dataHelper.readCategories(false);
        bankAccounts = dataHelper.readBankAccounts();
        defaultPrimaryCategories = dataHelper.readCategories(true);
        autoPays = dataHelper.readAutoPays();
    }

    public static void save(Context context){
        if(dataHelper != null){
            try {
                dataHelper.writeData(bankAccounts, primaryCategories, autoPays);
                load(context);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
