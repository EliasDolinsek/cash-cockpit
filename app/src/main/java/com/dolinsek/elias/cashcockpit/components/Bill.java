package com.dolinsek.elias.cashcockpit.components;

import java.util.Objects;

/**
 * Represents a bill what usually get added to a bank account
 * Created by elias on 06.01.2018.
 */

public class Bill {

    /**
     * This constants determine the type of the bill.
     * TYPE_OUTPUT = amount get removed from bank account
     * TYPE_INPUT = amount get added to the bank account
     * TYPE_TRANSFER = amount get removed from the bank account and get added to another bank account
     */
    public static final int TYPE_OUTPUT = 1;
    public static final int TYPE_INPUT = 0;
    public static final int TYPE_TRANSFER = 2;

    /**
     * Amount what get removed or added in cents
     */
    private long amount;

    /**
     * Contains a little description what helps the user to remember what this bill was for
     */
    private String description;

    /**
     * Subcategory what helps the app to make better statistics
     */
    private Subcategory subcategory;

    /**
     * Creation date of the bill
     */
    private long creationDate;

    /**
     * Type of the bill
     */
    private int type;

    private boolean autoPayBill;

    /**
     * Creates a new Bill
     * @param amount amount what get added or removed from the bank account
     * @param description a short description what can help the user to remember what this bill was for
     * @param type type of the bill
     * @param subcategory subcategory
     * @param creationDate creation date of the bill
     */
    public Bill(long amount, String description, Subcategory subcategory, int type, boolean autoPayBill, long creationDate) {
        this.amount = amount;
        this.description = description;
        this.subcategory = subcategory;
        this.creationDate = creationDate;
        this.autoPayBill = autoPayBill;
        this.type = type;
    }

    /**
     * Creates a new Bill and sets the creation date to the current date
     * @param amount amount what get added or removed from the bank account
     * @param description a short description what can help the user to remember what this bill was for
     * @param type type of the bill
     * @param subcategory subcategory
     */
    public Bill(long amount, String description, int type, boolean autoPayBill, Subcategory subcategory) {
        this.amount = amount;
        this.description = description;
        this.subcategory = subcategory;
        this.type = type;
        this.autoPayBill = autoPayBill;
        creationDate = System.currentTimeMillis();
    }

    public Bill(){ }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public boolean isAutoPayBill() {
        return autoPayBill;
    }

    public void setAutoPayBill(boolean autoPayBill) {
        this.autoPayBill = autoPayBill;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptions) {
        this.description = descriptions;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bill)) return false;
        Bill bill = (Bill) o;
        return amount == bill.amount &&
                creationDate == bill.creationDate &&
                type == bill.type &&
                autoPayBill == bill.autoPayBill &&
                Objects.equals(description, bill.description) &&
                Objects.equals(subcategory, bill.subcategory);
    }

    @Override
    public int hashCode() {

        return Objects.hash(amount, description, subcategory, creationDate, type, autoPayBill);
    }
}
