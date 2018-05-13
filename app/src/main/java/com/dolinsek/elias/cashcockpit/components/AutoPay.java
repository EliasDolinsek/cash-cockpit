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

    public AutoPay() {

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

    public void addFirstPayment(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        switch (type){
            case TYPE_YEARLY: calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
                break;
            case TYPE_MONTHLY: calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                break;
            case TYPE_WEEKLY: calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK));
                break;
            default: throw new IllegalStateException("Couldn't resolve " + type + " as a valid auto-pay-type");
        }

        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));

        payments.add(calendar.getTimeInMillis());
    }

    public void managePayment(){
        int paymentsToDo = getPaymentsToDo();
        for (int i = 0; i<=paymentsToDo; i++){
            addPayment();
        }
    }

    private void addPayment(){
        bankAccount.addBill(bill);
        payments.add(System.currentTimeMillis());
    }

    public boolean isPaymentRequired(){
        return getPaymentsToDo() != 0;
    }

    private int getPaymentsToDo(){
        long currentTimeStamp = payments.get(payments.size() - 1);
        int paymentsToDo = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int currentYear = calendar.get(Calendar.YEAR), currentMonth = calendar.get(Calendar.MONTH), currentWeek = calendar.get(Calendar.WEEK_OF_MONTH);
        calendar.setTimeInMillis(currentTimeStamp);

        do {
            paymentsToDo++;
            addDurationOfPaymentDifferenceToCalendar(calendar);
        } while (currentYear >= calendar.get(Calendar.YEAR) || currentMonth >= calendar.get(Calendar.MONTH) || currentWeek >= calendar.get(Calendar.WEEK_OF_MONTH));

        return paymentsToDo;
    }

    private void addDurationOfPaymentDifferenceToCalendar(Calendar calendar){
        switch (type){
            case TYPE_WEEKLY: calendar.add(Calendar.WEEK_OF_MONTH, 1);
                break;
            case TYPE_MONTHLY: calendar.add(Calendar.MONTH, 1);
                break;
            case TYPE_YEARLY: calendar.add(Calendar.YEAR, 1);
                break;
            default: throw new IllegalArgumentException("Couldn't resolve " + type + " as a valid bill type");
        }
    }

}
