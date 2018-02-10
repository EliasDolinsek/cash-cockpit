package com.dolinsek.elias.cashcockpit.components;


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
}
