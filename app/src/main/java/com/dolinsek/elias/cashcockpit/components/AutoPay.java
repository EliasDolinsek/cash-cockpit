package com.dolinsek.elias.cashcockpit.components;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * This class represents an AutoPay what basically is an bill what duplicates itself on a specified day of the week/month/year
 * Created by elias on 06.01.2018.
 */

public class AutoPay {

    /**
     * This constants what determine if the bill get duplicated weekly, monthly or yearly
     */
    public static final int TYPE_WEEKLY = 0;
    public static final int TYPE_MONTHLY = 1;
    public static final int TYPE_YEARLY = 2;

    /**
     * Bill what get duplicated
     */
    private Bill bill;

    /**
     * Determines with the constants above how if the bill get duplicated weekly, monthly or yearly
     */
    private int type;

    /**
     * Name of the AutoPay what helps the user to identify it
     */
    private String name;

    /**
     * Creation date of the AutoPay
     */
    private long creationDate;

    /**
     * AutoPay get added to this specified bank account
     */
    private BankAccount bankAccount;

    /**
     * Contains time-stamps when the AutoPay got payed
     */
    private ArrayList<Long> payments = new ArrayList<>();

    /**
     * Creates a new AutoPay
     * @param bill bill what get duplicated
     * @param type if the bill get duplicated weekly, monthly or yearly
     * @param name name of the AutoPay
     * @param bankAccount bill get duplicated to this bank account
     * @param creationDate date of creation of this AutoPay
     */
    public AutoPay(Bill bill, int type, String name, BankAccount bankAccount, long creationDate) {
        this.bill = bill;
        this.type = type;
        this.name = name;
        this.bankAccount = bankAccount;
        this.creationDate = creationDate;
    }

    /**
     * Creates a new AutoPay and sets creation date to the current date
     * @param bill bill what get duplicated
     * @param type if the bill get duplicated weekly, monthly or yearly
     * @param name name of the AutoPay
     * @param bankAccount bill get duplicated to this bank account
     */
    public AutoPay(Bill bill, int type, String name, BankAccount bankAccount) {
        this.bill = bill;
        this.type = type;
        this.name = name;
        this.bankAccount = bankAccount;
        creationDate = System.currentTimeMillis();
    }

    public void managePayments(){
        new PaymentManager(this).managePayments();
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public ArrayList<Long> getPayments() {
        return payments;
    }

    public void setPayments(ArrayList<Long> payments) {
        this.payments = payments;
    }

    public class PaymentManager {

        private AutoPay autoPay;

        public PaymentManager(AutoPay autoPay){
            this.autoPay = autoPay;
        }

        public void managePayments(){
            if(autoPay.getPayments().size() != 0){
                Calendar currentTimeCalender = Calendar.getInstance();
                currentTimeCalender.setTimeInMillis(System.currentTimeMillis());

                Calendar lastPaymentCalender = Calendar.getInstance();
                lastPaymentCalender.setTimeInMillis((autoPay.getPayments().size() == 0 ? 0 : autoPay.getPayments().get(autoPay.getPayments().size() - 1)));

                int yearsDifference = currentTimeCalender.get(Calendar.YEAR) - lastPaymentCalender.get(Calendar.YEAR);
                int monthsDifference = currentTimeCalender.get(Calendar.MONTH) - lastPaymentCalender.get(Calendar.MONTH);
                int weeksDifference = currentTimeCalender.get(Calendar.WEEK_OF_MONTH) - lastPaymentCalender.get(Calendar.WEEK_OF_MONTH);

                autoPay.getBill().setCreationDate(getPaymentDate());
                if(autoPay.getType() == TYPE_WEEKLY){
                    for(int i = 0; i<=weeksDifference; i++){
                        for(int y = 0; y<=monthsDifference; y++){
                            for(int x = 0; y<=yearsDifference; x++){
                                autoPay.getBankAccount().addBill(autoPay.getBill());
                            }
                        }
                    }
                } else if(autoPay.getType() == TYPE_MONTHLY){
                    for(int i = 0; i<=monthsDifference; i++){
                        for(int y = 0; y<=yearsDifference; y++){
                            autoPay.getBankAccount().addBill(autoPay.getBill());
                        }
                    }
                } else {
                    for(int i = 0; i<=yearsDifference; i++){
                        autoPay.getBankAccount().addBill(autoPay.getBill());
                    }
                }

                autoPay.getPayments().add(getPaymentDate());
            } else {
                autoPay.getPayments().add(getPaymentDate());
            }
        }

        private long getPaymentDate(){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            if(autoPay.getType() == TYPE_WEEKLY){
                return Timestamp.valueOf(calendar.get(Calendar.YEAR) + "-" + (String.valueOf(calendar.get(Calendar.MONTH)).length() != 2 ? "0" + String.valueOf(calendar.get(Calendar.MONTH)) : String.valueOf(calendar.get(Calendar.MONTH))) + "-" + (calendar.getActualMinimum(Calendar.DAY_OF_WEEK) != 2 ? "0" + calendar.getActualMinimum(Calendar.DAY_OF_WEEK) : calendar.getActualMinimum(Calendar.DAY_OF_WEEK)) + " 00:00:01.00").getTime();
            } else if(autoPay.getType() == TYPE_MONTHLY){
                return Timestamp.valueOf(calendar.get(Calendar.YEAR) + "-" + (String.valueOf(calendar.get(Calendar.MONTH)).length() != 2 ? "0" + String.valueOf(calendar.get(Calendar.MONTH)) : String.valueOf(calendar.get(Calendar.MONTH))) + "-01 00:00:01.00").getTime();
            } else {
               return Timestamp.valueOf(calendar.get(Calendar.YEAR) + "-01-01 00:00:01.00").getTime();
            }
        }
    }
}
