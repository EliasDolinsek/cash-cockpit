package com.dolinsek.elias.cashcockpit.components;

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
    public static final int TYPE_OUTPUT = 0;
    public static final int TYPE_INPUT = 1;
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
     * Creates a new Bill
     * @param amount amount what get added or removed from the bank account
     * @param description a short description what can help the user to remember what this bill was for
     * @param subcategory subcategory
     * @param creationDate creation date of the bill
     */
    public Bill(long amount, String description, Subcategory subcategory, long creationDate) {
        this.amount = amount;
        this.description = description;
        this.subcategory = subcategory;
        this.creationDate = creationDate;
    }

    /**
     * Creates a new Bill and sets the creation date to the current date
     * @param amount amount what get added or removed from the bank account
     * @param description a short description what can help the user to remember what this bill was for
     * @param subcategory subcategory
     */
    public Bill(long amount, String description, Subcategory subcategory) {
        this.amount = amount;
        this.description = description;
        this.subcategory = subcategory;
        creationDate = System.currentTimeMillis();
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
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
}
