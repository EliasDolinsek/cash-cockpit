package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

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
        primaryCategories = dataHelper.readCategories(false);
        bankAccounts = dataHelper.readBankAccounts();
        defaultPrimaryCategories = dataHelper.readCategories(true);
        autoPays = dataHelper.readAutoPays();

        CategoriesSorter.sortPrimaryCategories(primaryCategories);

        //Sorts the primary bank account to the first position
        for(int i = 0; i<getBankAccounts().size(); i++){
            if(getBankAccounts().get(i).isPrimaryAccount()){
                BankAccount primaryBankAccount = getBankAccounts().get(i);
                getBankAccounts().remove(i);
                getBankAccounts().add(0, primaryBankAccount);
            }
        }
    }

    /**
     * Saves all data
     * @param context context to get access to files
     */
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
