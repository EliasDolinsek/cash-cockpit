package com.dolinsek.elias.cashcockpit.components;

import java.util.ArrayList;

/**
 * This class represents a bank account
 * Created by elias on 06.01.2018.
 */

public class BankAccount {

    /**
     * Name of the bank account what helps the user to identify it
     */
    private String name;

    /**
     * Balance of the bank account in cents
     */
    private long balance; //In cents

    /**
     * List of all bills what got added to this bank account
     */
    private ArrayList<Bill> bills;

    /**
     * Determines if the bank account is the primary bank account. This basically means that all Bills get added automatically to this bank account if the user doesn't specify another bank account
     */
    private boolean primaryAccount;

    /**
     * Creation date of bank account
     */
    private long creationDate;

    /**
     * List of all balance changes. List does contain dates in time-millis
     */
    private ArrayList<Long> balanceChanges;

    /**
     * Creates a new bank account
     * @param name name of the bank account
     * @param balance balance of the bank account
     * @param primaryAccount if the bank account is the primary bank account
     * @param creationDate creation date of the bank account
     */
    public BankAccount(String name, long balance, boolean primaryAccount, long creationDate) {
        this.name = name;
        this.balance = balance;
        this.primaryAccount = primaryAccount;
        this.creationDate = creationDate;
        bills = new ArrayList<>();
        balanceChanges = new ArrayList<>();
    }
    /**
     * Creates a new bank account and sets creation date to the current date
     * @param name name of the bank account
     * @param balance balance of the bank account
     * @param primaryAccount if the bank account is the primary bank account
     */
    public BankAccount(String name, long balance, boolean primaryAccount) {
        this.name = name;
        this.balance = balance;
        this.primaryAccount = primaryAccount;
        creationDate = System.currentTimeMillis();
        bills = new ArrayList<>();
        balanceChanges = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addBill(Bill bill){
        bills.add(bill);
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public ArrayList<Bill> getBills() {
        return bills;
    }

    public void setBills(ArrayList<Bill> bills) {
        this.bills = bills;
    }

    public boolean isPrimaryAccount() {
        return primaryAccount;
    }

    public void setPrimaryAccount(boolean primaryAccount) {
        this.primaryAccount = primaryAccount;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public ArrayList<Long> getBalanceChanges() {
        return balanceChanges;
    }

    public void setBalanceChanges(ArrayList<Long> balanceChanges) {
        this.balanceChanges = balanceChanges;
    }
}
