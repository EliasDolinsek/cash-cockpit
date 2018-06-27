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

        CategoriesSorter.sortPrimaryCategories(primaryCategories);

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

    public static class Toolkit {

        public static ArrayList<Bill> getAllBillsInDatabase(){
            ArrayList<Bill> allBills = new ArrayList<>();
            for (BankAccount bankAccount: bankAccounts){
                allBills.addAll(bankAccount.getBills());
            }

            return allBills;
        }

        public static long getCreationDateOfFirstBill(ArrayList<Bill> bills){
            long firstCreationDate = System.currentTimeMillis();

            for (Bill bill:bills){
                if (bill.getCreationDate() < firstCreationDate){
                    firstCreationDate = bill.getCreationDate();
                }
            }

            return firstCreationDate;
        }

        public static ArrayList<Bill> getBillsOfMonth(long timeStampOfMonth){
            ArrayList<Bill> billsOfMonth = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeStampOfMonth);

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);

            for (Bill bill:getAllBillsInDatabase()){
                calendar.setTimeInMillis(bill.getCreationDate());

                int billYear = calendar.get(Calendar.YEAR);
                int billMonth = calendar.get(Calendar.MONTH);

                if (year == billYear && month == billMonth){
                    billsOfMonth.add(bill);
                }
            }

            return  billsOfMonth;
        }

        public static ArrayList<Bill> filterBillsOfBillType(ArrayList<Bill> billsToFilter, int billType){
            ArrayList<Bill> filteredBills = new ArrayList<>();
            for (Bill bill:billsToFilter){
                if (bill.getType() == billType){
                    filteredBills.add(bill);
                }
            }

            return filteredBills;
        }

        public static ArrayList<Bill> filterBillsOfAutoPayBill(ArrayList<Bill> billsToFilter){
            ArrayList<Bill> filteredBills = new ArrayList<>();
            for (Bill bill:getAllBillsInDatabase()){
                if (bill.isAutoPayBill()){
                    filteredBills.add(bill);
                }
            }

            return filteredBills;
        }

        public static void manageAllPayments(){
            for (AutoPay autoPay:Database.getAutoPays()){
                autoPay.managePayment();
            }
        }

        public static ArrayList<AutoPay> getAutoPaysWherePaymentsAreRequired(){
            ArrayList<AutoPay> autoPays = new ArrayList<>();
            for (AutoPay autoPay:Database.getAutoPays()){
                if (autoPay.isPaymentRequired()){
                    autoPays.add(autoPay);
                }
            }

            return autoPays;
        }

        public static long getTotalAmountOfBills(ArrayList<Bill> bills){
            long amount = 0;
            for (Bill bill:bills){
                if (bill.getType() == Bill.TYPE_INPUT){
                    amount += bill.getAmount();
                } else if (bill.getType() == Bill.TYPE_OUTPUT || bill.getType() == Bill.TYPE_TRANSFER){
                    amount -= bill.getAmount();
                }
            }

            return amount;
        }

        public static ArrayList<AutoPay> getAutoPaysOfType(int type){
            ArrayList<AutoPay> autoPays = new ArrayList<>();
            for (AutoPay autoPay:getAutoPays()){
                if (autoPay.getType() == type){
                    autoPays.add(autoPay);
                }
            }

            return autoPays;
        }

        public static long getTotalBalanceOfAllBankAccounts(){
            long balance = 0;
            for (BankAccount bankAccount:Database.getBankAccounts()){
                if (bankAccount.getBalance() < 0){
                    balance -= bankAccount.getBalance();
                } else {
                    balance += bankAccount.getBalance();
                }
            }

            return balance;
        }

        public static long getAmountOfAutoPays(ArrayList<AutoPay> autoPays){
            long amount = 0;
            for (AutoPay autoPay:autoPays){
                amount += autoPay.getBill().getAmount();
            }

            return amount;
        }

        public static ArrayList<Bill> removeAutoPayBillsFromCollection(ArrayList<Bill> bills){
            ArrayList<Bill> filteredBills = new ArrayList<>();
            for (Bill bill:bills){
                if (!bill.isAutoPayBill()){
                    filteredBills.add(bill);
                }
            }

            return filteredBills;
        }

        public static ArrayList<AutoPay> filterAutoPaysOfAutoPayBillType(ArrayList<AutoPay> autoPays, int billType){
            ArrayList<AutoPay> filteredAutoPays = new ArrayList<>();
            for (AutoPay autoPay:autoPays){
                if (autoPay.getBill().getType() == billType){
                    filteredAutoPays.add(autoPay);
                }
            }

            return filteredAutoPays;
        }
    }
}
