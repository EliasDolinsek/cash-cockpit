package com.dolinsek.elias.cashcockpit.model;


/**
 * Created by elias on 06.01.2018.
 */

public class AutoPay {

    public static final int TYPE_WEEKLY = 0;
    public static final int TYPE_MONTHLY = 1;
    public static final int TYPE_YEARLY = 2;

    private Bill bill;
    private int type;
    private String name;
    private long creationDate;
    private BankAccount bankAccount;

    public AutoPay(Bill bill, int type, String name, BankAccount bankAccount, long creationDate) {
        this.bill = bill;
        this.type = type;
        this.name = name;
        this.bankAccount = bankAccount;
        this.creationDate = creationDate;
    }

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
