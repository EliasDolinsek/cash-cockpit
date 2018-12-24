package com.dolinsek.elias.cashcockpit.components;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.DeleteBillDialogFragment;
import com.dolinsek.elias.cashcockpit.R;
import com.dolinsek.elias.cashcockpit.StartActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Elias Dolinsek on 06.07.2018 for cash-cockpit.
 */
public class Toolkit {

    private static final int PENDING_INTENT_ID = 978;

    public static ArrayList<Bill> getAllBills(){
        ArrayList<Bill> bills = new ArrayList<>();
        for (BankAccount bankAccount:Database.getBankAccounts()){
            bills.addAll(bankAccount.getBills());
        }

        return bills;
    }

    public static ArrayList<Bill> filterBillsByType(ArrayList<Bill> bills, int billType){
        ArrayList<Bill> filteredBills = new ArrayList<>();
        for (Bill bill:bills){
            if (bill.getType() == billType){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    public static ArrayList<Bill> getBillsByType(int billType){
        return filterBillsByType(getAllBills(), billType);
    }

    public static ArrayList<Bill> getBillsByMonth(long timeStamp){
        ArrayList<Bill> bills = new ArrayList<>();
        for (Bill bill:getAllBills()){
            if (wasBillCreatedDuringMonth(bill, timeStamp)){
                bills.add(bill);
            }
        }

        return bills;
    }

    private static boolean wasBillCreatedDuringMonth(Bill bill, long timeStampOfMonth){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);
        int currentMonth = calendar.get(Calendar.MONTH), currentYear = calendar.get(Calendar.YEAR);

        calendar.setTimeInMillis(bill.getCreationDate());
        int billCreationMonth = calendar.get(Calendar.MONTH), billCreationYear = calendar.get(Calendar.YEAR);

        return currentMonth == billCreationMonth && currentYear == billCreationYear;
    }

    public static ArrayList<Bill> getBillsByTypeAndMonth(int billType, long timeStamp){
        return filterBillsByType(getBillsByMonth(timeStamp), billType);
    }

    public static long getBillsAmount(ArrayList<Bill> bills){
        long amount = 0;

        for (Bill bill:bills){
            if (bill.getType() == Bill.TYPE_INPUT){
                amount += bill.getAmount();
            } else {
                amount -= bill.getAmount();
            }
        }

        return amount;
    }

    public static long getBillsTotalAmount(ArrayList<Bill> bills){
        long totalAmount = 0;
        for (Bill bill:bills){
            totalAmount += bill.getAmount();
        }

        return totalAmount;
    }

    public static long getBankAccountsBalance(){
        long balance = 0;
        for (BankAccount bankAccount:Database.getBankAccounts()){
            balance += bankAccount.getBalance();
        }

        return balance;
    }

    public static long getAutoPaysAmount(ArrayList<AutoPay> autoPays){
        long amount = 0;
        for (AutoPay autoPay:autoPays){
            amount += autoPay.getBill().getAmount();
        }

        return amount;
    }

    public static ArrayList<Bill> filterBillsByAutoPay(ArrayList<Bill> bills, boolean autoPayBill){
        ArrayList<Bill> filteredBills = new ArrayList<>();
        for (Bill bill:bills){
            if (bill.isAutoPayBill() == autoPayBill){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    public static ArrayList<AutoPay> getAutoPaysByType(int autoPayType){
        return filterAutoPaysByType(Database.getAutoPays(), autoPayType);
    }

    public static ArrayList<AutoPay> filterAutoPaysByType(ArrayList<AutoPay> autoPays, int autoPayType){
        ArrayList<AutoPay> filteredAutoPays = new ArrayList<>();
        for (AutoPay autoPay:autoPays){
            if (autoPay.getType() == autoPayType){
                filteredAutoPays.add(autoPay);
            }
        }

        return filteredAutoPays;
    }

    public static ArrayList<AutoPay> getAutoPaysByBillType(int billType){
        return filterAutoPaysByBillType(Database.getAutoPays(), billType);
    }

    public static ArrayList<AutoPay> filterAutoPaysByBillType(ArrayList<AutoPay> autoPays, int billType){
        ArrayList<AutoPay> filteredAutoPays = new ArrayList<>();
        for (AutoPay autoPay:autoPays){
            if (autoPay.getBill().getType() == billType){
                filteredAutoPays.add(autoPay);
            }
        }

        return filteredAutoPays;
    }

    public static void displayPleaseCheckInputsToast(Context context){
        Toast.makeText(context, context.getString(R.string.toast_please_check_inputs), Toast.LENGTH_SHORT).show();
    }

    public static BankAccount getBankAccountOfBill(Bill bill){
        for (BankAccount bankAccount: Database.getBankAccounts()){
            if (bankAccount.getBills().contains(bill)){
                return bankAccount;
            }
        }

        throw new IllegalArgumentException("Couldn't find bank account of bill");
    }

    public static ArrayList<Bill> filterBillsByCategory(ArrayList<Bill> billsToFilter, Category category){
        ArrayList<Bill> filteredBills = new ArrayList<>();
        for (Bill bill:billsToFilter){
            if (bill.getSubcategory().equals(category) || bill.getSubcategory().getPrimaryCategory().equals(category)){
                filteredBills.add(bill);
            }
        }

        return filteredBills;
    }

    public static int getIndexOfSubcategoryInPrimaryCategory(Subcategory subcategory){
        for (int i = 0; i<subcategory.getPrimaryCategory().getSubcategories().size(); i++){
            if (subcategory.equals(subcategory.getPrimaryCategory().getSubcategories().get(i))){
                return i;
            }
        }

        throw new Resources.NotFoundException("Couldn't find subcategory in database!");
    }

    public static int getIndexOfPrimaryCategoryInDatabase(PrimaryCategory primaryCategory){
        ArrayList<PrimaryCategory> primaryCategoriesInDatabase = Database.getPrimaryCategories();
        for (int i = 0; i<primaryCategoriesInDatabase.size(); i++){
            System.out.println(primaryCategory + " " + primaryCategoriesInDatabase.get(i));
            if (primaryCategory.equals(primaryCategoriesInDatabase.get(i))){
                return i;
            }
        }

        throw new Resources.NotFoundException("Couldn't find primary category in database!");
    }

    public static long convertStringToLongAmount(String amount){
        try {
            return (long) (Double.valueOf(amount) * 100);
        } catch (Exception e){
            return 0;
        }
    }

    public static String getBillTypeAsString(int billType, Context context){
        switch (billType){
            case Bill.TYPE_INPUT: return context.getString(R.string.label_input);
            case Bill.TYPE_OUTPUT: return context.getString(R.string.label_output);
            case Bill.TYPE_TRANSFER: return context.getString(R.string.label_transfer);
            default: throw new IllegalArgumentException("Couldn't resolve " + billType + " as a bill-type");
        }
    }

    public static BalanceChange getLastBalanceChangeOfBankAccountAndMonth(BankAccount bankAccount, long timeStampOfMonth){
        ArrayList<BalanceChange> balanceChangesOfBankAccount = bankAccount.getBalanceChanges();
        sortBalanceChangesOfCreationDates(balanceChangesOfBankAccount);

        ArrayList<BalanceChange> balanceChangesOfBankAccountAndMonth = filterBalanceChangesOfMonth(balanceChangesOfBankAccount, timeStampOfMonth);
        return balanceChangesOfBankAccountAndMonth.get(balanceChangesOfBankAccountAndMonth.size() - 1);
    }

    public static void sortBalanceChangesOfCreationDates(ArrayList<BalanceChange> balanceChangesToSort){
        Collections.sort(balanceChangesToSort, (balanceChange, t1) -> Long.compare(balanceChange.getTimeStampOfChange(), t1.getTimeStampOfChange()));
    }

    public static ArrayList<BalanceChange> filterBalanceChangesOfMonth(ArrayList<BalanceChange> balanceChangesToFilter, long timeStampOfMonth){
        ArrayList<BalanceChange> balanceChangesOfMonth = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampOfMonth);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        for (BalanceChange balanceChange:balanceChangesToFilter){
            calendar.setTimeInMillis(balanceChange.getTimeStampOfChange());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH);

            if (currentYear == year && currentMonth == month){
                balanceChangesOfMonth.add(balanceChange);
            }
        }

        return balanceChangesOfMonth;
    }

    public static boolean doesMonthExceedsCurrentTime(Calendar calendar){
        Calendar currentMonthCalendar = Calendar.getInstance();
        currentMonthCalendar.setTimeInMillis(System.currentTimeMillis());

        int currentYear = currentMonthCalendar.get(Calendar.YEAR);
        int currentMonth = currentMonthCalendar.get(Calendar.MONTH);

        if (currentYear <= calendar.get(Calendar.YEAR)){
            return currentMonth < calendar.get(Calendar.MONTH);
        } else {
            return false;
        }
    }

    public static int getIndexOfBankAccountInDatabase(BankAccount bankAccount){
        for (int i = 0; i<Database.getBankAccounts().size(); i++){
            if (Database.getBankAccounts().get(i).equals(bankAccount)){
                return i;
            }
        }

        throw new IllegalStateException("Couldn't find bankAccount in database");
    }

    public static void restartCashCockpit(Context context){
        Intent intent = new Intent(context, StartActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);

        System.exit(0);
    }

    public static class ActivityToolkit {
        public static void addBankAccountChipsToChipGroup(ChipGroup chipGroup, Context context){
            for (BankAccount bankAccount:Database.getBankAccounts()){
                addBankAccountChipToChipGroup(bankAccount, chipGroup, bankAccount.isPrimaryAccount(), context);
            }
        }

        public static void addBankAccountChipToChipGroup(BankAccount bankAccount, ChipGroup chipGroup, boolean checked, Context context) {
            Chip chip = new Chip(context);
            chip.setText(bankAccount.getName());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setCheckedIconVisible(false);

            chipGroup.addView(chip);
            if (checked){
                chipGroup.check(chip.getId());
            }
        }

        public static BankAccount getSelectedBankAccountFromChipGroup(ChipGroup chipGroup){
            for (int i = 0; i<chipGroup.getChildCount(); i++){
                if (((Chip)chipGroup.getChildAt(i)).isChecked()){
                    return Database.getBankAccounts().get(i);
                }
            }

            return Database.getBankAccounts().get(0);
        }
    }
}
