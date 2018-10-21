package com.dolinsek.elias.cashcockpit.components;

import android.content.Context;
import android.widget.Toast;

import com.dolinsek.elias.cashcockpit.DeleteBillDialogFragment;
import com.dolinsek.elias.cashcockpit.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Elias Dolinsek on 06.07.2018 for cash-cockpit.
 */
public class Toolkit {

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
}
