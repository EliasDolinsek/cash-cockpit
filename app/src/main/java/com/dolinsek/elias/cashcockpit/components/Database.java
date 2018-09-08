package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This has static array lists of all components
 * Created by elias on 10.01.2018.
 */

public class Database {

    /**
     * List of all bank accounts
     */
    private static ArrayList<BankAccount> bankAccounts = new ArrayList<>();

    /**
     * List of all primary categories
     */
    private static ArrayList<PrimaryCategory> primaryCategories = new ArrayList<>();

    /**
     * List of all default categories
     */
    private static ArrayList<PrimaryCategory> defaultPrimaryCategories = new ArrayList<>();

    /**
     * List of all AutoPays
     */
    private static ArrayList<AutoPay> autoPays = new ArrayList<>();

    /**
     * DataHelper what is used to read and write the data
     */
    private static DataHelper dataHelper;

    private static boolean loaded;

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

    /**
     * Loads all data from the database-file
     * @param context context to get access to files
     * @throws IOException when the system isn't able to read the database-file
     * @throws JSONException when the system isn't able to parse the read json from the file
     */
    public static void load(Context context) throws IOException, JSONException {
        dataHelper = new DataHelper(context);
        defaultPrimaryCategories = dataHelper.readCategories(true);
        primaryCategories = dataHelper.readCategories(false);
        bankAccounts = dataHelper.readBankAccounts();
        autoPays = dataHelper.readAutoPays();

        CategoriesSorter.sortPrimaryCategoriesIfPreferenceIsChecked(context, primaryCategories);

        //Sorts the primary bank account to the first position
        for(int i = 0; i<getBankAccounts().size(); i++){
            if(getBankAccounts().get(i).isPrimaryAccount()){
                BankAccount primaryBankAccount = getBankAccounts().get(i);
                getBankAccounts().remove(i);
                getBankAccounts().add(0, primaryBankAccount);
            }
        }

        loaded = true;
    }

    public static void deleteDatabase(){
        bankAccounts = new ArrayList<>();
        primaryCategories = new ArrayList<>();
        autoPays = new ArrayList<>();
    }

    public static String getDataAsString(){
        StringBuilder dataAsString = new StringBuilder();
        for (BankAccount bankAccount:bankAccounts){
            dataAsString.append(bankAccount.toString());
        }

        for (AutoPay autoPay:autoPays){
            dataAsString.append(autoPay.toString());
        }

        for (PrimaryCategory primaryCategory:primaryCategories){
            dataAsString.append(primaryCategory);
        }

        return dataAsString.toString();
    }

    /**
     * Saves all data
     * @param context context to get access to files
     */
    public static void save(Context context){
        if (dataHelper == null){
            dataHelper = new DataHelper(context);
        }

        try {
            dataHelper.writeData(bankAccounts, primaryCategories, autoPays);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBankAccounts(ArrayList<BankAccount> bankAccounts) {
        Database.bankAccounts = bankAccounts;
    }

    public static void setPrimaryCategories(ArrayList<PrimaryCategory> primaryCategories) {
        Database.primaryCategories = primaryCategories;
    }

    public static void setAutoPays(ArrayList<AutoPay> autoPays) {
        Database.autoPays = autoPays;
    }

    public static boolean isLoaded() {
        return loaded;
    }

}
