package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;
import android.content.res.AssetManager;
import android.service.autofill.Dataset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class has only static methods to read and write data
 * Created by elias on 08.01.2018.
 */

public class DataHelper {

    //General
    private static final String FILE_NAME_STANDARD_DATA = "standardData";
    private static final String FILE_NAME = "database.json";
    private static final String BANK_ACCOUNTS_JSON = "bankAccounts";
    private static final String PRIMARY_CATEGORIES_JSON = "primaryCategories";
    private static final String AUTO_PAYS_JSON = "autoPays";

    //BankAccount
    private static final String BANK_ACCOUNT_NAME = "name";
    private static final String BANK_ACCOUNT_BALANCE = "balance";
    private static final String BANK_ACCOUNT_CREATION_DATE = "creationDate";
    private static final String BANK_ACCOUNT_PRIMARY_ACCOUNT = "primaryAccount";

    //Balance changes
    private static final String BALANCE_CHANGES_JSON = "balanceChanges";
    private static final String BALANCE_CHANGE_DATE_JSON = "date";
    private static final String BALANCE_CHANGE_NEW_BALANCE_JSON = "newBalance";

    //Bill
    private static final String BILLS_JSON = "bills";
    private static final String BILL_AMOUNT_JSON = "amount";
    private static final String BILL_DESCRIPTION_JSON = "description";
    private static final String BILL_SUB_CATEGORY_JSON = "subCategory";
    private static final String BILL_CREATION_DATE_JSON = "creationDate";
    private static final String BILL_PRIMARY_CATEGORY_NAME = "primaryCategory";
    private static final String BILL_TYPE_JSON = "type";

    //PrimaryCategory
    private static final String PRIMARY_CATEGORY_NAME = "name";
    private static final String PRIMARY_CATEGORY_GOAL_AMOUNT = "goalAmount";
    private static final String PRIMARY_CATEGORY_GOAL_CREATION_DATE = "creationDate";
    private static final String PRIMARY_CATEGORY_SUBCATEGORIES = "subcategories";
    private static final String PRIMARY_CATEGORY_ICON = "icon";

    //Subcategory
    private static final String SUBCATEGORY_NAME_JSON = "name";
    private static final String SUBCATEGORY_GOAL_AMOUNT = "goalAmount";
    private static final String SUBCATEGORY_GOAL_CREATION_DATE = "creationDate";
    private static final String SUBCATEGORY_FAVOURED = "favoured";

    //AutoPays
    private static final String AUTO_PAY_NAME = "name";
    private static final String AUTO_PAY_TYPE = "type";
    private static final String AUTO_PAY_CREATION_DATE = "creationDate";
    private static final String AUTO_PAY_BILL = "bill";
    private static final String AUTO_PAY_BILL_PRIMARY_CATEGORY = "primaryCategory";
    private static final String AUTO_PAY_BANK_ACCOUNT = "bankAccount";
    private static final String AUTO_PAY_PAYMENTS = "payments";

    //Context to get access to app-files
    private Context context;

    /**
     * @param context Context to get access to app-files
     */
    public DataHelper(Context context) {
        this.context = context;
    }

    /**
     *
     * @param bankAccounts List of all BankAccounts
     * @param primaryCategories List of all PrimaryCategories
     * @param autoPays List of all AutoPays
     * @throws JSONException if couldn't parse json
     */

    public void writeData(ArrayList<BankAccount> bankAccounts, ArrayList<PrimaryCategory> primaryCategories, ArrayList<AutoPay> autoPays) throws JSONException, IOException {
        //Main JSON-Object
        JSONObject jsonObject = new JSONObject();

        //Contains all bank accounts
        JSONArray bankAccountsJSON = new JSONArray();

        //Contains all primary categories
        JSONArray primaryCategoriesJSON = new JSONArray();

        //Contains all AutoPays
        JSONArray autoPaysJSON = new JSONArray();


        //Adds every bank account to bankAccountsJSON
        for(int i = 0; i<bankAccounts.size(); i++){

            //Contains the current bank account
            JSONObject currentBankAccountJSON = new JSONObject();

            //Adds the current bank account to the list
            bankAccountsJSON.put(currentBankAccountJSON);

            //Current bank account
            BankAccount bankAccount = bankAccounts.get(i);

            //Adds name of the bank account
            currentBankAccountJSON.put(BANK_ACCOUNT_NAME, bankAccount.getName());

            //Adds balance of the current bank account
            currentBankAccountJSON.put(BANK_ACCOUNT_BALANCE, bankAccount.getBalance());

            //Adds creation date of the current bank account
            currentBankAccountJSON.put(BANK_ACCOUNT_CREATION_DATE, bankAccount.getCreationDate());

            //Adds if the current bank account is the primary account
            currentBankAccountJSON.put(BANK_ACCOUNT_PRIMARY_ACCOUNT, bankAccount.isPrimaryAccount());

            //Contains all balance changes
            JSONArray balanceChanges = new JSONArray();

            //Adds balance changes
            currentBankAccountJSON.put(BALANCE_CHANGES_JSON, balanceChanges);

            //Adds every balance changes of the current bank account
            for(int y = 0; y<bankAccount.getBalanceChanges().size(); y++){

                //Contains the current balance change
                JSONObject currentBalanceChangeJSON = new JSONObject();

                //Adds date of balance change to the array of balance changes
                balanceChanges.put(currentBalanceChangeJSON);

                //Adds data
                currentBalanceChangeJSON.put(BALANCE_CHANGE_NEW_BALANCE_JSON, bankAccount.getBalanceChanges().get(y).getNewBalance());
                currentBalanceChangeJSON.put(BALANCE_CHANGE_DATE_JSON, bankAccount.getBalanceChanges().get(y).getTimeStampOfChange());
            }

            //Contains all bills
            JSONArray bills = new JSONArray();

            //Adds bills
            currentBankAccountJSON.put(BILLS_JSON, bills);

            //Adds every bill to the current bank account
            for(int z = 0; z<bankAccount.getBills().size(); z++){

                //Contains current bill
                JSONObject currentBillJSON = new JSONObject();

                //Adds current bill to the array list of balance changes
                bills.put(currentBillJSON);

                //Contains current bill
                Bill bill = bankAccount.getBills().get(z);

                //Adds amount
                currentBillJSON.put(BILL_AMOUNT_JSON, bill.getAmount());

                //Adds description
                currentBillJSON.put(BILL_DESCRIPTION_JSON, bill.getDescription());

                //Adds primary category
                currentBillJSON.put(BILL_PRIMARY_CATEGORY_NAME, bill.getSubcategory().getPrimaryCategory().getName());

                //Adds subcategory
                currentBillJSON.put(BILL_SUB_CATEGORY_JSON, bill.getSubcategory().getName());

                //Adds creation date
                currentBillJSON.put(BILL_CREATION_DATE_JSON, bill.getCreationDate());

                //Adds type
                currentBillJSON.put(BILL_TYPE_JSON, bill.getType());
            }
        }

        //Adds every category to primaryCategoriesJSON
        for(int i = 0; i<primaryCategories.size(); i++){

            //Contains current priamry category
            JSONObject currentPrimaryCategoryJSON = new JSONObject();

            //Adds current primary category to the array of primary categories
            primaryCategoriesJSON.put(currentPrimaryCategoryJSON);

            //Contains current priamry category
            PrimaryCategory primaryCategory = primaryCategories.get(i);

            //Adds name
            currentPrimaryCategoryJSON.put(PRIMARY_CATEGORY_NAME, primaryCategory.getName());

            //Adds goal-amount
            currentPrimaryCategoryJSON.put(PRIMARY_CATEGORY_GOAL_AMOUNT, primaryCategory.getGoal().getAmount());

            //Adds creationDate
            currentPrimaryCategoryJSON.put(PRIMARY_CATEGORY_GOAL_CREATION_DATE, primaryCategory.getGoal().getCreationDate());

            //Adds icon
            currentPrimaryCategoryJSON.put(PRIMARY_CATEGORY_ICON, primaryCategory.getIconName());

            //Contains all subcategories
            JSONArray subcategories = new JSONArray();

            //Adds subcategories
            currentPrimaryCategoryJSON.put(PRIMARY_CATEGORY_SUBCATEGORIES, subcategories);

            for(int y = 0; y<primaryCategories.get(i).getSubcategories().size(); y++){

                //Contains current subcategory
                JSONObject currentSubcategoryJSON = new JSONObject();

                //Adds subcategory to the array of subcategories of the current primary category
                subcategories.put(currentSubcategoryJSON);

                //Contains current subcategory
                Subcategory subcategory = primaryCategory.getSubcategories().get(y);

                //Adds name
                currentSubcategoryJSON.put(SUBCATEGORY_NAME_JSON, subcategory.getName());

                //Adds goal-amount
                currentSubcategoryJSON.put(SUBCATEGORY_GOAL_AMOUNT, subcategory.getGoal().getAmount());

                //Adds goal-creation-date
                currentSubcategoryJSON.put(SUBCATEGORY_GOAL_CREATION_DATE, subcategory.getGoal().getCreationDate());

                //Sets if the current subcategory is favoured by the user
                currentSubcategoryJSON.put(SUBCATEGORY_FAVOURED, subcategory.isFavoured());
            }
        }

        //Adds ever AutoPay
        for(int i = 0; i<autoPays.size(); i++){
            //Contains current AutoPay
            JSONObject currentAutoPay = new JSONObject();

            //Adds current AutoPay to array of AutoPays
            autoPaysJSON.put(currentAutoPay);

            //Contains current AutoPay
            AutoPay autoPay = autoPays.get(i);

            //Contains bill of autopay
            JSONObject autoPayBill = new JSONObject();

            //Adds bill of current AutoPay
            currentAutoPay.put(AUTO_PAY_BILL, autoPayBill);

            //Adds primary category of bill
            currentAutoPay.put(AUTO_PAY_BILL_PRIMARY_CATEGORY, autoPay.getBill().getSubcategory().getPrimaryCategory().getName());

            //Adds amount of bill
            autoPayBill.put(BILL_AMOUNT_JSON, autoPay.getBill().getAmount());

            //Adds description of bill
            autoPayBill.put(BILL_DESCRIPTION_JSON, autoPay.getBill().getDescription());

            //Adds subcategory of bill
            autoPayBill.put(BILL_SUB_CATEGORY_JSON, autoPay.getBill().getSubcategory().getName());

            //Adds creation date of bill
            autoPayBill.put(BILL_CREATION_DATE_JSON, autoPay.getBill().getCreationDate());

            //Adds type of bill
            autoPayBill.put(BILL_TYPE_JSON, autoPay.getType());


            //Adds type
            currentAutoPay.put(AUTO_PAY_TYPE, autoPay.getType());

            //Adds name
            currentAutoPay.put(AUTO_PAY_NAME, autoPay.getName());

            //Adds cration date
            currentAutoPay.put(AUTO_PAY_CREATION_DATE, autoPay.getCreationDate());

            //Adds bank account
            currentAutoPay.put(AUTO_PAY_BANK_ACCOUNT, autoPay.getBankAccount().getName());


            //Contains payments
            JSONArray autoPayPaymentsJSON = new JSONArray();

            //Adds payments
            currentAutoPay.putOpt(AUTO_PAY_PAYMENTS, autoPayPaymentsJSON);

            //Adds payments to json-object
            for(int y = 0; y<autoPay.getPayments().size(); y++){
                autoPayPaymentsJSON.put(autoPay.getPayments().get(y));
            }

        }

        //Puts JSONArray to the main JSONObject
        jsonObject.putOpt(BANK_ACCOUNTS_JSON, bankAccountsJSON);
        jsonObject.putOpt(PRIMARY_CATEGORIES_JSON, primaryCategoriesJSON);
        jsonObject.putOpt(AUTO_PAYS_JSON, autoPaysJSON);

        //Gets file output stream
        FileOutputStream fileOutputStream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);

        //Writs json into file
        outputStreamWriter.write(jsonObject.toString());

        //Closes streams
        outputStreamWriter.close();
        fileOutputStream.close();
    }

    public ArrayList<BankAccount> readBankAccounts() throws IOException, JSONException {
        //Contains all bank accounts
        ArrayList<BankAccount> bankAccounts = new ArrayList<>();

        //Contains file
        String file = readFile(false);

        if(file == null){
            return null;
        }

        //Main JSON-Object
        JSONObject jsonObject = new JSONObject(file);

        //Array of bank accounts
        JSONArray bankAccountsJSON = jsonObject.getJSONArray(BANK_ACCOUNTS_JSON);

        for(int i = 0; i<bankAccountsJSON.length(); i++){
            //Contains current bank account
            JSONObject currentBankAccountJSON = bankAccountsJSON.getJSONObject(i);

            //Contains all balance changes of current bank account
            ArrayList<BalanceChange> balanceChanges = new ArrayList<>();

            //Checks if bank account has balance changes
            if(currentBankAccountJSON.has(BALANCE_CHANGES_JSON)){
                //Adds every balance change of current bank account
                JSONArray balanceChangesJSON = currentBankAccountJSON.getJSONArray(BALANCE_CHANGES_JSON);
                for(int y = 0; y<balanceChangesJSON.length(); y++){
                    JSONObject currentBalanceChangeJSON = balanceChangesJSON.getJSONObject(y);
                    balanceChanges.add(new BalanceChange(currentBalanceChangeJSON.getLong(BALANCE_CHANGE_DATE_JSON), currentBalanceChangeJSON.getLong(BALANCE_CHANGE_NEW_BALANCE_JSON)));
                }
            }

            //Contains all bills
            ArrayList<Bill> bills = new ArrayList<>();

            //Adds every bill
            for(int y = 0; y<currentBankAccountJSON.getJSONArray(BILLS_JSON).length(); y++){
                //Contains current bill
                JSONObject currentBillJSON = currentBankAccountJSON.getJSONArray(BILLS_JSON).getJSONObject(y);

                long billAmount, billCreationDate;
                String billDescription, billSubCategory, billPrimaryCategory;

                billCreationDate = currentBillJSON.getLong(BILL_CREATION_DATE_JSON);
                billAmount = currentBillJSON.getLong(BILL_AMOUNT_JSON);
                billDescription = currentBillJSON.getString(BILL_DESCRIPTION_JSON);
                billSubCategory = currentBillJSON.getString(BILL_SUB_CATEGORY_JSON);
                billPrimaryCategory = currentBillJSON.getString(BILL_PRIMARY_CATEGORY_NAME);

                //Subcategory of bill
                Subcategory subcategory = null;

                //Gets subcategory
                for(int p = 0; p< Database.getPrimaryCategories().size(); p++){
                    for(int s = 0; s< Database.getPrimaryCategories().get(p).getSubcategories().size(); s++){
                        if(Database.getPrimaryCategories().get(p).getName().equals(billPrimaryCategory)){
                            if(Database.getPrimaryCategories().get(p).getSubcategories().get(s).getName().equals(billSubCategory))
                                subcategory = Database.getPrimaryCategories().get(p).getSubcategories().get(s);
                        }
                    }
                }

                bills.add(new Bill(billAmount, billDescription, subcategory, currentBillJSON.getInt(BILL_TYPE_JSON), billCreationDate));
            }

            //Adds name
            String bankAccountName;
            long bankAccountBalance, bankAccountCreationDate;
            boolean bankAccountPrimaryAccount;

            //Add name
            bankAccountName = currentBankAccountJSON.getString(BANK_ACCOUNT_NAME);

            //Add balance
            bankAccountBalance = currentBankAccountJSON.getLong(BANK_ACCOUNT_BALANCE);

            //Add creation date
            bankAccountCreationDate = currentBankAccountJSON.getLong(BANK_ACCOUNT_CREATION_DATE);

            //Add primary account
            bankAccountPrimaryAccount = currentBankAccountJSON.getBoolean(BANK_ACCOUNT_PRIMARY_ACCOUNT);

            //Adds bank account
            BankAccount bankAccount = new BankAccount(bankAccountName, bankAccountBalance, bankAccountPrimaryAccount, bankAccountCreationDate);
            bankAccount.setBills(bills);
            bankAccount.setBalanceChanges(balanceChanges);
            bankAccounts.add(bankAccount);
        }

        return bankAccounts;
    }

    public ArrayList<PrimaryCategory> readCategories(boolean standardCategories) throws IOException, JSONException {

        //Contains all bank accounts
        ArrayList<PrimaryCategory> primaryCategories = new ArrayList<>();

        //Contains file
        String file = readFile(standardCategories);

        //Main JSON-Object
        JSONObject jsonObject = new JSONObject(file);

        //Contains Categories
        JSONArray primaryCategoriesJSON = jsonObject.getJSONArray(PRIMARY_CATEGORIES_JSON);

        for(int i = 0; i<primaryCategoriesJSON.length(); i++){
            //Contains current primary category
            JSONObject currentPrimaryCategoryJSON = primaryCategoriesJSON.getJSONObject(i);

            String primaryCategoryName, primaryCategoryIcon;
            long primaryCategoryGoalAmount, primaryCategoryGoalCreationDate = System.currentTimeMillis();

            //Gets name of primary category
            primaryCategoryName = currentPrimaryCategoryJSON.getString(PRIMARY_CATEGORY_NAME);

            //Gets goal-amount of primary category
            primaryCategoryGoalAmount = currentPrimaryCategoryJSON.getLong(PRIMARY_CATEGORY_GOAL_AMOUNT);

            try {
                //Gets goal-creation-date
                primaryCategoryGoalCreationDate = currentPrimaryCategoryJSON.getLong(PRIMARY_CATEGORY_GOAL_CREATION_DATE);
            } catch (JSONException e){}

            //Gets icon
            primaryCategoryIcon = currentPrimaryCategoryJSON.getString(PRIMARY_CATEGORY_ICON);

            //Current primary category to add
            PrimaryCategory primaryCategoryToAdd = new PrimaryCategory(primaryCategoryName, new Goal(primaryCategoryGoalAmount, primaryCategoryGoalCreationDate));

            //Sets icon
            primaryCategoryToAdd.setIconName(primaryCategoryIcon);

            //Adds Primary Category to array
            primaryCategories.add(primaryCategoryToAdd);

            //Contains all subcategories of current primary category
            ArrayList<Subcategory> subcategories = new ArrayList<>();

            for(int y = 0; y<primaryCategoriesJSON.getJSONObject(i).getJSONArray(PRIMARY_CATEGORY_SUBCATEGORIES).length(); y++){
                //Contains current subcategory
                JSONObject currentSubcategoryJSON = primaryCategoriesJSON.getJSONObject(i).getJSONArray(PRIMARY_CATEGORY_SUBCATEGORIES).getJSONObject(y);

                String subcategoryName;
                long subcategoryGoalAmount, subcategoryGoalCreationDate;
                boolean subcategoryFavored;

                //Gets name
                subcategoryName = currentSubcategoryJSON.getString(SUBCATEGORY_NAME_JSON);

                //Gets goal-amount
                subcategoryGoalAmount = currentSubcategoryJSON.getLong(SUBCATEGORY_GOAL_AMOUNT);

                try {
                    //Gets goal-creation-date
                    subcategoryGoalCreationDate = currentSubcategoryJSON.getLong(SUBCATEGORY_GOAL_CREATION_DATE);
                } catch (JSONException e){
                    subcategoryGoalCreationDate = System.currentTimeMillis();
                }

                //Gets if subcategory is favoured
                subcategoryFavored = currentSubcategoryJSON.getBoolean(SUBCATEGORY_FAVOURED);

                //Adds subcategory to array
                subcategories.add(new Subcategory(subcategoryName, new Goal(subcategoryGoalAmount, subcategoryGoalCreationDate), primaryCategories.get(i), subcategoryFavored));
            }

            //Adds subcategories to current primary category
            primaryCategories.get(i).setSubcategories(subcategories);
        }

        return primaryCategories;
    }

    public ArrayList<AutoPay> readAutoPays() throws IOException, JSONException {

        //Contains all bank accounts
        ArrayList<AutoPay> autoPays = new ArrayList<>();

        //Contains file
        String file = readFile(false);

        //Main JSON-Object
        JSONObject jsonObject = new JSONObject(file);

        //Contains AutoPays
        JSONArray autoPaysJSON = jsonObject.getJSONArray(AUTO_PAYS_JSON);
        for(int i = 0; i<autoPaysJSON.length(); i++){
            //Contains current AutoPay
            JSONObject currentAutoPayJSON = autoPaysJSON.getJSONObject(i);

            String autoPayName, autoPayBankAccountName;
            long autoPayCreationDate;
            int autoPayType;
            BankAccount autoPayBankAccount = null;

            //Adds name
            autoPayName = currentAutoPayJSON.getString(AUTO_PAY_NAME);

            //Adds creation date
            autoPayCreationDate = currentAutoPayJSON.getLong(AUTO_PAY_CREATION_DATE);

            //Adds type
            autoPayType = currentAutoPayJSON.getInt(AUTO_PAY_TYPE);

            //Name of AutoPays bank account
            autoPayBankAccountName = currentAutoPayJSON.getString(AUTO_PAY_BANK_ACCOUNT);

            //Contains Bill of current AutoPay
            JSONObject currentBillJSON = currentAutoPayJSON.getJSONObject(AUTO_PAY_BILL);

            //Name of subcategory of bill
            String subcategoryName = currentBillJSON.getString(BILL_SUB_CATEGORY_JSON);

            //Subcategory of bill
            Subcategory subcategory = null;

            //Gets subcategory
            for(int p = 0; p< Database.getPrimaryCategories().size(); p++){
                for(int s = 0; s< Database.getPrimaryCategories().get(p).getSubcategories().size(); s++){
                    if(Database.getPrimaryCategories().get(p).getName().equals(currentAutoPayJSON.getString(AUTO_PAY_BILL_PRIMARY_CATEGORY))){
                        if(Database.getPrimaryCategories().get(p).getSubcategories().get(s).getName().equals(subcategoryName))
                            subcategory = Database.getPrimaryCategories().get(p).getSubcategories().get(s);
                    }
                }
            }

            //Gets bank account
            for(int y = 0; y<Database.getBankAccounts().size(); y++){
                if(Database.getBankAccounts().get(y).getName().equals(autoPayBankAccountName))
                    autoPayBankAccount = Database.getBankAccounts().get(y);
            }

            //Reads payments
            ArrayList<Long> payments = new ArrayList<>();
            for(int y = 0; y<currentAutoPayJSON.getJSONArray(AUTO_PAY_PAYMENTS).length(); y++){
                payments.add(((long) currentAutoPayJSON.getJSONArray(AUTO_PAY_PAYMENTS).get(y)));
            }

            //Adds current AutoPay
            AutoPay autoPayToAdd = new AutoPay(new Bill(currentBillJSON.getLong(BILL_AMOUNT_JSON), currentBillJSON.getString(BILL_DESCRIPTION_JSON), subcategory, currentBillJSON.getInt(BILL_TYPE_JSON), currentBillJSON.getLong(BILL_CREATION_DATE_JSON)), autoPayType, autoPayName, autoPayBankAccount, autoPayCreationDate);
            autoPayToAdd.setPayments(payments);

            autoPays.add(autoPayToAdd);
        }

        return autoPays;
    }

    private String readFile(boolean standardData) throws IOException {

        AssetManager assetManager = context.getAssets();
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;

        if(standardData){
            try{
                inputStreamReader = new InputStreamReader(assetManager.open(FILE_NAME_STANDARD_DATA + Locale.getDefault().getDisplayLanguage() + ".json"));
            } catch (Exception e){
                //Whe current language isn't available it loads the english version
                if(inputStreamReader == null)
                    inputStreamReader = new InputStreamReader(assetManager.open(FILE_NAME_STANDARD_DATA + "English" + ".json"));
            }
        } else {
            inputStreamReader = new InputStreamReader((fileInputStream = context.openFileInput(FILE_NAME)));
        }

        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        //Reads JSON from File and saves it in json-String
        String currentLine = null;
        StringBuilder stringBuilder = new StringBuilder();
        while ((currentLine = bufferedReader.readLine()) != null){
            stringBuilder.append(currentLine);
        }

        //Closes streams
        bufferedReader.close();
        inputStreamReader.close();
        if(fileInputStream != null)
            fileInputStream.close();

        return stringBuilder.toString();
    }
}
