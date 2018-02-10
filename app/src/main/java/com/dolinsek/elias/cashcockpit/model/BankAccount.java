package com.dolinsek.elias.cashcockpit.model;

import java.util.ArrayList;

/**
 * Created by elias on 06.01.2018.
 */

public class BankAccount {

    private String name;
    private long balance; //In cents
    private ArrayList<Bill> bills;
    private boolean primaryAccount;
    private long creationDate;
    private ArrayList<Long> balanceChanges;

    public BankAccount(String name, long balance, boolean primaryAccount, long creationDate) {
        this.name = name;
        this.balance = balance;
        this.primaryAccount = primaryAccount;
        this.creationDate = creationDate;
        bills = new ArrayList<>();
        balanceChanges = new ArrayList<>();
    }

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
